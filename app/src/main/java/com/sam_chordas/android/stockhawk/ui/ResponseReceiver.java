package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by erikllerena on 6/12/16.
 */
public class ResponseReceiver extends BroadcastReceiver
{
public ResponseReceiver() {

}

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "here?", Toast.LENGTH_LONG).show();
    }
}
