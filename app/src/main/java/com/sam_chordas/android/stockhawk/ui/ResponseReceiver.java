package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by erikllerena on 6/12/16.
 */
public class ResponseReceiver extends BroadcastReceiver
{
public ResponseReceiver() {

}
    public static String BROADCAST_SEND_MSG = "broadcast_send_msg";
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "OOps - Symbol doesn't exists !", Toast.LENGTH_LONG).show();
    }
}
