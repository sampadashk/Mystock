package com.udacity.stockhawk;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by KV on 9/4/17.
 */

public class StockWidgetService extends RemoteViewsService {
    private Cursor data = null;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockListRemoteFactory();
    }

    private class StockListRemoteFactory implements RemoteViewsFactory {
        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            if (data != null) data.close();

            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);

        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;

            }
        }

        @Override
        public int getCount() {
            return data==null?0:data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }
            RemoteViews rViews = new RemoteViews(getPackageName(),
                    R.layout.list_item_quote);
            String sSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float sPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            int bkgDrawable;

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+");
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);
            if (absChange > 0) {
                bkgDrawable = R.drawable.percent_change_pill_green;
            } else {
                bkgDrawable = R.drawable.percent_change_pill_red;
            }
            rViews.setTextViewText(R.id.symbol, sSymbol);
            rViews.setTextViewText(R.id.price,dollarFormat.format(sPrice));
            rViews.setTextViewText(R.id.change,dollarFormat.format(absChange));
            rViews.setInt(R.id.change, "setBackgroundResource", bkgDrawable);
            rViews.setInt(R.id.list_item_quote, "setBackgroundResource", R.color.material_grey_850);
            final Intent fillInIntent = new Intent();
            Uri stockUri = Contract.Quote.makeUriForStock(sSymbol);
            fillInIntent.setData(stockUri);
            rViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);



            return rViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return data.moveToPosition(position)?data.getLong(Contract.Quote.POSITION_ID):position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
