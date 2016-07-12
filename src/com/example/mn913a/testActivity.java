package com.example.mn913a;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;

public class testActivity extends Activity {
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.log_file_chooser_menu, menu);
		return true;
	}
}
