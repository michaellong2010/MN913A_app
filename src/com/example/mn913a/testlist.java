package com.example.mn913a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import ar.com.daidalos.afiledialog.R;

public class testlist extends Activity {

	ListView lv;
	SimpleAdapter adapter;
	String [] from = { "No.", "Conc.", "A260", "A260_A230", "A260_A280" };
	int [] to = { com.example.mn913a.R.id.item1, com.example.mn913a.R.id.item2, com.example.mn913a.R.id.item3, com.example.mn913a.R.id.item4, com.example.mn913a.R.id.item5 };
	List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
	
	LinearLayout measure_root_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setContentView( R.layout.dna_result_listview );
		this.setContentView(R.layout.daidalos_file_chooser);
		
		for ( int i = 0; i < 10; i++ ) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "No.", "No" );
			map.put( "Conc.", "Conc." );
			map.put( "A260", "A260" );
			map.put( "A260_A230", "A260/A230" );
			map.put( "A260_A280", "A260/A280" );
			fillMaps.add( map );
		}
		
		measure_root_layout = ( LinearLayout ) this.findViewById( R.id.measure_result_Layout );
		measure_root_layout.addView( ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate ( com.example.mn913a.R.layout.result_listview, measure_root_layout, false ) );
		adapter = new SimpleAdapter ( this, fillMaps, com.example.mn913a.R.layout.dna_result_listview_item, from, to );
		lv = ( ListView ) this.findViewById( com.example.mn913a.R.id.listview );
		lv.setAdapter( adapter );
		lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		lv.setOnItemSelectedListener( new OnItemSelectedListener() {


			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.d ( "Tag", "dkvkdsk");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				Log.d ( "Tag", "dkvkdsk11111");
			}
			
		});
	}
}
