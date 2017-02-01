package com.m.loginapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static android.R.attr.data;
import static android.R.attr.scaleGravity;
import static android.R.id.button1;

public class LoginLockActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    private AppPrivData getPrivData() { return AppPrivData.getInstance(); }

    boolean _passwordCorrect;
    boolean _haveCameraPermission;
    boolean _haveStoragePermission;
    boolean _haveAlertDialogPermission;
    int _backCameraId;
    int _frontCameraId;
    int _currentCamera;

    Camera[] _cameras;
    SurfaceView[] _camPreviews;
    byte[] _rawImageBack;
    byte[] _rawImageFront;

    KeyLocker _keyLocker;
    ButtonsKeyboard _keyboard;

    BackgroundTaskQueue _taskQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Wczytujemy dane
        /* TODO: rozwazyc czy to w ogole potrzebne, jako ze aplikacja startuje z Main i tam laduje
               (mozliwe ze po usunieciu i przywroceniu procesu stworzy nowe AppPrivData to wtedy trzeba)
        */
        getPrivData().readPrivDataFromFile(this);
        _passwordCorrect = false;

        // Sprawdzamy czy mozna uzyc kamer
        // Jesli mozna, to znajdujemy ich ID
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        _haveCameraPermission = permissionCheck == PackageManager.PERMISSION_GRANTED;
        if(_haveCameraPermission)
        {
            getCameraIds();
            _cameras = new Camera[2];
            _camPreviews = new SurfaceView[2];
        }

        permissionCheck = ContextCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        _haveStoragePermission = permissionCheck == PackageManager.PERMISSION_GRANTED;

        permissionCheck = ContextCompat
                .checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW);
        _haveAlertDialogPermission = permissionCheck == PackageManager.PERMISSION_GRANTED;

        // Wysylamy wiadomosc ze nasze okienko jest stworzone - aby zapobiec ponownemu tworzeniu
        // przy ponownym wlaczeniu ekranu
        Intent i = new Intent(ListenForScreenService.ACTION_LOGIN_ACTIVITY_RUNNING);
        this.sendBroadcast(i);
    }

    @Override
    public void onDestroy()
    {
        _taskQueue.quit();
        _taskQueue = null;

        // Wysylamy wiadomosc ze nasze okienko jest zeniszczone - aby ponownie je stworzyc
        // przy ponownym wlaczeniu ekranu
        Intent i = new Intent(ListenForScreenService.ACTION_LOGIN_ACTIVITY_CLOSED);
        this.sendBroadcast(i);

        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow()
    {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        _keyboard = new ButtonsKeyboard(this);
        _keyboard.setKeyButtonClickedListener(new ButtonsKeyboard.KeyButtonClickedListener()
        {
            @Override
            public void buttonClicked(int key)
            {
                keyboardButtonClicked(key);
            }
        });

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id._keyboardHolderLayout);
        frameLayout.addView(_keyboard);

        if(_keyLocker != null)
        {
            _keyLocker.unlock();
            _keyLocker = null;
        }

        _keyLocker = new KeyLocker();
        _keyLocker.prepare(this);
        _keyLocker.lock();

        _taskQueue = new BackgroundTaskQueue();
        _taskQueue.start();
    }

    void keyboardButtonClicked(int key)
    {
        final EditText editPassword = (EditText) findViewById(R.id._passwordText);
        Editable text = editPassword.getText();
        String textString = text.toString();

        if(key == KeyEvent.KEYCODE_CLEAR)
        {
            // Usun ostatnia cyfre
            if(textString.length() > 1)
            {
                textString = textString.substring(0, textString.length() - 1);
            }
            if(textString.length() == 1)
            {
                textString = "";
            }
        }
        else if(key == KeyEvent.KEYCODE_ENTER)
        {
            requestPasswordAccept();
        }
        else
        {
            // Dadajemy cyfre na koniec
            textString = textString + (char) key;
        }

        editPassword.setText(textString, TextView.BufferType.EDITABLE);
    }

    protected Camera getCameraInstance(int id)
    {
        Camera c = null;
        try
        {
            c = Camera.open(id); // attempt to get a Camera instance
        }
        catch(Exception e)
        {
            // Camera is not available (in use or does not exist)
            Log.d("getCameraInstance", "Nie powodzenie otwarcia kamery: " + id);
        }
        return c; // returns null if camera is unavailable
    }

    protected void getCameraIds()
    {
        int camCount = Camera.getNumberOfCameras();
        _backCameraId = -1;
        _frontCameraId = -1;
        for(int c = 0; c < camCount; ++c)
        {
            Camera.CameraInfo cinfo = new Camera.CameraInfo();
            Camera.getCameraInfo(c, cinfo);
            if(cinfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK && _backCameraId == -1)
            {
                // Znajdz i zapisz pierwsza kamere tylna
                _backCameraId = c;
            }
            if(cinfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && _frontCameraId == -1)
            {
                // Znajdz i zapisz pierwsza kamere przednia
                _frontCameraId = c;
            }
        }
    }

    void requestPasswordAccept()
    {
        final EditText editPassword = (EditText) findViewById(R.id._passwordText);

        Toast msg1 = Toast.makeText(getBaseContext(), "Poprawne Hasło", Toast.LENGTH_LONG);
        Toast msg2 = Toast.makeText(getBaseContext(), "Błędne Hasło", Toast.LENGTH_LONG);
        if(editPassword.getText().toString().equals(getPrivData().getSavedPassword()))
        {
            msg1.show();
            // Poprawne haslo -> wylaczamy blokade
            _passwordCorrect = true;
            finish();
        }
        else
        {
            // Zle haslo -> robimy zdjecia
            msg2.show();
            _rawImageFront = null;
            _rawImageBack = null;

            if(_frontCameraId >= 0)
            {
                _taskQueue.pushTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        takePicture(_frontCameraId);
                    }
                });
            }

            if(_backCameraId >= 0)
            {
                _taskQueue.pushTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        takePicture(_backCameraId);
                    }
                });
            }

            if(getPrivData().isSendEmail())
            {
                _taskQueue.pushTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sendEmail();
                        _taskQueue.finishTask();
                    }
                });
            }
        }
    }

    protected void takePicture(int camId)
    {
        if(_cameras[camId] == null)
        {
            _cameras[camId] = getCameraInstance(camId);
        }

        if(_cameras[camId] != null)
        {
            _currentCamera = camId;

            try
            {
                _camPreviews[camId] = new SurfaceView(this);
                _cameras[camId].setPreviewDisplay(_camPreviews[camId].getHolder());
                _cameras[camId].startPreview();
                _cameras[camId].takePicture(null, null, new PictureTakenCallback());

            }
            catch(Exception e)
            {
                String msg = e.getMessage();
                Log.d("takePicture", "Error: " + msg);
            }
        }
    }

    protected void releaseCamera(int camId)
    {
        if(_cameras[camId] != null)
        {
            _cameras[camId].release();
            _cameras[camId] = null;
            _camPreviews[camId] = null;
        }
    }

    class PictureTakenCallback implements Camera.PictureCallback
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            android.util.Log.i("PictureTakenCallback", "Picture taken!");

            if(_currentCamera == _frontCameraId)
                _rawImageFront = data;
            else
                _rawImageBack = data;

            if(_haveStoragePermission)
            {
                saveImageToFile(data, _currentCamera);
            }

            releaseCamera(_currentCamera);

            _taskQueue.finishTask();
        }
    }

    private void sendEmail()
    {
        SendMail sm = new SendMail(this, _rawImageFront, _rawImageBack,
                                   getPrivData().getSavedEmail());
        sm.execute();
    }

    private void saveImageToFile(byte[] data, int cameraId)
    {
        final File pictureFile = getOutputMediaFile(
                cameraId == _frontCameraId ? "CamFront" : "CamBack");
        if(pictureFile == null)
        {
            Log.d("_pictureTaken", "Error creating media file, check storage permissions");
            return;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

        }
        catch(FileNotFoundException e)
        {
            Log.d("_pictureTaken", "File not found: " + e.getMessage());
        }
        catch(IOException e)
        {
            Log.d("_pictureTaken", "Error accessing file: " + e.getMessage());
        }
        catch(Exception e)
        {
            Log.d("_pictureTaken", "Other error: " + e.getMessage());
        }
    }

    /**
     * Create a File for saving an image
     */
    private File getOutputMediaFile(String cameraName)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "LoginApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists())
        {
            if(!mediaStorageDir.mkdirs())
            {
                Log.d("LoginApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                                cameraName + "_" + timeStamp + ".jpg");
    }
}
