package com.sam_chordas.android.stockhawk.service;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utility;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.AppPreferences;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  public static final String ACTION_DATA_UPDATED =
            "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;

  private Intent again;

  private AppPreferences sPref;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }


    private void updateWidgets() {
        Context context = mContext;
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


  @Override
  public int onRunTask(TaskParams params) {
      Cursor initQueryCursor;
      if (mContext == null) {
          mContext = this;
      }
      StringBuilder urlStringBuilder = new StringBuilder();

      if (!params.getTag().equals("history")){

      try {
          // Base URL for the Yahoo query
          urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
          urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                  + "in (", "UTF-8"));
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
      if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
          isUpdate = true;
          initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                  new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                  null, null);
          if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
              // Init task. Populates DB with quotes for the symbols seen below
              try {
                  urlStringBuilder.append(
                          URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
              } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
              }
          } else if (initQueryCursor != null) {
              DatabaseUtils.dumpCursor(initQueryCursor);
              initQueryCursor.moveToFirst();
              for (int i = 0; i < initQueryCursor.getCount(); i++) {
                  mStoredSymbols.append("\"" +
                          initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                  initQueryCursor.moveToNext();
              }
              mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
              try {
                  urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
              } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
              }
          }
      } else if (params.getTag().equals("add")) {
          isUpdate = false;
          // get symbol from params.getExtra and build query
          String stockInput = params.getExtras().getString("symbol");
          try {
              urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
          }
      }
      // finalize the URL for the API query.
      urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
              + "org%2Falltableswithkeys&callback=");

      String urlString;
      String getResponse;
      int result = GcmNetworkManager.RESULT_FAILURE;

      if (urlStringBuilder != null) {
          urlString = urlStringBuilder.toString();
          try {
              getResponse = fetchData(urlString);
              result = GcmNetworkManager.RESULT_SUCCESS;
              try {
                  ContentValues contentValues = new ContentValues();
                  // update ISCURRENT to 0 (false) so new data is current
                  if (isUpdate) {
                      contentValues.put(QuoteColumns.ISCURRENT, 0);
                      mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                              null, null);
                  }

                  Utils Classquote = new Utils();
                  ArrayList OBJresult = Classquote.quoteJsonToContentVals(getResponse);

                  //it OBJresult comes back empty that means the user entered a symbol that doesn't exist.
                  if (OBJresult.size() == 0 && !params.getTag().equals("history")) {
                      return -1;//if stock doesn't exist it finishes and exits the code.
                  } else {

                      mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                              OBJresult);
                  }

              } catch (RemoteException | OperationApplicationException e) {
                  Log.e(LOG_TAG, getResources().getString(R.string.errbi), e);
              }

          } catch (IOException e) {
              e.printStackTrace();
          }

          updateWidgets();
      }

          return result;
  }

      if (params.getTag().equals("history")){

          sPref = new AppPreferences(mContext);
          sPref.saveSmsBody("symbol", params.getExtras().getString("symbol"));

          StringBuilder urlChartBuilder = new StringBuilder();

          Cursor initChartCursor;

          initChartCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                  new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
                  null, null);


          urlChartBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");

          String urlSChart;
          String getResponseForChart="";
          int resultChart = GcmNetworkManager.RESULT_FAILURE;

          try {// ... + initChartCursor.getString(1).toString() + ...
              urlChartBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata " +
                      "where symbol = \"" + params.getExtras().getString("symbol") + "\" " +
                      "and startDate = \"" + Utils.currentDateOneYearAgo() + "\" and endDate = \"" + Utils.currentDate() + "\" "
                      , "UTF-8"));

              urlChartBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                      + "org%2Falltableswithkeys&callback=");


              if (urlChartBuilder != null) {
                  urlSChart = urlChartBuilder.toString();
                  try {
                      resultChart = GcmNetworkManager.RESULT_SUCCESS;
                      getResponseForChart = fetchData(urlSChart);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
          }

          //so I can plot the history later
          sPref.saveSmsBody(params.getExtras().getString("history"), getResponseForChart);

          return 1;

      }
     return 1;
  }

}
