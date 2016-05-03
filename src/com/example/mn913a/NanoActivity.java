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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.mn913a.MN_913A_Device.CMD_T;
import com.example.mn913a.file.FileOperateByteArray;
import com.example.mn913a.file.FileOperation;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;
import android.database.Cursor;
import ar.com.daidalos.afiledialog.FileChooserActivity;

@SuppressLint("NewApi") public class NanoActivity extends Activity {
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
	LinearLayout mLayout_DNA_MeasurePage, mLayout_MainPage, mLayout_SettingPage, gridlayout, mLayout_Protein_MeasurePage;
	Thread timerThread = null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd a HH:mm");
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
	
	AlertDialog alert_dlg;
	AlertDialog.Builder alert_dlg_builder;
	String alert_message = "Are you sure that you want to delete the file \'$file_name\'?";
	boolean Cur_A320_Involve = true, Cur_Led_Onoff_State = false, Cur_Auto_Measure = false;
	Thread Monitor_Auto_Measure_thread = null;
	ThreadPoolExecutor polling_data_executor = null;
	Thread_sync Thread_Sync_By_Obj;
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
		
		ImageButton imageButton1, imageButton2, imageButton3, imageButton4, btn_protein, btn_analysis, btn_calibration;
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
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	alert_message = "calibration device & blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

						    /*checkpoint*/
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	alert_message = "blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

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
					    alert_message = "sample measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();
					    
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
				              | View.SYSTEM_UI_FLAG_FULLSCREEN;
				decorView.setSystemUiVisibility(uiOptions);
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
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	alert_message = "calibration device & blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

						    /*checkpoint*/
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	alert_message = "blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

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
					    alert_message = "sample measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );

					}
					
				});
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
					    Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
					    sw.setEnabled( false );
					    Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
					    sw1.setEnabled( false );
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	alert_message = "calibration device & blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

						    /*checkpoint*/
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	alert_message = "blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

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
					    alert_message = "sample measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );
					}
					
				});
			}
			
		});
		btn_protein = ( ImageButton ) findViewById( R.id.imageButton5 );
		btn_protein.setOnClickListener( new OnClickListener() {
			ImageButton btn_blank, btn_sample;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
					    if ( mNano_dev.Has_Calibration == 0 ) {
					    	alert_message = "calibration device & blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

						    /*checkpoint*/
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
						    msg.sendToTarget ( );
						    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_BLANK );
						    msg.sendToTarget ( );
					    }
					    else {
					    	alert_message = "blank measuring!";
							alert_dlg.setMessage( alert_message );
						    alert_dlg.show();

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
					    Switch sw2 = ( Switch ) NanoActivity.this.findViewById( R.id.Auto_measure_switch );
					    sw2.setEnabled( false );
					    alert_message = "sample measuring!";
						alert_dlg.setMessage( alert_message );
					    alert_dlg.show();
					    
					    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_MEASURE_SAMPLE );
					    msg.sendToTarget ( );
					}
					
				});
			}
			
		});
		btn_calibration = ( ImageButton ) findViewById( R.id.imageButton6 );
		btn_calibration.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		    	alert_message = "calibration device!";
				alert_dlg.setMessage( alert_message );
			    alert_dlg.show();

			    /*checkpoint*/
			    msg = mWorker_thread_handler.obtainMessage( EXPERIMENT_CALIBRATION_DEVICE );
			    msg.sendToTarget ( );				
			}
			
		});
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
		metrics = new DisplayMetrics();
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
    	}
    	else {
    		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics((metrics));
    	}
		
		//adjust_ui_dimension ( ( ViewGroup ) this.findViewById( R.id.top_ui ) );
    	mLayout_MainPage = (LinearLayout) findViewById(R.id.top_ui);
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
		
		write_file.write_file ( file_name, true );
		switch ( measure_mode ) {
		case MEASURE_MODE_dsDNA:
		case MEASURE_MODE_ssDNA:
		case MEASURE_MODE_RNA:
			for ( DNA_measure_data dna_data: dna_data_list ) {
				measure_result = Integer.toString( dna_data.index ) + ", " + NanoSqlDatabase.truncateDecimal(  dna_data.Conc, 3 ).doubleValue() +  ", " +
						NanoSqlDatabase.truncateDecimal(  dna_data.A260 * 25.56, 3 ).doubleValue() +  ", " +
						NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 ) / ( dna_data.A230 * 167 ), 3 ).doubleValue() + ", " +
						NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 )/ ( dna_data.A280 * 23 ), 3 ).doubleValue();
				write_file.write_file ( measure_result, true );
			}
			break;
		case MEASURE_MODE_PROTEIN:
			for ( Protein_measure_data protein_data: protein_data_list ) {
				measure_result = Integer.toString( protein_data.index ) + ", " + NanoSqlDatabase.truncateDecimal(  protein_data.A280, 3 ).doubleValue();
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
		
		mLayout_Content.removeAllViews ( );
		if ( mLayout_MainPage == null )
			mLayout_MainPage = ( LinearLayout ) inflater.inflate( R.layout.activity_main1, null );
		mLayout_Content.addView( mLayout_MainPage );
		
		measure_mode = 0;
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
				Cur_Led_Onoff_State = isChecked;
				if ( isChecked )
					mNano_dev.Set_Illumination_State ( 1 );
				else
					mNano_dev.Set_Illumination_State ( 0 );
				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
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
				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
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
	}
	
	public void switch_to_protein_measure_page ( ) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_Protein_MeasurePage == null) {
			mLayout_Protein_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.protein_measure, null );
		}
		mLayout_Content.addView( mLayout_Protein_MeasurePage );
		
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
	}
	
	public void switch_to_setting_page ( View v) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_SettingPage == null )
			mLayout_SettingPage = ( LinearLayout ) inflater.inflate( R.layout.setting, null );
		mLayout_Content.addView( mLayout_SettingPage );
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
			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			mNano_dev.Set_Start_Calibration ( 0 );
			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
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
    	/*show_system_bar();
    	mNano_dev.Set_Illumination_State ( 0 );
		mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);*/
    }
    
    @Override
    protected void onStart() {
    	super.onStart();

    	/*if (mRequest_USB_permission==false)
    		hide_system_bar();
    	EnumerationDevice(getIntent());*/
    	//adjust_ui_dimension ( ( ViewGroup ) this.findViewById( R.id.top_ui ) );
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
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
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView3 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 19 ) / ( dna_data.A280 * 23 ), 3 ).doubleValue() ) );
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView4 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  ( dna_data.A260 * 210 ) / ( dna_data.A230 * 167 ), 3 ).doubleValue() ) );
    		    ( ( TextView ) NanoActivity.this.findViewById( R.id.textView5 ) ).setText( Double.toString( NanoSqlDatabase.truncateDecimal(  dna_data.A260 * 25.56, 3 ).doubleValue() ) );
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
    	double Transmission_rate = 0, I_blank = 0.0, I_sample = 0.0;
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
                		  /*checkpoint*/
                		  mNano_dev.Set_Start_Calibration ( 1 );
              			  mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
              			  mNano_dev.Set_Start_Calibration ( 0 );
              			  try {
							sleep ( 3000 );
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
              			  while ( mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
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
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if ( alert_dlg.isShowing( ) && alert_message.equals( "calibration device!"  ) )
									alert_dlg.dismiss ( );
							}
                		  });
                		  break;
                	  case EXPERIMENT_MEASURE_BLANK:
                		  if ( Is_MN913A_Online == true ) {
              				Cur_Voltage_Level = mNano_dev.Get_Max_Volt_Level();
              				mNano_dev.Set_Start_Calibration ( 0 );
              				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                			try {
                  				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                  				sleep ( 10 );
                    			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                				//sleep ( 3000 );
								while ( mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
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
                		  
                		  if ( mNano_dev.Itracker_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
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
									Switch sw1= ( Switch ) NanoActivity.this.findViewById( R.id.Led_switch );
									sw1.setEnabled( true );
								}

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
							}
                			  
                		  });
                		  //Thread_Sync_By_Obj.unlock();
                		  break;
                		  
                	  case EXPERIMENT_MEASURE_SAMPLE:
                		  if ( Is_MN913A_Online == true ) {
                			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                			try {
                				sleep ( 10 );
								while ( mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
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
                		  
                		  if ( mNano_dev.Itracker_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
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
                    				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 0);
                    				sleep ( 10 );
                        			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_MEASURE, 0, 0, null, 0);
                    				sleep ( 10 );
    								while ( mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0) ) {
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
                				
                    			if ( mNano_dev.Itracker_IOCTL ( CMD_T.HID_CMD_MN913A_RAW_DATA, 0, 16, composite_raw_data, 0) ) {
                    				channel_sample1.set_channel_raw_data ( composite_raw_data );
                    				//I_sample1 = Math.abs ( channel_sample1.ch2_xenon_mean - channel_sample1.ch2_no_xenon_mean );
                    				//I_blank1 = ( double ) mNano_dev.Get_Min_Voltage_Intensity(); 
                    			}
                    			
                				Cur_Voltage_Level = mNano_dev.Get_Max_Volt_Level();
                				mNano_dev.Set_Start_Calibration ( 0 );
                				mNano_dev.Set_Xenon_Voltage_Level ( Cur_Voltage_Level );
                				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
                			  }
                			  }
                	    	  /*checkpoint*/
                			  OD_Calculate ();
                		  }
                		  NanoActivity.this.runOnUiThread( new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								NanoActivity.this.findViewById( R.id.imageButton1 ).setEnabled( true );
								NanoActivity.this.findViewById( R.id.imageButton2 ).setEnabled( true );
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
    	double A260, A230, A280, Conc;
    }

    class Protein_measure_data {
    	int index;
    	double A280;
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
    			if ( measure_mode == MEASURE_MODE_dsDNA )
    				//dna_data.Conc = 1047.2 * dna_data.A260 - 11.606;
    				dna_data.Conc = 1278 * dna_data.A260 - 8.957;
        		else
        			if ( measure_mode == MEASURE_MODE_ssDNA )
        				//dna_data.Conc = 1047.2 * ( 33 / 50 ) * dna_data.A260 - 11.606;
        				dna_data.Conc = 1278 * dna_data.A260 * ( 33 / 50 ) - 8.957;
        			else
        				if ( measure_mode == MEASURE_MODE_RNA )
        					//dna_data.Conc = 1047.2 * ( 4 / 5 ) * dna_data.A260 - 11.606;
        					dna_data.Conc = 1278 * dna_data.A260 * ( 4 / 5 ) - 8.957;
    		}
    		else 
    			if ( 0.063 > Transmission_rate ) {
					//conc more than 1200, non-linear
    				dna_data.Conc = -13903 * dna_data.A260 * dna_data.A260 + 37750 * dna_data.A260 - 23585; 
    			}
    			else
    				if ( 0.944 < Transmission_rate ) {
        				//conc less than 25, non-linear, search minima voltage level and measure then reset
        				I_sample1 = Math.abs ( channel_sample1.ch2_xenon_mean - channel_sample1.ch2_no_xenon_mean );
        				I_blank1 = ( double ) mNano_dev.Get_Min_Voltage_Intensity();
        				if ( I_sample1 != 0) {
        	    			dna_data.A260 = ( Math.abs( dna_data.A260 ) + Math.abs( Math.log( I_blank1 / I_sample1 ) / Math.log(10) ) ) / 2;
        	    			dna_data.Conc = dna_data.A260; 
        				}
    				}
    					
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
    		/*if ( measure_mode == MEASURE_MODE_dsDNA )
    			dna_data.Conc = dna_data.A260 * dsDNA_CONC_FACTOR;
    		else
    			if ( measure_mode == MEASURE_MODE_ssDNA )
    				dna_data.Conc = dna_data.A260 * ssDNA_CONC_FACTOR;
    			else
    				if ( measure_mode == MEASURE_MODE_RNA )
    					dna_data.Conc = dna_data.A260 * RNA_CONC_FACTOR;*/
    		dna_data.index = dna_data_list.size();
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
    			protein_data.A280 = 25.56 * Math.log( I_blank / I_sample )  / Math.log(10);
    		else
    			protein_data.A280 = 0;
    		protein_data.index = protein_data_list.size();
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
				mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_STATUS, 0, 0, null, 0);
				if ( mNano_dev.AutoMeasure_Detected == 1 ) {
				  Log.d ( Tag, "Detect Auto Measure!" );
				  NanoActivity.this.runOnUiThread( new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						  alert_message = "sample measuring!";
						  alert_dlg.setMessage( alert_message );
						  alert_dlg.show();						
					}
					  
				  });
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
}