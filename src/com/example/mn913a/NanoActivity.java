package com.example.mn913a;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class NanoActivity extends Activity {
	PendingIntent mPermissionIntent;
	UsbManager mUsbManager;
	boolean mRequest_USB_permission;
	public final String Tag = "MN913_Activity";
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	MN_913A_Device mNano_dev;
	public static boolean mDebug_Nano = false;
	RelativeLayout mMain_Layout;
	ImageView connection_status_v;
	int Cur_Voltage_Level = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		
		mMain_Layout = ( RelativeLayout ) this.findViewById( R.id.ID_Main_Layout );
		connection_status_v = ( ImageView ) this.findViewById( R.id.usb_conn_logo );
		
		SeekBar seekbar1;
		seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		//seekbar1.setEnabled(Adjust_Detection_Sensitivity);
		seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
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
				seekbar_value.setText(Integer.toString(Cur_Voltage_Level) + "%");
			}
		});
		TextView seekbar_value;
		seekbar_value = (TextView) findViewById(R.id.textView1);
		seekbar_value.setText(Integer.toString(seekbar1.getProgress()) + "%");
	}

	public void EnumerationDevice(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MAIN)) {
			if (mNano_dev.Enumeration()) {
				connection_status_v.setImageResource ( R.drawable.usb_connection ); 
			}
			else {
				if (mNano_dev.isDeviceOnline()) {
					mRequest_USB_permission = true;
					mUsbManager.requestPermission(mNano_dev.getDevice(), mPermissionIntent);
				}
				else {
				}
			}
		}
    	else
    		if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (mNano_dev.Enumeration(device)) {
    				
    			}
    			else {
    				
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
						update_ui_state ( );
					}
				}
			}
			else
				if (action.equals(ACTION_USB_PERMISSION)) {
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							Log.d(Tag, "permission allowed for device "+device);
						}
					}
					else {
						Log.d(Tag, "permission denied for device " + device);
					}
					
					update_ui_state ( );
					if (mRequest_USB_permission==true) {
						//hide_system_bar();
						mRequest_USB_permission = false;
					}
				}
		}
    	
    };
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}    
}
