package com.example.mn913a;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.LinkedList;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

// Added for fixing exception in UsbManager.openDevice
import android.widget.Toast;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;
// End of added


public class MN_913A_Device {
	public final String Tag = "Nano_Device";
	// Added for fixing exception in UsbManager.openDevice
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	private final boolean DEBUG = false;
	private boolean mReceiverRegistered = false;
	private boolean mGotUsbPermission = false;
	private boolean mReceiverReceived = false; 
	//private BroadcastReceiver mUsbReceiver = null;
	// End of added
	public FileOutputStream fos;
	private UsbManager mManager;
	private Context mContext;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private UsbInterface mInterface;
    
    public static final int MN913A_USB_VID = 0x0416;
    public static final int MN913A_USB_PID = 0x5023;
    public static final int MN913A_USB_class = 0xff;
    public static final int MN913A_USB_subclass = 0x00;
    public static final int MN913A_USB_protocol = 0x00;
    UsbEndpoint mEndpointOut = null;
    UsbEndpoint mEndpointIn = null;
    // pool of requests for the OUT endpoint
    private final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();
    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();
	
    IntBuffer MN913A_dev_data;
    public int is_dev_busy = 0, Has_Calibration = 0, AutoMeasure_Detected = 0, Invalid_Measure_Assert = 0;
    Object lock1, lock2;
    
    public int Fw_Version_Code;
	public String Fw_Version_Name, Fw_md5_checksum;
	public int Hw_Version_Code;
	public byte[] fw_header_bytes = new byte[256];
	
    private final CMD_T message;

	public void Reset_Device_Info() {
    }
	
	public MN_913A_Device(Context context) {
    	mContext = context;
    	mDevice = null;
    	mInterface = null;
    	mDeviceConnection = null;    	

    	try {
			fos = new FileOutputStream("/mnt/sdcard/debug.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    	
    	Reset_Device_Info();
    	
    	lock1 = new Object();
    	lock2 = new Object();
    	message = new CMD_T();
	}
	
    public void show_debug(String str) {
		//if (show_temp_msg==1)
			//  Toast.makeText(this.mContext.getApplicationContext(), Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber(), Toast.LENGTH_LONG).show();
    	;
		if (NanoActivity.mDebug_Nano == true) {
			try {
				fos.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
	public boolean Enumeration(UsbDevice device) {
		
		UsbInterface intf = findInterface(device);

		if (device != null && intf != null) {
			//RegisterReceiver();
			if (setInterface(device, intf))
				return true;
		}
		
/*		if (mReceiver != null) {
		  mContext.unregisterReceiver(mReceiver);
		  mReceiver = null;
		}*/
		return false;
	}
	
    public boolean Enumeration() {
		mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		for (UsbDevice device : mManager.getDeviceList().values()) {
			if (device != null) {
				if (device.getVendorId() == MN913A_USB_VID
						&& device.getProductId() == MN913A_USB_PID) {
					UsbInterface intf = findInterface(device);

					if (device != null && intf != null) {
						// RegisterReceiver();
						if (setInterface(device, intf)) {
							show_debug(Tag
									+ "The line number is "
									+ new Exception().getStackTrace()[0]
											.getLineNumber() + "\n");
							return true;
						}
					}
				}
			}

			/*
			 * UsbInterface intf = findAdbInterface(device); if
			 * (setAdbInterface(device, intf)) { break; }
			 */
		}
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
/*		if (mReceiver != null) {
			  mContext.unregisterReceiver(mReceiver);
			  mReceiver = null;
		}*/
		return false;    	
    }
    
    public UsbInterface findInterface(UsbDevice device) {
        int count = device.getInterfaceCount();

        UsbInterface intf = null;
        for (int i = 0; i < count; i++) {
            intf = device.getInterface(i);
            if (intf.getInterfaceClass()==MN913A_USB_class && intf.getInterfaceSubclass()==MN913A_USB_subclass &&
                    intf.getInterfaceProtocol()==MN913A_USB_protocol) {
            	return intf;
            }
        }
        return null;
    }
    
	private boolean setInterface(UsbDevice device, UsbInterface intf) {
		int i = 0;

		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
        if (mDeviceConnection != null) {
            if (mInterface != null) {
                mDeviceConnection.releaseInterface(mInterface);
                mInterface = null;
            }
            mDeviceConnection.close();
            mDevice = null;
            mDeviceConnection = null;
        }
        mInterface = intf;
        mDevice = device;

        show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
		if (device==null) {
			Log.d(Tag, "device not found.");
			return false;
		}
		
		show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
		if (intf==null) {
			Log.d(Tag, "interface not found.");
			return false;			
		}
		
		//mDeviceConnection
		// Modified for fixing exception in UsbManager.openDevice
		// UsbDeviceConnection connection = mManager.openDevice(device);
		do {
			if (openUsbDevice(device))
				break;
		} while(true);	
			
		if (DEBUG)
		   Log.d(Tag, "setInterface() mDeviceConnection=" + mDeviceConnection);
		UsbDeviceConnection connection = mDeviceConnection;
		// End of modified
		
        if (connection==null) {
    	  show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
          Log.d(Tag, "open connection failed");
          return false;
        }
        else {
			if (connection.claimInterface(intf, true)) {
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
				Log.d(Tag, "connection interface success");
				mDevice = device;
				mDeviceConnection = connection;
				mInterface = intf;
				
				for (i = 0; i < mInterface.getEndpointCount(); i++) {
					UsbEndpoint ep = mInterface.getEndpoint(i);
					if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
						if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
					    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
							mEndpointOut = ep;
						} else {
							show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
							mEndpointIn = ep;
						}
					}
				}
			}
			else {
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
                Log.d(Tag, "claim interface failed");
                connection.close();
                return false;
			}
        }

        return true;
	}

	public UsbDevice getDevice() {
		return mDevice;
	}
	
	public boolean isDeviceOnline() {
		if (mDevice != null && mInterface != null)
			return true;
		else
			return false;
	}
	
	public void DeviceOffline() {
		setInterface(null, null);
		mOutRequestPool.clear();
		mInRequestPool.clear();
	}
	
    // get an OUT request from our pool
    public UsbRequest getOutRequest() {
        //synchronized(mOutRequestPool) {
		UsbRequest request;
		/*
		 * 20131227 modified by michael validation the request
		 */
		if (mOutRequestPool.isEmpty()) {
			request = new UsbRequest();
			if (request.initialize(mDeviceConnection, mEndpointOut))
				return request;
			else
				return null;
		} else {
			request = mOutRequestPool.removeFirst();
			if (request.initialize(mDeviceConnection, mEndpointOut))
				return request;
			else
				return null;
		}
        //}
    }	

    // get an IN request from the pool
    public UsbRequest getInRequest() {
        //synchronized(mInRequestPool) {
		UsbRequest request;
		/*
		 * 20131227 modified by michael validation the request
		 */
		if (mInRequestPool.isEmpty()) {
			request = new UsbRequest();
			if (request.initialize(mDeviceConnection, mEndpointIn))
				return request;
			else
				return null;
		} else {
			request = mInRequestPool.removeFirst();
			if (request.initialize(mDeviceConnection, mEndpointIn))
				return request;
			else
				return null;
		} 
        //}
    }

	public boolean MN913A_IOCTL(int itracker_cmd, int arg0, int arg1, byte[] dataBytes, int debug) {
    	boolean result = false;
    	
    	synchronized(lock1) {
    		if (isDeviceOnline()) {
    			message.set(itracker_cmd, arg0, arg1, dataBytes, debug);
    			result = message.process_command(0);
    			if (itracker_cmd==CMD_T.HID_CMD_MN913A_RAW_DATA && result) {    				
    				System.arraycopy( message.mDataBuffer.array(), 0, dataBytes, 0, 4096 );
    			}
    			else
    				if (itracker_cmd==CMD_T.HID_CMD_MN913A_STATUS && result) {
    					MN913A_dev_data = message.mDataBuffer.asIntBuffer();
    					//Log.d ( Tag, "int buffer limit: " + Integer.toString( this.MN913A_dev_data.limit ( ) ));
    					is_dev_busy = MN913A_dev_data.get ( 0 );
    					Max_Volt_Level = MN913A_dev_data.get ( 1 );
    					Min_Volt_Level = MN913A_dev_data.get ( 2 );
    					Max_Voltage_Intensity = MN913A_dev_data.get ( 3 );
    					Min_Voltage_Intensity = MN913A_dev_data.get ( 4 );
    					Has_Calibration = MN913A_dev_data.get ( 5 );
    					AutoMeasure_Detected = MN913A_dev_data.get ( 6 );
    					Invalid_Measure_Assert = MN913A_dev_data.get ( 7 );
    				}
    				else
    					if ( itracker_cmd == CMD_T.HID_CMD_GET_TIME && result ) {
    						System.arraycopy( message.mDataBuffer.array(), 0, dataBytes, 0, 256 );
    					}
    					else
    						if (itracker_cmd==CMD_T.HID_CMD_MN913_FW_HEADER && result) {
    							Log.d ( Tag, "??  HID_CMD_ITRACKER_FW_HEADER");
    							StringBuffer sb = new StringBuffer("");
    							byte b;
    							message.mDataBuffer.get(dataBytes, 0, (arg1-arg0)*PAGE_SIZE);
    							System.arraycopy(dataBytes, 0, fw_header_bytes, 0, (arg1-arg0)*PAGE_SIZE);
    							message.mDataBuffer.order(ByteOrder.LITTLE_ENDIAN);				

    							message.mDataBuffer.position(0);
    							Fw_Version_Code = message.mDataBuffer.getInt();
    							System.out.println("?Fw_Version_Code?????    =======");
    							Log.d ( Tag, "??  Fw_Version_Code" + Fw_Version_Code);
    							System.out.println("?Fw_Version_Code?????    =======");
    							message.mDataBuffer.position(4);
    							for (int i = 0; i < 48; i++) {
    								b = message.mDataBuffer.get();
    								if (b!=0x00)
    									sb.append(new String(new byte []{b}));
    								else
    									break;
    							}
    							Fw_Version_Name = sb.toString();
    							System.out.println("???????    =======");
    							System.out.println(Fw_Version_Name);
    							System.out.println("???????    =======");
    							sb.delete(0, sb.length());
    							message.mDataBuffer.position(56);
    							Hw_Version_Code = message.mDataBuffer.getInt();
    							message.mDataBuffer.position(60);
    							for (int i = 0; i < 16; i++) {
    								sb.append(Integer.toString((message.mDataBuffer.get() & 0xff) + 0x100, 16).substring(1));
    							}
    							this.Fw_md5_checksum = sb.toString();
    						}
    			if (result)
    				return true;
    			else
    				return false;
    		}
    		else
    			return false;
    	}
	}
	
	public void Set_Start_Calibration ( int start )
	{
		start_calibration = start;
	}
	
	public void Set_Xenon_Voltage_Level ( int level )
	{
		Xenon_Voltage_Level = level;
	}
	
	public void Set_Illumination_State ( int On_Off )
	{
		Illumination_State = On_Off;
	}
	
	public int Get_Max_Volt_Level ( )
	{
		return Max_Volt_Level;
	}
	
	public int Get_Min_Volt_Level ( )
	{
		return Min_Volt_Level;
	}

	public int Set_Max_Volt_Level ( int Max_Level )
	{
		Max_Volt_Level = Max_Level;
		return Max_Volt_Level;
	}
	
	public int Set_Min_Volt_Level ( int Min_Level )
	{
		Min_Volt_Level = Min_Level;
		return Min_Volt_Level;
	}

	public double Get_Max_Voltage_Intensity ( )
	{
		return ( (double) Max_Voltage_Intensity );
	}
	
	public double Get_Min_Voltage_Intensity ( )
	{
		return  ( (double) Min_Voltage_Intensity );
	}
	
	public double Set_Max_Voltage_Intensity ( float Max_Intensity )
	{
		Max_Voltage_Intensity = ( int ) Max_Intensity;
		return ( (double) Max_Voltage_Intensity );
	}
	
	public double Set_Min_Voltage_Intensity ( float Min_Intensity )
	{
		Min_Voltage_Intensity = ( int ) Min_Intensity;
		return  ( (double) Min_Voltage_Intensity );
	}
	
	public void Set_Auto_Measure ( int auto_measure )
	{
		Auto_Measure = auto_measure;
	}
	
	public void Set_Reset_MCU ( int reset )
	{
		Reset_MCU = reset;
	}
	/*public void Set_LCD_Brightness_Level ( int brightness )
	{
		LCD_Brightness_Level = brightness;
	}*/
	public static final int SZ_MN913A_setting_type = Integer.SIZE / Byte.SIZE;
	public static final int SZ_MN913A_status_type = Integer.SIZE / Byte.SIZE;
	public static final int SZ_MN913A_raw_data_type = 2 * 8 * 256;
	public static final int SZ_MN913A_datetime_data_type = 24;
	/* #define PAGE_SIZE 256 */
	public static final int PAGE_SIZE = 256;
	// #define HID_CMD_SIGNATURE 0x43444948
	public static final int HID_CMD_SIGNATURE = 0x43444948;
	public static final int MAX_PAYLOAD = 4096;
	public int Xenon_Voltage_Level = 0, Illumination_State = 0, start_calibration = 0, Max_Volt_Level = 0, Min_Volt_Level = 0, Max_Voltage_Intensity = 0, Min_Voltage_Intensity = 0, Auto_Measure = 0;
	public int Reset_MCU = 0, LCD_Brightness_Level = 0;
	
	public final class CMD_T {
		public final String Tag = "Command";
		/*typedef struct {
		    unsigned char cmd;
		    unsigned char len;
		    unsigned int arg1;
		    unsigned int arg2;
			unsigned int signature;
		    unsigned int checksum;
		}CMD_T;*/
	    public byte cmd;
	    public byte len;
	    //public final static byte[] padding = new byte[2];
	    public int  arg1;
	    public int  arg2;
	    public int Signature;
	    public int Checksum;
	    public static final int SZ_CMD_T = (2*Byte.SIZE+4*Integer.SIZE) / Byte.SIZE;
	    private final ByteBuffer mMessageBuffer;
	    private final ByteBuffer mDataBuffer;
	    
	    public static final int HID_CMD_MN913A_SETTING = 0x86;
	    public static final int HID_CMD_MN913A_MEASURE = 0x87;
	    public static final int HID_CMD_MN913A_RAW_DATA = 0x88;
	    public static final int HID_CMD_MN913A_STATUS = 0x89;
	    public static final int HID_CMD_PRINT_DNA_RESULT = 0x90;
	    public static final int HID_CMD_PRINT_PROTEIN_RESULT = 0x91;
	    public static final int HID_CMD_GET_TIME = 0x92;
	    public static final int HID_CMD_SET_TIME = 0x93;
	    public static final int HID_CMD_SET_LCD_BRIGHTNESS = 0x95;
	    public static final int HID_CMD_PRINT_META_DATA = 0xC1;
	    public static final int HID_CMD_MN913_FW_UPGRADE = 0xA0 ;  //20160705 Jan
	    public static final int HID_CMD_MN913_FW_HEADER =  0xA1 ;  //20160705 Jan
	    public static final int HID_CMD_PRINTER_POWER_ON =  0xA2 ;
	    public static final int HID_CMD_PRINTER_POWER_OFF =  0xA3 ;
	    
	    public CMD_T() {
	        mMessageBuffer = ByteBuffer.allocate(SZ_CMD_T);
	        mDataBuffer = ByteBuffer.allocate(MAX_PAYLOAD);
	        mMessageBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        mDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    }

	    private boolean send_request(UsbRequest request, ByteBuffer byte_buf) {
	    	boolean queue_result; 
	    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	request.setClientData(this);
	    	queue_result = request.queue(byte_buf, byte_buf.limit());
			
	    	if (queue_result==true) {
			request = mDeviceConnection.requestWait();
			CMD_T message = (CMD_T)request.getClientData();
			if (this==message)
				return true;
	    	}
			show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
			return false;
	    }
	    
	    private boolean write_out(ByteBuffer byte_buf, int length) {
	    	boolean result = false;
	    	//byte[] write_buf;
	    	//int byte_count = 0;
	    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	//write_buf = byte_buf.array();
	    	//byte_count = mDeviceConnection.bulkTransfer(mEndpointOut, write_buf, length, 10000);
	    	//return result;
	    	UsbRequest request = getOutRequest();
	    	if (request != null) {
	    		result =  send_request(request, byte_buf);
	    		mOutRequestPool.add(request);
	    	}
	    	else
	    		result = false;
	    	return result;
	    	/*show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	request.setClientData(this);
			request.queue(byte_buf, byte_buf.limit());
			
			request = mDeviceConnection.requestWait();
			CMD_T message = (CMD_T)request.getClientData();
			if (this==message)
				return true;
			show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber());
			return false;*/
	    }

	    private boolean read_in(ByteBuffer byte_buf, int length) {
	    	boolean result = false;
	    	byte[] read_buf;
	    	int byte_count = 0;
	    	/*show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
	    	read_buf = byte_buf.array();
	    	//Log.d("knight", "buffer length: " + Integer.toString(read_buf.length));
	    	byte_count = mDeviceConnection.bulkTransfer(mEndpointIn, read_buf, length, 0);
	    	//Log.d("knight", "receive bytes " + Integer.toString(byte_count));
	    	if (byte_count != length || length==0) {
	    		Log.d("knight", "receive bytes " + Integer.toString(byte_count));
	    	    return false;
	    	}
	    	else {
	    		return true;
	    	}*/
	    	UsbRequest request = getInRequest();
	    	if (request != null) {
	    		result = send_request(request, byte_buf);
	    		mInRequestPool.add(request);
	    		byte_buf.position( 0 );
	    	}
	    	else
	    		result = false;
	    	return result;
	    }

	    public boolean process_command(int debug) {
	    	boolean result;
	    	byte [] byte_buf;

	    	result = write_out(mMessageBuffer, mMessageBuffer.limit());
			int command;
			command = (int) cmd&0xff;
			if (command != CMD_T.HID_CMD_MN913_FW_UPGRADE&& command != CMD_T.HID_CMD_PRINT_DNA_RESULT && command != CMD_T.HID_CMD_PRINT_PROTEIN_RESULT && command != CMD_T.HID_CMD_SET_TIME  
					&& command != CMD_T.HID_CMD_SET_LCD_BRIGHTNESS && command != CMD_T.HID_CMD_PRINT_META_DATA ) {
				mDataBuffer.clear();
				mDataBuffer.limit((arg2-arg1)*PAGE_SIZE);
			}
			switch(command) {
			case HID_CMD_MN913A_SETTING:
				mDataBuffer.putInt(Xenon_Voltage_Level);
				mDataBuffer.putInt(Illumination_State);
				mDataBuffer.putInt(start_calibration);
				mDataBuffer.putInt(Auto_Measure);
				mDataBuffer.putInt( Reset_MCU );
				mDataBuffer.putInt( Max_Volt_Level );
				mDataBuffer.putInt( Min_Volt_Level );
				mDataBuffer.putInt( Has_Calibration );
				mDataBuffer.putInt( Max_Voltage_Intensity );
				mDataBuffer.putInt( Min_Voltage_Intensity );
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
		    	show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		    	if (result)
					Log.d(Tag, "write data complete");
				break;
			case HID_CMD_PRINT_DNA_RESULT:
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
				break;
			case HID_CMD_PRINT_PROTEIN_RESULT:
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
				break;
			case HID_CMD_SET_TIME:
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
				break;
			case HID_CMD_SET_LCD_BRIGHTNESS:
			case HID_CMD_PRINT_META_DATA:
				//mDataBuffer.putInt( LCD_Brightness_Level );
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
				break;
			}
			
			switch(command) {
			case HID_CMD_MN913A_STATUS:
				if (result)
					result = read_in(mDataBuffer, mDataBuffer.limit());
				break;
			case HID_CMD_MN913A_RAW_DATA:
				if (result)
					result = read_in(mDataBuffer, mDataBuffer.limit());
				show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
				break;
			case HID_CMD_GET_TIME:
				if (result)
					result = read_in(mDataBuffer, mDataBuffer.limit());
				break;
			    /*20160705 added by Jan*/
			case HID_CMD_MN913_FW_HEADER:
	        	if (result)
	        		result = read_in(mDataBuffer, mDataBuffer.limit());
	        	break;
			case HID_CMD_MN913_FW_UPGRADE:
				if (result)
					result = write_out(mDataBuffer, mDataBuffer.limit());
				break;
			}
			return result;
	    }
	    
		public void set(int command, int argu0, int argu1, byte[] data, int debug) {
			int remainder,index;
			cmd = (byte) command;

			switch (command) {
			case HID_CMD_MN913A_SETTING:
				if (debug != 0)
					Log.d(Tag, ">>> Setting up MN913A preference\n");
				arg1 = 0;
				remainder = SZ_MN913A_setting_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_MN913A_setting_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_MN913A_setting_type / PAGE_SIZE);
				break;
			case HID_CMD_MN913A_MEASURE:
				if (debug != 0)
					Log.d(Tag, ">>> Starting MN913A measurement\n");
				arg1 = 0;
				arg2 = 0;
				break;
			case HID_CMD_MN913A_STATUS:
				if (debug != 0)
					Log.d(Tag, ">>> Getting MN913A status\n");
				arg1 = 0;
				remainder = SZ_MN913A_status_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_MN913A_status_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_MN913A_status_type / PAGE_SIZE);
				break;				
			case HID_CMD_MN913A_RAW_DATA:
				if (debug != 0)
					Log.d(Tag, ">>> Retrieve MN913A raw data\n");
				arg1 = 0;
				remainder = SZ_MN913A_raw_data_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_MN913A_raw_data_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_MN913A_raw_data_type / PAGE_SIZE);
				break;
			case HID_CMD_GET_TIME:
				if (debug != 0)
					Log.d(Tag, ">>> Retrieve MN913A RTC date & time\n");
				arg1 = 0;
				remainder = SZ_MN913A_datetime_data_type % PAGE_SIZE;
				if (remainder != 0)
					arg2 = (SZ_MN913A_datetime_data_type / PAGE_SIZE) + 1;
				else
					arg2 = (SZ_MN913A_datetime_data_type / PAGE_SIZE);
				break;
			
			case HID_CMD_PRINT_DNA_RESULT:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_PRINT_PROTEIN_RESULT:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_SET_TIME:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_SET_LCD_BRIGHTNESS:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_PRINT_META_DATA:
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_MN913_FW_HEADER:
				System.out.println("HID_CMD_ITRACKER_FW_HEADER LINE is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
				arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
		    case HID_CMD_MN913_FW_UPGRADE:
	    		arg1 = argu0;
				arg2 = argu1;
				mDataBuffer.clear();
				mDataBuffer.limit(arg2*PAGE_SIZE);
				mDataBuffer.put(data, 0, arg2*PAGE_SIZE);
				break;
			case HID_CMD_PRINTER_POWER_ON:
				if (debug != 0)
					Log.d(Tag, ">>> MN913A turn on printer power\n");
				arg1 = 0;
				arg2 = 0;
				break;
			case HID_CMD_PRINTER_POWER_OFF:
				if (debug != 0)
					Log.d(Tag, ">>> MN913A turn off printer power\n");
				arg1 = 0;
				arg2 = 0;
				break;
			}
			len = CMD_T.SZ_CMD_T - 4; /* Not include checksum */
			Signature = HID_CMD_SIGNATURE;
			
			mMessageBuffer.clear();
			mMessageBuffer.put(cmd);
			mMessageBuffer.put(len);
			mMessageBuffer.putInt(arg1);
			mMessageBuffer.putInt(arg2);
			mMessageBuffer.putInt(Signature);
			
			byte [] byte_buf = mMessageBuffer.array();
			Checksum = genCheckSum(byte_buf, 0, byte_buf.length-4);
			mMessageBuffer.putInt(Checksum);
		}
		
	    private int genCheckSum(byte [] buf, int start_index, int end_index) {
	    	int i = 0, sum;
	
            for (sum = 0, i = start_index; i < end_index; i++)
            	sum += (int) (buf[i] & 0xff);
            return sum;
	    }
	    
	    public void stream_debug(byte [] buf) {
	    	stream_debug(buf, 0, buf.length);
	    }

	    public void stream_debug(byte [] buf, int byteOffset, int byteCount) {
			FileOutputStream debug_out;
			try {
				debug_out = new FileOutputStream("/mnt/sdcard/output.txt");
				debug_out.write(buf, byteOffset, byteCount);
				debug_out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	// Added for fixing exception in UsbManager.openDevice
	private boolean openUsbDevice(UsbDevice device){
        //before open usb device
        //should try to get usb permission
        return tryGetUsbPermission(device);
    }
     
    private boolean tryGetUsbPermission(UsbDevice usb_device){
        mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

		if (false == mReceiverRegistered)
		{
		    if (DEBUG)
			    Log.d(Tag, "tryGetUsbPermission() register receiver");
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            //mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
            mContext.getApplicationContext().registerReceiver(mUsbPermissionActionReceiver, filter);
			mReceiverRegistered = true;
		}
		
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if (mManager.hasPermission(usb_device)) {
			if (DEBUG)
			    Log.d(Tag, "tryGetUsbPermission() usb_device has permission 1");
			afterGetUsbPermission(usb_device);
			return true;
        }
		else
		{
            //if (DEBUG)
			//    Log.d(Tag, "tryGetUsbPermission() ask user whether to allow permission");
			mManager.requestPermission(usb_device, mPermissionIntent);
			//if (DEBUG)
			//    Log.d(Tag, "tryGetUsbPermission() after mManager.requestPermission()");
			int count = 0;
			while(count < 100) {
				 if (DEBUG)
				    Log.d(Tag, "tryGetUsbPermission() ask user whether to allow permission");
				//mManager.requestPermission(usb_device, mPermissionIntent);
				if (DEBUG)
				    Log.d(Tag, "tryGetUsbPermission() after mManager.requestPermission()");
				SystemClock.sleep(500);
				 count++;
				 if (mManager.hasPermission(usb_device)) {
			        if (DEBUG)
			            Log.d(Tag, "tryGetUsbPermission() usb_device has permission 2");
			        afterGetUsbPermission(usb_device);
					return true;
				 }
				 else
				 {
					 if (DEBUG)
				            Log.d(Tag, "tryGetUsbPermission() doesn't get permission");
				 }
			}	
			return false;		    
        }
    }
     
     
    private void afterGetUsbPermission(UsbDevice usb_device){
        //call method to set up device communication
        doYourOpenUsbDevice(usb_device);
    }
     
    private void doYourOpenUsbDevice(UsbDevice usb_device){
        //now follow line will NOT show: User has not given permission to device UsbDevice
        mDeviceConnection = mManager.openDevice(usb_device);
		if (DEBUG)
		    Log.d(Tag, "doYourOpenUsbDevice() mDeviceConnection=" + mDeviceConnection);
        //add your operation code here
    }
 
    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
			if (DEBUG)
			    Log.d(Tag, "onReceive() enter");
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if(null != usbDevice){
							mGotUsbPermission = true;
                            afterGetUsbPermission(usbDevice);
                       }
                    }
                    else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        mGotUsbPermission = false;
						Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
					mReceiverReceived = true;
                }
            }
        }
    };

	// End of added
}
