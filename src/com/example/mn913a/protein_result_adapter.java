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

public class protein_result_adapter extends BaseAdapter {
	private Context mContext;

    //private List<Object> mItems;
	private List<HashMap<String, String>> mItems;
    public protein_result_adapter(Context context, List<HashMap<String, String>> items) {
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
        LinearLayout protein_result_layout;
        TextView tv, tv1;
        
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.protein_result_listview_item, parent, false);
        }
        
        protein_result_layout = ( LinearLayout ) v;
        
        tv =  ( TextView ) protein_result_layout.findViewById( R.id.item1 );
        tv.setText( map.get("No.") );
        tv =  ( TextView ) protein_result_layout.findViewById( R.id.item2 );
        tv.setText( map.get("A280") );
        
        tv =  ( TextView ) protein_result_layout.findViewById( R.id.item3 );
        tv1 =  ( TextView ) protein_result_layout.findViewById( R.id.item4 );
        if ( Double.parseDouble( map.get("Coeff.") ) == -1.0 ) {
        	tv.setText( "           " );
        	tv1.setText( "           " );
        }
        else {
        	tv.setText( map.get( "Coeff." ) );
        	tv1.setText( map.get( "Conc." ) );        	
        }
        
        CheckBox checkbox1 = ( CheckBox ) protein_result_layout.findViewById( R.id.checkbox2 );
        if ( map.get("isSelected") != null && map.get("isSelected").equals( "true" ) ) {
        	checkbox1.setChecked( true );
        }
        else {
        	checkbox1.setChecked( false );
        }
		return v;
	}

}
