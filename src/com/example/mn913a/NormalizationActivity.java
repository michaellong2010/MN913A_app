package com.example.mn913a;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mn913a.NanoActivity.DecimalInputFilter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class NormalizationActivity extends Activity {
	public final String Tag = "NormalizationActivity";
	String[] src_from = new String[] { "No.", "Conc.", "A260", "A260_A230", "A260_A280" };
	String[] dst_from = new String[] { "No.", "Conc.", "sample_vol", "buffer_vol" };
	List<HashMap<String, String>> fillMaps, selected_fillMaps;
	LinearLayout measure_root_layout, listview_header;
	ListView result_listview;
	AlertDialog.Builder alert_dlg_builder;
	AlertDialog alert_dlg;
	normalization_adapter adapter2;
	public double target_volume = -1.0, target_conc = -1.0;
	EditText ed_target_volume, ed_target_conc;
	String alert_message;
	Button btn_calculate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView( R.layout.normalization_layout );
		ActionBar abr = this.getActionBar();
    	abr.setTitle("Analysis--Normalization");
    	abr.setDisplayHomeAsUpEnabled(true);
		fillMaps = ( List<HashMap<String, String>> ) this.getIntent().getSerializableExtra( "arraylist" );
		selected_fillMaps = new ArrayList<HashMap<String, String>>();
		Iterator<HashMap<String, String>> it;
		HashMap <String, String> map1, map2;
		it = fillMaps.iterator();
		while ( it.hasNext() ) {
			map1 = it.next();
			if ( map1.get( "isSelected" ) != null && map1.get( "isSelected" ).equals( "true" ) ) {
				map2 = new HashMap <String, String> (); 
				map2.put( dst_from[0], map1.get( src_from[0] ) );
				map2.put( dst_from[1], map1.get( src_from[1] ) );
				map2.put( dst_from[2], "" );
				map2.put( dst_from[3], "" );
				selected_fillMaps.add( map2 );
			}
		}
		
		measure_root_layout = ( LinearLayout ) this.findViewById( R.id.measure_result_Layout );
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout listview_toplayout = ( LinearLayout ) inflater.inflate ( R.layout.result_listview, measure_root_layout, false ).findViewById( R.id.listview_toplayout );
		listview_header = ( LinearLayout ) listview_toplayout.findViewById( R.id.listview_header );
		result_listview = ( ListView ) listview_toplayout.findViewById( R.id.listview );
		
		inflater.inflate ( R.layout.normalization_listview_header, listview_header, true );
		adapter2 = new normalization_adapter ( this, selected_fillMaps );
		result_listview.setAdapter( adapter2 );
		
		result_listview.setOnScrollListener( new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view,
					int scrollState) {
				// TODO Auto-generated method stub
				if ( scrollState == OnScrollListener.SCROLL_STATE_IDLE ) {
					view.smoothScrollToPosition( view.getFirstVisiblePosition() );
	            }
			}

			@Override
			public void onScroll(AbsListView view,
					int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				// TODO Auto-generated method stub
				
			} 
			
		} );
		measure_root_layout.addView( listview_toplayout );
		
		LinearLayout user_setting_layout;
		user_setting_layout = ( LinearLayout ) this.findViewById( R.id.user_setting );
		ed_target_volume = ( EditText ) user_setting_layout.findViewById( R.id.editText_volume );
		ed_target_conc = ( EditText ) user_setting_layout.findViewById( R.id.editText_conc );
		Bundle extras = this.getIntent().getExtras();
    	if ( extras.containsKey ( "target conc." ) ) {
    		target_conc = extras.getDouble( "target conc." );
    		ed_target_conc.setText( Double.toString( target_conc ) );
    	}
    	if ( extras.containsKey ( "target vol." ) ) {
    		target_volume = extras.getDouble( "target vol." );
    		ed_target_volume.setText( Double.toString( target_volume ) );
    	}
        
		ed_target_volume.setFilters(apped_input_filter(ed_target_volume.getFilters(), new DecimalInputFilter(ed_target_volume, 5, 2, 0.0, 2000, 500)));
		ed_target_volume.setOnEditorActionListener ( ed_action_listener );
		ed_target_volume.setOnFocusChangeListener( new View.OnFocusChangeListener( ) {
			EditText ed1;
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				ed1 = ( EditText ) v;
				// TODO Auto-generated method stub
				if ( v.getId() == R.id.editText_volume ) {
					if ( hasFocus == false ) {
						if ( ed1.getText().toString().equals("") == false )
							target_volume = Double.parseDouble( ed1.getText().toString() );
					}
				}
			}
		});
		
		ed_target_conc.setFilters(apped_input_filter(ed_target_conc.getFilters(), new DecimalInputFilter(ed_target_conc, 5, 2, 0.0, 2000, 100)));
		ed_target_conc.setOnEditorActionListener ( ed_action_listener );
		ed_target_conc.setOnFocusChangeListener( new View.OnFocusChangeListener( ) {
			EditText ed1;
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				ed1 = ( EditText ) v;
				// TODO Auto-generated method stub
				if ( v.getId() == R.id.editText_conc ) {
					if ( hasFocus == false ) {
						if ( ed1.getText().toString().equals("") == false )
							target_conc = Double.parseDouble( ed1.getText().toString() );
					}						
				}
			}
		});
		//if ( map1.get( "isSelected" ) != null && map1.get( "isSelected" ).equals( "true" ) ) {
		
		btn_calculate = ( Button ) user_setting_layout.findViewById( R.id.button1 );
        if ( this.target_volume != -1 && this.target_conc != -1 )
        	btn_calculate.setEnabled( true );
		
		alert_dlg_builder = new AlertDialog.Builder( this );
		alert_dlg = alert_dlg_builder.create();
		alert_message = "The file \'$file_name\' has been changed, save or discard change?";
		alert_dlg.setMessage( alert_message );
		alert_dlg.setTitle( "Message" );
		alert_dlg.setCanceledOnTouchOutside( true );
		alert_dlg.setCancelable( true );
		alert_dlg.setOnCancelListener( new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				IME_toggle();
			}
		});
	}
	
	@Override
	public void finish() {
	    Intent data = new Intent();
	    if ( this.target_conc != -1 )
	    	data.putExtra( "target conc.", this.target_conc);
	    if ( this.target_volume != -1 )
	    	data.putExtra( "target vol.", this.target_volume);
	    //data.getExtras().remove(key);
	    setResult(RESULT_OK, data); 

	    super.finish();
	}
	
	/*20140820 added by michael
	 * append the new input filter for edittext*/
	public InputFilter [] apped_input_filter(InputFilter []orig_filters, InputFilter filter) {
		int i;
		InputFilter [] new_filters = new InputFilter [orig_filters.length + 1];
		
		for (i = 0; i < orig_filters.length; i++) {
			new_filters[i] = orig_filters[i];
		}
		new_filters[i] = filter;
				
		return new_filters;
	}
	
	public class DecimalInputFilter implements InputFilter {

		Pattern mPattern;
		public EditText mEdit;
		public double Max, Min, default_val;
		CharSequence new_text;
		Matcher matcher;

		public DecimalInputFilter(EditText edit, int digitsBeforeDecimal,int digitsAfterDecimal, double min, double max, double def) {
		    //mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
			//mPattern = Pattern.compile("^[0-9]{0,1}?");
			if (digitsAfterDecimal == 0)
				mPattern = Pattern.compile("^[1-9][0-9]{0," + (digitsBeforeDecimal-1) +"}");
			else
				mPattern = Pattern.compile("^[1-9][0-9]{0," + (digitsBeforeDecimal-1) + "}(\\.[0-9]{0," + digitsAfterDecimal + "})?");
			//+([0-9]{1," + (digitsBeforeZero) + "})?+(\\.[0-9]{0," + (digitsAfterZero-1) + "})?");
			Max = max;
			Min = min;
			mEdit = edit;
			default_val = def;
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			//if (start < end) {
				//new_text = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
			//}
			//else
				//if (start==end && end==0) {
					/*detect backspace to delete chars*/
					//new_text = dest.subSequence(0, dstart);
				//}

			new_text = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
			matcher = mPattern.matcher(new_text);
			if (!new_text.toString().equals("") && !matcher.matches()) {
				/*Maybe backspace to delete chars and cause the regex is unmatched¡Atrim all chars after backspace char index*/
				if (start==end && end==0) {
					new_text = dest.subSequence(0, dstart);
					mEdit.setText(new_text);
				}
				return "";
			}
			return null;
		}
		
	}
	
	   //edit_txt.setOnEditorActionListener
	EditText.OnEditorActionListener ed_action_listener = new EditText.OnEditorActionListener() {
		DecimalInputFilter mDecimalFilter;

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			int i;
			InputFilter [] filters;
			EditText mEdit;
			
			if (v instanceof EditText && actionId == EditorInfo.IME_ACTION_DONE) {
				mEdit = (EditText) v;
				filters = v.getFilters();
				mDecimalFilter = null;
				for (i = 0; i < filters.length; i++) {
					if (filters[i] instanceof DecimalInputFilter) {
						mDecimalFilter = (DecimalInputFilter)filters[i];
						String enteredValue = mEdit.getText().toString();
						if (enteredValue != null && !enteredValue.equals("")) {
							if (Double.parseDouble(enteredValue.trim()) < mDecimalFilter.Min
								|| Double.parseDouble(enteredValue.trim()) > mDecimalFilter.Max) {
								alert_dlg_builder.setMessage("Value range=(" + Double.toString(mDecimalFilter.Min) + ", " +  Double.toString(mDecimalFilter.Max) + ")");
								alert_dlg_builder.show();
								mEdit.setText(Double.toString(mDecimalFilter.default_val));
							}
							else {
								/*sync & commit latest data into property*/
								//sync_property(v);
								//Protein_quantity_coefficient [ 3 ] = Double.parseDouble( ed_protein_quantity.getText().toString() );
								if ( v.getId() == R.id.editText_volume )
									target_volume = Double.parseDouble( mEdit.getText().toString() );
								else
									if ( v.getId() == R.id.editText_conc )
										target_conc = Double.parseDouble( mEdit.getText().toString() );
								IME_toggle();
							}
						}
						break;
					}
				}
				return true;
			}

			return false;
		}
	};
	
	public void IME_toggle(){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
            if ( this.target_volume != -1 && this.target_conc != -1 )
            	btn_calculate.setEnabled( true );
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
            btn_calculate.setEnabled( false );
        }
    }//end method

	public void Calculate ( View v ) {
		double sample_volume = 0.0, buffer_volume = 0.0; 
		ed_target_volume.clearFocus();
		ed_target_conc.clearFocus();
		if ( target_volume == -1 ) {
			ed_target_volume.requestFocus();
			alert_message = "Not specify target volume!";
			alert_dlg.setMessage( alert_message );
			alert_dlg.show();
		}
		else
			if ( target_conc == -1 ) {
				ed_target_conc.requestFocus();
				alert_message = "Not specify target concentration!";
				alert_dlg.setMessage( alert_message );
				alert_dlg.show();
			}
			else {
				Iterator<HashMap<String, String>> it;
				HashMap <String, String> map1;
				it = selected_fillMaps.iterator();
				double conc;
				while ( it.hasNext() ) {
					map1 = it.next();
					//map1.get( dst_from [ 1 ] )
					conc = Double.parseDouble( map1.get( dst_from [1] ) );
					if ( conc < target_conc) {
						map1.put( dst_from [2], "NA" );
						map1.put( dst_from [3], "NA" );
					}
					else
						if ( conc == target_conc) {
							map1.put( dst_from [2], ed_target_volume.getText().toString() );
							map1.put( dst_from [3], "0" );							
						}
						else
							if ( conc > target_conc) {
								sample_volume = ( target_conc * target_volume ) / conc;
								buffer_volume = target_volume - sample_volume;
								map1.put( dst_from [2], Double.toString( NanoSqlDatabase.truncateDecimal( sample_volume , 3 ).doubleValue() ) );
								map1.put( dst_from [3], Double.toString( NanoSqlDatabase.truncateDecimal( buffer_volume , 3 ).doubleValue() ) );
							}
							
				}
				adapter2.notifyDataSetChanged();				
			}
    }
}
