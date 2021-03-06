package com.sam_chordas.android.stockhawk.ui;

import android.accounts.Account;
import android.app.Fragment;
import android.app.IntentService;
import android.content.AbstractThreadedSyncAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by erikllerena on 6/13/16.
 */
public class DetailActivity extends ActionBarActivity{
    private String LOG_TAG = DetailActivity.class.getSimpleName();

    LineChart lineChart;

    AppPreferences _sPref;
    Bundle args = new Bundle();
    Intent intent;

    private String history;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_graph);

        _sPref = new AppPreferences(getApplicationContext());
        //intent = getIntent();

//        onHandleIntent(intent);

        lineChart = (LineChart) findViewById(R.id.chartID);

        String symbol = _sPref.getSmsBody(getResources().getString(R.string.symbol));
        new FetchStockList().execute(symbol);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //needed to navigate back to main activity
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //This asycTask is to retrieve all the stock symbol history.
    public class FetchStockList extends AsyncTask<String, Void, String[]> {
        private String LOG_TAG = FetchStockList.class.getSimpleName();

        private String[] getStockDataFromJson(String stockJsonStr)
                throws JSONException {

            //after receiving the stockJsonStr string, it is parsed in order to get its history
            JSONObject stockGroupJson = new JSONObject(stockJsonStr);
            JSONObject stockGroupJsonQ=stockGroupJson.getJSONObject("query");//move to stock header
            JSONObject stockGroupJsonR=stockGroupJsonQ.getJSONObject("results");//move to result header
            JSONArray StockHistoryArray=stockGroupJsonR.getJSONArray("quote");//move to price values


            ArrayList<Entry> yVals = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();

            float vals=0;

            String[] resultStrs = new String[StockHistoryArray.length()];
            for (int i = 0; i < StockHistoryArray.length(); i++) {


                JSONObject chartDataObj = StockHistoryArray.getJSONObject(i);

                //building x and y values for the chart
                yVals.add(new Entry(Float.parseFloat(chartDataObj.getString("Adj_Close")),i));
                xVals.add(i, String.valueOf(vals));

                vals++;

            }

           LineDataSet setting = new LineDataSet(yVals, "Stock Hawk");

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setting);


            LineData data = new LineData(xVals, dataSets);

            lineChart.setData(data);

            Legend l = lineChart.getLegend();
            l.setForm(Legend.LegendForm.LINE);
            l.setTextColor(Color.WHITE);

            XAxis x1 = lineChart.getXAxis();
            x1.setTextColor(Color.WHITE);
            x1.setDrawGridLines(false);
            x1.setAvoidFirstLastClipping(true);

            YAxis y1 = lineChart.getAxisLeft();
            y1.setTextColor(Color.WHITE);
            y1.setAxisMaxValue(120f);
            y1.setDrawGridLines(true);

            return null;
        }


        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String stockJsonStr = null;

            try {


                StringBuilder urlChartBuilder = new StringBuilder();

            urlChartBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                            urlChartBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata " +
                                    "where symbol = \"" + params[0] + "\" " +
                                    "and startDate = \"" + Utils.currentDateOneYearAgo() + "\" and endDate = \"" + Utils.currentDate() + "\" "
                                    , "UTF-8"));

                urlChartBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");


                URL url = new URL(urlChartBuilder.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    return null;
                }
                stockJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, getResources().getString(R.string.error_w_stream), e);
                    }
                }
            }

            try {
                return getStockDataFromJson(stockJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            lineChart.invalidate();
        }

    }
}


