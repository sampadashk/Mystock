package com.udacity.stockhawk.data;


import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {

    static final String AUTHORITY = "com.udacity.stockhawk";
    static final String PATH_QUOTE = "quote";
    static final String PATH_QUOTE_WITH_SYMBOL = "quote/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private Contract() {
    }

    @SuppressWarnings("unused")
    public static final class Quote implements BaseColumns {

        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build();
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_ABSOLUTE_CHANGE = "absolute_change";
        public static final String COLUMN_PERCENTAGE_CHANGE = "percentage_change";
        public static final String COLUMN_STOCK_NAME="stock_name";
        public static final String COLUMN_STOCK_EXCHANGE="stock_exchange";
        public static final String COLUMN_DAY_HISTORY="day_history";
        public static final String COLUMN_MONTH_HISTORY="month_history";
        public static final String COLUMN_HALFYEARLY_HISTORY="half_history";
        public static final String COLUMN_YEAR_HISTORY="year_history";
        public static final String COLUMN_DAY_HIGH="day_high";
        public static final String COLUMN_DAY_LOW="day_low";

       // public static final String COLUMN_HISTORY = "history";
        public static final int POSITION_ID = 0;
        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_PRICE = 2;
        public static final int POSITION_ABSOLUTE_CHANGE = 3;
        public static final int POSITION_PERCENTAGE_CHANGE = 4;
        public static final int POSITION_STOCK_NAME=5;
        public static final int POSITION_STOCK_EXCHANGE=6;
        public static final int POSITION_DAY_HISTORY = 7;
        public static final int POSITION_MONTH_HISTORY = 8;
        public static final int POSITION_HALFYEAR_HISTORY = 9;
        public static final int POSITION_YEAR_HISTORY = 10;
        public static final int POSITION_DAY_HIGH = 11;
        public static final int POSITION_DAY_LOW = 12;
        public static final String[] QUOTE_COLUMNS = {
                _ID,
                COLUMN_SYMBOL,
                COLUMN_PRICE,
                COLUMN_ABSOLUTE_CHANGE,
                COLUMN_PERCENTAGE_CHANGE,
                COLUMN_STOCK_NAME,
                COLUMN_STOCK_EXCHANGE,
                COLUMN_DAY_HISTORY,
                COLUMN_MONTH_HISTORY,
                COLUMN_HALFYEARLY_HISTORY,
                COLUMN_YEAR_HISTORY,
                COLUMN_DAY_HIGH,
                COLUMN_DAY_LOW


        };
        static final String TABLE_NAME = "quotes";

        public static Uri makeUriForStock(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        static String getStockFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }


    }

}
