package com.example.zoom.chat.managers;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.zoom.chat.ChatHelper;


public class BackgroundListener implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onBackground() {
        ChatHelper.getInstance().destroy();
        Log.d("BackgroundListener", "Background Successful");
    }
}