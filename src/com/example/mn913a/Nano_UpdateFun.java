package com.example.mn913a;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
//import android.widget.ScrollView;
import android.widget.TextView;
//import ar.com.daidalos.afiledialog.FileChooserActivity;
//import ar.com.daidalos.afiledialog.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import com.example.mn913a.MN_913A_Device.CMD_T;

//import java.util.regex.Pattern;

//import com.example.demo.I_Tacker_Activity.CheckNetworkTheTimerTask;
//import com.example.demo.I_Tacker_Activity.InternalHandler;
//import com.example.demo.I_Tracker_Device.CMD_T;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.style.URLSpan;
import android.util.Log;


/*20160627 add by JanChen*/
public class Nano_UpdateFun extends Activity {
	public final String Tag = "Nano_UpdateFun";

	private ProgressDialog progress_dialog;
	public String DL_fileExtenstion, DL_filename;
	public String iTrack_Cache_Dir;
	final String Http_Repo_Host1 = "http://www.maestrogen.com/ftp/";//"https://www.google.com.tw/?gfe_rd=cr&ei=XJ-FV4_MLZSg8wWu_JqICA";
	final String Http_Repo_Host = "https://googledrive.com/host/0ByxRe22Uei-JdHloZm56Rng2QVk/";
	private String MD5_list_filename = "iTrack_md5_list.txt"; 
	public String files_MD5_list = Http_Repo_Host + MD5_list_filename;
	public String files_MD5_list1 = Http_Repo_Host1 + MD5_list_filename;//add jan
	public String app_filename = "MN913-20160627-google-repo.txt";// "ItrackerDemo-20131125-google-repo.apk";
	public String firmware_filename ="maestronano.txt";//"ads7953 .release";//ads7953.release";
	private int versionCode;
	private String versionName;
	private String [] parsed_version;
	int download_phase = -1;
	byte[] dataBytes = new byte[1024];
	byte[] dataBytes1 = new byte[1024];
	static InternalHandler  mHandler;
	private static final int Msg_Refresh_About_Dlg = 0x10;
	private static final int Msg_Upgrade_App = 0x11;
	private static final int Msg_Upgrade_Firmware = 0x12;
	private static final int Msg_Show_Upgrade_Progress = 0x13;
	private static final int Msg_Upgrade_Error = 0x14;
	private static final int Msg_Next_Download = 0x15;
	private static final int Msg_Cancel_Dlg = 0x16;
	private static final int Msg_Upgrade_UserManual = 0x17;
	private static final int Msg_Open_UserManual = 0x18;
	public String Upgrade_Error_Message;
	LinearLayout preference_dialog_layout, about_dialog_layout,open_layout;
	public TextView about_status_msg = null;
	public String user_manual_filename = "TK-01_691_USER_MANUAL.pdf";
	public Dialog preference_dialog = null, about_dialog = null;
	public boolean app_up_to_date = true, firmware_up_to_date = true, force_upgrade = false;
	public ProgressBar inderterminate_progressbar;
	private ArrayList<URL> url_list = new ArrayList<URL>();
	ThreadPoolExecutor polling_data_executor, general_task_executor;
//	I_Tracker_Device mItracker_dev; //jan mark
	FileInputStream fis;
	FileOutputStream fos;
	private Iterator URL_list_itrator;
	DownloadFilesTask current_download;
	public URL url;
	private Editor preference_editor;
	private SharedPreferences preference;
	public static final String PREFS_NAME = "MyPrefsFile";
	WindowManager.LayoutParams params;
	//iTrack_Properties app_properties;
	double menu_height_mm;
	Nano_ConnectivityReceiver network_status_receiver;
	Nano_ConnectivityReceiver.OnNetworkAvailableListener network_available_listener;
	MN_913A_Device MN_913A_Device;
	PendingIntent mPermissionIntent;
	UsbManager mUsbManager;
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	boolean mRequest_USB_permission, Is_MN913A_Online = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.main_nano);
     
       // Bundle extras = this.getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	Bundle extras = this.getIntent().getExtras();
    	mHandler= new InternalHandler();
    	//app_properties = new iTrack_Properties();
    	//app_properties.load_property();
		polling_data_executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		general_task_executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		MN_913A_Device = new MN_913A_Device(this);//jan mark
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		IntentFilter mIntentFilter;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mIntentFilter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(mReceiver, mIntentFilter);
		mRequest_USB_permission = false;
		EnumerationDevice(getIntent());
		
		preference = this.getSharedPreferences(PREFS_NAME, 0);
		preference_editor = preference.edit();
		String md5_checksum = Running_App_MD5_checksum();
		Log.d(Tag, "running app md5 checksum: " + md5_checksum);
		preference_editor.putString("local app checksum", md5_checksum);
		preference_editor.commit();
		md5_checksum = UserManual_MD5_checksum();
		preference_editor.putString("local usermanual checksum", md5_checksum);
		if ( isValidMD5( md5_checksum ) == false ) {
			preference_editor.putBoolean( "isNeedUpgrade_UserManual", true );
			Log.d( "usermanual", "md5_checksum: true, " + md5_checksum );
		}
		else {
			preference_editor.putBoolean( "isNeedUpgrade_UserManual", false );
			Log.d( "usermanual", "md5_checksum: false, " + md5_checksum );
		}
		preference_editor.commit();
		if ( preference.getBoolean( "keep_wifi_on", false ) == false) {
			turn_off_wifi();
		}
		else {
			turn_on_wifi();
		}
		progress_dialog = new ProgressDialog( this );
		progress_dialog.setMessage( "Downloading user manual: ");
		progress_dialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		progress_dialog.setIndeterminate( true );
		progress_dialog.setProgressNumberFormat(null);
		progress_dialog.setProgressPercentFormat(null);
		params = progress_dialog.getWindow().getAttributes();
		params.x = 0;
		params.y = -350;
		progress_dialog.getWindow().setAttributes( params );
		progress_dialog.setCancelable( false );
		progress_dialog.setButton( DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
      		  turn_off_wifi();
      		  if ( progress_dialog.isShowing() ) {
      			  progress_dialog.dismiss();
      		  }
			}
			
		} );
		
		PackageManager pm = getPackageManager();
    	PackageInfo pkginfo =null;
    	ApplicationInfo App_Info =null;
    	
    	try {
    		pkginfo = pm.getPackageInfo(getPackageName(), 0);
			//App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
    		App_Info = pkginfo.applicationInfo;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	versionCode = pkginfo.versionCode;
    	versionName = pkginfo.versionName;
    	parsed_version = versionName.split(Pattern.quote("."));
    	MD5_list_filename = "iTrack_md5_list.txt";
    	MD5_list_filename = MD5_list_filename.substring(0, MD5_list_filename.indexOf("."));
    	MD5_list_filename = MD5_list_filename + "_app_ver_" + parsed_version[0] + ".txt";
    	files_MD5_list = Http_Repo_Host + MD5_list_filename;
    	
    	//this.setRequestedOrientation(Integer.valueOf(app_properties.getProperty(iTrack_Properties.prop_portrait, "1")));
    	//menu_height_mm = Double.valueOf(app_properties.getProperty(iTrack_Properties.prop_viewable_height)); 

    	network_status_receiver = new Nano_ConnectivityReceiver ( this );
    	network_available_listener = new Nano_ConnectivityReceiver.OnNetworkAvailableListener() {
    		Message message;
    		//Button dlgbtn_update;
    		boolean hasNextDownload = true;

			@Override
			public void onNetworkAvailable() {
				// TODO Auto-generated method stub
				Check_Network_timerTaskPause();
				/*hasNextDownload = true; //jan mark
				if ( url_list.size() > 0 && current_download != null && url_list.get(0) == current_download.url ) {
					if (current_download.isTaskFinish) {
						url_list.remove(url_list.get(0));
						hasNextDownload = Next_Download();
					}
				}
				else
					hasNextDownload = Next_Download();
				if (about_dialog != null && about_dialog.isShowing()) {
					message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: network connection");
					message.sendToTarget();
				}
				if ( ( download_phase==1 || download_phase==3 ) && about_dialog != null && about_dialog.isShowing() && ( app_up_to_date == false || firmware_up_to_date == false ) && hasNextDownload == false ) {
				//	dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
				//	dlgbtn_update.setEnabled(true);//jan mark
					Log.d ( Tag, "upgrade button enable 1" );
				}*/
			}

			@Override
			public void onNetworkUnavailable() {
				// TODO Auto-generated method stub
				if (current_download != null) {
					current_download.cancel(true);
					current_download = null;
					if (about_dialog != null && about_dialog.isShowing()) {
						inderterminate_progressbar.setVisibility(View.INVISIBLE);
						about_dialog.setCancelable( true );
					}
				}
				if (about_dialog != null && about_dialog.isShowing()) {
				//	message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: network disconnection");
				//	message.sendToTarget();//jan mark
				}
				if ( about_dialog != null && about_dialog.isShowing()) {
				//	dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
				//	dlgbtn_update.setEnabled(false);//jan mark
						turn_on_wifi();
				}
			}
    		
    	};
    	network_status_receiver.setOnNetworkAvailableListener(network_available_listener);

    	update();

    }

    public void update(){
    	// Button dlgbtn_update;
 		download_phase = -1;
 		turn_on_wifi();
 		Check_Network_timerTaskStart();
 		TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null, textView2 = null;
 		if (about_dialog==null) {
 			about_dialog = new Dialog(Nano_UpdateFun.this, R.style.CenterDialog);
 			if (about_dialog_layout == null) 
 				about_dialog_layout = (LinearLayout) LayoutInflater.from(Nano_UpdateFun.this.getApplicationContext()).inflate(R.layout.dialog_about, null);//dialog_about xml
 			about_dialog.getWindow().setGravity(Gravity.TOP);
 			about_dialog.setContentView(about_dialog_layout);
 			about_dialog.setTitle("Update FW/APP");
 			about_dialog.setCancelable(true);
 			about_dialog.setCancelMessage(mHandler.obtainMessage(Msg_Cancel_Dlg));
 		}
 		if (about_dialog_layout != null) {
 			iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
 			iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
 			//checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
 			//checkbox1.setEnabled( false );
 			//dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);//jan mark
 			//checkbox1.setChecked(false);
 			//checkbox1.setOnCheckedChangeListener(force_upgrade_listener);
 			//checkbox1.setVisibility(View.INVISIBLE);
 			about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
 			about_status_msg.setTextColor(Color.YELLOW);

 			inderterminate_progressbar = (ProgressBar) about_dialog_layout.findViewById(R.id.progressBar1);
 			inderterminate_progressbar.setVisibility(View.INVISIBLE);
 		//	dlgbtn_update.setOnClickListener(Upgrade_listener);
 		//	dlgbtn_update.setEnabled(false);
 		
 				try {
 					url = new URL(files_MD5_list);
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				url_list.clear();
 				if ( this.url_list.isEmpty() ){
 					url_list.add(url);
 				}
    			
 			textView2 = (TextView) about_dialog_layout.findViewById(R.id.user_manual_httplink);
 			
 			textView2.setText( "user manual: http://www.maestrogen.com/ftp/i-track/user_manual.html" );

 			SpannableStringBuilder ssb = new SpannableStringBuilder();
 			
 			ssb.append( textView2.getText() );
 			
 			ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 			
 			textView2.setText(ssb, TextView.BufferType.SPANNABLE);
 			about_dialog.show();
 		}

 		preference_editor.putLong("Version Code", this.versionCode);
 		preference_editor.putString("Version Name", this.versionName);
 		preference_editor.commit();
 		Refresh_About_Dialog();
    }
    private String UserManual_MD5_checksum() {
		PackageManager pm = getPackageManager();
		ApplicationInfo App_Info =null;
    	MessageDigest md =null;
    	File f = null;
    	int nReadBytes = 0;
    	StringBuffer sb = new StringBuffer("");

		try {
			App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( App_Info != null ) {
			try {
				md = MessageDigest.getInstance("MD5");
				//f = new File ( App_Info.dataDir + "//" + user_manual_filename );
				f = new File ( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + user_manual_filename );
				if ( f.exists() ) {
					fis = new FileInputStream( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + user_manual_filename );				    
					while ((nReadBytes = fis.read(dataBytes)) != -1) {
						md.update(dataBytes, 0, nReadBytes);
					}
					fis.close();
					
				    byte[] mdbytes = md.digest();
					//convert the byte to hex format
					for (int i = 0; i < mdbytes.length; i++) {
						sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
					}
				}
			} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sb.toString();
    }
    private String Running_App_MD5_checksum() {
		PackageManager pm = getPackageManager();
		ApplicationInfo App_Info =null;
		MessageDigest md =null;
		FileInputStream fis1 = null;
		int nReadBytes = 0;
		StringBuffer sb = new StringBuffer("");

		Log.d(Tag, "Running package: " + this.getPackageName());
		try {
			App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (App_Info != null) {
			Log.d(Tag, "Source dir: " + App_Info.sourceDir);
			Log.d(Tag, "Source dir: " + App_Info.publicSourceDir);

		    try {
		    	md = MessageDigest.getInstance("MD5");
				fis1 = new FileInputStream(App_Info.sourceDir);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    
		    try {
				while ((nReadBytes = fis1.read(dataBytes)) != -1) {
				    md.update(dataBytes, 0, nReadBytes);
				}
				fis1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		   
		    byte[] mdbytes = md.digest();
		  //convert the byte to hex format
		    for (int i = 0; i < mdbytes.length; i++) {
		    	Log.d(Tag, "integer: " + Integer.toHexString((mdbytes[i])));
		    	Log.d(Tag, "integer1: " + Integer.toString(((mdbytes[i] & 0xff) + 0x100), 16).substring(1));
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    Log.d(Tag, "MD5 checksum: " + sb.toString());
		}

    	return sb.toString();
    }
    public String getAppDesc() {
    	//return "versionCode=" + Integer.toString(versionCode) + "  " + "versionName=" + versionName;
    	return Integer.toString(versionCode) + "---"  + versionName;
    }
    
    public String getFirmwareDesc() {
    	//return Integer.toString(versionCode); 
    	return Integer.toString(MN_913A_Device.Fw_Version_Code) + "---" +  MN_913A_Device.Fw_Version_Name;//jan mark
    }
    
    public boolean isValidMD5(String s) {
    	if (s!=null)
    		return s.matches("[a-fA-F0-9]{32}");
    	else
    		return false;
    }

    void Refresh_About_Dialog() {
		TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;
		CheckBox checkbox1;
	  //  Button dlgbtn_update;
	    File f;
//	    MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_HEADER, 0, 1, dataBytes, 1);
    	if (about_dialog!=null && about_dialog.isShowing()) {
			iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
			iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
			checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
			force_upgrade = checkbox1.isChecked();
			//dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);//jan mark
			//dlgbtn_update.setEnabled(false);//jan mark
			
			Properties defaultProps = new Properties();
			String server_app_md5, server_firmware_md5, local_app_md5, local_firmware_md5;
			String server_usermanual_md5, local_usermanual_md5;
			Color color;
			local_app_md5 = preference.getString("local app checksum", "");
			local_firmware_md5 = MN_913A_Device.Fw_md5_checksum; //jan mark
			local_usermanual_md5 = preference.getString("local usermanual checksum", "");
			try {
				f = new File(iTrack_Cache_Dir + this.MD5_list_filename);
				if (f.exists()) {
					fis = new FileInputStream(f);
					defaultProps.load(fis);
					fis.close();
				}
				server_app_md5 = defaultProps.getProperty("app", "");
				server_firmware_md5 = defaultProps.getProperty("firmware", "");
				server_usermanual_md5 = defaultProps.getProperty("usermanual", "");
				
				Log.d(Tag, "#app= " + server_app_md5); 
				Log.d(Tag, "local_app_md5= " + local_app_md5); 
				
				Log.d(Tag, "app Md5 found: " + Boolean.toString(isValidMD5(server_app_md5)));
				if (isValidMD5(local_app_md5)) {
					if (isValidMD5(server_app_md5)) {
						if (server_app_md5.equalsIgnoreCase(local_app_md5)) {
							iTrack_app_ver_desc.setText("Nano Pro App ver.:  " + getAppDesc() + "(up-to-date)");
							if (force_upgrade && is_internet_available()) {
								//dlgbtn_update.setEnabled(true);//jan mark
								//Log.d ( Tag, "upgrade button enable 2" );
							}
							//else//jan mark
							//	dlgbtn_update.setEnabled(false);
						}
						else {
							iTrack_app_ver_desc.setText("Nano Pro App ver.:  " + getAppDesc() + "(out-of-date)");
							if (is_internet_available() && url_list.isEmpty() ) {
								//dlgbtn_update.setEnabled(true);//jan mark
								//Log.d ( Tag, "upgrade button enable 3" );
							}
							app_up_to_date = false;
						}										
					}
					else{
						Log.d ( Tag, "#@##  Nano Pro ver.:  NO " );
						iTrack_app_ver_desc.setText("Nano Pro App ver.:  " + getAppDesc());
					}
				}
				else {
					iTrack_app_ver_desc.setText("Nano Pro App ver.:  ");
				}
					
				Log.d(Tag, "##firmware= " + server_firmware_md5);
				Log.d(Tag, "### ##  local_firmware_md5 = " + local_firmware_md5);
				Log.d(Tag, "##firmware Md5 found: " + Boolean.toString(isValidMD5(server_firmware_md5)));
				Log.d ( Tag, "###MN913 Firmware ver.:  " + getFirmwareDesc() + "()");
				if (isValidMD5(local_firmware_md5)) {
					if (isValidMD5(server_firmware_md5)) {
						if (server_firmware_md5.equalsIgnoreCase(local_firmware_md5)) {
							Log.d ( Tag, "MN913 Firmware ver.:  " + getFirmwareDesc() + "(up-to-date)");
							iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  " + getFirmwareDesc() + "(up-to-date)");
							if (force_upgrade && is_internet_available()) {
							//	dlgbtn_update.setEnabled(true);//jan mark
								//Log.d ( Tag, "upgrade button enable 4" );
							}
							//else//jan mark
							//	if (dlgbtn_update.isEnabled()==false)
							//		dlgbtn_update.setEnabled(false);
						}
						else {
							Log.d ( Tag, "MN913 Firmware ver.:  " + getFirmwareDesc() + "(out-to-date)");
							iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  " + getFirmwareDesc() + "(out-of-date)");
							if (is_internet_available() && url_list.isEmpty() ) {
							//	dlgbtn_update.setEnabled(true); //jan mark
								//Log.d ( Tag, "upgrade button enable 5" );
							}
							firmware_up_to_date = false;
						}
					}
					else {
						Log.d ( Tag, "MN913 Firmware ver.: ###########" );
						iTrack_firmware_ver_desc.setText("Nano Pro Firmware ver.:  " + getFirmwareDesc());
					}
				}
				else {
					//MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_HEADER, 0, 1, dataBytes, 1);
					//Log.d ( Tag, "MN913 Firmware ver.:  " + getFirmwareDesc() + "(up-to-date)");
					iTrack_firmware_ver_desc.setText("Nano Pro Firmware ver.:  " + getFirmwareDesc() + "fail");
				}	 //jan mark

				if ( isValidMD5( local_usermanual_md5 ) ) {
					if ( isValidMD5( server_usermanual_md5 ) ) {
						if ( server_usermanual_md5.equalsIgnoreCase( local_usermanual_md5 ) ) {							
						}
						else {
							preference_editor.putBoolean( "isNeedUpgrade_UserManual", true );
							preference_editor.commit();							
						}
					}					
				}
				else {
					preference_editor.putBoolean( "isNeedUpgrade_UserManual", true );
					preference_editor.commit();					
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	if((app_up_to_date == false) || (firmware_up_to_date == false)){
    		Upgrade_System();
    	}else{
    		Log.d ( Tag, "Do not update any \\\\\\" );
    	}
    }
    
    String get_upgrade_firmware_filename() {
    	//return  "ads7953" + ".release";
    	return firmware_filename;
    } 
    
    String get_upgrade_app_filename() {
    	app_filename = "MN913-20160627-google-repo.txt";//"ItrackerDemo-20131125-google-repo.apk";//
    	app_filename = app_filename.substring(0, app_filename.indexOf("."));
    	app_filename = app_filename + "_app_ver_" + parsed_version[0] + ".txt";
    	return app_filename;
    } 

    
    public void Upgrade_System(View v) {
    	TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;
    	//DownloadFilesTask downloadTask, downloadTask1;
    	
		iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
		iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
		about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
		about_status_msg.setText("");
		url_list.clear();

		//downloadTask = new DownloadFilesTask();
		try {
			if ((iTrack_firmware_ver_desc != null && iTrack_firmware_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				firmware_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_firmware_filename());
				url_list.add(url);
				//downloadTask.execute(url);
			}
			else
				firmware_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//downloadTask1 = new DownloadFilesTask();
		try {
			if ((iTrack_app_ver_desc != null && iTrack_app_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				app_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_app_filename());
				url_list.add(url);
				//downloadTask1.execute(url);
			}
			else
				app_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//URL_list_itrator = url_list.iterator();
		Next_Download();
    }
    
    public void Upgrade_System() {
    	TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;

		iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
		iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
		about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
		about_status_msg.setText("");
		url_list.clear();

		try {
			if ((iTrack_firmware_ver_desc != null && iTrack_firmware_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				firmware_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_firmware_filename());
				url_list.add(url);
			}
			else
				firmware_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if ((iTrack_app_ver_desc != null && iTrack_app_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				app_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_app_filename());
				url_list.add(url);
			}
			else
				app_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Next_Download();
    }
    
    public View.OnClickListener Upgrade_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			CheckBox checkbox1;
			
			v.setEnabled(false);
			checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
			
			checkbox1.setEnabled(false);
			
			Upgrade_System(v);
			about_dialog.setCancelable(false);
		}
    	
    };
    
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
		/*try {
				 Thread.sleep(3000);
				 Log.d(Tag, "?????  ###################  exec_shell_command: " );
	     } catch (Exception ex) {
	     }*/
		try {
			
			rt = Runtime.getRuntime();
			p = rt.exec(new String[] { "/system/xbin/su" });
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			InputStreamReader is_reader = new InputStreamReader ( p.getInputStream() );
			BufferedReader buf_is_reader = new BufferedReader ( is_reader );
			os.writeBytes(shell_cmd);
			os.writeBytes( "exit\n" );
			os.flush();
			p.waitFor();
			if ( p.exitValue() == 0 ) {
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
			Log.d(Tag, "???  ###################  exec_shell_command error: " );
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			Log.d(Tag, "???  ###################  exec_shell_command Exception: " );
		}
		
		return result;
	}
	Runnable upgrade_firmware_runnable = new Runnable() {
    	File f;
    	int nReadBytes, nPage;
    	byte [] b = new byte [256];
    	byte [] b1 = new byte [256];
    	byte [] byte_array = new byte [1024];
    	//CheckBox checkbox1;
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        	f = new File(iTrack_Cache_Dir + DL_filename); //jan mark
			if (f.exists()) {
				for (int j = 0; j < 1; j++) {
					Log.d(Tag, "flash programming test iteration: " + Integer.toString(j));
					try {
						fis = new FileInputStream(f);
						nReadBytes = fis.read(byte_array, 0, 256);
						System.arraycopy(byte_array, 0, b, 0, 256);
						ByteBuffer byte_buf;
						byte_buf = ByteBuffer.allocate(256);
						byte_buf = ByteBuffer.wrap(b);
						byte_buf.order(ByteOrder.LITTLE_ENDIAN);
						byte_buf.position(56);
			
						byte_buf.putInt(MN_913A_Device.Hw_Version_Code);
						byte_buf.position(0);
						int Fw_Version_Code = byte_buf.getInt();
						if ((Fw_Version_Code >= MN_913A_Device.Fw_Version_Code) || force_upgrade) {

							    System.arraycopy(MN_913A_Device.fw_header_bytes, 0, b1, 0, 256);
								byte_buf.position(255);
								byte_buf.put((byte) (b1[255] + 1));
								Log.d(Tag, "dirty byte: " + Integer.toString(b[255], 16));
							nPage = 1;
							while ((nReadBytes = fis.read(byte_array, 0, 256)) != -1) {
								if (nReadBytes == 256)
									MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_UPGRADE, nPage, 1, byte_array, 0);
								else {
									Arrays.fill(byte_array, nReadBytes, byte_array.length, (byte) 0);
									MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_UPGRADE, nPage, 1, byte_array, 0);
							    }
								nPage = nPage + 1;
								try {
									Thread.sleep(5);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							nPage = 0;
							if (MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_UPGRADE, nPage, 1, b, 0)) {
								Log.d(Tag, "write firmware bin data complete");
							}
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (fis != null)
							try {
								fis.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				}
			}
			firmware_up_to_date = true;
			Message message = null;
			if ( app_up_to_date == true && firmware_up_to_date == true ) {
				message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Refresh_About_Dlg);
				message.sendToTarget();
				message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: upgrade complete");
				message.sendToTarget();
			}
			Next_Download();
		}
	};
	/*rt = Runtime.getRuntime();
	p = rt.exec(new String[] { "/system/xbin/su" });
	DataOutputStream os = new DataOutputStream(p.getOutputStream());

*/
	
	private String getAllStoragePath() {
	    String finalPath = "";
	    try {
	        Runtime runtime = Runtime.getRuntime();
	        Process process = runtime.exec("mount");
	        InputStream inputStream = process.getInputStream();
	        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	        String line;
	        String[] pathArray = new String[4];
	        int i = 0;

	        BufferedReader br = new BufferedReader(inputStreamReader);
	        while ((line = br.readLine()) != null) {
	            String mount = "";
	            if (line.contains("secure"))
	                continue;
	            if (line.contains("asec"))
	                continue;

	            if (line.contains("fat")) {// TF card
	                String columns[] = line.split(" ");
	                if (columns.length > 1) {
	                    mount = mount.concat(columns[1] + "/someFiles");
	                    pathArray[i++] = mount;

	                    // check directory inputStream exist or not
	                    File dir = new File(mount);
	                    if (dir.exists() && dir.isDirectory()) {
	                        // do something here
	                        finalPath = mount;
	                        break;
	                    }
	                }
	            }
	        }

	        for(String path:pathArray){
	            if(path!=null){
	                finalPath =finalPath + path +"\n";
	                System.out.println("========jan==========??");
	                System.out.println(finalPath);
	                System.out.println("========jan==========??");
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return finalPath;
	}
	
	Runnable upgrade_app_runnable = new Runnable() {
		File f, f1;
		java.lang.Process p;
		int nReadBytes;
		String result = "";
		byte[] b = new byte[256];
		byte[] b1 = new byte[256];
		//CheckBox checkbox1;
		String download_dir;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
			String result;
        	f = new File(iTrack_Cache_Dir + DL_filename);
			if (f.exists()) {
			
					download_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
					f1 = new File ( download_dir );
					if ( !f1.exists() ) {
						f1.mkdirs();
					}
			    	result = exec_shell_command ( "cp " + iTrack_Cache_Dir + DL_filename + " " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename + "\n" );
			    	Log.d(Tag, "download new app result##: " + result);
			    	//exec_shell_command();
			    	//exec_shell_command( "mount -o remount,rw -t vfat /dev/block/sda1 /mnt/sdcard/internal_sd");
			    	//exec_shell_command("mount -t vfat /dev/block/sda1 /mnt/sdcard/internal_sd");
			    	//exec_shell_command(" /dev/block/sda /mnt/sdcard/media");
			    	//exec_shell_command(" /dev/block/sda1 /mnt/media1");
			    	/*exec_shell_command(" /dev/block/sda2 /mnt/media2");
	            	exec_shell_command("/dev/block/sda3 /mnt/media3");
	            	exec_shell_command("/dev/block/sda4 /mnt/media4");
	            	exec_shell_command(" /dev/block/sda5 /mnt/media5");
	            	exec_shell_command(" /dev/block/sda6 /mnt/media6");
	            	exec_shell_command(" /dev/block/sda7 /mnt/media7");*/
			    	//getAllStoragePath();
			    	result = exec_shell_command ( "pm install -r " + Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename + "\n" );
			    	Log.d(Tag, "install new app result: " + result);

			}
			app_up_to_date = true;
			Message message = null;
			if ( app_up_to_date == true && firmware_up_to_date == true ) {
		
				message = mHandler.obtainMessage(Nano_UpdateFun.Msg_Refresh_About_Dlg);
				message.sendToTarget();
				message = mHandler.obtainMessage(Nano_UpdateFun.Msg_Upgrade_Error, "status: upgrade complete");
				message.sendToTarget();
			}
			Next_Download();
		}
	};
    void upgrade_usermanual () {
		PackageManager pm = getPackageManager();
		ApplicationInfo App_Info =null;
    	File f, f1;
    	String result = "";
    	String download_dir;

		try {
			App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( App_Info != null ) {
			f = new File( iTrack_Cache_Dir + user_manual_filename );
			if ( f.exists() ) {
				//Log.d ( Tag, "cp " + iTrack_Cache_Dir + user_manual_filename + " " + App_Info.dataDir + "//" + user_manual_filename + "\n" );
				download_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
				f1 = new File ( download_dir );
				if ( !f1.exists() ) {
					f1.mkdirs();
				}
				result = exec_shell_command ( "cp " + iTrack_Cache_Dir + user_manual_filename + " " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + user_manual_filename + "\n" );
			}
		}
    };
    private class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	  case Msg_Upgrade_Error:
        		  if (about_dialog_layout == null)
        			  about_dialog_layout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_about, null);
        		  about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
        		  if (about_status_msg != null) {
        			  Log.d (Tag, (String)msg.obj);
        			  about_status_msg.setText((String) msg.obj);
        		  }
        		  break;
        	  case Msg_Show_Upgrade_Progress:
				  if (about_dialog != null && about_dialog.isShowing()) {
					  inderterminate_progressbar.setVisibility(View.VISIBLE);
					  about_dialog.setCancelable( false );
				  }
        		  //inderterminate_progressbar.setVisibility(View.VISIBLE);        	      
        	      break;
        	  case Msg_Refresh_About_Dlg:
        		  CheckBox checkbox1 = null;
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        		  checkbox1.setEnabled( true );
        		  Refresh_About_Dialog();
        		  inderterminate_progressbar.setVisibility(View.INVISIBLE);
        		  about_dialog.setCancelable(true);  				
        		  break;
        	  case Msg_Upgrade_App:
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  general_task_executor.execute(upgrade_app_runnable);
        		  break;

        	  case Msg_Upgrade_Firmware:
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  general_task_executor.execute(upgrade_firmware_runnable);
        		  break;
        	  case Msg_Cancel_Dlg:
        		  turn_off_wifi();        		  
        		  Check_Network_timerTaskPause();
        		  break;
        	  case Msg_Upgrade_UserManual:
        		  Log.d ( "usermanual", "upgrade usermanual" );
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  turn_off_wifi();
        		  upgrade_usermanual ();
        		  if ( progress_dialog.isShowing() )
        			  progress_dialog.dismiss();        		  
        		  break;
        	  case Msg_Open_UserManual:
        		  Intent browserIntent = new Intent( Intent.ACTION_VIEW );
        		  File f;
        		
        		  f = new File ( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + user_manual_filename );
        		  if ( f.exists() ) {
        			  browserIntent.setDataAndType( Uri.fromFile( f ), "application/pdf" );
            		  try {
            			  startActivity( browserIntent );
            		  }
            		  catch (ActivityNotFoundException e) {
            			//  Show_Toast_Msg ( "There are no application to open " + f.getName() ); 
            		  }
        		  }
        		  else
        			//  Show_Toast_Msg ( "Can't find user manual" + f.getName() );
        		  //browserIntent.setDataAndType(Uri.parse("https://googledrive.com/host/0ByxRe22Uei-JYk5MS1NWY3Vob2M/NUC1xx(reference_manual).pdf"), "application/pdf");
        		  break;
        	}
        }
    }
    
	void turn_on_wifi () {
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		wifi.setWifiEnabled(true);
		wifi.reassociate();
	}
	Timer connecting_network_mTimer = null;
	CheckNetworkTheTimerTask connecting_network_timertask;
	boolean is_internet_available() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork, wifi_network, mobile_network;
		wifi_network = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobile_network = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		return isConnected;
	}
	protected void Check_Network_timerTaskPause() {
		if (connecting_network_timertask != null) {
			connecting_network_timertask.cancel();
			connecting_network_timertask = null;
			connecting_network_mTimer.purge();
		}
	}
	void turn_off_wifi () {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		NetworkInfo activeNetwork;
		activeNetwork = cm.getActiveNetworkInfo();		
		boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();		
		boolean isWiFi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		
		wifi.disconnect();
		wifi.setWifiEnabled(false);
	}
	
    private class DownloadFilesTask extends AsyncTask<URL, Integer, Integer> {
    	public boolean isDownloadSuccess, isTaskFinish;
    	URL url;

    	DownloadFilesTask( URL... urls ) {
    		url = urls[0];
    	}
		@SuppressWarnings("null")
		@Override
		protected Integer doInBackground(URL... urls) {
			// TODO Auto-generated method stub			
			HttpURLConnection connection = null;
			int fileLength = -1, nRead_Bytes;
			InputStream input = null;
			OutputStream output = null;
			long totalSize = 0;
			Message message;
			 System.out.println("?????");
			 System.out.println(url);
			 System.out.println("?????");
			isDownloadSuccess = false;
			isTaskFinish = false;
			try {
				connection = (HttpURLConnection) urls[0].openConnection();
				connection .setRequestProperty("Accept-Encoding", "identity");
				connection.setInstanceFollowRedirects(true);
				connection.connect();

				DL_fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(urls[0].toString());
				DL_filename = URLUtil.guessFileName(urls[0].toString(), null, DL_fileExtenstion);
				Log.d (Tag, "DL_filename ::: " + DL_filename);
				iTrack_Cache_Dir = Nano_UpdateFun.this.getCacheDir().getPath() + "//";
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	                 Log.d(Tag, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
	                 Nano_UpdateFun.this.Upgrade_Error_Message = DL_filename + ", " + "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
	     			 message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, Upgrade_Error_Message);
					 message.sendToTarget();
	                 
	                 if (connection.getResponseCode() == 301) {
	                	 connection.setInstanceFollowRedirects(false);
	                	 String redirect_link = connection.getHeaderField("Location");
	                	 Log.d(Tag, redirect_link);
	                	 connection.connect();
	                 }
	                 else {
	                	 Log.d(Tag, "LINE 1089");
	                 }
				}
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Show_Upgrade_Progress);
		            message.sendToTarget();					
					fileLength = connection.getContentLength();
					input = connection.getInputStream();
					Log.d(Tag, "iTrack_Cache_Dir: " + iTrack_Cache_Dir + DL_filename + DL_fileExtenstion);
					output = new FileOutputStream(iTrack_Cache_Dir + DL_filename);
					
		             while ((nRead_Bytes = input.read(dataBytes1)) != -1) {
		                 totalSize += nRead_Bytes;
		                 // publishing the progress....
		                 if (fileLength > 0) // only if total length is known
		                     publishProgress((int) (totalSize * 100 / fileLength));
		                 output.write(dataBytes1, 0, nRead_Bytes);
		             }
					isDownloadSuccess = true;
				}
				else {
					isDownloadSuccess = false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isDownloadSuccess = false;
			}
			finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (connection != null)
					connection.disconnect();
	         }
			
			isTaskFinish = true;

			Log.d (Tag, "downloadfile: " + DL_filename + "isDownloadSuccess: " + Boolean.toString( isDownloadSuccess ) );
			if ( isDownloadSuccess ==true ) {
			if (DL_filename!=null && DL_filename.equals(MD5_list_filename)) {
				download_phase = 1;
				//message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: dowloand  " + MD5_list_filename + "  finish");
				message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: version check completed");
				message.sendToTarget();
				message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Refresh_About_Dlg);
	            message.sendToTarget();
			}
			else
				if (DL_filename!=null && DL_filename.equals(app_filename)) {
					download_phase = 2;
					message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: dowloand  " + app_filename + "  finish");
					message.sendToTarget();
					message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_App);
		            message.sendToTarget();					
				}
				else
					if (DL_filename!=null && DL_filename.equals(firmware_filename)) {
						download_phase = 3;
						message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: dowloand  " + firmware_filename + "  finish");
						message.sendToTarget();
						message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Firmware);
			            message.sendToTarget();
					}
					else
						if ( DL_filename != null && DL_filename.equals( user_manual_filename )) {
							download_phase = 4;
							message = mHandler.obtainMessage( Nano_UpdateFun.this.Msg_Upgrade_UserManual );
				            message.sendToTarget();
				            message = mHandler.obtainMessage( Nano_UpdateFun.this.Msg_Open_UserManual );
				            message.sendToTarget();
						}
						else {
							
							Log.d(Tag, "Fail Second\\\\\\\\\\\\\\\\");
							parsed_version = versionName.split(Pattern.quote("."));
					    	MD5_list_filename = "iTrack_md5_list.txt";
					    	MD5_list_filename = MD5_list_filename.substring(0, MD5_list_filename.indexOf("."));
					    	MD5_list_filename = MD5_list_filename + "_app_ver_" + parsed_version[0] + ".txt";
			                files_MD5_list1 = Http_Repo_Host1 + MD5_list_filename;
							try {
			 					url = new URL(files_MD5_list1);
			 				} catch (MalformedURLException e) {
			 					// TODO Auto-generated catch block
			 					e.printStackTrace();
			 				}
			 				url_list.clear();
			 				//if ( this.url_list.isEmpty() ){
			 				//	url_list.add(url);
			 				//}
							//if((app_up_to_date == false) || (firmware_up_to_date == false)){
			    		       // Upgrade_System();
							    try {
									url = new URL(Http_Repo_Host1 + get_upgrade_firmware_filename());
							        url_list.add(url);
							        System.out.println(Http_Repo_Host1);
								} catch (MalformedURLException e) {
								   // TODO Auto-generated catch block
								 e.printStackTrace();
								}
								try {
							        url = new URL(Http_Repo_Host1 + get_upgrade_app_filename());
							        url_list.add(url);
							        System.out.println(Http_Repo_Host1);
								} catch (MalformedURLException e) {
								   // TODO Auto-generated catch block
								 e.printStackTrace();
								}
								Next_Download();
			    	       // }else{
			    		   //     Log.d ( Tag, "Do not update any \\\\\\" );
			    	       // }
						}
			}

			if (isDownloadSuccess==false)
			  return -1;
			else {
				return 0;
			}
		}
		
		// This is called each time you call publishProgress()
		protected void onProgressUpdate(Integer... progress) {
			if (progress_dialog.isShowing()) {
				//progress_dialog.setIndeterminate(false);
				progress_dialog.setProgress(progress[0] + 1);
			}
		}
    }
    
    boolean Next_Download() {
    	DownloadFilesTask downloadTask;
    	
    	URL_list_itrator = url_list.iterator();
		if (URL_list_itrator.hasNext()) {
			url = (URL)URL_list_itrator.next();
	    	downloadTask = new DownloadFilesTask(url);
	    	current_download = downloadTask;			
			downloadTask.execute(url);
			return true;
		}
		return false;
    }
    
    protected class CheckNetworkTheTimerTask extends TimerTask {
		Message message;
		int remain_secconds = 45;

		@Override
		public void run() {
			if (is_internet_available() == false) {
				if ( remain_secconds == 0 ) {
					message = mHandler.obtainMessage( Nano_UpdateFun.this.Msg_Upgrade_Error, "status: no network connection");
					message.sendToTarget();
					Check_Network_timerTaskPause();
			
					if ( progress_dialog.isShowing() ) {
						progress_dialog.dismiss();
						turn_off_wifi();
					}
				}
				else {
					Log.d(Tag,"###################################status connecting to network...");
					message = mHandler.obtainMessage( Nano_UpdateFun.this.Msg_Upgrade_Error, "status connecting to network..." + Integer.toString( remain_secconds ));
					message.sendToTarget();
					remain_secconds = remain_secconds - 1;					
				}				
			}
			else {
				message = mHandler.obtainMessage(Nano_UpdateFun.this.Msg_Upgrade_Error, "status: network connection");
				message.sendToTarget();
				Check_Network_timerTaskPause();
				Next_Download();
			}			
		}
	}
    
	protected void Check_Network_timerTaskStart() {
		if ( connecting_network_mTimer == null )
			connecting_network_mTimer  = new Timer();
	    if ( connecting_network_timertask==null ) {
	    	connecting_network_timertask = new CheckNetworkTheTimerTask();
	    }
	    connecting_network_mTimer.schedule( connecting_network_timertask, 0, 1000); // wait network available for timeout duration 20 seconds
	}
	
	public void EnumerationDevice(Intent intent) {
		Log.d( Tag, "###@@MN_913A_Device  EnumerationDevice " );
		if ( intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			if (MN_913A_Device.Enumeration()) {
				Log.d( Tag, "----  jan MN_913A_Device.Enumeration" );
				//connection_status_v.setImageResource ( R.drawable.usb_connection );
				//Is_MN913A_Online = true;
				/*20160706 Jan*/
				MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_HEADER, 0, 1, dataBytes, 1);
			}
			else {
				if (MN_913A_Device.isDeviceOnline()) {
					Log.d( Tag, "MN_913A_Device.isDeviceOnline" );
					mRequest_USB_permission = true;
					mUsbManager.requestPermission(MN_913A_Device.getDevice(), mPermissionIntent);
				}
				else {
					//Is_MN913A_Online = false;
					Log.d( Tag, "MN_913A_Device.false " );
				}
			}
		}
    	else
    		if ( intent != null && intent.getAction() != null && intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			Log.d( Tag, "----------------------------------------------- " );
    			if (MN_913A_Device.Enumeration(device)) {
    			//	Is_MN913A_Online = true;
    				/*20160706 Jan*/
    				MN_913A_Device.MN913A_IOCTL(CMD_T.HID_CMD_MN913_FW_HEADER, 0, 1, dataBytes, 1);
    		    }
    			else {
    			//	Is_MN913A_Online = false;
    			}
    		}else{
    			Log.d( Tag, "))---- MN_913A_Device.isDeviceOnline" );
    		}
	//	update_ui_state ( );
	}
	//Create a broadcast receiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		
			if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
				if (MN_913A_Device != null && MN_913A_Device.getDevice() != null) {
					if (device.getProductId() == MN_913A_Device.getDevice().getProductId() && device.getVendorId() == MN_913A_Device.getDevice().getVendorId()) {
						MN_913A_Device.DeviceOffline();
						Log.d( Tag, "MN913A DETACHED" );
						MN_913A_Device.Reset_Device_Info();
					//	Is_MN913A_Online = false;
					//	update_ui_state ( );
					}
				}
			}
			else
				if (action.equals(ACTION_USB_PERMISSION)) {
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
						//	Is_MN913A_Online = true;
							Log.d(Tag, "permission allowed for device "+device);
						}
					}
					else {
					//	Is_MN913A_Online = false;
						Log.d(Tag, "permission denied for device " + device);
					}
					
					//update_ui_state ( );
					if (mRequest_USB_permission==true) {
						//hide_system_bar();
						mRequest_USB_permission = false;
					}
					
					if (mRequest_USB_permission==true) {
					//	hide_system_bar();
						mRequest_USB_permission = false;
					}
				}
		}
    	
    };
    @Override
    protected void onStart() {
    	super.onStart();
    	Log.d(Tag, "permission denied for onStart ");
    	EnumerationDevice(getIntent());
    }
	@Override
    protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
   /* protected void onNewIntent(Intent intent) {
    	Log.d(Tag, "permission denied for onNewIntent ");
    	if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		EnumerationDevice(intent);
    }*/
}