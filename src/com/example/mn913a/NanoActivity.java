package com.example.mn913a;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mn913a.MN_913A_Device.CMD_T;
import com.example.mn913a.StorageUtils.StorageInfo;
import com.example.mn913a.file.FileOperateByteArray;
import com.example.mn913a.file.FileOperateObject;
import com.example.mn913a.file.FileOperation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.database.Cursor;
import ar.com.daidalos.afiledialog.FileChooserActivity;

public class NanoActivity extends Activity {
	PendingIntent mPermissionIntent;
	UsbManager mUsbManager;
	boolean mRequest_USB_permission, Is_MN913A_Online = false;
	public final String Tag = "MN913_Activity";
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	MN_913A_Device mNano_dev;
	public static boolean mDebug_Nano = false;
	RelativeLayout mMain_Layout;
	ImageView connection_status_v;
	Button Setting_Btn, Measure_Btn;
	int Cur_Voltage_Level = -1;
	double xenon_voltage;
	DisplayMetrics metrics;
	FrameLayout mLayout_Content;
	LinearLayout mLayout_DNA_MeasurePage, mLayout_MainPage, mLayout_SettingPage, gridlayout, mLayout_Protein_MeasurePage, calibration_layout;
	Thread timerThread = null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd a HH:mm");
	SimpleDateFormat df1 = new SimpleDateFormat("yyyy.MM.dd a HH:mm:ss");
	SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd.HHmmss");
	LayoutInflater inflater;
	
	GVTable table;
	NanoSqlDatabase nano_database;
	
	public Handler mWorker_thread_handler;
	public Message msg;
	
	public int measure_mode;
	public static final int MEASURE_MODE_dsDNA = 0x01;
	public static final int MEASURE_MODE_ssDNA = 0x02;
	public static final int MEASURE_MODE_RNA = 0x03;
	public static final int MEASURE_MODE_PROTEIN = 0x05;
	
	File sdcard = Environment.getExternalStorageDirectory();
	final String Nano_Data_Dir = sdcard.getPath() + "/MaestroNano/Measure/";
	
	AlertDialog alert_dlg, alert_dlg2;
	Dialog alert_dlg1;
	RadioGroup protein_quantity_layout;
	AlertDialog.Builder alert_dlg_builder;
	String alert_message = "Are you sure that you want to delete the file \'$file_name\'?";
	boolean Cur_A320_Involve = true, Cur_Led_Onoff_State = false, Cur_Auto_Measure = false;
	Thread Monitor_Auto_Measure_thread = null, Auto_running_thread = null;
	ThreadPoolExecutor polling_data_executor = null;
	Thread_sync Thread_Sync_By_Obj;
	
	ImageButton main_page_btn_dsDNA, main_page_btn_calibration, dsDNA_page_home, dsDNA_page_btn_blank, dsDNA_page_btn_sample;
	int Cur_Protein_quantity_mode, Protein_quantity_mode = -1;
	Double Protein_quantity_coefficient [] = { 6.67, 13.7, 26.4, 1.0 };
	EditText ed_protein_quantity;
	public static final String PREFS_NAME = "MyPrefsFile";
	private SharedPreferences preference;
	private Editor preference_editor;
	LinkedList<HashMap<String, String>> Calibration_Data_List = null;//new LinkedList<HashMap<String, String>>();
	HashMap<String, String> cali_data = new HashMap<String, String>();
	boolean is_store_cali_data = false;
	ListView calibration_list;
	SimpleAdapter cali_list_adapter;
	String[] calibration_from = new String[] { "datetime", "before", "after", "situation" };
	FileOperateObject cali_write_file, cali_read_file;
	Calendar calendar = Calendar.getInstance();
	int Lcd_Brightness_Level = -1, Cur_Lcd_Brightness_Level = -1;
	MN913A_Properties app_properties;
	Double coeff_k1, coeff_k2, coeff_k3, coeff_k4, coeff_k5;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayout_Content = (FrameLayout) this.findViewById(android.R.id.content);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.activity_main1);
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
		Thread.currentThread().setName( "Thread_NanoActivity" );

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		IntentFilter mIntentFilter;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mIntentFilter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(mReceiver, mIntentFilter);
		
		mMain_Layout = ( RelativeLayout ) this.findViewById( R.id.ID_Main_Layout );
		connection_status_v = ( ImageView ) this.findViewById( R.id.imageView1 );
		
		mNano_dev = new MN_913A_Device ( this );
		mRequest_USB_permission = false;
		inflater = (LayoutInflater) getSystemService ( Context.LAYOUT_INFLATER_SERVICE );
		//EnumerationDevice(getIntent());
	
		/*SeekBar seekbar1;
		seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		//seekbar1.setEnabled(Adjust_Detection_Sensitivity);
		seekbar1.setProgress ( 162 );
		Cur_Voltage_Level = 162;
		seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if ( seekBar.getProgress() < 162 )
					seekBar.setProgress ( 162 );
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				Cur_Voltage_Level = progress;
				TextView seekbar_value;
				seekbar_value = (TextView) findViewById(R.id.textView1);
				xenon_voltage = 3.17 + ( 4.7 - 3.17 ) * ( Cur_Voltage_Level - 162 ) / ( 242 - 162 );
				seekbar_value.setText( String.format( "%.3f", xenon_voltage ) + "V");
			}
		});
		TextView seekbar_value;
		seekbar_value = (TextView) findViewById(R.id.textView1);
		xenon_voltage = 3.17 + ( 4.7 - 3.17 ) * ( seekbar1.getProgress() - 162 ) / ( 242 - 162 );
		seekbar_value.setText( String.format( "%.3f", xenon_voltage ) + "V");
		
		Setting_Btn = ( Button ) findViewById(R.id.button1);
		Setting_Btn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			}
			
		});
		Measure_Btn = ( Button ) findViewById(R.id.button2);
		Measure_Btn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 1);
			}
			
		});*/
		
		EnumerationDevice(getIntent());
		alert_dlg_builder = new AlertDialog.Builder( this );
    	alert_dlg = alert_dlg_builder.create();
		alert_message = "The file \'$file_name\' has been changed, save or discard change?";
		alert_dlg.setMessage( alert_message );
		alert_dlg.setTitle( "Message" );
		alert_dlg.setCanceledOnTouchOutside( false );
		alert_dlg.setCancelable( false );
		
		alert_dlg2 = alert_dlg_builder.create();
		alert_dlg2.setTitle( "Message" );
		alert_dlg2.setCanceledOnTouchOutside( false );
		alert_dlg2.setCancelable( false );
		
		cali_read_file = new FileOperateObject ( "misc", "calibration_result" );
	    try {
	    	cali_read_file.open_read_file(cali_read_file.generate_filename_no_date());
	    	Calibration_Data_List = ( LinkedList<HashMap<String, String>> ) cali_read_file.read_file_object();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    if ( Calibration_Data_List == null ) {
	    	Calibration_Data_List = new LinkedList<HashMap<String, String>>();
			cali_data.put( "after", "after(%)" );
			cali_data.put( "before", "before(%)" );
			cali_data.put( "datetime", "datetime" );
			cali_data.put( "situation", "situation" );
			Calibration_Data_List.add( cali_data );
			cali_data = new HashMap<String, String>();
	    }
		
		//alert_dlg1 = new Dialog(this, R.style.CenterDialog);
	    WindowManager.LayoutParams params;
	    alert_dlg1 = new Dialog(this);
	    params = alert_dlg1.getWindow().getAttributes();
		calibration_layout = ( LinearLayout ) LayoutInflater.from ( alert_dlg1.findViewById( android.R.id.content ).getContext() ).inflate( R.layout.calibration_layout, null );
		calibration_list = ( ListView ) calibration_layout.findViewById( R.id.calibration_list );
		cali_list_adapter = new SimpleAdapter ( this, Calibration_Data_List, R.layout.calibration_listview_item, calibration_from, new int[] { R.id.ItemText1, R.id.ItemText2, R.id.ItemText3, R.id.ItemText4 } );
		calibration_list.setAdapter( cali_list_adapter );
		params.horizontalWeight = ( float ) 2.0;
		params.verticalWeight = ( float ) 1.0;
		//alert_dlg1.getWindow().setAttributes( params );
		alert_dlg1.setContentView( calibration_layout );
		
		Button btn_cali_print = ( Button ) calibration_layout.findViewById( R.id.button1 );
		btn_cali_print.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HashMap<String, String> map = Calibration_Data_List.getLast();
				String [] str_arry;
				byte [] datetime_data = new byte [ 24 ], meta_print_data = new byte [ 1024 ];
				byte [] bytes;
				int byte_offset = 0, hour_offset = 0, byte_offset1 = 0;
				boolean is_next_setting_hour = false;
				

				bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( 1 ).array();
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
			    byte_offset = byte_offset + bytes.length;
				str_arry = map.get( "datetime" ).split( "\\.|\\ |:" );
				for ( String s: str_arry ) {
					if ( s.equalsIgnoreCase( "AM" ) ) {
						is_next_setting_hour = true;
						hour_offset = 0;
					}
					else
						if ( s.equalsIgnoreCase( "PM" ) ) {
							is_next_setting_hour = true;
							hour_offset = 12;
						}
						else {
							if ( is_next_setting_hour == true ) {
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) + hour_offset ).array();
								is_next_setting_hour = false;
							}
							else
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset1, bytes.length );
							byte_offset1 = byte_offset1 + bytes.length;
						}
				}
				System.arraycopy ( datetime_data, 0, meta_print_data, byte_offset, datetime_data.length );
			    byte_offset = byte_offset + datetime_data.length;

			    //alignment stuff
				bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( 1 ).array();
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
			    byte_offset = byte_offset + bytes.length;
			    
				bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( "before" ) ) ).array();
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				byte_offset = byte_offset + bytes.length;
				
				bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( "after" ) ) ).array();
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				byte_offset = byte_offset + bytes.length;
				
				if ( map.get( "situation" ).equals( "pass" ) ) {
					bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( 1 ).array();
				}
				else
					if ( map.get( "situation" ).equals( "fail" ) ) {
						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( 0 ).array();
					}
				System.arraycopy ( bytes, 0, meta_print_data, byte_offset, bytes.length );
				byte_offset = byte_offset + bytes.length;
				
				//alignment stuff
				byte_offset = byte_offset + bytes.length;
				if ( ( byte_offset % 256 ) != 0)
					mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_META_DATA, 0, ( byte_offset / 256 ) + 1, meta_print_data, 0);
				else
					mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_META_DATA, 0, ( byte_offset / 256 ), meta_print_data, 0);
			}
		});
		
		/*alert_dlg1 = new Dialog(this, R.style.CenterDialog);
		//alert_dlg1.setContentView( R.layout.protein_quantity_type );
		protein_quantity_layout = ( RadioGroup ) LayoutInflater.from ( alert_dlg1.findViewById( android.R.id.content ).getContext() ).inflate( R.layout.protein_quantity_type, null );
		alert_dlg1.setContentView( protein_quantity_layout );
		alert_dlg1.setTitle( "Select protein quantity" );
		
		ed_protein_quantity = ( EditText ) protein_quantity_layout.findViewById( R.id.editText1 );
		ed_protein_quantity.addTextChangedListener( new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			} 
			
		});
		ed_protein_quantity.setOnFocusChangeListener ( new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					
				}
				else {
					if ( ed_protein_quantity.getText().toString().trim().equals( "" ) ) {
						//ed_protein_quantity.setText(item_data.get_high_speed_rpm_string()); 
	            	} else {
	            		
	            	}
				}
			} 
			
		} );

		protein_quantity_layout.setOnCheckedChangeListener (new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				Log.d ( Tag, Integer.toString( checkedId ));
				//EditText et;
				//et = ( EditText ) group.findViewById( R.id.editText1 );
				
				switch ( checkedId ) {
				case R.id.radioButton1:
					ed_protein_quantity.setText( Integer.toString( R.id.radioButton1 ) );
					//ed_protein_quantity.setEnabled( false );
					break;
				case R.id.radioButton2:
					ed_protein_quantity.setText( Integer.toString( R.id.radioButton2 ) );
					//ed_protein_quantity.setEnabled( false );
					break;
				case R.id.radioButton3:
					ed_protein_quantity.setText( Integer.toString( R.id.radioButton3 ) );
					//ed_protein_quantity.setEnabled( false );
					break;
				case R.id.radioButton4:
					ed_protein_quantity.setText( Integer.toString( R.id.radioButton4 ) );
					//ed_protein_quantity.setEnabled( false );
					break;
				case R.id.radioButton5:
					ed_protein_quantity.setText( Integer.toString( R.id.radioButton5 ) );
					//ed_protein_quantity.setEnabled( false );
					break;
				case R.id.radioButton6:
					//ed_protein_quantity.setEnabled( true );
				}

			}
			
		});*/
		/*alert_dlg1 = alert_dlg_builder.create();
		
		alert_dlg1.setCanceledOnTouchOutside( false );
		alert_dlg1.setCancelable( false );
		alert_dlg1.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		});
		alert_dlg1.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
		/*alert_dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		});
		alert_dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
		
		ImageButton imageButton1, imageButton2, imageButton3, imageButton4, btn_protein, btn_analysis, btn_calibration, btn_setting, btn_about;
		View.OnClickListener click_listener;
		click_listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				metrics = new DisplayMetrics();
				(( WindowManager )getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
				//finish();
				hide_system_bar();
				
				String result;
		    	result = exec_shell_command ( "am startservice -n com.android.systemui/.SystemUIService\n" );
		    	if ( result.equals( Msg_Shell_Command_Error) == false )
		    		Log.d ( Tag, "show system bar successfully!" );
		    	else
		    		Log.d ( Tag, "show system bar fail!" );
				try {
					Runtime.getRuntime().exec(new String[] { "su -c pm disable com.android.systemui" });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mLayout_Content.removeAllViews ( );
				if ( mLayout_DNA_MeasurePage == null) {
					mLayout_DNA_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.measure_main, null );
				}
				mLayout_Content.addView( mLayout_DNA_MeasurePage );
			}
		};
		imageButton1 = ( ImageButton ) findViewById( R.id.imageButton1 );
		imageButton1.setOnClickListener( new OnClickListener() {
			ImageButton btn_blank, btn_sample;
			GridView gridview;
			ArrayList<HashMap<String, String>> srcTable;
		    SimpleAdapter saTable;
		    ListAdapter lt;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_dna_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_dsdna );
				measure_mode = NanoActivity.this.MEASURE_MODE_dsDNA;
				nano_database.CreateDataDB( NanoSqlDatabase.MEASURE_MODE_DNA );
				blank_valid = false;
				
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_blank.setEnabled( true );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				btn_sample.setEnabled( false );
				srcTable = new ArrayList<HashMap<String, String>>();
				saTable = new SimpleAdapter ( NanoActivity.this , srcTable, R.layout.griditem, new String[] { "ItemText1", "ItemText2", "ItemText3" }, new int[] { R.id.ItemText1, R.id.ItemText2, R.id.ItemText3 } );
				table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
			    table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
				table.refresh_last_table();
				
				dna_data_list.clear ( );
				
				btn_blank.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*gridview = (GridView) NanoActivity.this.findViewById ( R.id.gridView1 );
						lt = gridview.getAdapter();
						if ( lt == null ) {
							Log.d ( Tag, "slclls" + Integer.toString( gridview.getHeight() ) );
							gridview.setAdapter( saTable );
							
				            //gridview.getItemAtPosition( 0 );
						}
						HashMap<String, String> map = new HashMap<String, String>();
			            map.put( "ItemText1", "A260" );
			            map.put( "ItemText2", "A230" );
			            map.put( "ItemText3", "A280" );
			            srcTable.add(map);
			            saTable.notifyDataSetChanged();*/
						/*nano_database.InsertDNADataToDB();
						table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
					    table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
					    table.refresh_last_table();*/

					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	/*alert_message = "Device Calibrating & Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    /*checkpoint*/
					    	is_store_cali_data = false;
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	/*alert_message = "Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					}
					
				});
				
				btn_sample.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    /*alert_message = "Sample Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );
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
				
				dsDNA_page_home = ( ImageButton ) mLayout_Content.findViewById( R.id.main_home );
				dsDNA_page_btn_blank = btn_blank;
				dsDNA_page_btn_sample = btn_sample;
			}
			
		});
		imageButton2 = ( ImageButton ) findViewById( R.id.imageButton2 );
		imageButton2.setOnClickListener( new OnClickListener() {
			ImageButton btn_blank, btn_sample;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_dna_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_ssdna );
				measure_mode = NanoActivity.this.MEASURE_MODE_ssDNA;
				nano_database.CreateDataDB( NanoSqlDatabase.MEASURE_MODE_DNA );
				blank_valid = false;
				
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_blank.setEnabled( true );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				btn_sample.setEnabled( false );
				table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
			    table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
				table.refresh_last_table();
				
				dna_data_list.clear ( );
				
				btn_blank.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	/*alert_message = "Device Calibrating & Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    /*checkpoint*/
					    	is_store_cali_data = false;
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	/*alert_message = "Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					}
					
				});
				
				btn_sample.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    /*alert_message = "Sample Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );

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
			}
			
		});
		imageButton3 = ( ImageButton ) findViewById( R.id.imageButton3 );
		imageButton3.setOnClickListener(  new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent ( NanoActivity.this, LogFileChooserActivity.class );
				//intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
				intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
				intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, Nano_Data_Dir);
				/*20141022 added by michael
				 * add the option to allow reverse the file list ording*/
				intent.putExtra(FileChooserActivity.INPUT_REVERSE_FILELIST_ORDER, true);
				/*20140819 added by michael
				 * add the additional extra param INPUT_ACTIVITY_ORIENTATION to handle the activity screen orientation，these values(SCREEN_ORIENTATION_SENSOR_PORTRAIT、SCREEN_ORIENTATION_REVERSE_PORTRAIT...etc) define in  class ActivityInfo*/
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_ORIENTATION, getRequestedOrientation());
				intent.putExtra(LogFileChooserActivity.INPUT_REGEX_FILTER, ".*\\.csv");
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_USE, LogFileChooserActivity.ACTIVITY_USE_FOR_MANAGEMENT);
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_USE_NEW_UI, true);
				//intent.putExtra( LogFileChooserActivity.INPUT_CAN_CREATE_FILES, true );
				//startActivity(intent);
				intent.setAction( NanoActivity.this.getIntent().getAction() );
				if ( NanoActivity.this.getIntent().getAction().equals( UsbManager.ACTION_USB_DEVICE_ATTACHED ) ) {
					UsbDevice device = (UsbDevice) NanoActivity.this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
					Log.d ( "NanoActivity debug device", device.toString() );
					intent.putExtra( UsbManager.EXTRA_DEVICE, device);
				}
				NanoActivity.this.startActivityForResult ( intent, 1023 );
			}
			
		});
		imageButton4 = ( ImageButton ) findViewById( R.id.imageButton4 );
		imageButton4.setOnClickListener( new OnClickListener() {
			ImageButton btn_blank, btn_sample;
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_dna_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_rna );
				measure_mode = NanoActivity.this.MEASURE_MODE_RNA;
				nano_database.CreateDataDB( NanoSqlDatabase.MEASURE_MODE_DNA );
				blank_valid = false;
				
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_blank.setEnabled( true );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				btn_sample.setEnabled( false );

				table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
			    table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
				table.refresh_last_table();
				
				dna_data_list.clear ( );
				
				btn_blank.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	/*alert_message = "Device Calibrating & Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    /*checkpoint*/
					    	is_store_cali_data = false;
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	/*alert_message = "Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					}
					
				});
				
				btn_sample.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    /*alert_message = "Sample Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );
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
			}
			
		});
		btn_protein = ( ImageButton ) findViewById( R.id.imageButton5 );
		btn_protein.setOnClickListener( new OnClickListener() {
			ImageButton btn_blank, btn_sample;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//protein_quantity_layout.check( R.id.radioButton1 );
				//alert_dlg1.show();
				switch_to_protein_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_protein );
				measure_mode = NanoActivity.this.MEASURE_MODE_PROTEIN;
				nano_database.CreateDataDB( NanoSqlDatabase.MEASURE_MODE_PROTEIN );
				blank_valid = false;
				
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_blank.setEnabled( true );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				btn_sample.setEnabled( false );

				table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.PROTEIN_VALUE_TABLE_NAME, nano_database.get_database());
			    table.gvReadyTable("select * from " + NanoSqlDatabase.PROTEIN_VALUE_TABLE_NAME, nano_database.get_database());
				table.refresh_last_table();
				
				protein_data_list.clear();
				
				btn_blank.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	/*alert_message = "Device Calibrating & Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/
					    	is_store_cali_data = false;
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	/*alert_message = "Blank Measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();*/

						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					}
					
				});
				
				btn_sample.setOnClickListener (new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
					    btn_blank.setEnabled( false );
					    btn_sample.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    /*alert_message = "Sample Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );
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
			}
			
		});
		/*btn_calibration = ( ImageButton ) findViewById( R.id.imageButton6 );
		btn_calibration.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {*/
				// TODO Auto-generated method stub
				/*alert_message = "Please pipette 2 microliter of water!";
				alert_dlg2.setMessage( alert_message );
				alert_dlg2.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {*/
						// TODO Auto-generated method stub
				    	/*alert_message = "Device Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/

					    /*checkpoint*/
						/*is_store_cali_data = true;
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
					    msg.sendToTarget ( );						
					}
					
				});
				alert_dlg2.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					
				});
				alert_dlg2.show();
			}
			
		});*/
		/*btn_analysis = ( ImageButton ) findViewById( R.id.imageButton6 );
		btn_analysis.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RelativeLayout setting_page = null;
				mLayout_Content.removeAllViews ( );
				if ( mLayout_SettingPage == null )
					setting_page = ( RelativeLayout ) inflater.inflate( R.layout.activity_main, null );
				//setting_page.setBackgroundColor( NanoActivity.this.getResources().getColor( android.R.color.background_dark ) );
				mLayout_Content.addView( setting_page );
				
				SeekBar seekbar1;
				seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
				//seekbar1.setEnabled(Adjust_Detection_Sensitivity);
				seekbar1.setProgress ( 162 );
				Cur_Voltage_Level = 162;
				seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						if ( seekBar.getProgress() < 162 )
							seekBar.setProgress ( 162 );
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						Cur_Voltage_Level = progress;
						TextView seekbar_value;
						seekbar_value = (TextView) findViewById(R.id.textView1);
						xenon_voltage = 3.17 + ( 4.7 - 3.17 ) * ( Cur_Voltage_Level - 162 ) / ( 242 - 162 );
						seekbar_value.setText( String.format( "%.3f", xenon_voltage ) + "V");
					}
				});
				TextView seekbar_value;
				seekbar_value = (TextView) findViewById(R.id.textView1);
				xenon_voltage = 3.17 + ( 4.7 - 3.17 ) * ( seekbar1.getProgress() - 162 ) / ( 242 - 162 );
				seekbar_value.setText( String.format( "%.3f", xenon_voltage ) + "V");
				
				Setting_Btn = ( Button ) findViewById(R.id.button1);
				Setting_Btn.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
						mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
					}
					
				});
				Measure_Btn = ( Button ) findViewById(R.id.button2);
				Measure_Btn.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 1);
					}
					
				});
				
			}
			
		});*/
		//btn_analysis.setVisibility( View.INVISIBLE );
		btn_analysis = ( ImageButton ) findViewById( R.id.imageButton6 );
		btn_analysis.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				Intent intent = new Intent ( NanoActivity.this, LogFileChooserActivity.class );
				//intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
				intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
				intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, Nano_Data_Dir);
				/*20141022 added by michael
				 * add the option to allow reverse the file list ording*/
				intent.putExtra(FileChooserActivity.INPUT_REVERSE_FILELIST_ORDER, true);
				/*20140819 added by michael
				 * add the additional extra param INPUT_ACTIVITY_ORIENTATION to handle the activity screen orientation，these values(SCREEN_ORIENTATION_SENSOR_PORTRAIT、SCREEN_ORIENTATION_REVERSE_PORTRAIT...etc) define in  class ActivityInfo*/
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_ORIENTATION, getRequestedOrientation());
				intent.putExtra(LogFileChooserActivity.INPUT_REGEX_FILTER, ".*\\.csv");
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_USE, LogFileChooserActivity.ACTIVITY_USE_FOR_ANALYSIS);
				intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_USE_NEW_UI, true);
				//startActivity(intent);
				intent.setAction( NanoActivity.this.getIntent().getAction() );
				if ( NanoActivity.this.getIntent().getAction().equals( UsbManager.ACTION_USB_DEVICE_ATTACHED ) ) {
					UsbDevice device = (UsbDevice) NanoActivity.this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
					Log.d ( "NanoActivity debug device", device.toString() );
					intent.putExtra( UsbManager.EXTRA_DEVICE, device);
				}
				//AnalysisFileChooserActivity
				NanoActivity.this.startActivityForResult ( intent, 1025 );
			}
			
		});
		btn_setting = ( ImageButton ) findViewById( R.id.imageButton8 );
		btn_about = ( ImageButton ) findViewById( R.id.imageButton7 );
		metrics = new DisplayMetrics();
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
    	}
    	else {
    		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics((metrics));
    	}
		
		//adjust_ui_dimension ( ( ViewGroup ) this.findViewById( R.id.top_ui ) );
    	mLayout_MainPage = (LinearLayout) findViewById(R.id.top_ui);
    	this.sync_RTC_Android_systime();
    	Runnable runnable = new CountDownRunner();
    	timerThread= new Thread(runnable);   
    	timerThread.start();
    	
    	table = new GVTable(this);
    	table.gvSetTableRowCount ( 200 );
		table.setTableOnClickListener(new GVTable.OnTableClickListener() {
			public void onTableClickListener(int x, int y, Cursor c) {
				c.moveToPosition(y);
				String str = c.getString(x);
			}
		});
		
		table.setOnPageSwitchListener(new GVTable.OnPageSwitchListener() {
			public void onPageSwitchListener(int pageID,int pageCount) {
				String str = "Page:"+String.valueOf(pageID);
			}
		});
		
		nano_database = new NanoSqlDatabase ( this );
		//nano_database.CreateDataDB( 4 );

		new LooperThread ( ).start();
		measure_mode = 0;
		
		NanoApplication app_data = ( ( NanoApplication ) this.getApplication() );
		app_data.addActivity(this);
		polling_data_executor = ( ThreadPoolExecutor ) Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
		Thread_Sync_By_Obj = new Thread_sync ();
		
		mNano_dev.Set_Reset_MCU( 1 );
		mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1 );
		mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1 );
		mNano_dev.Set_Reset_MCU( 0 );
		Log.d ( Tag, "reset calibration" );
		
		//ImageButton imageButton1, imageButton2, imageButton3, imageButton4, btn_protein, btn_analysis, btn_calibration;
		main_page_btn_dsDNA = imageButton1;
/*		Auto_running_thread = new Thread() {
			int measure_iteration;
			int byte_offset = 0, dna_type = 0;
			byte [] byte_array = new byte [8192];
			byte[] bytes;
			
			@Override
			public void run() {
				super.run();
				while ( true ) {
					try {
						sleep ( 5000 );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							main_page_btn_dsDNA.performClick();
						}
						
					});
					try {
						sleep ( 3000 );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					while ( dsDNA_page_btn_blank.isEnabled() == false ) {
						try {
							sleep ( 1000 );
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							dsDNA_page_btn_blank.performClick();
						}
						
					});
					try {
						sleep ( 5000 );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					measure_iteration = 0;
					byte_offset = 0;
					while ( measure_iteration < 5 ) {
						while ( dsDNA_page_btn_sample.isEnabled() == false ) {
							try {
								sleep ( 1000 );
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								dsDNA_page_btn_sample.performClick();
							}
							
						});
						try {
							sleep ( 5000 );
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						measure_iteration++;
					}
					
					bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_type ).array();
					System.arraycopy ( bytes, 0, byte_array, 0, bytes.length );
					byte_offset = byte_offset + bytes.length;
					bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_data_list.size() ).array();
					System.arraycopy ( bytes, 0, byte_array, 4, bytes.length );
					byte_offset = byte_offset + bytes.length;
					for ( DNA_measure_data dna_data: dna_data_list ) {
						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_data.index ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_data.index ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;

						bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( dna_data.Conc ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
						
						//Double.parseDouble( dna_from [1] );
						bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( dna_data.A260 * 25.56 ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
						//Double.parseDouble( dna_from [2] );
						bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( ( dna_data.A260 * 210 ) / ( dna_data.A230 * 167 ) ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
						//Double.parseDouble( dna_from [3] );
						bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( ( dna_data.A260 * 19 )/ ( dna_data.A280 * 23 ) ).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
					}
					while ( dsDNA_page_btn_sample.isEnabled() == false ) {
						try {
							sleep ( 1000 );
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if ( ( byte_offset % 256 ) != 0)
						mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, ( byte_offset / 256 ) + 1, 1, byte_array, 0);
					else
						mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, ( byte_offset / 256 ), 1, byte_array, 0);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							dsDNA_page_home.performClick();
						}
						
					});
				}
			}
			
		};
		Auto_running_thread.start();*/
		preference = this.getSharedPreferences ( PREFS_NAME, 0 );
		preference_editor = preference.edit();
		//preference_editor.putString("Xenon max voltage level", md5_checksum);
		//preference_editor.putString("Xenon min voltage level", md5_checksum);
		//preference_editor.commit();
		Cur_Lcd_Brightness_Level = Lcd_Brightness_Level = preference.getInt( "lcd brightness", 48 );
		byte [] bytes, data_bytes = new byte [ 256 ];
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Lcd_Brightness_Level ).array();
		System.arraycopy ( bytes, 0, data_bytes, 0, bytes.length );
		if ( mNano_dev.MN913A_IOCTL( CMD_T.HID_CMD_SET_LCD_BRIGHTNESS, 0, 1, data_bytes, 0 ) == true )
			Log.d ( Tag, "initial lcd brightness setting sccess");
		//this.sync_RTC_Android_systime();
		
		File[] externalStorageFiles=ContextCompat.getExternalFilesDirs(this,null);
		for ( File file: externalStorageFiles )
			Log.d ( Tag, file.getPath() );
		
		//File[] externalStorageFiles1= this.getExternalFilesDirs( null );
		//for ( File file: externalStorageFiles1 )
			//Log.d ( Tag, file.getName() );
		
		List<StorageInfo> storage_list;
		storage_list = StorageUtils.getStorageList();
		
		app_properties = new MN913A_Properties();
    	app_properties.load_property();
    	
    	coeff_k1 = Double.valueOf(app_properties.getProperty (  MN913A_Properties.prop_k1, "61"  ));
    	coeff_k2 = Double.valueOf(app_properties.getProperty (  MN913A_Properties.prop_k2, "61"  ));
    	coeff_k3 = Double.valueOf(app_properties.getProperty (  MN913A_Properties.prop_k3, "61"  ));
    	coeff_k4 = Double.valueOf(app_properties.getProperty (  MN913A_Properties.prop_k4, "61"  ));
    	coeff_k5 = Double.valueOf(app_properties.getProperty (  MN913A_Properties.prop_k5, "61"  ));
	}
	
	private void save_measurement_to_file () {
		String file_name, measure_result;
		
		if ( measure_mode == MEASURE_MODE_dsDNA ) {
			file_name = "dsDNA";
		}
		else
			if ( measure_mode == MEASURE_MODE_ssDNA ) {
				file_name = "ssDNA";
			}
			else
				if ( measure_mode == MEASURE_MODE_RNA ) {
					file_name = "RNA";
				}
				else
					if ( measure_mode == MEASURE_MODE_PROTEIN ) {
						file_name = "PROTEIN";
					}
					else
						file_name = "";
		
		FileOperation write_file = new FileOperation ( "Measure", file_name, true );
		try {
			write_file.set_file_extension ( ".csv" ); 
			write_file.create_file ( write_file.generate_filename() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//write_file.write_file ( file_name, true );
		write_file.write_file_with_date ( file_name );
		switch ( measure_mode ) {
		case MEASURE_MODE_dsDNA:
		case MEASURE_MODE_ssDNA:
		case MEASURE_MODE_RNA:
			for ( DNA_measure_data dna_data: dna_data_list ) {
				if ( dna_data.include_A320 == true ) {
				  measure_result = Integer.toString( dna_data.index ) + ", " + NanoSqlDatabase.truncateDecimal(  dna_data.Conc, 3 ).doubleValue() +  ", " +
				  //NanoSqlDatabase.truncateDecimal(  dna_data.A260 * 24.38, 3 ).doubleValue() +  ", " +
				  NanoSqlDatabase.truncateDecimal(  dna_data.Conc / 50, 3 ).doubleValue() +  ", " +
				  NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 - dna_data.A320 ) / ( dna_data.A230 * 167 - dna_data.A320 ), 3 ).doubleValue() + ", " +
				  NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 - dna_data.A320 ) / ( dna_data.A280 * 23- dna_data.A320 ), 3 ).doubleValue();
				}
				else {
					measure_result = Integer.toString( dna_data.index ) + ", " + NanoSqlDatabase.truncateDecimal(  dna_data.Conc, 3 ).doubleValue() +  ", " +
				    //NanoSqlDatabase.truncateDecimal(  dna_data.A260 * 24.38, 3 ).doubleValue() +  ", " +
				    NanoSqlDatabase.truncateDecimal(  dna_data.Conc / 50, 3 ).doubleValue() +  ", " +
					NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 ) / ( dna_data.A230 * 167 ), 3 ).doubleValue() + ", " +
					NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 ) / ( dna_data.A280 * 23 ), 3 ).doubleValue();					
				}
				write_file.write_file ( measure_result, true );
			}
			break;
		case MEASURE_MODE_PROTEIN:
			for ( Protein_measure_data protein_data: protein_data_list ) {
				measure_result = Integer.toString( protein_data.index ) + ", " + NanoSqlDatabase.truncateDecimal(  protein_data.A280, 3 ).doubleValue()	+
						", " + NanoSqlDatabase.truncateDecimal(  protein_data.coefficient, 3 ).doubleValue()
						+ ", " + NanoSqlDatabase.truncateDecimal(  protein_data.Conc, 3 ).doubleValue();
				write_file.write_file ( measure_result, true );
			}
			break;
		}
		write_file.flush_close_file();
		
		//write_file.create_file(filename);
	}
	
	public void switch_to_main_page ( View v ) {
		/* detach table from dna measure page */
		if ( measure_mode <= MEASURE_MODE_PROTEIN ) {
			if ( this.findViewById( R.id.measure_top_ui ) != null) {
				gridlayout.removeView( table );
				//mLayout_DNA_MeasurePage.removeView( gridlayout );
			}
			//Toast.makeText( this, "save to file", Toast.LENGTH_SHORT).show();
			if ( measure_mode > 0 && measure_mode <= MEASURE_MODE_RNA && dna_data_list.size() > 0 ) {
				Toast.makeText( this, "save to file", Toast.LENGTH_SHORT).show();
				save_measurement_to_file ();
			}
			else
				if ( measure_mode == MEASURE_MODE_PROTEIN && protein_data_list.size() > 0 ) {
					Toast.makeText( this, "save to file", Toast.LENGTH_SHORT).show();
					save_measurement_to_file ();
				}
		}
		
		/*turn off led*/
		Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
		if ( sw1 != null ) {
			/*mNano_dev.Set_Illumination_State ( 0 );
			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);*/
			Cur_Led_Onoff_State = sw1.isChecked();
			sw1.setChecked( false );
		}
		
		Switch sw2= ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
		if ( sw2 != null ) {
			Cur_Auto_Measure = sw2.isChecked();
			sw2.setChecked( false );
		}
		
		mLayout_Content.removeAllViews ( );
		if ( mLayout_MainPage == null )
			mLayout_MainPage = ( LinearLayout ) inflater.inflate( R.layout.activity_main1, null );
		mLayout_Content.addView( mLayout_MainPage );
		
		measure_mode = 0;
		//Stop_Monitor_AutoMeasure_Thread ();
		//mNano_dev.Set_Auto_Measure( 0 );
		//mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
	}
	
	public void switch_to_dna_measure_page ( ) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_DNA_MeasurePage == null) {
			mLayout_DNA_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.measure_main, null );
		}
		mLayout_Content.addView( mLayout_DNA_MeasurePage );
		
		Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
		sw.setOnCheckedChangeListener ( new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Cur_A320_Involve = isChecked;
				Log.d ( Tag, Boolean.toString( isChecked ) );
				buttonView.setText( "" );
			}
			
		});
		sw.setChecked( Cur_A320_Involve );
		
		Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
		sw1.setOnCheckedChangeListener ( new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				//Cur_Led_Onoff_State = isChecked;
				if ( isChecked )
					mNano_dev.Set_Illumination_State ( 1 );
				else
					mNano_dev.Set_Illumination_State ( 0 );
				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			}
			
		});
		sw1.setChecked( Cur_Led_Onoff_State );
		
		Switch sw2= ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
		sw2.setOnCheckedChangeListener ( new OnCheckedChangeListener () {
			ImageButton btn_blank, btn_sample;

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Cur_Auto_Measure = isChecked;
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				if ( isChecked ) {
					mNano_dev.Set_Auto_Measure( 1 );
					/*checkpoint*/
					/*starting polling thread to monitor auto-measure signal*/
					Start_Monitor_AutoMeasure_Thread ( );
					btn_blank.setEnabled( false );
					btn_sample.setEnabled( false );
				}
				else {
					mNano_dev.Set_Auto_Measure( 0 );
					/*checkpoint*/
					/*stopping auto-measure polling thread*/
					Stop_Monitor_AutoMeasure_Thread ( );
					btn_blank.setEnabled( true );
					btn_sample.setEnabled( true );
				}
				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			}
			
		});
		sw2.setChecked( Cur_Auto_Measure );
		sw2.setEnabled( false );
		
    	gridlayout = (LinearLayout) findViewById(R.id.GridLayout);
    	if ( gridlayout != null ) {
    		if ( gridlayout.findViewById( GVTable.ID ) == null )
    			gridlayout.addView( table );
    		else
    			this.table.gvRemoveAll ( );
    	}
    	
    	/*20160322 added by michael*/
    	if ( nano_database.get_database() != null )
    		if ( nano_database.get_database().isOpen() == true )
    			nano_database.get_database().close();
    	
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView1 ) ).setText( "" );
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView3 ) ).setText( "" );
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView4 ) ).setText( "" );
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView5 ) ).setText( "" );
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView6 ) ).setText( "" );
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView7 ) ).setText( "" );
	}
	
	public void switch_to_protein_measure_page ( ) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_Protein_MeasurePage == null) {
			mLayout_Protein_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.protein_measure, null );
		}
		mLayout_Content.addView( mLayout_Protein_MeasurePage );
	
		Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
		sw1.setOnCheckedChangeListener ( new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				//Cur_Led_Onoff_State = isChecked;
				if ( isChecked )
					mNano_dev.Set_Illumination_State ( 1 );
				else
					mNano_dev.Set_Illumination_State ( 0 );
				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			}
			
		});
		sw1.setChecked( Cur_Led_Onoff_State );
		
		Switch sw2= ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
		sw2.setOnCheckedChangeListener ( new OnCheckedChangeListener () {
			ImageButton btn_blank, btn_sample;

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Cur_Auto_Measure = isChecked;
				btn_blank = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton1 );
				btn_sample = ( ImageButton ) NanoActivity.this.findViewById ( R.id.imageButton2 );
				if ( isChecked ) {
					mNano_dev.Set_Auto_Measure( 1 );
					/*checkpoint*/
					/*starting polling thread to monitor auto-measure signal*/
					Start_Monitor_AutoMeasure_Thread ( );
					btn_blank.setEnabled( false );
					btn_sample.setEnabled( false );
				}
				else {
					mNano_dev.Set_Auto_Measure( 0 );
					/*checkpoint*/
					/*stopping auto-measure polling thread*/
					Stop_Monitor_AutoMeasure_Thread ( );
					btn_blank.setEnabled( true );
					btn_sample.setEnabled( true );
				}
				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			}
			
		});
		sw2.setChecked( Cur_Auto_Measure );
		sw2.setEnabled( false );
		
    	gridlayout = (LinearLayout) findViewById(R.id.GridLayout);
    	if ( gridlayout != null ) {
    		if ( gridlayout.findViewById( GVTable.ID ) == null )
    			gridlayout.addView( table );
    		else
    			this.table.gvRemoveAll ( );
    	}

    	/*20160323 added by michael*/
    	if ( nano_database.get_database() != null )
    		if ( nano_database.get_database().isOpen() == true )
    			nano_database.get_database().close();
    	
	    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView1 ) ).setText( "" );
	    
	    Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	    // Create an ArrayAdapter using the string array and a default spinner layout
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this, R.array.protein_quantity_array, android.R.layout.simple_spinner_item );
	    // Specify the layout to use when the list of choices appears
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    // Apply the adapter to the spinner
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener( Protein_quantity_selection );
	    spinner.setPrompt ( "Select a quantity" ); 
	    
	    int spinner_adapter_item_count = spinner.getCount();
	    if (spinner_adapter_item_count > 0) {
	    	//spinner.setSelection( -1 );
	    	Protein_quantity_mode = 0;
		}
	    
	    ed_protein_quantity = ( EditText ) findViewById( R.id.editText1 );
		ed_protein_quantity.addTextChangedListener( new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			} 
			
		});
		ed_protein_quantity.setOnFocusChangeListener ( new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					
				}
				else {
					if ( ed_protein_quantity.getText().toString().trim().equals( "" ) ) {
						//ed_protein_quantity.setText(item_data.get_high_speed_rpm_string()); 
	            	} else {
	            		
	            	}
				}
			} 
			
		} );
		ed_protein_quantity.setFilters(apped_input_filter(ed_protein_quantity.getFilters(), new DecimalInputFilter(ed_protein_quantity, 2, 2, 0.0, 5.0, 1.0)));
		ed_protein_quantity.setOnEditorActionListener ( ed_action_listener );
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
				/*Maybe backspace to delete chars and cause the regex is unmatched，trim all chars after backspace char index*/
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
								IME_toggle();
								/*sync & commit latest data into property*/
								//sync_property(v);
								Protein_quantity_coefficient [ 3 ] = Double.parseDouble( ed_protein_quantity.getText().toString() );
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
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
        }
    }//end method
	
	public void switch_to_setting_page ( View v) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_SettingPage == null )
			mLayout_SettingPage = ( LinearLayout ) inflater.inflate( R.layout.setting, null );
		mLayout_Content.addView( mLayout_SettingPage );
		
		ImageButton btn_calibration, btn_lcd_brightness, btn_time_date;
		btn_calibration = ( ImageButton ) findViewById( R.id.imageButton1 );
		btn_lcd_brightness = ( ImageButton ) findViewById( R.id.imageButton2 );
		btn_time_date = ( ImageButton ) findViewById( R.id.imageButton3 );
		
		btn_calibration.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert_message = "Please pipette 2 microliter of water!";
				alert_dlg2.setMessage( alert_message );
				alert_dlg2.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
				    	/*alert_message = "Device Measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();*/

					    /*checkpoint*/
				            is_store_cali_data = true;
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
					    msg.sendToTarget ( );						
					}
					
				});
				alert_dlg2.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					
				});
				alert_dlg2.show();
			}
			
		});
		
		btn_time_date.setOnClickListener( new OnClickListener() {
			DatePicker datepicker1;
			TimePicker timepicker1;
			
			@Override
			public void onClick(View v) {
				 // Initialize a new date picker dialog fragment
                //DialogFragment dFragment = new DatePickerFragment();

                // Show the date picker dialog fragment
                //dFragment.show(getFragmentManager(), "Date Picker");
				byte [] datetime_data = new byte [ 256 ];
				int [] datetime_data_int = new int [ 256 / 4 ];
				AlertDialog.Builder dlg_builder = new AlertDialog.Builder( NanoActivity.this );
				LinearLayout date_time_setting_view = ( LinearLayout ) LayoutInflater.from ( alert_dlg.findViewById( android.R.id.content ).getContext() ).inflate( R.layout.date_time_setting_layout, null );
				AlertDialog date_time_dlg = dlg_builder.create();
				date_time_dlg.setView( date_time_setting_view );
				//date_time_dlg.setMessage( alert_message );
				date_time_dlg.setTitle( "Setting Date & Time" );
				date_time_dlg.setCanceledOnTouchOutside( true );
				date_time_dlg.setCancelable( true );

				calendar.setTime( new Date () );
				datepicker1 = ( DatePicker ) date_time_setting_view.findViewById( R.id.datePicker1 );
				timepicker1 = ( TimePicker ) date_time_setting_view.findViewById( R.id.timePicker1 );
				datepicker1.updateDate( calendar.get( Calendar.YEAR ), calendar.get( Calendar.MONTH ),  calendar.get( Calendar.DATE ) );
				timepicker1.setCurrentHour( calendar.get( Calendar.HOUR_OF_DAY ) );
				timepicker1.setCurrentMinute( calendar.get( Calendar.MINUTE ) );
				date_time_dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						calendar.setTimeInMillis ( 0 );
						calendar.set( datepicker1.getYear(), datepicker1.getMonth(), datepicker1.getDayOfMonth(), timepicker1.getCurrentHour(), timepicker1.getCurrentMinute(), calendar.get( Calendar.SECOND ) );
						//exec_shell_command ( "date -s" + dateformat.format( calendar.getTime() ) + "; \n" );
				        try {
				          Process loProcess = Runtime.getRuntime().exec("su");
				          DataOutputStream loDataOutputStream = new DataOutputStream(loProcess.getOutputStream());
				          loDataOutputStream.writeBytes( "date -s" + dateformat.format( calendar.getTime() ) + "; \n" );
				        } catch (IOException e) {
							// TODO Auto-generated catch block
						  e.printStackTrace();
					    }
				        hide_system_navigation ();
				        sync_Android_RTC_systime ();
					}
					
				});
				date_time_dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						hide_system_navigation ();
					}
					
				});
				date_time_dlg.setOnDismissListener( new OnDismissListener () {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						hide_system_navigation ();
					}
					
				});
				date_time_dlg.show();
				
				/*if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_GET_TIME, 0, 1, datetime_data, 0) ) {
					ByteBuffer.wrap ( datetime_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( datetime_data_int );
					Log.d ( Tag, "year: " + Integer.toString( datetime_data_int [ 0 ] ) );
					Log.d ( Tag, "month: " + Integer.toString( datetime_data_int [ 1 ] ) );
					Log.d ( Tag, "dayofmonth: " + Integer.toString( datetime_data_int [ 2 ] ) );
					Log.d ( Tag, "hour: " + Integer.toString( datetime_data_int [ 3 ] ) );
					Log.d ( Tag, "minute: " + Integer.toString( datetime_data_int [ 4 ] ) );
					Log.d ( Tag, "second: " + Integer.toString( datetime_data_int [ 5 ] ) );
					datepicker1.updateDate( datetime_data_int [ 0 ], datetime_data_int [ 1 ],  datetime_data_int [ 2 ] );
					timepicker1.setCurrentHour( datetime_data_int [ 3 ] );
					timepicker1.setCurrentMinute( datetime_data_int [ 4 ] );
				}*/
				
				
				//date_time_dlg.setContentView ( date_time_setting_view );
				/*Dialog date_time_dlg = new Dialog(NanoActivity.this, R.style.CenterDialog);
				LinearLayout date_time_setting_view = ( LinearLayout ) LayoutInflater.from ( date_time_dlg.findViewById( android.R.id.content ).getContext() ).inflate( R.layout.date_time_setting_layout, null );
				date_time_dlg.setContentView( date_time_setting_view );
				date_time_dlg.show();*/
			}
		});
		
		btn_lcd_brightness.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dlg_builder = new AlertDialog.Builder( NanoActivity.this );
				RelativeLayout lcd_brightness_adjust_view = ( RelativeLayout ) LayoutInflater.from ( alert_dlg.findViewById( android.R.id.content ).getContext() ).inflate( R.layout.lcd_brightness_layout, null );
				AlertDialog lcd_brightness_dlg = dlg_builder.create();
				
				lcd_brightness_dlg.setView( lcd_brightness_adjust_view );
				//date_time_dlg.setMessage( alert_message );
				lcd_brightness_dlg.setTitle( "Adjust lcd brightness" );
				lcd_brightness_dlg.setCanceledOnTouchOutside( true );
				lcd_brightness_dlg.setCancelable( true );
				
				SeekBar seekbar1 = (SeekBar) lcd_brightness_adjust_view.findViewById(R.id.seekBar1);
				seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						byte [] bytes, data_bytes = new byte [ 256 ];;
						int progress = seekBar.getProgress();

						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( progress ).array();
						System.arraycopy ( bytes, 0, data_bytes, 0, bytes.length );
						if ( mNano_dev.MN913A_IOCTL( CMD_T.HID_CMD_SET_LCD_BRIGHTNESS, 0, 1, data_bytes, 0 ) == true )
							Log.d ( Tag, "lcd brightness setting sccess");
						
        			    preference_editor.putInt( "lcd brightness", progress );
        			    preference_editor.commit( );
        			    Lcd_Brightness_Level = progress;						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						byte [] bytes, data_bytes = new byte [ 256 ];;

						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( progress ).array();
						System.arraycopy ( bytes, 0, data_bytes, 0, bytes.length );
						if ( mNano_dev.MN913A_IOCTL( CMD_T.HID_CMD_SET_LCD_BRIGHTNESS, 0, 1, data_bytes, 0 ) == true )
							Log.d ( Tag, "lcd brightness setting sccess");
						
        			    preference_editor.putInt( "lcd brightness", progress );
        			    preference_editor.commit( );
        			    Lcd_Brightness_Level = progress;
					}
				});
				lcd_brightness_dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Cur_Lcd_Brightness_Level = Lcd_Brightness_Level; 
					}
				});
				
				lcd_brightness_dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						byte [] bytes, data_bytes = new byte [ 256 ];;

						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Lcd_Brightness_Level ).array();
						System.arraycopy ( bytes, 0, data_bytes, 0, bytes.length );
						if ( mNano_dev.MN913A_IOCTL( CMD_T.HID_CMD_SET_LCD_BRIGHTNESS, 0, 1, data_bytes, 0 ) == true )
							Log.d ( Tag, "lcd brightness setting sccess");
						
        			    preference_editor.putInt( "lcd brightness", Lcd_Brightness_Level );
        			    preference_editor.commit( );
        			    Lcd_Brightness_Level = Cur_Lcd_Brightness_Level;
					}
				});
				lcd_brightness_dlg.show();
			}
			
		});
	}

	public void switch_to_about_page ( View v) {
		
	}
	
	class CountDownRunner implements Runnable{
	    // @Override
	    public void run() {
	            while(!Thread.currentThread().isInterrupted()){
	                try {
	                	runOnUiThread(new Runnable() {
	                		public void run() {
	                			try {
	                				TextView txtCurrentTime = (TextView) findViewById( R.id.lbltime );
	                				String curTime = df.format(new Date());
	                				
	                				if ( txtCurrentTime != null )
	                					txtCurrentTime.setText(curTime);
	                			} catch (Exception e) {
	                				
	                			}
	                		}
	                	});
					Thread.sleep(1000);
	                } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();
	                } catch(Exception e){
	                }
	            }
	    }
	}

	public void adjust_ui_dimension ( ViewGroup vg ) {
		int i;

		Log.d ( Tag, "child count: " + vg.getChildCount() + "width: " + vg.getMeasuredWidth() + "height: " + vg.getMeasuredHeight() );
		for (i = 0; i < vg.getChildCount ( ); i++ ) {
			if ( vg.getChildAt( i ) instanceof ViewGroup )
				adjust_ui_dimension ( ( ViewGroup ) vg.getChildAt( i ) );
			else
				Log.d ( Tag, "child: " + vg.getChildAt( i ) );
		} 
	}
	
	public void EnumerationDevice(Intent intent) {
		if ( intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
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
    		if ( intent != null && intent.getAction() != null && intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (mNano_dev.Enumeration(device)) {
    				Is_MN913A_Online = true;
    			}
    			else {
    				Is_MN913A_Online = false;
    			}
    		}
		update_ui_state ( );
	}
	
	protected void update_ui_state() {
		TextView seekbar_value;
		seekbar_value = (TextView) findViewById(R.id.textView1);
		
		SeekBar seekbar1;
		seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		//connection_status_v.setImageResource(drawable)
		if ( Is_MN913A_Online == true ) {
			//connection_status_v.setImageResource ( R.drawable.usb_connection );
			//seekbar_value.setEnabled( true );
			//seekbar1.setEnabled( true );
			//mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			//Setting_Btn.setEnabled( true );
			//Measure_Btn.setEnabled( true );
			Cur_Voltage_Level = 162;
			mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
			mNano_dev.Set_Start_Calibration ( 0 );
			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			mNano_dev.Set_Start_Calibration ( 0 );
			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
		}
		else {
			/*connection_status_v.setImageResource ( R.drawable.usb_disconnection );
			seekbar_value.setEnabled( false );
			seekbar1.setEnabled( false );
			Setting_Btn.setEnabled( false );
			Measure_Btn.setEnabled( false );*/
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
						update_ui_state ( );
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
					
					update_ui_state ( );
					if (mRequest_USB_permission==true) {
						//hide_system_bar();
						mRequest_USB_permission = false;
					}
					
					if (mRequest_USB_permission==true) {
						hide_system_bar();
						mRequest_USB_permission = false;
					}
				}
		}
    	
    };
    
    @Override
    protected void onStop() {
    	super.onStop();
    	/*show_system_bar();*/
    	mNano_dev.Set_Illumination_State ( 0 );
		mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();

    	/*if (mRequest_USB_permission==false)
    		hide_system_bar();*/
    	EnumerationDevice(getIntent());
    	//adjust_ui_dimension ( ( ViewGroup ) this.findViewById( R.id.top_ui ) );
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		save_calibration_data ();
	}
	
    protected void onNewIntent(Intent intent) {
    	mNano_dev.show_debug("New intent: "+intent.getAction()+"\n");
    	if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		EnumerationDevice(intent);
    }
    
	/*20141105 added by michael
	 * execute shell command in superuser privilege */
	private final String Msg_Shell_Command_Error = "shell command error";
	public String exec_shell_command( String shell_cmd ) {
		// TODO Auto-generated method stub
		String result, line;
		java.lang.Process p;
		java.lang.Runtime rt;
		byte[] buff;
		int readed;

		result = Msg_Shell_Command_Error;
		buff = new byte[100];

		try {
			rt = Runtime.getRuntime();
			p = rt.exec(new String[] { "/system/xbin/su" });
			// p = Runtime.getRuntime().exec( "/system/bin/ls" );
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			//DataInputStream is = new DataInputStream(p.getInputStream());
			InputStreamReader is_reader = new InputStreamReader ( p.getInputStream() );
			BufferedReader buf_is_reader = new BufferedReader ( is_reader );
			os.writeBytes( shell_cmd );
			os.writeBytes( "exit\n" );
			os.flush();
			p.waitFor();
			if ( p.exitValue() == 0 ) {
			/*while (is.available() > 0) {
				readed = is.read(buff);
				if (readed <= 0)
					break;
				String seg = new String(buff, 0, readed);
				result = result + seg; // result is a string to show in textview
			}*/
				result = "";
				while ( ( line = buf_is_reader.readLine() ) != null ) {
					result += line;
				}
			}
			else
				result = Msg_Shell_Command_Error;
			os.flush();
			os.close();
			is_reader.close();
			buf_is_reader.close();
			p.waitFor();
			p.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
		}
		
		return result;
	}
	
    public void hide_system_bar() {    	
    	String result;
    	result = exec_shell_command ( "service call activity 42 s16 com.android.systemui\n" );
    	if ( result.equals( Msg_Shell_Command_Error) == false )
    		Log.d ( Tag, "hide system bar successfully!" );
    	else
    		Log.d ( Tag, "hide system bar fail!" );
    	
    }
    
    public void show_system_bar() {
    	String result;
    	result = exec_shell_command ( "am startservice -n com.android.systemui/.SystemUIService\n" );
    	if ( result.equals( Msg_Shell_Command_Error) == false )
    		Log.d ( Tag, "show system bar successfully!" );
    	else
    		Log.d ( Tag, "show system bar fail!" );
    }
    
    /*20160318 added by michael*/
    public static final int EXPERIMENT_MEASURE_BLANK = 0;
    public static final int EXPERIMENT_MEASURE_SAMPLE = 1;
    public static final int EXPERIMENT_CALIBRATION_DEVICE = 2;
    public static final int EXPERIMENT_TEST = 10;
    channel_raw_data channel_blank = new channel_raw_data ( 50 ); 
    channel_raw_data channel_sample = new channel_raw_data ( 50 ); 
    channel_raw_data channel_sample1 = new channel_raw_data ( 50 );
    byte [] composite_raw_data = new byte [ 4096 ];
    boolean blank_valid = false;
    class channel_raw_data {
    	int [] ch1_xenon_raw_data, ch1_no_xenon_raw_data, ch2_xenon_raw_data, ch2_no_xenon_raw_data, ch3_xenon_raw_data, ch3_no_xenon_raw_data, ch4_xenon_raw_data, ch4_no_xenon_raw_data;
    	static final int default_ch_data_num = 100;
    	int channel_elements = 0;
        double ch1_xenon_sum, ch1_no_xenon_sum, ch2_xenon_sum, ch2_no_xenon_sum, ch3_xenon_sum, ch3_no_xenon_sum, ch4_xenon_sum, ch4_no_xenon_sum;
        double ch1_xenon_mean, ch1_no_xenon_mean, ch2_xenon_mean, ch2_no_xenon_mean, ch3_xenon_mean, ch3_no_xenon_mean, ch4_xenon_mean, ch4_no_xenon_mean;
        double ch1_xenon_stdev, ch1_no_xenon_stdev, ch2_xenon_stdev, ch2_no_xenon_stdev, ch3_xenon_stdev, ch3_no_xenon_stdev, ch4_xenon_stdev, ch4_no_xenon_stdev;

    	channel_raw_data () {
    		ch1_xenon_raw_data = new int [ default_ch_data_num ];
    		ch1_no_xenon_raw_data = new int [ default_ch_data_num ];
    		ch2_xenon_raw_data = new int [ default_ch_data_num ];
    		ch2_no_xenon_raw_data = new int [ default_ch_data_num ];
    		ch3_xenon_raw_data = new int [ default_ch_data_num ];
    		ch3_no_xenon_raw_data = new int [ default_ch_data_num ];
    		ch4_xenon_raw_data = new int [ default_ch_data_num ];
    		ch4_no_xenon_raw_data = new int [ default_ch_data_num ];
    		channel_elements = default_ch_data_num;
    	}

		channel_raw_data ( int data_num ) {
    		ch1_xenon_raw_data = new int [ data_num ];
    		ch1_no_xenon_raw_data = new int [ data_num ];
    		ch2_xenon_raw_data = new int [ data_num ];
    		ch2_no_xenon_raw_data = new int [ data_num ];
    		ch3_xenon_raw_data = new int [ data_num ];
    		ch3_no_xenon_raw_data = new int [ data_num ];
    		ch4_xenon_raw_data = new int [ data_num ];
    		ch4_no_xenon_raw_data = new int [ data_num ];
    		channel_elements = data_num;
    	}
		
		public int set_channel_raw_data ( byte [] composite_data) {
			int ch_ocuupy_pages;
			
			if ( channel_elements * ( Integer.SIZE / Byte.SIZE ) / 256 != 0) {
				ch_ocuupy_pages = channel_elements * ( Integer.SIZE / Byte.SIZE ) / 256 + 1;
			}
			else {
				ch_ocuupy_pages = channel_elements * ( Integer.SIZE / Byte.SIZE ) / 256;
			}
			
			//byte [] one_ch_raw_data = new byte [ ch_ocuupy_pages * 256 ];
			ch_ocuupy_pages = 2;
			byte [] one_ch_raw_data = new byte [ ch_ocuupy_pages * 256 ];
		    for ( int i = 0; i < 8; i++ ) {
		    	System.arraycopy( composite_data , i * ch_ocuupy_pages * 256 , one_ch_raw_data, 0, one_ch_raw_data.length );	
		    	switch ( i ) {
		    	case 0:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch1_no_xenon_raw_data );
		    		ch1_no_xenon_sum = sum_of_raw_data ( ch1_no_xenon_raw_data );
		    		ch1_no_xenon_mean = ch1_no_xenon_sum / channel_elements;
		    		ch1_no_xenon_stdev = stdev_of_raw_data ( ch1_no_xenon_raw_data, ch1_no_xenon_mean );
		    		break;
		    	case 1:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch1_xenon_raw_data );
		    		ch1_xenon_sum = sum_of_raw_data ( ch1_xenon_raw_data );
		    		ch1_xenon_mean = ch1_xenon_sum / channel_elements;
		    		ch1_xenon_stdev = stdev_of_raw_data ( ch1_xenon_raw_data, ch1_xenon_mean );
		    		break;
		    	case 2:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch2_no_xenon_raw_data );
		    		ch2_no_xenon_sum = sum_of_raw_data ( ch2_no_xenon_raw_data );
		    		ch2_no_xenon_mean = ch2_no_xenon_sum / channel_elements;
		    		ch2_no_xenon_stdev = stdev_of_raw_data ( ch2_no_xenon_raw_data, ch2_no_xenon_mean );
		    		break;
		    	case 3:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch2_xenon_raw_data );
		    		ch2_xenon_sum = sum_of_raw_data ( ch2_xenon_raw_data );
		    		ch2_xenon_mean = ch2_xenon_sum / channel_elements;
		    		ch2_xenon_stdev = stdev_of_raw_data ( ch2_xenon_raw_data, ch2_xenon_mean );
		    		break;
		    	case 4:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch3_no_xenon_raw_data );
		    		ch3_no_xenon_sum = sum_of_raw_data ( ch3_no_xenon_raw_data );
		    		ch3_no_xenon_mean = ch3_no_xenon_sum / channel_elements;
		    		ch3_no_xenon_stdev = stdev_of_raw_data ( ch3_no_xenon_raw_data, ch3_no_xenon_mean );
		    		break;
		    	case 5:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch3_xenon_raw_data );
		    		ch3_xenon_sum = sum_of_raw_data ( ch3_xenon_raw_data );
		    		ch3_xenon_mean = ch3_xenon_sum / channel_elements;
		    		ch3_xenon_stdev = stdev_of_raw_data ( ch3_xenon_raw_data, ch3_xenon_mean );
		    		break;
		    	case 6:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch4_no_xenon_raw_data );
		    		ch4_no_xenon_sum = sum_of_raw_data ( ch4_no_xenon_raw_data );
		    		ch4_no_xenon_mean = ch4_no_xenon_sum / channel_elements;
		    		ch4_no_xenon_stdev = stdev_of_raw_data ( ch4_no_xenon_raw_data, ch4_no_xenon_mean );
		    		break;
		    	case 7:
		    		ByteBuffer.wrap ( one_ch_raw_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( ch4_xenon_raw_data );
		    		ch4_xenon_sum = sum_of_raw_data ( ch4_xenon_raw_data );
		    		ch4_xenon_mean = ch4_xenon_sum / channel_elements;
		    		ch4_xenon_stdev = stdev_of_raw_data ( ch4_xenon_raw_data, ch4_xenon_mean );
		    		break;
		    	}
		    }
		    //Log.d ( Tag, "ch1 xenon: " + Double.toString( ch1_xenon_mean ) + " ch2 xenon: " + Double.toString( ch2_xenon_mean ) + " ch3 xenon: " + Double.toString( ch3_xenon_mean ) + " ch4 xenon: " + Double.toString( ch4_xenon_mean ) );
		    //Log.d ( Tag, "ch1 no xenon: " + Double.toString( ch1_no_xenon_mean ) + " ch2 no xenon: " + Double.toString( ch2_no_xenon_mean ) + " ch3 no xenon: " + Double.toString( ch3_no_xenon_mean ) + " ch4 no xenon: " + Double.toString( ch4_no_xenon_mean ) );
		    //Log.d ( Tag, "ch1 no xenon: " + Double.toString( ch1_no_xenon_stdev ) + " ch2 no xenon: " + Double.toString( ch2_no_xenon_stdev ) + " ch3 no xenon: " + Double.toString( ch3_no_xenon_stdev ) + " ch4 no xenon: " + Double.toString( ch4_no_xenon_stdev ) );
		    //Log.d ( Tag, "ch1 xenon: " + Double.toString( ch1_xenon_stdev ) + " ch2 xenon: " + Double.toString( ch2_xenon_stdev ) + " ch3 xenon: " + Double.toString( ch3_xenon_stdev ) + " ch4 xenon: " + Double.toString( ch4_xenon_stdev ) );
			return 0;
		}
		
		double sum_of_raw_data ( int [] data ) {
			double sum = 0.0f;
			for ( int value  : data ) {
				sum += value;
			}
			return sum;
		}
		
		double stdev_of_raw_data ( int [] data, double mean ) {
			double mse = 0.0f;
			for ( int value  : data ) {
				mse += Math.pow( value - mean,  2);
			}
			mse /= data.length;
			mse = Math.pow( mse, .5);
			return mse;
		}
    }
    
    /*20160323 added by michael*/
    static final int UPDATE_DNA_RESULT_UI = 0x81;
    static final int UPDATE_PROTEIN_RESULT_UI = 0x82;
    Handler mHandler = new Handler () {
    	public void handleMessage ( Message msg ) {
    		switch ( msg.what ) {
    		case UPDATE_DNA_RESULT_UI:
    			table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
    		    table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
    		    table.refresh_last_table();
    		    DNA_measure_data dna_data = ( DNA_measure_data ) msg.obj;
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView1 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.Conc, 3 ).doubleValue() ) );
    		    if ( dna_data.include_A320 == true ) {
    		    	//( ( TextView ) NanoActivity.this.findViewById( R.id.textView3 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 - dna_data.A320 ) / ( dna_data.A280 * 23 - dna_data.A320 ), 3 ).doubleValue() ) );
    		    	//( ( TextView ) NanoActivity.this.findViewById( R.id.textView4 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 - dna_data.A320 ) / ( dna_data.A230 * 167 - dna_data.A320 ), 3 ).doubleValue() ) );
    		    	( ( TextView ) NanoActivity.this.findViewById( R.id.textView6 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 - dna_data.A320 ) / ( dna_data.A280 * 23 - dna_data.A320 ), 3 ).doubleValue() ) );
    		    	( ( TextView ) NanoActivity.this.findViewById( R.id.textView7 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 - dna_data.A320 ) / ( dna_data.A230 * 167 - dna_data.A320 ), 3 ).doubleValue() ) );
    		    }
    		    else {
    		    	//( ( TextView ) NanoActivity.this.findViewById( R.id.textView3 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( ( dna_data.A260 ) * 19 ) / ( ( dna_data.A280 ) * 23 ), 3 ).doubleValue() ) );
    		    	//( ( TextView ) NanoActivity.this.findViewById( R.id.textView4 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( ( dna_data.A260 ) * 210 ) / ( ( dna_data.A230 ) * 167 ), 3 ).doubleValue() ) );
    		    	( ( TextView ) NanoActivity.this.findViewById( R.id.textView6 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( ( dna_data.A260 ) * 19 ) / ( ( dna_data.A280 ) * 23 ), 3 ).doubleValue() ) );
    		    	( ( TextView ) NanoActivity.this.findViewById( R.id.textView7 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( ( dna_data.A260 ) * 210 ) / ( ( dna_data.A230 ) * 167 ), 3 ).doubleValue() ) );
    		    }
    		    //( ( TextView ) NanoActivity.this.findViewById( R.id.textView5 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.A260 * 24.38, 3 ).doubleValue() ) );
    		    //( ( TextView ) NanoActivity.this.findViewById( R.id.textView5 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.Conc / 50, 3 ).doubleValue() ) );
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView3 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.Conc / 50, 3 ).doubleValue() ) );
    		    
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView4 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.A230, 3 ).doubleValue() ) );
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView5 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.A280, 3 ).doubleValue() ) );
    			break;
    			
    		case UPDATE_PROTEIN_RESULT_UI:
    			table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.PROTEIN_VALUE_TABLE_NAME, nano_database.get_database());
    		    table.gvReadyTable("select * from " + NanoSqlDatabase.PROTEIN_VALUE_TABLE_NAME, nano_database.get_database());
    		    table.refresh_last_table();
    		    Protein_measure_data protein_data = ( Protein_measure_data ) msg.obj;
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView1 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  protein_data.A280, 3 ).doubleValue() ) );
    			break;
    		}
    	}
    };
    
    /*checkpoing*/
    
    class LooperThread extends Thread {
    	double Transmission_rate = 0, I_blank = 0.0, I_sample = 0.0, Cali_result = 0.0;
    	int count = 0;

        public void run() {
            Looper.prepare();

            mWorker_thread_handler = new Handler() {
                public void handleMessage ( Message msg ) {
                    // process incoming messages here
                	switch ( msg.what ) {
                	  case EXPERIMENT_TEST:
                		  Thread_Sync_By_Obj.unlock();
                		  break;
                	  case EXPERIMENT_CALIBRATION_DEVICE:
                		  NanoActivity.this.runOnUiThread( new Runnable() {
							@Override
							public void run() {
								alert_message = "Device Measuring!";
								alert_dlg.setMessage( alert_message );
								alert_dlg.show();
							}
                		  });
                		  if ( Is_MN913A_Online == true ) {
                			  mNano_dev.Set_Max_Volt_Level ( preference.getInt( "Xenon max voltage level", 162) );
                			  mNano_dev.Set_Min_Volt_Level ( preference.getInt( "Xenon min voltage level", 162) );
                			  mNano_dev.Set_Max_Voltage_Intensity ( preference.getFloat( "Xenon max voltage intensity", 850000 ) );
                			  mNano_dev.Set_Min_Voltage_Intensity ( preference.getFloat( "Xenon min voltage intensity", 350000 ) );
                			  if ( mNano_dev.Get_Max_Volt_Level() < 162 )
                				  Cur_Voltage_Level = 162;
                			  else
                				  Cur_Voltage_Level = mNano_dev.Get_Max_Volt_Level();
              				mNano_dev.Set_Start_Calibration ( 0 );
              				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                			try {
                  				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                  				sleep ( 10 );
                    			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                				//sleep ( 3000 );
								while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
									sleep ( 10 );
									if ( mNano_dev.is_dev_busy == 0 ) {
										//Log.d ( Tag, "MN913A device not busy");
										break;
									}
								}
								Log.d ( Tag, "Getting MN913A status finish");
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		  }
                		  
                		  if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                			  channel_blank.set_channel_raw_data ( composite_raw_data );
                			  if ( channel_blank.ch2_xenon_mean < 850000 )
                				  cali_data.put( "before", Double.toString( NanoSqlDatabase.truncateDecimal( 100 * channel_blank.ch2_xenon_mean / 850000, 3 ).doubleValue() ) );
                			  else
                				  cali_data.put( "before", Double.toString( NanoSqlDatabase.truncateDecimal( 100 * 850000 / channel_blank.ch2_xenon_mean, 3 ).doubleValue() ) );
                			  /*checkpoint*/
                			  //if ( channel_blank.ch2_xenon_mean  mNano_dev.Max_Voltage_Intensity )
                		  }
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								alert_dlg.dismiss();
								alert_message = "Device Calibrating!";
								alert_dlg.setMessage( alert_message );
								alert_dlg.setCanceledOnTouchOutside( true );
								alert_dlg.setCancelable( true );
		  					    alert_dlg.show();								
							}
                			  
                		  });

                		  /*checkpoint*/
              			  try {
							sleep ( 3000 );
						  } catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							  e1.printStackTrace();
						  }
                		  if ( channel_blank.ch2_xenon_mean > 867000 || channel_blank.ch2_xenon_mean < 833000 ) {
                			  mNano_dev.Set_Start_Calibration ( 1 );
                			  mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
                			  mNano_dev.Set_Start_Calibration ( 0 );
              			  while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
							  try {
								sleep ( 1000 );
							  } catch (InterruptedException e) {
								// TODO Auto-generated catch block
								  e.printStackTrace();
							  }
							  if ( mNano_dev.Has_Calibration == 1 ) {
								//Log.d ( Tag, "MN913A device not busy");
								  break;
							  }
						  }
              			      preference_editor.putInt( "Xenon max voltage level", mNano_dev.Get_Max_Volt_Level ( ) );
              			      preference_editor.putInt( "Xenon min voltage level", mNano_dev.Get_Min_Volt_Level ( ) );
              			      preference_editor.putFloat( "Xenon max voltage intensity", ( float ) mNano_dev.Get_Max_Voltage_Intensity ( ) );
              			      preference_editor.putFloat( "Xenon min voltage intensity", (float) mNano_dev.Get_Min_Voltage_Intensity ( ) );
              			      preference_editor.commit( );
                		  }
                		  else {
                			  mNano_dev.Has_Calibration = 1;
                			  mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                		  }
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if ( alert_dlg.isShowing( ) && alert_message.equals( "Device Calibrating!"  ) )
								//if ( alert_dlg.isShowing( ) )
									alert_dlg.dismiss ( );
								View decorView = getWindow().getDecorView();
								// Hide both the navigation bar and the status bar.
								// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
								// a general rule, you should design your app to hide the status bar whenever you
								// hide the navigation bar.
								int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
								decorView.setSystemUiVisibility(uiOptions);
							}
                		  });
                		  
                		  try {
							while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
								sleep ( 10 );
								if ( mNano_dev.is_dev_busy == 0 ) {
									//Log.d ( Tag, "MN913A device not busy");
									break;
								}
							}
							Log.d ( Tag, "Getting MN913A status finish");
						  } catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						  }
                		  if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                			  channel_sample.set_channel_raw_data ( composite_raw_data );

                    		  Cali_result = channel_sample.ch2_xenon_mean + ( 2 * Math.random() - 1 ) * ( 1350 );
                    		  if ( Cali_result < 850000 )
                    			  cali_data.put( "after", Double.toString( NanoSqlDatabase.truncateDecimal( 100 * ( Cali_result ) / 850000, 3 ).doubleValue() ) );
                    		  else
                    			  cali_data.put( "after", Double.toString( NanoSqlDatabase.truncateDecimal( 100 * ( 850000 ) / Cali_result, 3 ).doubleValue() ) );
                			  if ( is_store_cali_data == true ) {
                        		  NanoActivity.this.runOnUiThread( new Runnable() {
            							@Override
            							public void run() {
            								alert_dlg1.setTitle( "Calibration Result" );
            								alert_dlg1.show();
            								alert_dlg1.getWindow().setLayout( LayoutParams.MATCH_PARENT, 600 );
            							}
                          		  });
                        		  
                				  cali_data.put( "datetime", df1.format(new Date()) );
                				  
                				  if ( channel_sample.ch2_xenon_mean > 824500 || channel_sample.ch2_xenon_mean < 875500 ) {
                					  cali_data.put( "situation", "pass" );
                				  }
                				  else
                					  cali_data.put( "situation", "fail" );
                				  
                				  Calibration_Data_List.add( cali_data );
                				  if ( Calibration_Data_List.size() > 11 ) {
                					  //Calibration_Data_List.removeFirst();
                					  Calibration_Data_List.remove( 1 );
                				  }
                				  cali_data = new HashMap<String, String>(); 
                				  cali_list_adapter.notifyDataSetChanged();
                			  }
                			  /*checkpoint*/
                			  //if ( channel_blank.ch2_xenon_mean  mNano_dev.Max_Voltage_Intensity )
                		  }
                		  break;
                	  case EXPERIMENT_MEASURE_BLANK:
                		  NanoActivity.this.runOnUiThread( new Runnable() {
							@Override
							public void run() {
								alert_message = "Blank Measuring!";
								alert_dlg.setMessage( alert_message );
								alert_dlg.show();	
							}
                		  });
						    
                		  if ( Is_MN913A_Online == true ) {
              				Cur_Voltage_Level = mNano_dev.Get_Max_Volt_Level();
              				mNano_dev.Set_Start_Calibration ( 0 );
              				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                			try {
                  				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                  				sleep ( 10 );
                    			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                				//sleep ( 3000 );
								while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
									sleep ( 10 );
									if ( mNano_dev.is_dev_busy == 0 ) {
										//Log.d ( Tag, "MN913A device not busy");
										break;
									}
								}
								Log.d ( Tag, "Getting MN913A status finish");
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		  }
                		  
                		  if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                			  channel_blank.set_channel_raw_data ( composite_raw_data );
                			  /*checkpoint*/
                			  //if ( channel_blank.ch2_xenon_mean  mNano_dev.Max_Voltage_Intensity )
                		  }
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if ( alert_dlg.isShowing( ) )
									alert_dlg.dismiss ( );
								if ( measure_mode < MEASURE_MODE_PROTEIN ) {
									Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
									sw.setEnabled( true );
								}

								Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
								sw1.setEnabled( true );
								Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
								sw2.setEnabled( true );
							    if ( sw2.isChecked() ) {
									NanoActivity.this.findViewById( R.id.imageButton1 ).setEnabled( false );
									NanoActivity.this.findViewById( R.id.imageButton2 ).setEnabled( false );							    	
							    }
							    else {
									NanoActivity.this.findViewById( R.id.imageButton1 ).setEnabled( true );
									NanoActivity.this.findViewById( R.id.imageButton2 ).setEnabled( true );
							    }
							    
								View decorView = getWindow().getDecorView();
								// Hide both the navigation bar and the status bar.
								// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
								// a general rule, you should design your app to hide the status bar whenever you
								// hide the navigation bar.
								int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
								decorView.setSystemUiVisibility(uiOptions);
							}
                			  
                		  });
                		  //Thread_Sync_By_Obj.unlock();
                		  break;
                		  
                	  case EXPERIMENT_MEASURE_SAMPLE:
                		  NanoActivity.this.runOnUiThread( new Runnable() {
							@Override
							public void run() {
								alert_message = "Sample Measuring!";
								alert_dlg.setMessage( alert_message );
								alert_dlg.show();	
							}
                		  });

                		  if ( Is_MN913A_Online == true ) {
                			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                			try {
                				sleep ( 10 );
								while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
									sleep ( 10 );
									if ( mNano_dev.is_dev_busy == 0 ) {
										//Log.d ( Tag, "MN913A device not busy");
										break;
									}
								}
								Log.d ( Tag, "Getting MN913A status finish");
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		  }
                		  
                		  if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                			  channel_sample.set_channel_raw_data ( composite_raw_data );
                			  if ( measure_mode < MEASURE_MODE_PROTEIN ) {
                	    	  I_blank = Math.abs( channel_blank.ch2_xenon_mean - channel_blank.ch2_no_xenon_mean );
                	    	  I_sample = Math.abs ( channel_sample.ch2_xenon_mean - channel_sample.ch2_no_xenon_mean );
                	    	  /*checkpoint*/
                	    	  if ( I_blank != 0 )
                	    			Transmission_rate = I_sample / I_blank;
                	    	  if ( 0.944 < Transmission_rate ) {
                				//conc less than 25, non-linear, search minima voltage level and measure then reset
                				Cur_Voltage_Level = mNano_dev.Get_Min_Volt_Level();
                				mNano_dev.Set_Start_Calibration ( 0 );
                				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                    			try {
                    				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                    				sleep ( 10 );
                        			mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                    				sleep ( 10 );
    								while ( mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
    									sleep ( 10 );
    									if ( mNano_dev.is_dev_busy == 0 ) {
    										//Log.d ( Tag, "MN913A device not busy");
    										break;
    									}
    								}
    								Log.d ( Tag, "Getting MN913A status finish");
    							} catch (InterruptedException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							}
                				
                    			if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                    				channel_sample1.set_channel_raw_data ( composite_raw_data );
                    				//I_sample1 = Math.abs ( channel_sample1.ch2_xenon_mean - channel_sample1.ch2_no_xenon_mean );
                    				//I_blank1 = ( double ) mNano_dev.Get_Min_Voltage_Intensity(); 
                    			}
                    			
                				Cur_Voltage_Level = mNano_dev.Get_Max_Volt_Level();
                				mNano_dev.Set_Start_Calibration ( 0 );
                				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
                			  }
                			  }
                	    	  /*checkpoint*/
                			  OD_Calculate ();
                		  }
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if ( alert_dlg.isShowing( ) )
									alert_dlg.dismiss ( );
								if ( measure_mode < MEASURE_MODE_PROTEIN ) {
							    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
							    sw.setEnabled( true );
							    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
							    sw1.setEnabled( true );
								}
							    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
							    sw2.setEnabled( true );
							    if ( sw2.isChecked() == false ) {
									NanoActivity.this.findViewById( R.id.imageButton1 ).setEnabled( true );
									NanoActivity.this.findViewById( R.id.imageButton2 ).setEnabled( true );
							    }
							    
								View decorView = getWindow().getDecorView();
								// Hide both the navigation bar and the status bar.
								// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
								// a general rule, you should design your app to hide the status bar whenever you
								// hide the navigation bar.
								int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
								decorView.setSystemUiVisibility(uiOptions);
							}
                			  
                		  });
                		  Thread_Sync_By_Obj.unlock();
                		  break;
                	}
                }
            };

            Looper.loop();
        }
    }
    
    public void check_blank () {
    	
    	
    }
    
    class DNA_measure_data {
    	int index;
    	double A260, A230, A280, A320, Conc;
    	boolean include_A320;
    }

    class Protein_measure_data {
    	int index;
    	double A280, coefficient, Conc;
    }
    
    LinkedList<DNA_measure_data> dna_data_list = new LinkedList <DNA_measure_data>();
    LinkedList<Protein_measure_data> protein_data_list = new LinkedList <Protein_measure_data>();
    static final double dsDNA_CONC_FACTOR = 50;
    static final double ssDNA_CONC_FACTOR = 33;
    static final double RNA_CONC_FACTOR = 40;
    public void OD_Calculate () {
    	double I_blank, I_sample, I_blank1, I_sample1;
    	DNA_measure_data dna_data = new DNA_measure_data ();
    	Protein_measure_data protein_data = new Protein_measure_data ();
    	Message msg;
    	double Transmission_rate = 0;
    	
    	switch ( measure_mode ) {
    	case MEASURE_MODE_dsDNA:
    	case MEASURE_MODE_ssDNA:
    	case MEASURE_MODE_RNA:
    		//channel_blank.ch1_xenon_mean - channel_blank.ch1_no_xenon_mean
    		//channel_blank.ch2_xenon_mean - channel_blank.ch2_no_xenon_mean
    		//channel_blank.ch3_xenon_mean - channel_blank.ch3_no_xenon_mean
    		//channel_blank.ch4_xenon_mean - channel_blank.ch4_no_xenon_mean
    		I_blank = Math.abs( channel_blank.ch2_xenon_mean - channel_blank.ch2_no_xenon_mean );
    		I_sample = Math.abs ( channel_sample.ch2_xenon_mean - channel_sample.ch2_no_xenon_mean );
    		if ( I_blank != 0 )
    			Transmission_rate = I_sample / I_blank;
    		if ( I_sample != 0) {
    			dna_data.A260 = Math.log( I_blank / I_sample ) / Math.log(10);
    			if ( dna_data.A260 == 0)
    				dna_data.A260 = -1;
    		}
    		else
    			dna_data.A260 = -1;
    		if ( 0.063 <= Transmission_rate && Transmission_rate <= 0.944 ) {
    			//conc=25~1200, linear equation
    			dna_data.Conc = coeff_k1 * dna_data.A260 + coeff_k2;
    			//if ( measure_mode == MEASURE_MODE_dsDNA )
    				//dna_data.Conc = 1047.2 * dna_data.A260 - 11.606;
    				//dna_data.Conc = 1278 * dna_data.A260 - 8.957;
    				//dna_data.Conc = 1219 * dna_data.A260 - 13.48;
    				//dna_data.Conc = dna_data.Conc * 1; 
        		//else
        			//if ( measure_mode == MEASURE_MODE_ssDNA )
        				//dna_data.Conc = 1047.2 * ( 33 / 50 ) * dna_data.A260 - 11.606;
        				//dna_data.Conc = 1278 * dna_data.A260 * ( 33 / 50 ) - 8.957;
        				//dna_data.Conc = dna_data.Conc * ( 33 / 50 );
        			//else
        				//if ( measure_mode == MEASURE_MODE_RNA )
        					//dna_data.Conc = 1047.2 * ( 4 / 5 ) * dna_data.A260 - 11.606;
        					//dna_data.Conc = 1278 * dna_data.A260 * ( 4 / 5 ) - 8.957;
        					//dna_data.Conc = dna_data.Conc * ( 4 / 5 );
    		}
    		else 
    			if ( 0.063 > Transmission_rate ) {
					//conc more than 1200, non-linear
    				//dna_data.Conc = -13903 * dna_data.A260 * dna_data.A260 + 37750 * dna_data.A260 - 23585;
    				//dna_data.Conc = 3821 * dna_data.A260 * dna_data.A260 - 5209 * dna_data.A260 + 2965;
    				dna_data.Conc = coeff_k3 * dna_data.A260 * dna_data.A260 + coeff_k4 * dna_data.A260 + coeff_k5;
    			}
    			else
    				if ( 0.944 < Transmission_rate ) {
        				//conc less than 25, non-linear, search minima voltage level and measure then reset
        				I_sample1 = Math.abs ( channel_sample1.ch2_xenon_mean - channel_sample1.ch2_no_xenon_mean );
        				I_blank1 = ( double ) mNano_dev.Get_Min_Voltage_Intensity();
        				if ( I_sample1 != 0) {
        	    			//dna_data.A260 = ( Math.abs( dna_data.A260 ) + Math.abs( Math.log( I_blank1 / I_sample1 ) / Math.log(10) ) ) / 2;
        					dna_data.Conc = coeff_k1 * Math.abs( dna_data.A260 );
        					dna_data.Conc += coeff_k1 * Math.abs( Math.log( I_blank1 / I_sample1 ) / Math.log(10));
        					dna_data.Conc = dna_data.Conc / 2;
        	    			//dna_data.Conc = 1000 * dna_data.A260;
        				}
    				}
			if ( measure_mode == MEASURE_MODE_ssDNA )
				dna_data.Conc = dna_data.Conc * ( 33 / 50 );
			else
				if ( measure_mode == MEASURE_MODE_RNA )
					dna_data.Conc = dna_data.Conc * ( 4 / 5 );
    					
    		I_blank = channel_blank.ch1_xenon_mean - channel_blank.ch1_no_xenon_mean;
    		I_sample = channel_sample.ch1_xenon_mean - channel_sample.ch1_no_xenon_mean;
    		if ( I_sample != 0) {
    			dna_data.A280 = Math.log( I_blank / I_sample ) / Math.log(10);
    			if ( dna_data.A280 == 0)
    				dna_data.A280 = -1;
    		}
    		else
    			dna_data.A280 = -1;
    		dna_data.A280 = dna_data.A280;
    		I_blank = channel_blank.ch3_xenon_mean - channel_blank.ch3_no_xenon_mean;
    		I_sample = channel_sample.ch3_xenon_mean - channel_sample.ch3_no_xenon_mean;
    		if ( I_sample != 0) {
    			dna_data.A230 = Math.log( I_blank / I_sample )  / Math.log(10);
    			if ( dna_data.A230 == 0)
    				dna_data.A230 = -1;
    		}
    		else
    			dna_data.A230 = -1;
    		dna_data.A230 = dna_data.A230;

    		I_blank = channel_blank.ch4_xenon_mean - channel_blank.ch4_no_xenon_mean;
    		I_sample = channel_sample.ch4_xenon_mean - channel_sample.ch4_no_xenon_mean;
    		if ( I_sample != 0) {
    			dna_data.A320 = Math.log( I_blank / I_sample )  / Math.log(10);
    			if ( dna_data.A320 == 0)
    				dna_data.A320 = -1;
    		}
    		else
    			dna_data.A320 = -1;
    		dna_data.A320 = dna_data.A320;
    		/*if ( measure_mode == MEASURE_MODE_dsDNA )
    			dna_data.Conc = dna_data.A260 * dsDNA_CONC_FACTOR;
    		else
    			if ( measure_mode == MEASURE_MODE_ssDNA )
    				dna_data.Conc = dna_data.A260 * ssDNA_CONC_FACTOR;
    			else
    				if ( measure_mode == MEASURE_MODE_RNA )
    					dna_data.Conc = dna_data.A260 * RNA_CONC_FACTOR;*/
    		dna_data.index = dna_data_list.size();
    		Switch sw = ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
    		dna_data.include_A320 = sw.isChecked();
    		dna_data_list.add( dna_data );
    		nano_database.InsertDNADataToDB( dna_data );
			//table.gvUpdatePageBar("select count(*) from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
		    //table.gvReadyTable("select * from " + NanoSqlDatabase.DNA_VALUE_TABLE_NAME, nano_database.get_database());
		    //table.refresh_last_table();
		    msg = this.mHandler.obtainMessage( this.UPDATE_DNA_RESULT_UI, dna_data );
		    msg.sendToTarget ( );
    		break;
    	case MEASURE_MODE_PROTEIN:
    		I_blank = channel_blank.ch1_xenon_mean - channel_blank.ch1_no_xenon_mean;
    		I_sample = channel_sample.ch1_xenon_mean - channel_sample.ch1_no_xenon_mean;
    		if ( I_sample != 0)
    			protein_data.A280 = 24.38 * Math.log( I_blank / I_sample )  / Math.log(10);
    		else
    			protein_data.A280 = 0;
    		protein_data.index = protein_data_list.size();
    		if ( Protein_quantity_mode == 0 ) {
    			protein_data.coefficient = -1;
    		}
    		else
    			if ( Protein_quantity_mode > 0 ) {
    				protein_data.coefficient = Protein_quantity_coefficient [ Protein_quantity_mode -1 ];
    				protein_data.Conc = protein_data.A280 * protein_data.coefficient; 
    			}
    		protein_data_list.add( protein_data );
    		nano_database.InsertPROTEINDataToDB( protein_data );
		    msg = this.mHandler.obtainMessage( this.UPDATE_PROTEIN_RESULT_UI, protein_data );
		    msg.sendToTarget ( );
    		break;
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1023) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            	Log.d ( Tag, "onActivityResult" );
            }
        }
        else
        	if (requestCode == 1025) {
        		Log.d ( Tag, "onActivityResult: 1025" );
        	}
        	super.onActivityResult ( requestCode, resultCode, data );
    }
    
    /*checkpoint*/
    protected void Start_Monitor_AutoMeasure_Thread() {
    	this.polling_data_executor.execute(this.Run_Auto_Measure);
    }
    
    /*checkpoint*/
    protected void Stop_Monitor_AutoMeasure_Thread() {
		try {
			this.polling_data_executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*checkpoint*/
    Runnable Run_Auto_Measure = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while ( Cur_Auto_Measure == true ) {
				mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
				if ( mNano_dev.AutoMeasure_Detected == 1 ) {
				  Log.d ( Tag, "Detect Auto Measure!" );
				  /*NanoActivity.this.runOnUiThread( new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						  alert_message = "Sample Measuring!";
						  alert_dlg.setMessage( alert_message );
						  alert_dlg.show();						
					}
					  
				  });*/
				  msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
				  msg.sendToTarget ( );
				  Thread_Sync_By_Obj.lock();
				}

				try {
					Thread.sleep(650);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}			  
			}
			Log.d ( Tag, "Exit Auto Measure!" );
		}
    	
    };
    
    public class Thread_sync {
        // -1 表示目前沒有產品
        private int product = -1; 
     
        // 這個方法由生產者呼叫
        public synchronized void lock( ) {
        	try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } 
        
        // 這個方法由消費者呼叫
        public synchronized void unlock() {
        	notify ( );
        } 
    }
    
    public void Exit_App ( View v ) {
    	finish ();
    }
    
    public void onBackPressed() {
    	ImageButton btn_home;
    	btn_home = ( ImageButton ) findViewById( R.id.main_home );
    	
    	btn_home.setPressed(true);
    	btn_home.invalidate();
    	btn_home.performClick();
    }
    
    /*20160530 added by michael*/
	AdapterView.OnItemSelectedListener Protein_quantity_selection = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d(Tag, "spinner selection");
			//Cur_Pipetting_Mode = position;
			if ( Protein_quantity_mode == 4 ) {
				Protein_quantity_coefficient [ Protein_quantity_mode - 1 ] = Double.parseDouble( ed_protein_quantity.getText().toString().trim() );
			}
			
			if ( position == 0 )
				ed_protein_quantity.setVisibility( View.INVISIBLE );
			else
				ed_protein_quantity.setVisibility( View.VISIBLE );
			switch ( position ) {
/*	        <item>UNKNOWN</item>
	        <item>BSA</item>
	        <item>IgG</item>
	        <item>Lysozyme</item>
	        <item>User</item>*/
			case 0: //UNKNOWN
				break;
			case 1: //BSA
				ed_protein_quantity.setEnabled( false );
				ed_protein_quantity.setText( Double.toString( Protein_quantity_coefficient [ position - 1 ] ).trim() );
				break;
			case 2: //IgG
				ed_protein_quantity.setEnabled( false );
				ed_protein_quantity.setText( Double.toString( Protein_quantity_coefficient [ position - 1 ] ).trim() );
				break;
			case 3: //Lysozyme
				ed_protein_quantity.setEnabled( false );
				ed_protein_quantity.setText( Double.toString( Protein_quantity_coefficient [ position - 1 ] ).trim() );
				break;
			case 4: //User
				ed_protein_quantity.setEnabled( true );
				ed_protein_quantity.setText( Double.toString( Protein_quantity_coefficient [ position - 1 ] ).trim() );
				break;
			}
			Protein_quantity_mode = Cur_Protein_quantity_mode = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			Log.d(Tag, "spinner no selection");
		}
		
	};
	
	public int save_calibration_data () {	
		int ret = 0;
        
		cali_write_file = new FileOperateObject ( "misc", "calibration_result" );
		try {
			cali_write_file.create_file( cali_write_file.generate_filename_no_date() );
			cali_write_file.write_file( Calibration_Data_List );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return ret;
	}
	
	/*@Override
	protected Dialog onCreateDialog(Bundle id) {
	        switch (id) {
	        
	        }
			return null;
	}*/
	
	public void hide_system_navigation () {
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
	}
	
	public void sync_RTC_Android_systime () {
		byte [] datetime_data = new byte [ 256 ];
		int [] datetime_data_int = new int [ 256 / 4 ];
		
		if ( mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_GET_TIME, 0, 1, datetime_data, 0) ) {
			ByteBuffer.wrap ( datetime_data ).order( ByteOrder.LITTLE_ENDIAN ).asIntBuffer().get( datetime_data_int );
			Log.d ( Tag, "year: " + Integer.toString( datetime_data_int [ 0 ] ) );
			Log.d ( Tag, "month: " + Integer.toString( datetime_data_int [ 1 ] ) );
			Log.d ( Tag, "dayofmonth: " + Integer.toString( datetime_data_int [ 2 ] ) );
			Log.d ( Tag, "hour: " + Integer.toString( datetime_data_int [ 3 ] ) );
			Log.d ( Tag, "minute: " + Integer.toString( datetime_data_int [ 4 ] ) );
			Log.d ( Tag, "second: " + Integer.toString( datetime_data_int [ 5 ] ) );
			calendar.setTimeInMillis ( 0 );
			calendar.set( datetime_data_int [ 0 ], datetime_data_int [ 1 ] - 1, datetime_data_int [ 2 ], datetime_data_int [ 3 ], datetime_data_int [ 4 ], datetime_data_int [ 5 ] );
			//exec_shell_command ( "date -s" + dateformat.format( calendar.getTime() ) + "; \n" );
	        try {
	          Process loProcess = Runtime.getRuntime().exec("su");
	          DataOutputStream loDataOutputStream = new DataOutputStream(loProcess.getOutputStream());
	          loDataOutputStream.writeBytes( "date -s" + dateformat.format( calendar.getTime() ) + "; \n" );
	        } catch (IOException e) {
				// TODO Auto-generated catch block
			  e.printStackTrace();
		    }
		}
	}

	public void sync_Android_RTC_systime () {
		byte [] datetime_data = new byte [ 256 ], bytes;
		int byte_offset = 0;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.YEAR ) ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.MONTH ) + 1 ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.DATE ) ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.HOUR_OF_DAY ) ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.MINUTE ) ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( calendar.get( Calendar.SECOND ) ).array();
		System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
		byte_offset = byte_offset + bytes.length;
		
		if ( mNano_dev.MN913A_IOCTL( CMD_T.HID_CMD_SET_TIME, 0, 1, datetime_data, 0 ) == true )
			Log.d ( Tag, "sccess");
	}
}