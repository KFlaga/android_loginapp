package com.m.loginapp;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// Kontener dla danych naszej aplikacji (haslo, numer)
// Zapisuje dane w pliku w prywatnej lokalizacji
class AppPrivData
{
    private static AppPrivData ourInstance = new AppPrivData();

    static AppPrivData getInstance()
    {
        return ourInstance;
    }

    private AppPrivData()
    {

    }

    private final String STORAGE_FILE_NAME = "priv_file";

    private String _tempPassword = "";
    private String _savedPassword = "";
    private String _tempEmail = "";
    private String _savedEmail = "";

    private boolean _isLockTurnedOn = false;
    private boolean _isSendEmail = false;

    public String getTempPassword()
    {
        return _tempPassword;
    }

    public void setTempPassword(String value)
    {
        _tempPassword = value;
    }

    public String getSavedPassword()
    {
        return _savedPassword;
    }

    public String getTempEmail()
    {
        return _tempEmail;
    }

    public void setTempEmail(String value)
    {
        _tempEmail = value;
    }

    public String getSavedEmail()
    {
        return _savedEmail;
    }

    public boolean isLockTurnedOn()
    {
        return _isLockTurnedOn;
    }

    public void setLockTurnedOn(boolean value)
    {
        _isLockTurnedOn = value;
    }

    public boolean isSendEmail()
    {
        return _isSendEmail;
    }

    public void setSendEmail(boolean value)
    {
        _isSendEmail = value;
    }

    // Wczytuje nasze dane z domyslnego pliku
    // Zwraca false jesli plik nie istnieje
    boolean readPrivDataFromFile(Context context)
    {
        FileInputStream privFileRead;
        try
        {
            privFileRead = context.openFileInput(STORAGE_FILE_NAME);
        }
        catch(FileNotFoundException ignored) { return false; }

        if(privFileRead != null)
        {
            // Format pliku bardzo prosty:
            // pierw haslo (kazdy znak po bajcie), az do napotkania bajtu 0
            // potem numer telefonu (kazda cyfra po bajcie), az do napotkania bajtu 0
            // potem bajt 1 jesli mamy wlaczone zabezpieczenie lub 0
            // potem bajt 1 jesli mamy wlaczone MMS lub 0

            try
            {
                int c;
                // 1) Haslo
                String password = "";
                while((c = privFileRead.read()) != 0)
                {
                    password = password + Character.toString((char) c);
                }
                _tempPassword = _savedPassword = password;

                // 2) Email
                String address = "";
                while((c = privFileRead.read()) != 0)
                {
                    address = address + Character.toString((char) c);
                }
                _savedEmail = address;

                // 3) Flagi
                _isLockTurnedOn = (c = privFileRead.read()) != 0;
                _isSendEmail = (c = privFileRead.read()) != 0;

                privFileRead.close();
            }
            catch(IOException e)
            {
                return false;
            }
        }
        return true;
    }

    boolean writePrivDataToFile(Context context)
    {
        FileOutputStream privFileWrite;
        try
        {
            privFileWrite = context.openFileOutput(STORAGE_FILE_NAME, Context.MODE_PRIVATE);
        }
        catch(FileNotFoundException ignored) { return false; }

        if(privFileWrite != null)
        {
            // Format pliku bardzo prosty:
            // pierw haslo (kazdy znak po bajcie), az do napotkania bajtu 0
            // potem numer telefonu (kazda cyfra po bajcie), az do napotkania bajtu 0
            // potem bajt 1 jesli mamy wlaczone zabezpieczenie lub 0
            // potem bajt 1 jesli mamy wlaczone MMS lub 0

            try
            {
                // 1) Haslo
                for(int i = 0; i < _tempPassword.length(); ++i)
                {
                    privFileWrite.write(_tempPassword.charAt(i));
                }
                _savedPassword = _tempPassword;
                privFileWrite.write(0);

                // 2) Numer
                for(int i = 0; i < _tempEmail.length(); ++i)
                {
                    privFileWrite.write(_tempEmail.charAt(i));
                }
                _savedEmail = _tempEmail;
                privFileWrite.write(0);

                // 3) Flagi
                privFileWrite.write(_isLockTurnedOn ? 1 : 0);
                privFileWrite.write(_isSendEmail ? 1 : 0);

                privFileWrite.close();
            }
            catch(IOException e)
            {
                return false;
            }
        }
        return true;
    }

    boolean createPrivFile(Context context)
    {
        // Tworzymy nowy plik, narazie ma 4 bajty : 0000
        try
        {
            File file = new File(context.getFilesDir(), STORAGE_FILE_NAME);
            file.createNewFile();
            file.setWritable(true, true);
            file.setReadable(true, true);

            FileOutputStream privFileWrite = context.openFileOutput(STORAGE_FILE_NAME, Context.MODE_PRIVATE);
            privFileWrite.write(new byte[]{ 0, 0, 0, 0 }, 0, 4);
            privFileWrite.close();
        }
        catch(IOException e)
        {
            return false;
        }
        return true;
    }
}
