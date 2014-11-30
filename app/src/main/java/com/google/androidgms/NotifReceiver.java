package com.google.androidgms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

public class NotifReceiver extends WakefulBroadcastReceiver {
    public NotifReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            String payload=intent.getExtras().getString("message");
            Toast.makeText(context, payload, Toast.LENGTH_SHORT).show();
        }
    }
}
