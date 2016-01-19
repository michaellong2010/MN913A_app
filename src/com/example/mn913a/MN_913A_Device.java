package com.example.mn913a;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class MN_913A_Device {
	public final String Tag = "Nano_Device";
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
		UsbDeviceConnection connection = mManager.openDevice(device);
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
}
