package com.example.mn913a;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class dna_result_adapter extends BaseAdapter {
	private Context mContext;

    //private List<Object> mItems;
	private List<HashMap<String, String>> mItems;


    public dna_result_adapter(Context context, List<HashMap<String, String>> items) {
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
        LinearLayout dna_result_layout;
        TextView tv;
        
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.dna_result_listview_item, parent, false);
        }
        dna_result_layout = ( LinearLayout ) v;
        
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item1 );
        tv.setText( Integer.toString ( position ) );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item2 );
        tv.setText( map.get("Conc.") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item3 );
        tv.setText( map.get("A260") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item4 );
        tv.setText( map.get("A260_A230") );
        tv =  ( TextView ) dna_result_layout.findViewById( R.id.item5 );
        tv.setText( map.get("A260_A280") );
		return v;
	}

}
