package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.CustomMarkerView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.XAxisDateFormatter;
import com.udacity.stockhawk.YAxisPriceFormatter;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

//import static android.R.color.Color.WHITE;

/**
 * Created by KV on 28/3/17.
 */

public class DataGraph extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @SuppressWarnings("WeakerAccess")
    private static int LOADER_ID = 0;

    TextView tv;
    TextView stex;
    TextView lowst;
    TextView highst;
    TextView stckprice;
    TextView abschng;
    Uri stockUri;
    LineChart lineChart;
    String dateFormat;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        tv=(TextView)findViewById(R.id.stName);
        stex=(TextView) findViewById(R.id.stExchange);
        lowst=(TextView) findViewById(R.id.lowst);
        highst=(TextView) findViewById(R.id.hightst);
        lineChart=(LineChart) findViewById(R.id.chart);
        stckprice=(TextView) findViewById(R.id.stock_pri);
        abschng=(TextView) findViewById(R.id.abs_change);
        

        Intent intent=getIntent();
        stockUri=intent.getData();
        getSupportLoaderManager().initLoader(LOADER_ID,null,this);
        dateFormat="dd";





       // ButterKnife.bind(this);




    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    this,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            String stockName = data.getString(Contract.Quote.POSITION_STOCK_NAME);
            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.detail_contentdesc), stockName));

            String stockExchange = data.getString(Contract.Quote.POSITION_STOCK_EXCHANGE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);

            Float dayLowest = data.getFloat(Contract.Quote.POSITION_DAY_LOW);
            Float dayHighest = data.getFloat(Contract.Quote.POSITION_DAY_HIGH);
            String his = data.getString(Contract.Quote.POSITION_MONTH_HISTORY);
            setLineChart(his);
            //This DecimalFomat CurrencyInstance is for putting currency sign before price
            DecimalFormat currencyInstance = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            currencyInstance.setMaximumFractionDigits(2);
            currencyInstance.setMinimumFractionDigits(2);



            lowst.setText(currencyInstance.format(dayLowest));
            highst.setText(currencyInstance.format(dayHighest));
            stex.setText(stockExchange);
            tv.setText(stockName);
            stckprice.setText(currencyInstance.format(stockPrice));
            abschng.setText(currencyInstance.format(absoluteChange));
            Log.d("chkabs",currencyInstance.format(absoluteChange));
            if(absoluteChange>0)
            {
                abschng.setBackgroundResource(R.drawable.percent_change_pill_green);
                abschng.setContentDescription (String.format(getString(R.string.increased_cd), abschng.getText()));
            }
            else
                abschng.setBackgroundResource(R.drawable.percent_change_pill_red);
            abschng.setContentDescription (String.format(getString(R.string.decreased_cd), abschng.getText()));

        }
    }
    private void setLineChart(String history) {

        Pair<Float, List<Entry>> result = com.udacity.stockhawk.Parser.getFormattedStockHistory(history);
        List<Entry> dataPairs = result.second;
        Float referenceTime = result.first;
        Log.d("refch"," "+referenceTime);
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
       // Timber.d(referenceTime.toString());
        //1.45978556E12

       // Timber.d(dataPairs.toString());
        //This is giving result like this :[Entry, x: 0.0 y: 49.87, Entry, x: 2.33281946E9 y: 53.0,
        dataSet.setColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        //lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter(dateFormat, referenceTime));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(12f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(true);
        yAxis.setAxisLineColor(Color.WHITE);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(12f);


        CustomMarkerView customMarkerView = new CustomMarkerView(this,
                R.layout.marker_view, getLastButOneData(dataPairs), referenceTime);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        lineChart.setMarker(customMarkerView);
        lineChart.setVisibility(View.VISIBLE);

        //disable all interactions with the graph
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragDecelerationEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        lineChart.setDescription(description);
        lineChart.setExtraOffsets(10, 0, 0, 10);
        lineChart.animateX(1500, Easing.EasingOption.Linear);
        // TODO: 1/7/2017 Raise issue on MPAndroidChart to add accessibility for chart elements
    }
    private Entry getLastButOneData(List<Entry> dataPairs) {
        if (dataPairs.size() > 2) {
            return dataPairs.get(dataPairs.size() - 2);
        } else {
            return dataPairs.get(dataPairs.size() - 1);
        }
    }



    

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }
}
