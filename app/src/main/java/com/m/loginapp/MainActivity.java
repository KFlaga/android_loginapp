package com.m.loginapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity
{
    private AppPrivData getPrivData() { return AppPrivData.getInstance(); }

    private static final int REQUEST_CHANGE_CAMERA_PERMISSION = 2;
    private static final int REQUEST_CHANGE_STORAGE_PERMISSION = 4;
    private static final int REQUEST_CHANGE_ALERTDIALOG_PERMISSION = 8;

    private EditText _editPassword;
    private Switch _switchPassword;
    private EditText _editEmail;
    private Switch _switchEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _editPassword = (EditText) findViewById(R.id._editPassword);
        _switchPassword = (Switch) findViewById(R.id._switchLock);
        _editEmail = (EditText) findViewById(R.id._editEmail);
        _switchEmail = (Switch) findViewById(R.id._switchSendEmail);

        // Sprawdzamy czy nasza aplikacja ma zapisane w prywatnej pamieci
        // haslo i numer telefonu oraz czy wysylac MMS
        if(!getPrivData().readPrivDataFromFile(this))
        {
            // Nie powiodlo sie otworzyc pliku, wirc robimy nowy
            getPrivData().createPrivFile(this);
        }
        else
        {
            updateWidgets();
        }

        // Takze sprawdzamy czy nasz ListenForScreenService jest wlaczony i ustawiamy odpowiednio switch
        boolean lockServiceRunning = isServiceRunning(this, ListenForScreenService.class);
        if(lockServiceRunning && !getPrivData().isLockTurnedOn())
        {
            // Wylaczamy nasza blokade
            turnScreenServiceOff();
        }
        else if(!lockServiceRunning && getPrivData().isLockTurnedOn())
        {
            // Wlaczamy nasza blokade
            turnScreenServiceOn();
        }
        // else : obie zmienne sie zgadzaja wiec nie robimy nic

        // Ustawiamy obserwatorow zdarzen dla naszych kontrolek
        _editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                editPasswordFocusChanged(hasFocus);
            }
        });

        _switchPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                onSwitchPasswordCheckedChanged(isChecked);
            }
        });
        _editEmail.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                editEmailFocusChanged(hasFocus);
            }
        });

        _switchEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                onSwitchEmailCheckedChanged(isChecked);
            }
        });

        // Na koniec sprawdzamy czy mamy odpowiednie pozowlenia
        checkAndRequestPermission(Manifest.permission.CAMERA, REQUEST_CHANGE_CAMERA_PERMISSION);
        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CHANGE_STORAGE_PERMISSION);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                                                            Manifest.permission.SYSTEM_ALERT_WINDOW);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            // Check if Android M or higher
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if(!Settings.canDrawOverlays(this))
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                               Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CHANGE_ALERTDIALOG_PERMISSION);
                }
            }
        }
    }

    private void turnScreenServiceOn()
    {
        Intent screenServiceIntent = new Intent(this, ListenForScreenService.class);
        this.startService(screenServiceIntent);
    }

    private void turnScreenServiceOff()
    {
        Intent screenServiceIntent = new Intent(this, ListenForScreenService.class);
        this.stopService(screenServiceIntent);
    }

    private void editPasswordFocusChanged(boolean hasFocus)
    {
        if(!hasFocus)
        {
            // Pole mialo focus, ale go stracilo, czyli skonczona edycja tekstu
            getPrivData().setTempPassword(_editPassword.getText().toString());
            // Sprobujmy zapisac haslo w pamieci prywatnej aplikacji
            getPrivData().writePrivDataToFile(this);
        }
    }

    private void onSwitchPasswordCheckedChanged(boolean isChecked)
    {
        if(isChecked)
        {
            // Proba wlaczenia zabezpieczenia
            // 1) Sprawdz czy mamy zapisane jakies haslo
            if(getPrivData().getSavedPassword().length() > 0)
            {
                // 2) Uruchamiamy ListenForScreenService
                turnScreenServiceOn();
                getPrivData().setLockTurnedOn(true);
                getPrivData().writePrivDataToFile(this);
            }
            else
            {
                Toast msg = Toast.makeText(getBaseContext(), "Należy podać hasło", Toast.LENGTH_LONG);
                msg.show();
                // Odznaczamy switcha
                _switchPassword.setChecked(false);
            }
        }
        else
        {
            // Wylaczamy zabezpieczenie
            // Nalezy wylaczyc ListenForScreenService
            turnScreenServiceOff();

            getPrivData().setLockTurnedOn(false);
            getPrivData().writePrivDataToFile(this);
        }
    }

    private void editEmailFocusChanged(boolean hasFocus)
    {
        if(!hasFocus)
        {
            // Pole mialo focus, ale go stracilo, czyli skonczona edycja tekstu
            getPrivData().setTempEmail(_editEmail.getText().toString());
            // Sprobujmy zapisac haslo w pamieci prywatnej aplikacji
            getPrivData().writePrivDataToFile(this);
        }
    }

    private void onSwitchEmailCheckedChanged(boolean isChecked)
    {
        getPrivData().setSendEmail(isChecked);
        getPrivData().writePrivDataToFile(this);
    }

    void updateWidgets()
    {
        _editPassword.setText(getPrivData().getSavedPassword());
        _editEmail.setText(getPrivData().getSavedEmail());
        _switchPassword.setChecked(getPrivData().isLockTurnedOn());
        _switchEmail.setChecked(getPrivData().isSendEmail());
    }

    protected void checkAndRequestPermission(String permission, int requestCode)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                                              new String[]{ permission },
                                              requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults)
    {
        if(grantResults.length > 0)
        {
            if(requestCode == REQUEST_CHANGE_CAMERA_PERMISSION)
            {
                int newPermission = grantResults[0];
                if(newPermission == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("RequestPermissions", "Camera permission granted");
                }
                else
                {
                    Log.d("RequestPermissions", "Camera permission denied");
                }
            }
            else if(requestCode == REQUEST_CHANGE_STORAGE_PERMISSION)
            {
                int newPermission = grantResults[0];
                if(newPermission == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("RequestPermissions", "ExtStorage permission granted");
                }
                else
                {
                    Log.d("RequestPermissions", "ExtStorage permission denied");
                }
            }
        }
    }

    boolean isServiceRunning(Context context, Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
