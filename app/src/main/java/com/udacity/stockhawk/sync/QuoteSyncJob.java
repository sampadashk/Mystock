package com.udacity.stockhawk.sync;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

import static android.os.Looper.getMainLooper;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");
        Calendar from;

      //  Calendar from =Calendar.getInstance();
        Calendar to = Calendar.getInstance();
       // from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                setStockStatus(context, STOCK_STATUS_EMPTY);
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            if (quotes.isEmpty()) {
                setStockStatus(context, STOCK_STATUS_SERVER_DOWN);
                return;
            }
            //StockQuote and stock are classes in yahoofinance

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                float price,change,percentChange;
                float dayLow=-1;
                float dayHigh=-1;
                String stockExchange;
                String stockName;
                final String symbol = iterator.next();


                Stock stock = quotes.get(symbol);
                try {
                    StockQuote quote = stock.getQuote();
                    BigDecimal tmp=quote.getDayHigh();
                    if(tmp!=null)
                    {
                        dayLow=quote.getDayLow().floatValue();
                        dayHigh=tmp.floatValue();
                    }
                    stockName=stock.getName();
                    stockExchange=stock.getStockExchange();


                    price = quote.getPrice().floatValue();
                   change = quote.getChange().floatValue();
                     percentChange = quote.getChangeInPercent().floatValue();

                }
                catch (NullPointerException exception) {

                    Timber.e(exception, "Incorrect stock symbol entered : " + symbol);
                    Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, String.format(context.getString(R.string.invalid),symbol), Toast.LENGTH_LONG).show();
                        }
                    });
                  //  showErrorToast(context, symbol);
                    PrefUtils.removeStock(context, symbol);
                    if (PrefUtils.getStocks(context).size() == 0) {
                        setStockStatus(context, STOCK_STATUS_EMPTY);
                    } else {
                        setStockStatus(context, STOCK_STATUS_INVALID);
                    }
                   // invalidFlag = true;
                    continue;
                }
                //this id for 1 week
                from=Calendar.getInstance();
                from.add(Calendar.DAY_OF_YEAR,-5);
                String dayHistory=getHistory(stock,from,to,Interval.DAILY);
                Timber.d("dhis",dayHistory);
                from=Calendar.getInstance();
                from.add(Calendar.MONTH,-1);
                String monthHistory=getHistory(stock,from,to,Interval.DAILY);
                from=Calendar.getInstance();
                from.add(Calendar.MONTH,-6);
                String halfyearHistory=getHistory(stock,from,to,Interval.MONTHLY);
                from=Calendar.getInstance();
                from.add(Calendar.YEAR,-1);
                String yearly=getHistory(stock,from,to,Interval.MONTHLY);



                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
             /*   List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }
                */

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_NAME,stockName);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_EXCHANGE,stockExchange);
                quoteCV.put(Contract.Quote.COLUMN_DAY_HISTORY,dayHistory);
                quoteCV.put(Contract.Quote.COLUMN_MONTH_HISTORY,monthHistory);
                quoteCV.put(Contract.Quote.COLUMN_HALFYEARLY_HISTORY,halfyearHistory);
                quoteCV.put(Contract.Quote.COLUMN_YEAR_HISTORY,yearly);
                quoteCV.put(Contract.Quote.COLUMN_DAY_HIGH,dayHigh);
                quoteCV.put(Contract.Quote.COLUMN_DAY_LOW,dayLow);


              //  quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }



    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }
    private static String getHistory(Stock stock, Calendar from, Calendar to, Interval interval) throws IOException {

        List<HistoricalQuote> history = new ArrayList<>();

        //At times, query over 5-7 days history returns very less data at times.
        //hence performing iterative queries until 5 days of data is received.

        if (interval.equals(Interval.DAILY)) {
            while (history.size() < 5) {
                history = stock.getHistory(from, to, interval);
                from.add(Calendar.DAY_OF_YEAR, -1);
            }
        } else {
            history = stock.getHistory(from, to, interval);
        }

        StringBuilder historyBuilder = new StringBuilder();
        for (HistoricalQuote it : history) {
            historyBuilder.append(it.getDate().getTimeInMillis());
            historyBuilder.append(":");
            historyBuilder.append(it.getClose());
            historyBuilder.append("$");
        }
        return historyBuilder.toString();
    }
    @SuppressLint("CommitPrefEdits")
    static private void setStockStatus(Context c, @StockStatus int setStockStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(c.getString(R.string.pref_stock_status_key), setStockStatus);
        editor.commit();
    }
    //TODO
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_STATUS_OK, STOCK_STATUS_SERVER_DOWN, STOCK_STATUS_SERVER_INVALID, STOCK_STATUS_INVALID, STOCK_STATUS_UNKNOWN, STOCK_STATUS_EMPTY})
    public @interface StockStatus {
    }
    public static final int STOCK_STATUS_OK=0;
    public static final int STOCK_STATUS_SERVER_DOWN = 1;
    public static final int STOCK_STATUS_SERVER_INVALID = 2;
    public static final int STOCK_STATUS_UNKNOWN = 3;
    public static final int STOCK_STATUS_INVALID = 4;
    public static final int STOCK_STATUS_EMPTY = 5;


}
