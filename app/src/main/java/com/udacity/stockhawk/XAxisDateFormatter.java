package com.udacity.stockhawk;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by KV on 5/4/17.
 */

public class XAxisDateFormatter implements IAxisValueFormatter {

    private final SimpleDateFormat dateFormat;
    private final Date date;
    private final Float referenceTime;

    public XAxisDateFormatter(String dateFormat, Float referenceTime) {
        this.dateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        this.date = new Date();
        this.referenceTime = referenceTime;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        date.setTime((long) (value + referenceTime));
        return dateFormat.format(date);
    }
}
