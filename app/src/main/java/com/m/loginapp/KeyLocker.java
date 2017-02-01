package com.m.loginapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.m.loginapp.R;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;


public class KeyLocker
{
    private OverlayDialog mOverlayDialog;

    public OverlayDialog getOverlayDialog()
    {
        return mOverlayDialog;
    }


    public void prepare(Activity activity)
    {
        if(mOverlayDialog == null)
        {
            mOverlayDialog = new OverlayDialog(activity);
        }
    }

    public void lock()
    {
        if(mOverlayDialog != null)
        {
            mOverlayDialog.show();
        }
    }

    public void unlock()
    {
        if(mOverlayDialog != null)
        {
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
        }
    }

    private static class OverlayDialog extends AlertDialog
    {
        Activity _activity;

        public OverlayDialog(Activity activity)
        {
            super(activity, 0);
            _activity = activity;

            Display display = _activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.type = TYPE_SYSTEM_ERROR;
            params.dimAmount = 0.0F; // transparent
            params.width = 1;
            params.height = 1;
            params.gravity = Gravity.NO_GRAVITY;
            getWindow().setAttributes(params);
            getWindow().setFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_NOT_TOUCH_MODAL, 0xffffff);
            setOwnerActivity(activity);
            setCancelable(false);
        }

        protected final void onCreate(Bundle bundle)
        {
            super.onCreate(bundle);
            FrameLayout framelayout = new FrameLayout(getContext());
            framelayout.setBackgroundColor(0);
            setContentView(framelayout);
        }
    }
}
