package com.m.loginapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ListenForScreenService extends Service
{
    public static final String SCREEN_STATE_TAG = "ScreenState";
    public static final String ACTION_LOGIN_ACTIVITY_RUNNING = "loginapp.LOGIN_ACTIVITY_RUNNING";
    public static final String ACTION_LOGIN_ACTIVITY_CLOSED = "loginapp.LOGIN_ACTIVITY_CLOSED";
    public static final String LOGIN_PASSWORD_OK_TAG = "PasswordTag";
    public static final String LOGIN_ACTIVITY_STATE_TAG = "LoginActivityState";

    private boolean _loginActivityRunning = false;

    public class ScreenReceiver extends BroadcastReceiver
    {
        private boolean _screenOff;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            android.util.Log.i("ScreenReceiver", "wywolane onReceive");
            if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
            {
                _screenOff = true;
                android.util.Log.i("ScreenReceiver", "Ekran wylaczony");
            }
            else if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY))
            {
                _screenOff = false;
                android.util.Log.i("ScreenReceiver", "Ekran wlaczony");
            }

            Intent i = new Intent(context, ListenForScreenService.class);
            i.putExtra(SCREEN_STATE_TAG, _screenOff);
            context.startService(i);
        }
    }

    public class LoginActivityStatusChangedReceiver extends BroadcastReceiver
    {
        private boolean _loginActivityRunning;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            android.util.Log.i("LoginAct...Receiver", "Wywolane onReceive");
            if(intent.getAction().equals(ACTION_LOGIN_ACTIVITY_RUNNING))
            {
                android.util.Log.i("LoginAct...Receiver",
                                   "LoginLockActivity uruchomione");
                _loginActivityRunning = true;
            }
            else if(intent.getAction().equals(ACTION_LOGIN_ACTIVITY_CLOSED))
            {
                android.util.Log.i("LoginAct...Receiver",
                                   "LoginLockActivity wylaczone");
                _loginActivityRunning = false;
            }

            Intent i = new Intent(context, ListenForScreenService.class);
            i.putExtra(LOGIN_ACTIVITY_STATE_TAG, _loginActivityRunning);
            if(intent.hasExtra(LOGIN_PASSWORD_OK_TAG))
            {
                i.putExtra(LOGIN_PASSWORD_OK_TAG, intent.getBooleanExtra(LOGIN_PASSWORD_OK_TAG, true));
            }
            context.startService(i);
        }
    }

    BroadcastReceiver _screenReceiver;
    BroadcastReceiver _loginActivityStatusChangedReceiver;

    public ListenForScreenService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Rejestrujemy nasz ScreenReceiver, dla ktorego bedzie
        // wywolywana onReceive przy wlaczaniu/wylaczaniu ekranu (wybor akcji w IntentFilter)
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        _screenReceiver = new ScreenReceiver();
        registerReceiver(_screenReceiver, filter);

        // Rejestrujemy LoginActivityStatusChangedReceiver sledzacy stan naszej blokady
        IntentFilter filter2 = new IntentFilter(ACTION_LOGIN_ACTIVITY_RUNNING);
        filter2.addAction(ACTION_LOGIN_ACTIVITY_CLOSED);
        _loginActivityStatusChangedReceiver = new LoginActivityStatusChangedReceiver();
        registerReceiver(_loginActivityStatusChangedReceiver, filter2);

        android.util.Log.i("ListenForScreenService", "Usluga stworzona");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null)
        {
            if(intent.hasExtra(SCREEN_STATE_TAG))
            {
                // Usluga dziala z poziomu ScreenReceiver - zmiana stanu ekranu
                boolean screenOff = intent.getBooleanExtra(SCREEN_STATE_TAG, false);
                if(screenOff)
                {
                    // Ekran zostal wylaczony
                    android.util.Log.i("ListenForScreenService", "Ekran wylaczony");
                }
                else
                {
                    // Ekran zostal wlaczony
                    android.util.Log.i("ListenForScreenService", "Ekran wlaczony");
                    if(!_loginActivityRunning)
                    {
                        // Nasza aplikacja blokujaca jest wylaczona, tak wiec stworzmy nowa
                        Intent actStartIntent = new Intent(this, LoginLockActivity.class);
                        actStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(actStartIntent);
                    }
                }
            }
            else if(intent.hasExtra(LOGIN_ACTIVITY_STATE_TAG))
            {
                // Usluga dziala z poziomu LoginActivityStatusChangedReceiver - zmiana stanu login activity
                _loginActivityRunning = intent.getBooleanExtra(LOGIN_ACTIVITY_STATE_TAG, false);
            }
        }

        return Service.START_STICKY;
    }

    // Bindowanie nie obslugiwane
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    // Bindowanie nie obslugiwane
    @Override
    public boolean onUnbind(Intent intent)
    {
        return false;
    }


    // Bindowanie nie obslugiwane
    @Override
    public void onRebind(Intent intent)
    {

    }

    @Override
    public void onDestroy()
    {
        // Usuwamy (unregister) nasz ScreenReceiver
        unregisterReceiver(_screenReceiver);
        unregisterReceiver(_loginActivityStatusChangedReceiver);
        android.util.Log.i("ListenForScreenService", "Usluga zniszczona");
    }
}
