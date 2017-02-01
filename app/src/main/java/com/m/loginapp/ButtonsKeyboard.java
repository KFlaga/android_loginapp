package com.m.loginapp;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class ButtonsKeyboard extends FrameLayout
{

    public interface KeyButtonClickedListener
    {
        void buttonClicked(int key);
    }
    KeyButtonClickedListener _keyListener;

    public ButtonsKeyboard(Context context)
    {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.keyboard_layout, this);
    }

    @Override
    public void onVisibilityChanged(View view, int visibility)
    {
        if(visibility == VISIBLE)
        {
            setButtonEvents();
        }
        else
        {
            resetButtonEvents();
        }
    }

    void setKeyButtonClickedListener(KeyButtonClickedListener listener)
    {
        _keyListener = listener;
    }

    protected void buttonClicked(int buttonKey)
    {
        if(_keyListener != null)
        {
            _keyListener.buttonClicked(buttonKey);
        }
    }

    protected void setButtonEvents()
    {
        Button but_0 = (Button) this.findViewById(R.id._but_0);
        but_0.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('0');
            }
        });

        Button but_1 = (Button) this.findViewById(R.id._but_1);
        but_1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('1');
            }
        });

        Button but_2 = (Button) this.findViewById(R.id._but_2);
        but_2.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('2');
            }
        });

        Button but_3 = (Button) this.findViewById(R.id._but_3);
        but_3.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('3');
            }
        });

        Button but_4 = (Button) this.findViewById(R.id._but_4);
        but_4.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('4');
            }
        });

        Button but_5 = (Button) this.findViewById(R.id._but_5);
        but_5.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('5');
            }
        });

        Button but_6 = (Button) this.findViewById(R.id._but_6);
        but_6.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('6');
            }
        });

        Button but_7 = (Button) this.findViewById(R.id._but_7);
        but_7.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('7');
            }
        });

        Button but_8 = (Button) this.findViewById(R.id._but_8);
        but_8.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('8');
            }
        });

        Button but_9 = (Button) this.findViewById(R.id._but_9);
        but_9.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked('9');
            }
        });

        Button but_clear = (Button) this.findViewById(R.id._but_clear);
        but_clear.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked(KeyEvent.KEYCODE_CLEAR);
            }
        });

        Button but_ok = (Button) this.findViewById(R.id._but_ok);
        but_ok.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonClicked(KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    protected void resetButtonEvents()
    {
        Button but_0 = (Button) this.findViewById(R.id._but_0);
        but_0.setOnClickListener(null);
        Button but_1 = (Button) this.findViewById(R.id._but_1);
        but_1.setOnClickListener(null);
        Button but_2 = (Button) this.findViewById(R.id._but_2);
        but_2.setOnClickListener(null);
        Button but_3 = (Button) this.findViewById(R.id._but_3);
        but_3.setOnClickListener(null);
        Button but_4 = (Button) this.findViewById(R.id._but_4);
        but_4.setOnClickListener(null);
        Button but_5 = (Button) this.findViewById(R.id._but_5);
        but_5.setOnClickListener(null);
        Button but_6 = (Button) this.findViewById(R.id._but_6);
        but_6.setOnClickListener(null);
        Button but_7 = (Button) this.findViewById(R.id._but_7);
        but_7.setOnClickListener(null);
        Button but_8 = (Button) this.findViewById(R.id._but_8);
        but_8.setOnClickListener(null);
        Button but_9 = (Button) this.findViewById(R.id._but_9);
        but_9.setOnClickListener(null);
        Button but_clear = (Button) this.findViewById(R.id._but_clear);
        but_clear.setOnClickListener(null);
        Button but_ok = (Button) this.findViewById(R.id._but_ok);
        but_ok.setOnClickListener(null);
    }
}