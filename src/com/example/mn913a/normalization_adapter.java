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

public class normalization_adapter extends BaseAdapter {
	private Context mContext;
	private List<HashMap<String, String>> mItems;
	
	public normalization_adapter(Context context, List<HashMap<String, String>> items) {
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
        LinearLayout normalization_layout;
        TextView tv;
        
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.normalization_listview_item, parent, false);
        }
        normalization_layout = ( LinearLayout ) v;
        
        tv =  ( TextView ) normalization_layout.findViewById( R.id.item1 );
        tv.setText( map.get("No.") );
        tv =  ( TextView ) normalization_layout.findViewById( R.id.item2 );
        tv.setText( map.get("Conc.") );
        tv =  ( TextView ) normalization_layout.findViewById( R.id.item3 );
        tv.setText( map.get("sample_vol") );
        tv =  ( TextView ) normalization_layout.findViewById( R.id.item4 );
        tv.setText( map.get("buffer_vol") );
		return v;
	}

}
