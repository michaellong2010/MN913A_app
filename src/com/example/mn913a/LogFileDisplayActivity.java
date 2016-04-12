package com.example.mn913a;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import ar.com.daidalos.afiledialog.FileChooserActivity;
import ar.com.daidalos.afiledialog.R;


public class LogFileDisplayActivity extends Activity {

	//the selected iTracker log file
	File iTracker_logfile;
	String line;
	StringBuilder text;
	ViewGroup mContent_Layout;
	ViewGroup.MarginLayoutParams margin_lp;
	TextView tv;
	ScrollView sv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      Bundle extras = this.getIntent().getExtras();
      if(extras != null) {
    	  if (extras.containsKey(FileChooserActivity.OUTPUT_FILE_OBJECT)) {
    		  iTracker_logfile = (File) extras.getSerializable(FileChooserActivity.OUTPUT_FILE_OBJECT); 
    	  }
      }
      
      ActionBar abr;
      abr = this.getActionBar();
      if (abr != null && iTracker_logfile != null) {
    	  abr.setTitle(iTracker_logfile.getPath());
    	  abr.setDisplayHomeAsUpEnabled(true);
      }
      
      text = new StringBuilder();
      if (iTracker_logfile != null) {
    	  try {
			BufferedReader buf  = new BufferedReader(new FileReader(iTracker_logfile));
            try {
				while ((line = buf.readLine()) != null) {
					text.append(line);
					text.append('\n');
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }

      View v = getWindow().getDecorView();
      View v1 = getWindow().getDecorView().getRootView();
      //v1.setBackgroundDrawable((getResources().getDrawable(android.R.id.background));
      v1.setBackgroundColor(getResources().getColor(R.color.daidalos_backgroud));
      mContent_Layout = (ViewGroup) this.findViewById(android.R.id.content);
      sv = new ScrollView(this);
      tv = new TextView(this);
      tv.setTextColor(0xFFFFFFFF);
      //mContent_Layout.addView(tv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      sv.addView(tv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      mContent_Layout.addView(sv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);      
      tv.setText(text);     
      margin_lp = (ViewGroup.MarginLayoutParams)tv.getLayoutParams();
      margin_lp.setMargins(20, 0, 0, 0);
      
    }
    
    
    protected void onNewIntent(Intent intent) {
    	
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
	}
	
/*20141022 added by michael
* allowe home as up arrow to back to previous activity */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		  case android.R.id.home:
			  onBackPressed();
			  return true;
		}
		return super.onOptionsItemSelected(item);		
	}
}
