package com.example.mn913a;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class dna_result_adapter extends BaseAdapter {
	private Context mContext;

    //private List<Object> mItems;
	private List<HashMap<String, String>> mItems;
    boolean adapter_use_new_ui;

    public dna_result_adapter(Context context, List<HashMap<String, String>> items, boolean use_new_ui) {
        mContext = context;
        mItems = items;
        adapter_use_new_ui = use_new_ui;
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
        LinearLayout dna_result_layout;
        TextView tv;
        AbsListView abs_listview;
        
        if (v == null) {
        	if ( adapter_use_new_ui == false )
        		v = LayoutInflater.from(mContext).inflate(R.layout.dna_result_listview_item, parent, false);
        	else
        		v = LayoutInflater.from(mContext).inflate(R.layout.dna_result_listview_item1, parent, false);
        }
        abs_listview = ( AbsListView ) parent;
        if ( abs_listview.isItemChecked( position ) )
        	v.setSelected( true );
        else
        	v.setSelected( false );
        dna_result_layout = ( LinearLayout ) v;
        
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item1 );
        tv.setText( map.get("No.") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item2 );
        tv.setText( map.get("Conc.") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item3 );
        tv.setText( map.get("A260") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item4 );
        tv.setText( map.get("A260_A230") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item5 );
        tv.setText( map.get("A260_A280") );
        if ( adapter_use_new_ui == true ) {
        	tv =  ( TextView ) dna_result_layout.findViewById( R.id.item6 );
        	if ( map.get("A230") != null)
        		tv.setText( map.get("A230") );
        	else
        		tv.setText( "2.3" );
        	tv =  ( TextView ) dna_result_layout.findViewById( R.id.item7 );
        	
        	if ( map.get("A280") != null)
        		tv.setText( map.get("A280") );
        	else
        		tv.setText( "1.8" );
        }
        CheckBox checkbox1 = ( CheckBox ) dna_result_layout.findViewById( R.id.checkbox2 );
        if ( map.get("isSelected") != null && map.get("isSelected").equals( "true" ) ) {
        	checkbox1.setChecked( true );
        }
        else {
        	checkbox1.setChecked( false );
        }

		return v;
	}
	
	public void setItemSelection ( int position ) {
		
	}

}
