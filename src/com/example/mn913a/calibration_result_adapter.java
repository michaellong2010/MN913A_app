package com.example.mn913a;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class calibration_result_adapter extends BaseAdapter {
	private Context mContext;
	private List<HashMap<String, String>> mItems;

    public calibration_result_adapter( Context context, List<HashMap<String, String>> items ) {
        mContext = context;
        mItems = items;
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	@Override
	public HashMap<String, String> getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = convertView;
		HashMap<String, String> map = getItem(position);
		LinearLayout calibration_result_layout;
		TextView tv;
		
		if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.calibration_listview_item, parent, false);
        }
		
		calibration_result_layout = ( LinearLayout ) v;
		
		tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText1 );
        tv.setText( map.get( "datetime" ) );
        
        tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText2 );
        tv.setText( map.get( "before" ) );
        
        tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText3 );
        tv.setText( map.get( "after" ) );
        
        tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText4 );
        tv.setText( map.get( "situation" ) );
        
        if ( map.get( "situation" ).equals( "fail" ) ) {
        	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText1 );
        	tv.setBackgroundResource( android.R.color.holo_red_light );
        	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText2 );
        	tv.setBackgroundResource( android.R.color.holo_red_light );
        	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText3 );
        	tv.setBackgroundResource( android.R.color.holo_red_light );
        	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText4 );
        	tv.setBackgroundResource( android.R.color.holo_red_light );
        }
        else
        	if ( map.get( "situation" ).equals( "pass" ) ) {
            	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText1 );
            	tv.setBackgroundResource( android.R.color.holo_green_light );
            	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText2 );
            	tv.setBackgroundResource( android.R.color.holo_green_light );
            	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText3 );
            	tv.setBackgroundResource( android.R.color.holo_green_light );
            	tv =  ( TextView ) calibration_result_layout.findViewById( R.id.ItemText4 );
            	tv.setBackgroundResource( android.R.color.holo_green_light );
        	}
        
        CheckBox checkbox1 = ( CheckBox ) calibration_result_layout.findViewById( R.id.checkbox2 );
        if ( map.get("isSelected") != null && map.get("isSelected").equals( "true" ) ) {
        	checkbox1.setChecked( true );
        	//protein_result_layout.setSelected( true );
        }
        else {
        	checkbox1.setChecked( false );
        	//protein_result_layout.setSelected( false );
        }
        return v;
	}

}
