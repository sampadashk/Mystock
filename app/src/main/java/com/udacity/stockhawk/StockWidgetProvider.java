package com.udacity.stockhawk;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DataGraph;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by KV on 9/4/17.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context ct, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(ct, appWidgetManager, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(ct.getPackageName(), R.layout.stock_widget);
            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(ct, remoteViews);
            } else {
                setRemoteAdapterV11(ct, remoteViews);
            }
            Intent intent = new Intent(ct, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(ct, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            Intent clickIntentTemplate = new Intent(ct, DataGraph.class);

            PendingIntent pendingIntentTemplate = TaskStackBuilder.create(ct)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list, pendingIntentTemplate);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);
            remoteViews.setInt(R.id.widget_list, "setBackgroundResource", R.color.material_grey);
            remoteViews.setInt(R.id.widget_content, "setBackgroundResource", R.color.material_grey);
            remoteViews.setContentDescription(R.id.widget_list, ct.getString(R.string.widget_desc));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, StockWidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, StockWidgetService.class));
    }

}
