package com.example.mn913a;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mn913a.MN_913A_Device.CMD_T;
import com.example.mn913a.NanoActivity.DecimalInputFilter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class NormalizationActivity extends Activity {
	public final String Tag = "NormalizationActivity";
	public static final String INPUT_ACTIVITY_USE_NEW_UI = "input_activity_use_new_ui";
	String[] src_from = new String[] { "No.", "Conc.", "A260", "A260_A230", "A260_A280" };
	String[] dst_from = new String[] { "No.", "Conc.", "sample_vol", "buffer_vol" };
	List<HashMap<String, String>> fillMaps, selected_fillMaps;
	LinearLayout measure_root_layout, listview_header;
	ListView result_listview;
	AlertDialog.Builder alert_dlg_builder;
	AlertDialog alert_dlg;
	normalization_adapter adapter2;
	public double target_volume = -1.0, target_conc = -1.0, last_target_volume = -1.0, last_target_conc = -1.0;
	EditText ed_target_volume, ed_target_conc;
	String alert_message;
	Button btn_calculate;
	boolean activity_use_new_ui, allow_checked = false;
	int selection_count = 0;
	
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	MN_913A_Device mNano_dev;
	UsbManager mUsbManager;
	PendingIntent mPermissionIntent;
	boolean mRequest_USB_permission, Is_MN913A_Online = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView( R.layout.normalization_layout );
		
		Bundle extras = this.getIntent().getExtras();
		if (extras.containsKey(this.INPUT_ACTIVITY_USE_NEW_UI))
    		activity_use_new_ui = extras.getBoolean( INPUT_ACTIVITY_USE_NEW_UI );
    	else
    		activity_use_new_ui = false;
		
		ActionBar abr = this.getActionBar();
    	abr.setTitle("Analysis--Normalization");
    	abr.setDisplayHomeAsUpEnabled(true);
    	abr.setBackgroundDrawable( this.getResources().getDrawable( R.drawable.top_border ) );
    	
    	Window window = getWindow();
        View v = window.getDecorView();
        int actionBarId = getResources().getIdentifier("action_bar", "id", "android");
        ViewGroup actionBarView = (ViewGroup) v.findViewById(actionBarId);
        try {
            Field f = actionBarView.getClass().getSuperclass().getDeclaredField("mContentHeight");
            f.setAccessible(true);
            f.set(actionBarView, 96);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        
    	abr.setDisplayHomeAsUpEnabled( false );
        abr.setDisplayShowTitleEnabled ( false );
        abr.setDisplayUseLogoEnabled ( false );
        abr.setDisplayShowHomeEnabled ( false );
        abr.setDisplayShowCustomEnabled ( true );
        abr.setHomeButtonEnabled( false );
        LinearLayout customActionView = ( LinearLayout ) getLayoutInflater().inflate(R.layout.actionbar_custom_view1, null);
        abr.setCustomView(customActionView);
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
		
		if ( activity_use_new_ui == false )
			inflater.inflate ( R.layout.normalization_listview_header, listview_header, true );
		else
			if ( activity_use_new_ui == true )
				inflater.inflate ( R.layout.normalization_listview_header1, listview_header, true );
		adapter2 = new normalization_adapter ( this, selected_fillMaps, activity_use_new_ui );
		result_listview.setAdapter( adapter2 );
		
		result_listview.setDividerHeight( 3 );
		result_listview.setOnItemClickListener( new OnItemClickListener () {
			CheckBox checkbox1;
			//int selection_count = 0;

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				checkbox1 = ( CheckBox ) view.findViewById( R.id.checkbox2 );
				
				if ( allow_checked == true ) {
					checkbox1.toggle();
					if ( checkbox1.isChecked() ) {
						if ( selected_fillMaps.get( position ).get( dst_from [2] ).equals( "NA" ) == false ) {
						selected_fillMaps.get( position ).put( "isSelected", "true" );
						( ( ListView ) parent ).setItemChecked( position, true );
						}
					}
					else {
						if ( selected_fillMaps.get( position ).get( dst_from [2] ).equals( "NA" ) == false ) {
						selected_fillMaps.get( position ).put( "isSelected", "false" );
						( ( ListView ) parent ).setItemChecked( position, false );
						}
					}
					
					//selection_count = 0;
					if ( selected_fillMaps.get( position ).get( "isSelected" ) != null )
						if ( selected_fillMaps.get( position ).get( "isSelected" ).equals( "true" ) )
							selection_count++;
						else
							if ( selected_fillMaps.get( position ).get( "isSelected" ).equals( "false" ) )
								selection_count--;
					/*for ( HashMap <String, String> map : selected_fillMaps ) {
						if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" )) {
							selection_count++;
							//break;
						}
					}*/
				}
				
				if ( NormalizationActivity.this.activity_use_new_ui == true ) {
					if ( selection_count > 0 ) {
						item_print_result.setVisible( true );
					}
					else {
						item_print_result.setVisible( false );
					}
				}
			}
		} );
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
		
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
		
    	mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		IntentFilter mIntentFilter;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mIntentFilter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(mReceiver, mIntentFilter);
		
    	mNano_dev = new MN_913A_Device ( this );
    	mRequest_USB_permission = false;
    	EnumerationDevice(getIntent());
    	
    	mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_PRINTER_POWER_ON, 0, 0, null, 0 );
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
        
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
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
				allow_checked = true;
				last_target_volume = target_volume; 
				last_target_conc = target_conc;
			}
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int button_action_id = 0;
		
		if ( item.getItemId() == 0 ) {
			button_action_id = this.action_id;
		}
		else {
			button_action_id = item.getItemId();
		}
		
		switch ( button_action_id ) {
		  case android.R.id.home:
			  onBackPressed();
			  return true;
		
		}
		return super.onOptionsItemSelected( item );
	}
	
	MenuItem item_goback, item_print_result, Dummy_menu_item;
	Menu main_menu;
	ImageButton btn_goback, btn_print_result;
	int action_id = 0;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.normalization_right_side_menu, menu);
		item_goback = menu.findItem ( R.id.item_goback );
		item_goback.setActionView( R.layout.actionview_item_goback );
		btn_goback = ( ImageButton ) item_goback.getActionView ( );
		
		item_print_result = menu.findItem(R.id.item_print);
	    item_print_result.setActionView( R.layout.actionview_item_print );
	    item_print_result.setVisible( false );
	    btn_print_result = ( ImageButton ) item_print_result.getActionView();
	    
	    Dummy_menu_item = menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "dummy");
	    Dummy_menu_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	    Dummy_menu_item.setVisible( false );
	    
	    btn_goback.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				action_id = android.R.id.home;
				onOptionsItemSelected ( Dummy_menu_item );
			}
		} );
	    
	    btn_print_result.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				byte[] bytes;
				int byte_offset = 0, data_count = 0, packed_data_count = 0, total_packed_data_count = 0;
				byte [] meta_print_data = new byte [ 1024 ];
				Log.d ( "btn_print_result", "click" );
				//selected_fillMaps
				data_count = selection_count;
				bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( 2 ).array();
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				byte_offset = byte_offset + bytes.length;

				Iterator<HashMap<String, String>> it;
				HashMap <String, String> map1;
				it = selected_fillMaps.iterator();
				while ( data_count > 0 ) {
				  byte_offset = 4;
				  
				  packed_data_count = 0;
				  if ( data_count > 10)
					  total_packed_data_count = 10;
				  else
					  total_packed_data_count = data_count;

				  bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( total_packed_data_count ).array();
				  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				  byte_offset = byte_offset + bytes.length;
				  
				  bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( last_target_volume ).array();
				  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				  byte_offset = byte_offset + bytes.length;
				  
				  bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( last_target_conc ).array();
				  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				  byte_offset = byte_offset + bytes.length;

				  while ( packed_data_count < total_packed_data_count ) {
					  map1 = it.next();
					  if ( map1.get( "isSelected" ) != null && map1.get( "isSelected" ).equals( "true" ) ) {
						  bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map1.get( dst_from[0] ) ) ).array();
						  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
						  byte_offset = byte_offset + bytes.length;

						  //word alignment stuffing
						  bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map1.get( dst_from[0] ) ) ).array();
						  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
						  byte_offset = byte_offset + bytes.length;
						  
						  bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map1.get( dst_from[1] ) ) ).array();
						  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
						  byte_offset = byte_offset + bytes.length;
						  
						  bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map1.get( dst_from[2] ) ) ).array();
						  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
						  byte_offset = byte_offset + bytes.length;
						  
						  bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map1.get( dst_from[3] ) ) ).array();
						  System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
						  byte_offset = byte_offset + bytes.length;
						  packed_data_count++;
					  }
				  }
				  if ( ( byte_offset % 256 ) != 0)
					  mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_META_DATA, 0, ( byte_offset / 256 ) + 1, meta_print_data, 0);
				  else
					  mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_META_DATA, 0, ( byte_offset / 256 ), meta_print_data, 0);
				  
				  if ( data_count > 10) {
					  data_count -= 10;
				  }
				  else {
					  data_count -= data_count;
				  }
				}
			}
	    } );
		return true;
	}
	
	@Override
    protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
	//Create a broadcast receiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			
			if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
				if (mNano_dev != null && mNano_dev.getDevice() != null) {
					if (device.getProductId() == mNano_dev.getDevice().getProductId() && device.getVendorId() == mNano_dev.getDevice().getVendorId()) {
						mNano_dev.DeviceOffline();
						Log.d( Tag, "MN913A DETACHED" );
						mNano_dev.Reset_Device_Info();
						Is_MN913A_Online = false;
					}
				}
			}
			else
				if (action.equals(ACTION_USB_PERMISSION)) {
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							Is_MN913A_Online = true;
							Log.d(Tag, "permission allowed for device "+device);
						}
					}
					else {
						Is_MN913A_Online = false;
						Log.d(Tag, "permission denied for device " + device);
					}
					
					if (mRequest_USB_permission==true) {
						//hide_system_bar();
						mRequest_USB_permission = false;
					}
					
					if (mRequest_USB_permission==true) {
						mRequest_USB_permission = false;
					}
				}
		}
    	
    };
    
    protected void onNewIntent(Intent intent) {
    	mNano_dev.show_debug("New intent: "+intent.getAction()+"\n");
    	if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		EnumerationDevice(intent);
    }
    
	public void EnumerationDevice(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MAIN)) {
			if (mNano_dev.Enumeration()) {
				//connection_status_v.setImageResource ( R.drawable.usb_connection );
				Is_MN913A_Online = true;
			}
			else {
				if (mNano_dev.isDeviceOnline()) {
					mRequest_USB_permission = true;
					mUsbManager.requestPermission(mNano_dev.getDevice(), mPermissionIntent);
				}
				else {
					Is_MN913A_Online = false;
				}
			}
		}
    	else
    		if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			Log.d ( "LogFileChooser debug device", device.toString() );
    			if (mNano_dev.Enumeration(device)) {
    				Is_MN913A_Online = true;
    			}
    			else {
    				Is_MN913A_Online = false;
    			}
    		}
	}
}
