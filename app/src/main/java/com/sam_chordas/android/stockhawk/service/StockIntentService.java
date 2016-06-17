package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.sam_chordas.android.stockhawk.ui.ResponseReceiver;

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
    int i;
    if (intent.getStringExtra("tag").equals("add")){
        args.putString("symbol", intent.getStringExtra("symbol"));
    }



      if (intent.getStringExtra("tag").equals("history"))
      {
          Bundle bundleH = new Bundle();
          bundleH.putString("symbol", intent.getStringExtra("symbol"));
          stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), bundleH));

          Intent again = new Intent (this, DetailActivity.class);

          intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);


//          again = intent;

  //        startActivity(again);
//
//          receiver = new ResponseReceiver();
//          Intent intentmsg = new Intent(DetailActivity.BROADCAST_CHART_MSG);
//          LocalBroadcastManager.getInstance(StockIntentService.this).registerReceiver(receiver, new IntentFilter(DetailActivity.BROADCAST_CHART_MSG));
//          LocalBroadcastManager.getInstance(StockIntentService.this).sendBroadcast(intentmsg);

      }


      i = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

      if (i == -1) {
          receiver = new ResponseReceiver();
          Intent intentmsg = new Intent(ResponseReceiver.BROADCAST_SEND_MSG);
          LocalBroadcastManager.getInstance(StockIntentService.this).registerReceiver(receiver, new IntentFilter(ResponseReceiver.BROADCAST_SEND_MSG));

          LocalBroadcastManager.getInstance(StockIntentService.this).sendBroadcast(intentmsg);

      }


  }
}
