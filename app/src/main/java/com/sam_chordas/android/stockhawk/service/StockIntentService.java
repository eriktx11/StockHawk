package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.ResponseReceiver;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    private ResponseReceiver receiver;

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
        args.putString("symbol", intent.getStringExtra("symbol"));
    }

    int i = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

      if(i==-1) {
          receiver = new ResponseReceiver();
          Intent intentmsg = new Intent(ResponseReceiver.BROADCAST_SEND_MSG);
          LocalBroadcastManager.getInstance(StockIntentService.this).registerReceiver(receiver, new IntentFilter(ResponseReceiver.BROADCAST_SEND_MSG));

          LocalBroadcastManager.getInstance(StockIntentService.this).sendBroadcast(intentmsg);

      }
  }
}
