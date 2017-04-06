package com.udacity.stockhawk;

import android.support.v4.util.Pair;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by KV on 5/4/17.
 */

public class Parser {

    /**
     * @param history data retrieved from database
     *                example : 1480050000000:60.4453453$148005002340:61.442343453$
     * @return formatted {@link java.util.List} where key
     * is the date and value is the closing stock value
     */
    public static Pair<Float, List<Entry>> getFormattedStockHistory(String history) {
        Log.d("chkhistory",history);
        List<Entry> entries = new ArrayList<>();
        List<Float> timeData = new ArrayList<>();
        List<Float> stockPrice = new ArrayList<>();
        String[] dataPairs = history.split("\\$");

        for (String pair : dataPairs) {
            String[] entry = pair.split(":");
            timeData.add(Float.valueOf(entry[0]));
            stockPrice.add(Float.valueOf(entry[1]));
        }



        Collections.reverse(timeData);
        Collections.reverse(stockPrice);
        Float referenceTime = timeData.get(0);
        Log.d("chkref",referenceTime.toString());
        for (int i = 0; i < timeData.size(); i++) {
            Log.d("chktime", String.valueOf((timeData.get(i)- referenceTime)));
            entries.add(new Entry(timeData.get(i) - referenceTime, stockPrice.get(i)));
        }
        return new Pair<>(referenceTime, entries);
    }
}
