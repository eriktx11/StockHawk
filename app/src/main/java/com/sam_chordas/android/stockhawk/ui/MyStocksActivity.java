package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.widget.DetailWidgetProvider;

import javax.security.auth.callback.Callback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
//AppCompatActivity
  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;
  private Intent mServiceIntent;
    private Intent again;
  private ItemTouchHelper mItemTouchHelper;
  private static final int CURSOR_LOADER_ID = 0;
    private static final int CURSOR_LOADER_ID_SID = 1;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;
  private Cursor mCursor;
  boolean isConnected;
  private AppPreferences _sPref;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    ConnectivityManager cm =
        (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);


    //ensure device is connected to the network
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    isConnected = activeNetwork != null &&
        activeNetwork.isConnectedOrConnecting();
    setContentView(R.layout.activity_my_stocks);
    // The intent service is for executing immediate pulls from the Yahoo API
    // GCMTaskService can only schedule tasks, they cannot execute immediately
    mServiceIntent = new Intent(this, StockIntentService.class);

    if (savedInstanceState == null){
      // Run the initialize task service so that some stocks appear upon an empty database
      mServiceIntent.putExtra("tag", "init");
      if (isConnected){
        startService(mServiceIntent);
      } else{
        networkToast();
      }
    }
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    _sPref = new AppPreferences(mContext);//initialize sharedPreferences

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
              @Override public void onItemClick(View v, int position) {

                  mCursor.moveToPosition(position);
                  String symbol = mCursor.getString(mCursor.getColumnIndex("symbol"));
                  mServiceIntent.putExtra("tag", "history");


                  //sending the stock symbol to the AsyncAdaptar
                  again = new Intent(mContext, DetailActivity.class);
                  again.putExtra("symbol", symbol);
                  _sPref.saveSmsBody("symbol", symbol);
                  startActivity(again);
              }
                }));

    recyclerView.setAdapter(mCursorAdapter);


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recyclerView);
    fab.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isConnected) {
                new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                        .content(R.string.content_test)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                        new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                        new String[]{input.toString().toUpperCase()}, null);

                                if (c.getCount() != 0) {
                                    Toast toast =
                                            Toast.makeText(MyStocksActivity.this, R.string.already_saved,
                                                    Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                    toast.show();
                                    return;
                                } else {

                                    //go and add a new symbol in the list
                                    mServiceIntent.putExtra("tag", "add");
                                    mServiceIntent.putExtra("symbol", input.toString());
                                    startService(mServiceIntent);
                                }

                                }

                            }).show();

                        }else{
                    networkToast();
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper=new

        ItemTouchHelper(callback);

        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle=getTitle();

        if(isConnected)

        {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


      @Override
  public void onResume() {
    super.onResume();
    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
  }



  public void networkToast(){
    Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
  }

  public void restoreActionBar() {

      AppBarLayout actionBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
      actionBar.setVisibility(AppBarLayout.VISIBLE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_change_units){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){

    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
        QuoteColumns.ISCURRENT + " = ?",
        new String[]{"1"},
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
    mCursor = data;
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }

}
