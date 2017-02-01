package com.m.loginapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.R.attr.id;

/**
 * Created by Kamil on 01-Feb-17.
 */

class BackgroundTaskQueue extends Thread
{
    public static final String MessageTypeTag = "MsgType";

    public static final int MsgIdQuit = 0;
    public static final int MsgIdFinished = 1;
    public static final int MsgIdPushTask = 2;

    private Handler _handler;
    private ConcurrentLinkedQueue<Runnable> _taskQueue = new ConcurrentLinkedQueue<>();
    private boolean _taskRunning;

    public void pushTask(Runnable task)
    {
        _taskQueue.add(task);
        _handler.dispatchMessage(createMessage(MsgIdPushTask));
    }

    public void finishTask()
    {
        _handler.dispatchMessage(createMessage(MsgIdFinished));
    }

    public void quit()
    {
        _handler.dispatchMessage(createMessage(MsgIdQuit));
    }

    private Message createMessage(int id)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(MessageTypeTag, id);
        Message msg = new Message();
        msg.setData(bundle);
        return msg;
    }

    @Override
    public void run()
    {
        Looper.prepare();

        _handler = new MsgHandler();

        _taskRunning = false;
        Looper.loop();
    }

    private class MsgHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            int msgType = msg.getData().getInt(MessageTypeTag);

            if(msgType == MsgIdQuit)
            {
                Looper.myLooper().quit();
            }
            else if(msgType == MsgIdFinished)
            {
                _taskRunning = false;
                if(_taskQueue.size() > 0)
                {
                    Runnable task = _taskQueue.poll();
                    task.run();
                }
            }
            else if(msgType == MsgIdPushTask && !_taskRunning)
            {
                _taskRunning = true;
                Runnable task = _taskQueue.poll();
                task.run();
            }
        }
    }
}
