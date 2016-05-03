package com.example.mn913a;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

public class NanoAppWidgetProvider extends AppWidgetProvider {
	String Tag = "Appwidget for maestrogen advertisement";
	RemoteViews views;
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		views = new RemoteViews(context.getPackageName(), R.layout.ad1_appwidget_layout);
		views.setImageViewResource(R.id.mn913a_ad_imagebutton1, R.drawable.mn913a_logo);
		//views.setTextViewText(R.id.maestrogen_ad_textView1, Html.fromHtml("<a href=" + "\"http://www.maestrogen.com\"" + ">" + context.getResources().getString(R.string.main_title) + "</a> "));
		views.setTextViewText(R.id.maestrogen_ad_textView1, context.getResources().getString(R.string.main_title));
		
		// Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, NanoActivity.class);
        //intent.setAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
		views.setOnClickPendingIntent(R.id.mn913a_ad_imagebutton1, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		Log.d(Tag, "onUpdate");
	}
	
    public void onDeleted(Context context, int[] appWidgetIds) {
    	Log.d(Tag, "onDeleted"+Integer.toString(appWidgetIds.length));
    }
    
    public void onEnabled(Context context) {
    	Log.d(Tag, "onEnabled");
    }
    
    public void onDisabled(Context context) {
    	Log.d(Tag, "onDisabled");
    }
}