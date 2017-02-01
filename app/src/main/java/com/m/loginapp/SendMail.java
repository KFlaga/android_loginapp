package com.m.loginapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.net.PasswordAuthentication;

import javax.sql.DataSource;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by Mateusz on 28.01.2017.
 */

public class SendMail extends AsyncTask<Void, Void, Void>
{

    public static final String EMAIL = "some_email";
    public static final String PASSWORD = "some_password";

    private Context context;
    private Session session;
    private String targetEmail;
    private byte[] imgDataFront;
    private byte[] imgDataBack;

    private ProgressDialog progressDialog;

    public SendMail(Context context, byte[] imgDataFront, byte[] imgDataBack, String targetEmail)
    {
        this.context = context;
        this.targetEmail = targetEmail;
        this.imgDataFront = imgDataFront;
        this.imgDataBack = imgDataBack;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        Log.d("SendMail", "email sent");
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props,
                                             new javax.mail.Authenticator()
                                             {
                                                 //Authenticating the password
                                                 protected PasswordAuthentication getPasswordAuthentication()
                                                 {
                                                     return new PasswordAuthentication(EMAIL, PASSWORD);
                                                 }
                                             });

        try
        {
            MimeMessage mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(EMAIL));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail)); // docelowy taki jak źródła
            mm.setSubject("LoginApp - próba odblokowania zakończona niepowodzeniem");
            mm.setSentDate(new Date());
            // mm.setText("LoginApp - próba odblokowania zakończona niepowodzeniem");

            MimeMultipart _multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("LoginApp - próba odblokowania zakończona niepowodzeniem");
            _multipart.addBodyPart(messageBodyPart);

            if(imgDataFront != null)
            {
                MimeBodyPart messageBodyPart_imgFront = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(imgDataFront, "image/jpeg");
                messageBodyPart_imgFront.setDataHandler(new DataHandler(source));
                messageBodyPart_imgFront.setFileName("cam_front.jpg");
                _multipart.addBodyPart(messageBodyPart_imgFront);
            }

            if(imgDataBack != null)
            {
                MimeBodyPart messageBodyPart_imgBack = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(imgDataBack, "image/jpeg");
                messageBodyPart_imgBack.setDataHandler(new DataHandler(source));
                messageBodyPart_imgBack.setFileName("cam_back.jpg");
                _multipart.addBodyPart(messageBodyPart_imgBack);
            }

            mm.setContent(_multipart);
            Transport.send(mm);
        }
        catch(MessagingException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
