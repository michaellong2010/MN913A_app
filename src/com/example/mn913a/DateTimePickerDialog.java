package com.example.mn913a;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class DateTimePickerDialog extends AlertDialog {
	
	protected DateTimePickerDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		//this.requestWindowFeature ( Window.); 
    	super.onCreate( savedInstanceState );
    	this.setCanceledOnTouchOutside( true );
    	this.setCancelable( true );
    	
    	LinearLayout date_time_setting_view = ( LinearLayout ) LayoutInflater.from ( findViewById( android.R.id.content ).getContext() ).inflate( R.layout.date_time_setting_layout, null );
    	setContentView ( date_time_setting_view );
    	this.setTitle( "Date & Time setting" );
    	//setContentView ( R.layout.date_time_setting_layout );
    }

}
