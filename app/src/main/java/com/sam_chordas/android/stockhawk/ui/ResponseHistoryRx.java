package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by erikllerena on 6/16/16.
 */
public class ResponseHistoryRx extends BroadcastReceiver
{
    public ResponseHistoryRx() {

    }

    public static String BROADCAST_SEND_MSG = "broadcast_send_msg";
    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "From History Rx", Toast.LENGTH_LONG).show();

    }
}