package com.example.mn913a;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {
	boolean update = false;
	static SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd.HHmmss");
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
		Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        update = false;
        DatePickerDialog dpd = new DatePickerDialog( getActivity(), AlertDialog.THEME_HOLO_LIGHT,this,year,month,day );
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new    
        	    android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d ( "tag", "print" );
						update = true;
					}
        	
        });
        
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new    
        	    android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d ( "tag", "print" );
						update = false;
						dismiss();
					}
        	
        });
        dpd.getDatePicker().setCalendarViewShown( false );
        return dpd;
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		
		if ( update == true ) {
          Log.d ( "tag", "set date" );
          //( ( DatePickerDialog ) this.getDialog() ).updateDate ( year, monthOfYear, dayOfMonth );
          cal.setTimeInMillis ( 0 );
          cal.set( year, monthOfYear, dayOfMonth, 0, 0, 0 );
          //Date chosenDate = cal.getTime();
          Log.d ( "tag", df1.format( cal.getTime() ) );
          try {
              Process loProcess = Runtime.getRuntime().exec("su");
              DataOutputStream loDataOutputStream = new DataOutputStream(loProcess.getOutputStream());
              loDataOutputStream.writeBytes("date -s" + df1.format( cal.getTime() ) + "; \n");
          } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
          View decorView = getActivity().getWindow().getDecorView();
          // Hide both the navigation bar and the status bar.
          // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
          // a general rule, you should design your app to hide the status bar whenever you
          // hide the navigation bar.
          int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
          decorView.setSystemUiVisibility(uiOptions);
		}
	}
	
	@Override
	public void onDismiss ( DialogInterface dialog ) {
		super.onDismiss( dialog );
	}

}
