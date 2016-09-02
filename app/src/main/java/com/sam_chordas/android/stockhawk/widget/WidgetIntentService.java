package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by erikllerena on 6/22/16.
 */
//unused class
public class WidgetIntentService extends IntentService {

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                DetailWidgetProvider.class));


        Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                null,
                null,
                null);


        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        int weatherId = data.getColumnIndex("_id");

        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;

            layoutId = R.layout.widget_detail;


            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.widget_detail_list_item);

//                symbol = (TextView) findViewById(R.id.stock_symbol);
//                bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
//                change = (TextView) itemView.findViewById(R.id.change);

//                viewHolder.symbol.setText(data.getString(data.getColumnIndex("symbol")));
//                viewHolder.bidPrice.setText(data.getString(data.getColumnIndex("bid_price")));
            views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex(getResources().getString(R.string.symbol))));
            views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex("bid_price")));


            Intent launchIntent = new Intent(this, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}