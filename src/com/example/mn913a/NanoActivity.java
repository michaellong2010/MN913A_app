package com.example.mn913a;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.mn913a.MN_913A_Device.CMD_T;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Switch;

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
	LinearLayout mLayout_MeasurePage, mLayout_MainPage, mLayout_SettingPage;
	Thread timerThread = null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd a HH:mm");
	LayoutInflater inflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayout_Content = (FrameLayout) this.findViewById(android.R.id.content);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.activity_main1);

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
			
		});
		
		EnumerationDevice(getIntent());*/
		ImageButton imageButton1, imageButton2, imageButton3, imageButton4;
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
				if ( mLayout_MeasurePage == null) {
					mLayout_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.measure_main, null );
				}
				mLayout_Content.addView( mLayout_MeasurePage );
			}
		};
		imageButton1 = ( ImageButton ) findViewById( R.id.imageButton1 );
		imageButton1.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_dsdna );
			}
			
		});
		imageButton2 = ( ImageButton ) findViewById( R.id.imageButton2 );
		imageButton2.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_ssdna );
			}
			
		});
		imageButton3 = ( ImageButton ) findViewById( R.id.imageButton3 );
		imageButton3.setOnClickListener( click_listener );
		imageButton4 = ( ImageButton ) findViewById( R.id.imageButton4 );
		imageButton4.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//getResources().getText(R.string.main_title)
				switch_to_measure_page ( );
				TextView tv;
				tv = (TextView) NanoActivity.this.findViewById( R.id.main_title_id );
				tv.setText( R.string.main_title_rna );
			}
			
		});
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
	}
	
	public void switch_to_main_page ( View v ) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_MainPage == null )
			mLayout_MainPage = ( LinearLayout ) inflater.inflate( R.layout.activity_main1, null );
		mLayout_Content.addView( mLayout_MainPage );
	}
	
	public void switch_to_measure_page ( ) {
		mLayout_Content.removeAllViews ( );
		if ( mLayout_MeasurePage == null) {
			mLayout_MeasurePage = ( LinearLayout ) inflater.inflate( R.layout.measure_main, null );
		}
		mLayout_Content.addView( mLayout_MeasurePage );
		
		Switch sw= ( Switch ) NanoActivity.this.findViewById( R.id.mySwitch );
		sw.setOnCheckedChangeListener ( new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Log.d ( Tag, Boolean.toString( isChecked ) );
			}
			
		});
		sw.setChecked( true );
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
			connection_status_v.setImageResource ( R.drawable.usb_connection );
			seekbar_value.setEnabled( true );
			seekbar1.setEnabled( true );
			//mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
			Setting_Btn.setEnabled( true );
			Measure_Btn.setEnabled( true );
			mNano_dev.Itracker_IOCTL(CMD_T.HID_CMD_MN913A_SETTING, 0, 0, null, 1);
		}
		else {
			connection_status_v.setImageResource ( R.drawable.usb_disconnection );
			seekbar_value.setEnabled( false );
			seekbar1.setEnabled( false );
			Setting_Btn.setEnabled( false );
			Measure_Btn.setEnabled( false );
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
    protected void onStart() {
    	super.onStart();

    	if (mRequest_USB_permission==false)
    		hide_system_bar();
    	
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
}