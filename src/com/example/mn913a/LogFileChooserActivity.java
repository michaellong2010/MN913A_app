package com.example.mn913a;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.example.mn913a.MN_913A_Device.CMD_T;
import com.example.mn913a.file.FileOperation;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ActionMenuView;
import android.widget.ActionMenuView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserActivity;
import ar.com.daidalos.afiledialog.FileChooserCore;
import ar.com.daidalos.afiledialog.view.FileItem;

/*20131213 added by michael
 * List iTracker meta data log files in folder /sdcard/iTracker/*.txt */
public class LogFileChooserActivity extends FileChooserActivity {
	public final String Tag = "LogFileChooserActivity";
	
	/**20140819 added by michael
	 * setting child activity orientation
	 */
	public static final String INPUT_ACTIVITY_ORIENTATION = "input_activity_orientation";
	public static final String INPUT_ACTIVITY_USE = "input_activity_use";
	public static final String ACTIVITY_USE_FOR_MANAGEMENT = "management";
	public static final String ACTIVITY_USE_FOR_ANALYSIS = "analysis";
	public static final String INPUT_ACTIVITY_USE_NEW_UI = "input_activity_use_new_ui";
	
	/**20141123 added by michael*/
	StateListDrawable orig_file_item_drawable = null, new_file_item_drawable;
	LinkedList<FileItem> selected_file_items;
	public Dialog file_rename_dialog = null;
	LinearLayout file_rename_dialog_layout = null;
	EditText edit_new_filename;
	AlertDialog alert_dlg;
	AlertDialog.Builder alert_dlg_builder;
	String alert_message = "Are you sure that you want to delete the file \'$file_name\'?";
	FileItem[] Array_file_item = null;
	boolean filelist_reverse_order_by_modyfy, filelist_reverse_order_by_alphabet;
	
	/*20160412 added by michael*/
	List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> fillMaps1 = new ArrayList<HashMap<String, String>>();
	String[] from = new String[] { "No.", "Conc.", "A260", "A260_A230", "A260_A280" };
	int[] to = new int [] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
	ListView result_listview;
	ActionMode mMode = null;
	ActionMode.Callback mCallback;
	File mActiveFile = null, mActiveFile1 = null;
	String measure_type, measure_result;
	String [] measure_result_array;
	String[] dna_from = new String[] { "No.", "Conc.", "A260", "A260_A230", "A260_A280", "A230", "A280" };
	String[] protein_from = new String[] { "No.", "A280", "Coeff.", "Conc." };
	Boolean mIsFileDirty = false, mIsFileDirty1 = false;
	int mSelected_items_count = 0;
	
	private static final String ACTION_USB_PERMISSION = "com.example.mn913a.USB_PERMISSION";
	MN_913A_Device mNano_dev;
	boolean mRequest_USB_permission, Is_MN913A_Online = false;
	UsbManager mUsbManager;
	PendingIntent mPermissionIntent;
	String activity_use_for;
	int selection_count = 0;
	byte [] datetime_data = new byte [ 24 ];
	boolean activity_use_new_ui;
	ImageButton btn_rename, btn_delete_files, btn_storage;
	ImageButton btn_sort_by_alpha, btn_sort_by_most_updated, btn_select_all;
	int action_id = 0;
	Iterator<FileItem> selected_file_items_it;
	LinearLayout result_view_header, result_content_view;
	String first_line;
	
    @Override
    public void onCreate(Bundle savedInstanceState) { 
		this.getIntent().putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
		this.getIntent().putExtra(FileChooserActivity.INPUT_START_FOLDER, Environment.getExternalStorageDirectory().getPath() + "/MaestroNano/Measure/" );
		/*20141022 added by michael
		 * add the option to allow reverse the file list ording*/
		this.getIntent().putExtra(FileChooserActivity.INPUT_REVERSE_FILELIST_ORDER, true);
		/*20140819 added by michael
		 * add the additional extra param INPUT_ACTIVITY_ORIENTATION to handle the activity screen orientation¡Athese values(SCREEN_ORIENTATION_SENSOR_PORTRAIT¡BSCREEN_ORIENTATION_REVERSE_PORTRAIT...etc) define in  class ActivityInfo*/
		this.getIntent().putExtra(LogFileChooserActivity.INPUT_ACTIVITY_ORIENTATION, getRequestedOrientation());
		this.getIntent().putExtra(LogFileChooserActivity.INPUT_REGEX_FILTER, ".*\\.csv");

    	Log.d(Tag, Boolean.toString(getWindow().hasFeature(Window.FEATURE_ACTION_BAR)));
    	Log.d(Tag, Boolean.toString(getWindow().hasFeature((Window.FEATURE_NO_TITLE))));
    	super.onCreate(savedInstanceState);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	View v1 = ( View ) getWindow().getDecorView().getRootView();
    	
    	/*20140819 added by michael
    	 * set the activity orientation*/
    	Bundle extras = this.getIntent().getExtras();
    	if (extras.containsKey(INPUT_ACTIVITY_ORIENTATION)) {
    		if (getRequestedOrientation() != extras.getInt(INPUT_ACTIVITY_ORIENTATION))
    			setRequestedOrientation(extras.getInt(INPUT_ACTIVITY_ORIENTATION));
    	}
    	if (extras.containsKey(INPUT_ACTIVITY_USE))
    		activity_use_for = extras.getString( INPUT_ACTIVITY_USE );
    	else
    		activity_use_for = "";
    	if (extras.containsKey(this.INPUT_ACTIVITY_USE_NEW_UI))
    		activity_use_new_ui = extras.getBoolean( INPUT_ACTIVITY_USE_NEW_UI );
    	else
    		activity_use_new_ui = false;

    	ActionBar abr;
    	abr = this.getActionBar();
    	//abr.setTitle("knight");
    	//Log.d(Tag, (String) abr.getTitle());
    	abr.setDisplayHomeAsUpEnabled( true );
    	if ( activity_use_new_ui == true ) {
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
            LinearLayout customActionView = ( LinearLayout ) getLayoutInflater().inflate(R.layout.actionbar_custom_view, null);
            abr.setCustomView(customActionView);
            btn_rename = ( ImageButton ) customActionView.findViewById( R.id.imageButton1 );
            btn_delete_files = ( ImageButton ) customActionView.findViewById( R.id.imageButton2 );
            btn_storage = ( ImageButton ) customActionView.findViewById( R.id.imageButton3 );

            btn_storage.setOnClickListener( new View.OnClickListener() {
                /*20160727 Jan*/
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ShellExecuter exe = new ShellExecuter();
					Iterator<FileItem> it;
					FileItem iterator_item;
					it = selected_file_items.iterator();
					String outputPath = "/mnt/media_rw/udisk" ;
					String result=null;
					String inputfile1=null;
					while (it.hasNext()) {
						iterator_item = it.next();
						inputfile1 =iterator_item.getFile().getAbsolutePath();
					    try {
                             String cmd_str = "/system/xbin/su & cp " + inputfile1 + " " + outputPath;
                             result =  exe.exec_shell_command_mn913a(cmd_str);
                             Log.d(Tag, "copy : " + result);
					        }  
					        catch (Exception e) {
					        Log.e("tag", e.getMessage());
					    }
					}
					if(result == "COPY FILES PASS"){
						Show_Toast_Msg("COPY  " + inputfile1 + " Files Success");
					}else{
						
					}
					update_actionbar_optiomenu();
				}
            	
            } );
            
            btn_delete_files.setOnClickListener( new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					action_id = R.id.file_delete; 
					LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
				}
            	
            } );
            
            btn_rename.setOnClickListener( new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					action_id = R.id.file_rename; 
					LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
					v.setEnabled( false );
				}
            	
            } );
        	
            /*20160712 added by michael*/
            LinearLayout file_browser_layout, measure_root_layout; 
    		file_browser_layout = this.getRootLayout();
    		LinearLayout file_browser_header =  ( LinearLayout ) file_browser_layout.findViewById( R.id.left_header );
    		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		inflater.inflate ( R.layout.management_left_textbar_layout, file_browser_header, true );
    		btn_sort_by_alpha = ( ImageButton ) file_browser_header.findViewById( R.id.imageButton1 );
    		btn_sort_by_most_updated = ( ImageButton ) file_browser_header.findViewById( R.id.imageButton2 );
    		btn_select_all = ( ImageButton ) file_browser_header.findViewById( R.id.imageButton3 );
    		
    		btn_sort_by_alpha.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ImageButton btn_img = null;
					if ( v instanceof ImageButton ) {
						btn_img = ( ImageButton ) v;
					}

					if ( btn_img != null && btn_img.isSelected() == false) {
						Log.d ( "btn_sort_by_alpha", "toggle" );
						btn_img.setSelected( true );
						btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.sort_by_alphabet_b ) );
					}
					else
						if ( btn_img != null && btn_img.isSelected() == true) {
							Log.d ( "btn_sort_by_alpha", "hold" );
							btn_img.setSelected( false );
							btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.sort_by_alphabet_t ) );
						}
					action_id = R.id.filelist_sort_by_alphabet; 
					LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
				}
			});
    		
    		btn_sort_by_most_updated.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ImageButton btn_img = null;
					if ( v instanceof ImageButton ) {
						btn_img = ( ImageButton ) v;
					}

					if ( btn_img != null && btn_img.isSelected() == false) {
						Log.d ( "btn_sort_by_most_updated", "toggle" );
						btn_img.setSelected( true );
						btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.sort_by_most_update_b ) );
					}
					else
						if ( btn_img != null && btn_img.isSelected() == true) {
							Log.d ( "btn_sort_by_most_updated", "hold" );
							btn_img.setSelected( false );
							btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.sort_by_most_update_t ) );
						}
					action_id = R.id.filelist_sort_by_timestamp; 
					LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );							
				}
			});

    		btn_select_all.setOnClickListener( new View.OnClickListener() {
    			LinearLayout root = LogFileChooserActivity.this.getRootLayout();
    			final LinearLayout layout = (LinearLayout) root.findViewById(ar.com.daidalos.afiledialog.R.id.linearLayoutFiles);
    			int i = 0;
    			FileItem file_item;
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ImageButton btn_img = null;
					if ( v instanceof ImageButton ) {
						btn_img = ( ImageButton ) v;
					}

					if ( btn_img != null && btn_img.isSelected() == false) {
						Log.d ( "btn_select_all", "toggle" );
						btn_img.setSelected( true );
						btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.files_select_all ) );
						/*for ( i = 0; i < layout.getChildCount(); i++ ) {
							file_item = ( FileItem ) layout.getChildAt(i);
							//if ( selected_file_items.contains( file_item ) == false ) {
							if ( file_item.isSelected() == false ) {
								file_item.setSelected( true );
								selected_file_items.add( file_item );
							}
						}*/
						action_id = R.id.file_selection_all; 
						LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
						update_actionbar_optiomenu();
					}
					else
						if ( btn_img != null && btn_img.isSelected() == true) {
							Log.d ( "btn_select_all", "hold" );
							btn_img.setSelected( false );
							btn_img.setImageDrawable( LogFileChooserActivity.this.getResources().getDrawable( R.drawable.files_unselect_all ) );
							/*for ( i = 0; i < layout.getChildCount(); i++ ) {
								file_item = ( FileItem ) layout.getChildAt(i);
								//if ( selected_file_items.contains( file_item ) == false ) {
								if ( file_item.isSelected() == true ) {
								  file_item.setSelected( false );
								  selected_file_items.removeAll ( Collections.singletonList( file_item ) );
								}
							}*/
							action_id = R.id.file_unselection_all; 
							LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
							update_actionbar_optiomenu();
						}
						
				}
			});
    		
    		measure_root_layout = this.getMeasureResultLayout();
    		result_view_header = ( LinearLayout ) measure_root_layout.findViewById( R.id.right_header );
    		result_content_view = ( LinearLayout ) measure_root_layout.findViewById( R.id.right_content_view );
    		inflater.inflate ( R.layout.dna_result_listview_header1, result_view_header, false );
    		LinearLayout.LayoutParams lp = ( LinearLayout.LayoutParams ) result_view_header.getLayoutParams();
    		lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
    		result_view_header.setLayoutParams( lp );
    		result_view_header.setBackground( this.getResources().getDrawable( R.drawable.managament_textbar_right_bg ) );

        	if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) { 
        	}
        	else
        		if ( activity_use_for.equals( this.ACTIVITY_USE_FOR_ANALYSIS ) ) {
        			btn_delete_files.setVisibility( View.INVISIBLE );
        			btn_rename.setVisibility( View.INVISIBLE );
        			btn_storage.setVisibility( View.INVISIBLE );
        			btn_select_all.setVisibility( View.INVISIBLE );
        		}
        	
    	}
		
        /*20131214 added by michael
         * register file select listener */
    	
    	getFileChooserCore().addListener(new FileChooserCore.OnFileSelectedListener() {

			@Override
			public void onFileSelected(File file, FileItem source) {
				// TODO Auto-generated method stub
				//select a log file then open the txt content
				/*Intent intent;
				intent = new Intent(LogFileChooserActivity.this, LogFileDisplayActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra(OUTPUT_FILE_OBJECT, file);
				startActivity(intent);*/
				/*if ( item_open_file.isVisible() == false )
					item_open_file.setVisible( true );

				if ( item_delete_file.isVisible() == false )
					item_delete_file.setVisible( true );*/
				/*int [] st = source.getDrawableState();
				if ( orig_file_item_drawable == null ) {
					orig_file_item_drawable = (StateListDrawable) source.getBackground();
					new_file_item_drawable = new StateListDrawable();
					new_file_item_drawable = orig_file_item_drawable;
					new_file_item_drawable.addState(new int [] { android.R.attr.state_enabled, android.R.attr.state_selected }, new ColorDrawable(0xFF52D017) );									  
				}*/
				//if ( source.isSelected() )
					//source.setSelected( false );
				//else {
					//if ( (StateListDrawable) source.getBackground() != new_file_item_drawable )
						//source.setBackgroundDrawable( new_file_item_drawable );
					//source.setSelected( true );
				//}
				FileItem file_item;
				if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
					/*if( source.isSelectable () ) {
						if ( source.isSelected() )
							source.setSelected( false );
						else
							source.setSelected( true );
					}
					if ( source.isSelected() ) {
						selected_file_items.add( source );
					}
					else
						selected_file_items.removeAll ( Collections.singletonList( source ) );*/

					if ( selected_file_items.size() > 1 ) {
						if( source.isSelectable () ) {
							if ( source.isSelected() )
								source.setSelected( false );
							else
								source.setSelected( true );
						}
						if ( source.isSelected() ) {
							selected_file_items.add( source );
						}
						else
							selected_file_items.removeAll ( Collections.singletonList( source ) );
						
						if ( selected_file_items.size() == 1 ) {
							if ( activity_use_new_ui == false )
								onOptionsItemSelected( item_open_file );
							else {
								action_id = R.id.file_open; 
								LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
							}
						}
					}
					else
						if ( selected_file_items.size() <= 1 ) {
							if ( selected_file_items.size() > 0 ) {
								file_item = selected_file_items.get( 0 );
								file_item.setSelected( false );
								selected_file_items.removeAll ( Collections.singletonList( file_item ) );
							}
							else
								file_item = null;
							if( source.isSelectable () ) {
								source.setSelected( true );
							}
							selected_file_items.add( source );
							if ( activity_use_new_ui == false )
								onOptionsItemSelected( item_open_file );
							else {
								action_id = R.id.file_open; 
								LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
							}
						}
				}
				else
					if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
						if ( selected_file_items.size() > 0 ) {
							file_item = selected_file_items.get( 0 );
							file_item.setSelected( false );
							selected_file_items.removeAll ( Collections.singletonList( file_item ) );
						}
						else
							file_item = null;
						if( source.isSelectable () ) {
							source.setSelected( true );
						}
						selected_file_items.add( source );
						if ( activity_use_new_ui == false )
							onOptionsItemSelected( item_open_file );
						else {
							action_id = R.id.file_open; 
							LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
						}
					}
				
				update_actionbar_optiomenu();
				if ( mMode != null )
					mMode.finish ( );
			}

			@Override
			public void onFileSelected(File folder, String name) {
				// TODO Auto-generated method stub
				//create a new file
			}
        	
        });
    	selected_file_items = new LinkedList <FileItem>();
    	//this.openOptionsMenu();
    	WindowManager.LayoutParams params;
    	Button dlgbtn_cancel, dlgbtn_ok;
    	if ( file_rename_dialog == null ) {
    		file_rename_dialog = new Dialog( this, R.style.CenterDialog );
    		file_rename_dialog_layout = (LinearLayout) LayoutInflater.from( this.getApplicationContext()).inflate(R.layout.dialog_file_rename, null );
    		//file_rename_dialog.getWindow().setGravity( Gravity.CENTER_HORIZONTAL );
    		params = file_rename_dialog.getWindow().getAttributes();
    		params.x = 0;
    		params.y = -100;
    		file_rename_dialog.getWindow().setAttributes( params );
    		file_rename_dialog.setContentView( file_rename_dialog_layout );
    		file_rename_dialog.setTitle( "File Rename" );
    		file_rename_dialog.setCancelable(true);
    		
    		dlgbtn_cancel = (Button) file_rename_dialog_layout.findViewById( R.id.button2_cancel );
    		dlgbtn_cancel.setOnClickListener(new View.OnClickListener() {
    			
				@Override
				public void onClick(View v) { // TODO
					file_rename_dialog.dismiss();
				}
				
			});
    		dlgbtn_ok = (Button) file_rename_dialog_layout.findViewById( R.id.button1_ok );
    		dlgbtn_ok.setOnClickListener( new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					FileItem file_item;
					file_item = selected_file_items.get( 0 );
					TextView text_new_filename = ( TextView ) file_item.findViewById( ar.com.daidalos.afiledialog.R.id.textViewLabel );
					text_new_filename.setText( edit_new_filename.getText() );					
					Log.d ( Tag, file_item.getFile().getParent() + "//" + text_new_filename.getText() );
					file_item.getFile().renameTo( new File ( file_item.getFile().getParent() + "//" + text_new_filename.getText() ) );
					file_rename_dialog.dismiss();
				}
    			
    		});
    		
    		file_rename_dialog.setOnDismissListener( new DialogInterface.OnDismissListener () {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					Log.d ( "file_rename_dialog", "dismiss" );
					if ( selected_file_items_it.hasNext() ) {
						FileItem file_item = selected_file_items_it.next();
						edit_new_filename.setText( file_item.getFile().getName() );
						file_rename_dialog.show();
					}
					else
						if ( LogFileChooserActivity.this.activity_use_new_ui == true )
							btn_rename.setEnabled( true );
				}
    			
    		} );
    		
    		edit_new_filename = (EditText) file_rename_dialog_layout.findViewById( R.id.edit_rename_file );
    	}
    
    	alert_dlg_builder = new AlertDialog.Builder( this );
    	alert_dlg = alert_dlg_builder.create();
    	
    	filelist_reverse_order_by_modyfy = filelist_reverse_order_by_alphabet = false;
    	
    	List<FileItem> fileitems = this.getFileChooserCore().getFileItems();
    	ImageView icon;
    	
    	for ( FileItem fileitem : fileitems ) {
    		icon = (ImageView) fileitem.findViewById(R.id.imageViewIcon);
    		
    		if ( fileitem.getFile().getName().contains( "dsDNA" ) == true ) {
    			icon.setImageDrawable(getResources().getDrawable( com.example.mn913a.R.drawable.file_dsdna ));
    		}
    		else
    			if ( fileitem.getFile().getName().contains( "ssDNA" ) == true ) {
    				icon.setImageDrawable(getResources().getDrawable( com.example.mn913a.R.drawable.file_ssdna ));
    			}
    			else
        			if ( fileitem.getFile().getName().contains( "RNA" ) == true ) {
        				icon.setImageDrawable(getResources().getDrawable( com.example.mn913a.R.drawable.file_rna ));
        			}
        			else
            			if ( fileitem.getFile().getName().contains( "PROTEIN" ) == true ) {
            				icon.setImageDrawable(getResources().getDrawable( com.example.mn913a.R.drawable.file_protein ));
            			}
    	}
    	

    	mCallback = new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				//mode.setTitle( "1 Item Selected" );
				mode.setTitle( Integer.toString ( selection_count ) + " Item Selected");
				getMenuInflater().inflate(R.menu.dna_result_menu, menu);

				menu.findItem( R.id.item_selection_all );
				menu.findItem( R.id.item_unselection_all );
				if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
					menu.findItem( R.id.item_normalization_analysis ).setVisible( false );
				}
				else
				   if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
					   menu.findItem( R.id.item_delete ).setVisible( false );
					   menu.findItem( R.id.item_print ).setVisible( false );
					   menu.findItem( R.id.item_normalization_analysis ).setVisible( true );
				   }
				
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.item_delete:
					Iterator<HashMap<String, String>> it;
					HashMap <String, String> map1;
					it = fillMaps.iterator();
					while ( it.hasNext() ) {
						map1 = it.next();
						if ( map1.get( "isSelected" ) != null && map1.get( "isSelected" ).equals( "true" ) ) {
							it.remove();
							//fillMaps.remove( map1 );
						}
					}
					/*for ( HashMap <String, String> map : fillMaps ) {
						if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" ) )
							fillMaps.remo
					}*/
					if ( result_listview.getAdapter() instanceof dna_result_adapter )
						( ( dna_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					else
						if ( result_listview.getAdapter() instanceof protein_result_adapter )
							( ( protein_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					mMode.setTitle( Integer.toString ( 0 ) + " Item Selected");
					mSelected_items_count = 0;
					mIsFileDirty = true;
					return true;
				case R.id.item_print:
					int byte_offset = 0, dna_type = 0;
					byte [] byte_array = new byte [8192];
					byte[] bytes;
					if ( mActiveFile.getName().contains( "dsDNA" ) ) {
						dna_type = 0;
					}
					else
						if ( mActiveFile.getName().contains( "ssDNA" ) ) {
							dna_type = 1;
						}
						else
							if ( mActiveFile.getName().contains( "RNA" ) ) {
								dna_type = 2;
							}
					Log.d ( "Tag", Integer.toString( byte_array.length ) );
					//double conc, A260, A260_A280, A260_A230;
					if ( result_listview.getAdapter() instanceof dna_result_adapter ) {
						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_type ).array();
						System.arraycopy ( bytes, 0, byte_array, 0, bytes.length );
						byte_offset = byte_offset + bytes.length;
						bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
						System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
						byte_offset = byte_offset + bytes.length;
						
						System.arraycopy ( datetime_data, 0, byte_array, byte_offset, datetime_data.length );
						byte_offset = byte_offset + datetime_data.length;
						for ( HashMap<String, String> map : fillMaps ) {
							if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" ) ) {
								//String[] dna_from = new String[] { "No.", "Conc.", "A260", "A260_A280", "A260_A230" };
								//Integer.toString(i)//map.get(  )
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( dna_from [0] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								//Integer.toString(i)//map.get(  )
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( dna_from [0] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								//Double.parseDouble( dna_from [0] );
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [1] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								//Double.parseDouble( dna_from [1] );
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [2] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								//Double.parseDouble( dna_from [2] );
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [3] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								//Double.parseDouble( dna_from [3] );
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [4] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								
								if ( map.get( dna_from [5] ) != null ) {
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [5] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								}
								else {
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( 2.3 ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
								}
								if ( map.get( dna_from [6] ) != null ) {
								bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [6] ) ) ).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								}
								else {
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( 1.8 ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
								}
							}
						}
						if ( ( byte_offset % 256 ) != 0)
							mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, 0, ( byte_offset / 256 ) + 1, byte_array, 0);
						else
							mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, 0, ( byte_offset / 256 ), byte_array, 0);
					}
					else
						if ( result_listview.getAdapter() instanceof protein_result_adapter ) {
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
							System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							/*bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
							System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;*/
							
							System.arraycopy ( datetime_data, 0, byte_array, byte_offset, datetime_data.length );
							byte_offset = byte_offset + datetime_data.length;
							
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
							System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							for ( HashMap<String, String> map : fillMaps ) {
								if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" ) ) {
									//String[] protein_from = new String[] { "No.", "A280" };
									bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( protein_from [0] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( protein_from [0] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;									
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( protein_from [1] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
								}
							}
							if ( ( byte_offset % 256 ) != 0)
								mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_PROTEIN_RESULT, 0, ( byte_offset / 256 ) + 1, byte_array, 0);
							else
								mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_PROTEIN_RESULT, 0, ( byte_offset / 256 ), byte_array, 0);
						}
					break;
				case R.id.item_normalization_analysis:
					Intent intent = new Intent ( LogFileChooserActivity.this, NormalizationActivity.class );
					intent.putExtra( "arraylist" , (Serializable) fillMaps );
					//intent.getExtras().putSerializable("arraylist", (Serializable) fillMaps);
					if ( LogFileChooserActivity.this.getIntent().getExtras().containsKey( "target conc." ) )
						intent.putExtra( "target conc.", LogFileChooserActivity.this.getIntent().getExtras().getDouble( "target conc." ) );
					if ( LogFileChooserActivity.this.getIntent().getExtras().containsKey( "target vol." ) )
						intent.putExtra( "target vol.", LogFileChooserActivity.this.getIntent().getExtras().getDouble( "target vol." ) );
					intent.putExtra( NormalizationActivity.INPUT_ACTIVITY_USE_NEW_UI, false );
					LogFileChooserActivity.this.startActivityForResult ( intent, 2005 );
					break;
				case R.id.item_selection_all:
					for ( HashMap <String, String> map : fillMaps )
						map.put( "isSelected", "true" );
					if ( result_listview.getAdapter() instanceof dna_result_adapter )
						( ( dna_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					else
						if ( result_listview.getAdapter() instanceof protein_result_adapter )
							( ( protein_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					mMode.setTitle( Integer.toString ( fillMaps.size() ) + " Item Selected");
					mSelected_items_count = fillMaps.size();
					return true;
				case R.id.item_unselection_all:
					for ( HashMap <String, String> map : fillMaps )
						map.put( "isSelected", "false" );
					if ( result_listview.getAdapter() instanceof dna_result_adapter )
						( ( dna_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					else
						if ( result_listview.getAdapter() instanceof protein_result_adapter )
							( ( protein_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
					mMode.setTitle( Integer.toString ( 0 ) + " Item Selected");
					mSelected_items_count = 0;
					return true;

				default:
					return false;
				}
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				mMode = null;
			}
    		
    	};
    	
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
    	
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(uiOptions);
		
		if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) )
			mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_PRINTER_POWER_ON, 0, 0, null, 0 );
    }
    
	@Override
    protected void onDestroy() {
		//mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_PRINTER_POWER_ON, 0, 0, null, 0 );
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

/*20141022 added by michael
 * allowe home as up arrow to back to previous activity */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		LinearLayout root = this.getRootLayout();
		final LinearLayout layout = (LinearLayout) root.findViewById(ar.com.daidalos.afiledialog.R.id.linearLayoutFiles);
		int i = 0, button_action_id = 0;
		FileItem file_item;
		
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
		  case R.id.file_delete:
			  alert_message = "Are you sure that you want to delete the slelected file(s): \'$file_name\'?";
			  if ( selected_file_items.size() == 1 ) {
				  alert_message = alert_message.replace( "$file_name", selected_file_items.get(0).getFile().getName());
			  }
			  else
				  if ( selected_file_items.size() > 1 ) {
					  alert_message = alert_message.replace ( "$file_name", Integer.toString( selected_file_items.size() ) + " files" );
				  }
			  alert_dlg.setMessage( alert_message );
			  alert_dlg.setTitle( "Delete file(s)" );
			  alert_dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
							Iterator<FileItem> it;
							FileItem iterator_item;
							it = selected_file_items.iterator();
							while (it.hasNext()) {
								iterator_item = it.next();
								iterator_item.getFile().delete();
								layout.removeView(iterator_item);
								it.remove();
							}
							update_actionbar_optiomenu();
				}
				  
			  });
			  alert_dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
					  
			  });
			  alert_dlg.show();
			  return true;
		  case R.id.file_open:
			/*intent = new Intent(LogFileChooserActivity.this, LogFileDisplayActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(OUTPUT_FILE_OBJECT, selected_file_items.get(0).getFile());
			startActivity(intent);*/
			if ( mActiveFile == selected_file_items.get(0).getFile() )
				return true;
			else {
				if ( mActiveFile != null ) {
					alert_message = "The file \'$file_name\' has been changed, save or discard change?";
					alert_message = alert_message.replace( "$file_name", mActiveFile.getName());
					alert_dlg.setMessage( alert_message );
					alert_dlg.setTitle( "Save file" );
					fillMaps1.clear();
					for ( HashMap<String, String> map : fillMaps ) {
						HashMap<String, String> map1 = new HashMap<String, String> ( );
						for ( Map.Entry<String, String> entry: map.entrySet() )
							map1.put( entry.getKey ( ), new String(entry.getValue ( ) ) );
						fillMaps1.add ( map1 );
						//fillMaps1.add( (HashMap<String, String>) map.clone() );
					}
					mActiveFile1 = mActiveFile;
					mIsFileDirty1 = mIsFileDirty;
					alert_dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							FileOperation write_file = new FileOperation ( "Measure", mActiveFile1.getName(), false );
							try {
								write_file.set_file_extension ( ".csv" ); 
								write_file.create_file ( mActiveFile1.getName() );
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if ( mActiveFile1.getName().contains( "dsDNA" ) )
								measure_type = "dsDNA";
							else
								if ( mActiveFile1.getName().contains( "ssDNA" ) )
									measure_type = "ssDNA";
								else
									if ( mActiveFile1.getName().contains( "RNA" ) )
										measure_type = "RNA";
									else
										if ( mActiveFile1.getName().contains( "PROTEIN" ) )
											measure_type = "PROTEIN";
									
							//write_file.write_file ( measure_type, true );
							write_file.write_file ( first_line, true );
							
							if ( measure_type == "dsDNA" || measure_type == "ssDNA" || measure_type == "RNA" ) {
							for ( HashMap<String, String> map : fillMaps1 ) {
								measure_result = map.get( "No." ) + ", " + map.get( "Conc." ) + ", " + map.get( "A260" ) + ", " + map.get( "A260_A280" ) + ", " + map.get( "A260_A230" );
								write_file.write_file ( measure_result, true );
							}
							}
							else {
								for ( HashMap<String, String> map : fillMaps1 ) {
									measure_result = map.get( "No." ) + ", " + map.get( "A280" ) + ", " + map.get( protein_from [ 2 ] ) + ", " + map.get( protein_from [ 3 ] );
									write_file.write_file ( measure_result, true );
								}								
							}
							write_file.flush_close_file();
						}
						
					});
					alert_dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
						
					});
					if ( mIsFileDirty == true )
						alert_dlg.show();
				}
				mActiveFile = selected_file_items.get(0).getFile();
			}
			LinearLayout measure_root_layout, listview_header;
			measure_root_layout = this.getMeasureResultLayout();
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//result_listview = ( ListView ) inflater.inflate ( R.layout.result_listview, measure_root_layout, false );
			LinearLayout listview_toplayout = ( LinearLayout ) inflater.inflate ( R.layout.result_listview, measure_root_layout, false ).findViewById( R.id.listview_toplayout );
			result_listview = ( ListView ) listview_toplayout.findViewById( R.id.listview );
			listview_header = ( LinearLayout ) listview_toplayout.findViewById( R.id.listview_header );
			//result_listview.setBackgroundColor( this.getResources().getColor( android.R.color.background_dark ) );
			SimpleAdapter adapter = null;// = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
			String[] sports = new String [] { "123", "111", "234" };
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.simple_list_item_multiple_choice, sports );
			dna_result_adapter adapter2 = null;
			protein_result_adapter adapter3 = null;
			if ( this.activity_use_new_ui == true ) {
				adapter2 = new dna_result_adapter ( this, fillMaps, true );
				adapter3 = new protein_result_adapter ( this, fillMaps, true );
			}
			else {
				adapter2 = new dna_result_adapter ( this, fillMaps, false );
				adapter3 = new protein_result_adapter ( this, fillMaps, false );
			}
			
			if ( this.activity_use_new_ui == false )
				measure_root_layout.removeAllViews();
			FileOperation read_file = new FileOperation ( "Measure", mActiveFile.getName(), false );
			fillMaps.clear();
			result_listview.setDividerHeight( 3 );
			mIsFileDirty = false;
			if ( selected_file_items.get(0).getFile().getName().contains( "dsDNA" ) == true ||
				 selected_file_items.get(0).getFile().getName().contains( "ssDNA" ) == true ||
				 selected_file_items.get(0).getFile().getName().contains( "RNA" ) == true ) {
				if ( this.activity_use_new_ui == false )
					inflater.inflate ( R.layout.dna_result_listview_header, listview_header, true );
				else {
					if ( result_view_header.getChildCount() > 0 )
						result_view_header.removeAllViews();
					if ( this.result_content_view.getChildCount() > 0 )
						result_content_view.removeAllViews();
	        		inflater.inflate ( R.layout.dna_result_listview_header1, result_view_header, true );
				}
				/*for ( int j = 0; j < 50; j++ ) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put( "No.", Integer.toString( j ) );
				map.put( "Conc.", "50" );
				map.put( "A260", "1" );
				map.put( "A260_A230", "2.2" );
				map.put( "A260_A280", "1.8" );
				fillMaps.add( map );
				}*/
				try {
					read_file.open_read_file( mActiveFile.getName() );
					first_line = measure_result = read_file.read_file();
					//measure_result = "";
					if ( measure_result != null && measure_result != "") {
						//"yyyy/MM/dd HH:mm:ss"
						measure_result_array = measure_result.split( "\\/|\\ |\\:" );
						//StringBuilder builder = new StringBuilder();
						byte [] bytes;
						int byte_offset = 0, count = 0;
						for ( String s : measure_result_array ) {
						    //builder.append(s);
							
							if ( count < 6 && isNumeric ( s ) ) {
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
								System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								count++;
							}
							
							/*bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
							System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;*/
						}
						//return builder.toString();
					}
					while ( ( measure_result = read_file.read_file() ) != null ) {
					  Log.d ( "Tag", measure_result);
					  measure_result_array = measure_result.split( ", " );
					  if ( measure_result_array.length == dna_from.length || ( measure_result_array.length == ( dna_from.length - 2 ) ) ) {
							HashMap<String, String> map = new HashMap<String, String>();
							for (int j = 0; j < measure_result_array.length; j++) {
								map.put(dna_from[j], measure_result_array[j]);
								//map.put( "isSelected", "false" );
							}
							fillMaps.add(map);
					  }
					}
					read_file.flush_close_file();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter = new SimpleAdapter ( this, fillMaps, R.layout.dna_result_listview_item, from, to );
				result_listview.setAdapter( adapter2 );
				result_listview.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );
				result_listview.setMultiChoiceModeListener( new MultiChoiceModeListener () {

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onItemCheckedStateChanged(ActionMode mode,
							int position, long id, boolean checked) {
						// TODO Auto-generated method stub
						int count = result_listview.getCheckedItemCount();

						mode.setTitle(String.format("%d Selected", count));
					}
					
				});
				result_listview.setOnItemSelectedListener( new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						Log.d ( "tag", "select");
						view.setBackgroundResource(android.R.color.black );
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						Log.d ( "tag", "unselect");
					}
					
				});
				result_listview.setOnItemClickListener( new OnItemClickListener () {
					CheckBox checkbox1;
					//int selection_count = 0;

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						selection_count = 0;
						checkbox1 = ( CheckBox ) view.findViewById( R.id.checkbox2 );
						Log.d ( "tag", "click");
						/*if ( view.isSelected() ) {
							view.setSelected( false );
							Log.d ( "Tag: ", Boolean.toString( view.isSelected() ) );
						}
						else {
							view.setSelected( true );
							Log.d ( "Tag: ", Integer.toString( parent.getChildCount() ) );							
						}*/
						
						//parent.getAdapter().
						checkbox1.toggle();
						if ( checkbox1.isChecked() ) {
							fillMaps.get( position ).put( "isSelected", "true" );
							( ( ListView ) parent ).setItemChecked( position, true );
						}
						else {
							fillMaps.get( position ).put( "isSelected", "false" );
							( ( ListView ) parent ).setItemChecked( position, false );
						}
						
						for ( HashMap <String, String> map : fillMaps ) {
							if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" )) {
								selection_count++;
								//break;
							}
						}
						
						if ( LogFileChooserActivity.this.activity_use_new_ui == false ) {
							if ( selection_count > 0 ) {
								if (mMode == null)
									mMode = startActionMode(mCallback);
								else {
									mMode.setTitle( Integer.toString ( selection_count ) + " Item Selected");
								}
								mSelected_items_count = selection_count;
							}
							else {
								mMode.finish();
							}
						}
						else {
							if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
								if ( selection_count > 0 ) {
									if ( item_print_result.isVisible() == false )
										item_print_result.setVisible( true );
									if ( item_delete_result.isVisible() == false )
										item_delete_result.setVisible( true );
									mSelected_items_count = selection_count;
								}
								else {
									if ( item_print_result.isVisible() == true )
										item_print_result.setVisible( false );
									if ( item_delete_result.isVisible() == true )
										item_delete_result.setVisible( false );
								}
							}
							else
								if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
									if ( selection_count > 0 ) {
										item_normalization.setVisible( true );
										mSelected_items_count = selection_count;
									}
									else
										item_normalization.setVisible( false );
								}
						}
					}
					
				});
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
				
				//ViewGroup header = (ViewGroup) this.getLayoutInflater().inflate( R.layout.dna_result_listview_header, result_listview, false );
				//result_listview.addHeaderView ( header );
        	}
			else
				if ( selected_file_items.get(0).getFile().getName().contains( "PROTEIN" ) == true ) {
					if ( this.activity_use_new_ui == false )
						inflater.inflate ( R.layout.protein_result_listview_header, listview_header, true );
					else {
						if ( result_view_header.getChildCount() > 0 )
							result_view_header.removeAllViews();
						if ( this.result_content_view.getChildCount() > 0 )
							result_content_view.removeAllViews();
		        		inflater.inflate ( R.layout.protein_result_listview_header1, result_view_header, true );
					}
					/*for ( int j = 0; j < 50; j++ ) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put( "No.", Integer.toString( j ) );
					map.put( "A280", "0.5" );
					fillMaps.add( map );
					}*/
					try {
						read_file.open_read_file( mActiveFile.getName() );
						
						first_line = measure_result = read_file.read_file();
						//measure_result = "";
						if ( measure_result != null && measure_result != "") {
							//"yyyy/MM/dd HH:mm:ss"
							measure_result_array = measure_result.split( "\\/|\\ |\\:" );
							//StringBuilder builder = new StringBuilder();
							byte [] bytes;
							int byte_offset = 0, count = 0;
							for ( String s : measure_result_array ) {
							    //builder.append(s);
								
								if ( count < 6 && isNumeric ( s ) ) {
									bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( s ) ).array();
									System.arraycopy ( bytes, 0, datetime_data, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									count++;
								}
								
														}
							//return builder.toString();
						}
						
						while ( ( measure_result = read_file.read_file() ) != null ) {
						  Log.d ( "Tag", measure_result);
						  measure_result_array = measure_result.split( ", " );
						  if ( measure_result_array.length == protein_from.length ) {
								HashMap<String, String> map = new HashMap<String, String>();
								for (int j = 0; j < measure_result_array.length; j++) {
									map.put(protein_from[j], measure_result_array[j]);
									//map.put( "isSelected", "false" );
								}
								fillMaps.add(map);
						  }
						}
						read_file.flush_close_file();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					result_listview.setAdapter( adapter3 );
					result_listview.setOnItemClickListener( new OnItemClickListener () {
						CheckBox checkbox1;
						//int selection_count = 0;
						
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// TODO Auto-generated method stub
							selection_count = 0;
							checkbox1 = ( CheckBox ) view.findViewById( R.id.checkbox2 );
							
							checkbox1.toggle();
							if ( checkbox1.isChecked() ) {
								fillMaps.get( position ).put( "isSelected", "true" );
								( ( ListView ) parent ).setItemChecked( position, true );
							}
							else {
								fillMaps.get( position ).put( "isSelected", "false" );
								( ( ListView ) parent ).setItemChecked( position, false );
							}
							
							for ( HashMap <String, String> map : fillMaps ) {
								if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" )) {
									selection_count++;
									//break;
								}
							}
							
							if ( LogFileChooserActivity.this.activity_use_new_ui == false ) {
								if ( selection_count > 0 ) {
									if (mMode == null)
										mMode = startActionMode(mCallback);
									else {
										mMode.setTitle( Integer.toString ( selection_count ) + " Item Selected");
									}
									mSelected_items_count = selection_count;
								}
								else {
									mMode.finish();
								}
							}
							else {
								if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
									if ( selection_count > 0 ) {
										if ( item_print_result.isVisible() == false )
											item_print_result.setVisible( true );
										if ( item_delete_result.isVisible() == false )
											item_delete_result.setVisible( true );
										mSelected_items_count = selection_count;
									}
									else {
										if ( item_print_result.isVisible() == true )
											item_print_result.setVisible( false );
										if ( item_delete_result.isVisible() == true )
											item_delete_result.setVisible( false );
									}
								}
								else
									if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
										if ( selection_count > 0 ) {
											item_normalization.setVisible( true );
											mSelected_items_count = selection_count;
										}
										else
											item_normalization.setVisible( false );
									}
							}
						}
						
					});
				}
			//adapter.notifyDataSetChanged();
			if ( this.activity_use_new_ui == false )
				measure_root_layout.addView( listview_toplayout );
			else
				result_content_view.addView( listview_toplayout );
			//measure_root_layout.addView( result_listview );
			//result_listview.setBackgroundColor( this.getResources().getColor( android.R.color.background_dark ) );
			return true;			
		  case R.id.file_rename:
			  file_item = selected_file_items.get( 0 );
			  selected_file_items_it = selected_file_items.iterator();
			  selected_file_items_it.next();
			  if ( file_rename_dialog != null ) {
				  edit_new_filename.setText( file_item.getFile().getName() );
				  file_rename_dialog.show();
			  }
			  return true;
		  case R.id.file_selection_all:
			  for ( i = 0; i < layout.getChildCount(); i++ ) {
				  file_item = ( FileItem ) layout.getChildAt(i);
				//if ( selected_file_items.contains( file_item ) == false ) {
				  if ( file_item.isSelected() == false ) {
					  file_item.setSelected( true );
					  selected_file_items.add( file_item );
				  }
			  }
			  update_actionbar_optiomenu();
			  return true;
		  case R.id.file_unselection_all:
			  for ( i = 0; i < layout.getChildCount(); i++ ) {
				  file_item = ( FileItem ) layout.getChildAt(i);
				//if ( selected_file_items.contains( file_item ) == false ) {
				  if ( file_item.isSelected() == true ) {
					  file_item.setSelected( false );
					  selected_file_items.removeAll ( Collections.singletonList( file_item ) );
				  }
			  }
			  update_actionbar_optiomenu();
			  return true;
		  case R.id.filelist_sort_by_alphabet:
			  if ( Array_file_item == null )
				  Array_file_item = new FileItem[layout.getChildCount()];
			  for (i = 0; i < layout.getChildCount(); i++)
				  Array_file_item[i] = (FileItem) layout.getChildAt(i);
			  if ( filelist_reverse_order_by_alphabet == false ) {
				  Arrays.sort(Array_file_item, new Comparator<FileItem>() {
					  public int compare(FileItem file_item1, FileItem file_item2) {
						  if (file_item1 != null && file_item2 != null)
							  return file_item1.getFile().getName().compareTo( file_item2.getFile().getName() );
						  return 0;						  
					  }
				  });
				  filelist_reverse_order_by_alphabet = true;
			  }
			  else {
				  Arrays.sort(Array_file_item, new Comparator<FileItem>() {
					  public int compare(FileItem file_item1, FileItem file_item2) {
						  if (file_item1 != null && file_item2 != null)
	    					return file_item2.getFile().getName().compareTo( file_item1.getFile().getName() );
						  return 0;
					  }
				  });
				  filelist_reverse_order_by_alphabet = false;
			  }
			  layout.removeAllViews();
			  for (i = 0; i < Array_file_item.length; i++)
				  layout.addView( Array_file_item[i] );
			  return true;
		  case R.id.filelist_sort_by_timestamp:
			  if ( Array_file_item == null )
				Array_file_item = new FileItem[layout.getChildCount()];
				for (i = 0; i < layout.getChildCount(); i++) {
					Array_file_item[i] = (FileItem) layout.getChildAt(i);
				}
		    	if ( filelist_reverse_order_by_modyfy == false ) {
		    		Arrays.sort(Array_file_item, new Comparator<FileItem>() {
		    			public int compare(FileItem file_item1, FileItem file_item2) {
		    				if (file_item1 != null && file_item2 != null) {
		    					// return file1.getName().compareTo(file2.getName());
		    					return Long.toString( file_item1.getFile().lastModified()).compareTo( Long.toString(file_item2.getFile().lastModified()));
		    				}
		    				return 0;
		    			}
		    		});
		    		filelist_reverse_order_by_modyfy = true;
		    	}
		    	else {
		    		Arrays.sort(Array_file_item, new Comparator<FileItem>() {
		    			public int compare(FileItem file_item1, FileItem file_item2) {
		    				if (file_item1 != null && file_item2 != null) {
		    					// return file1.getName().compareTo(file2.getName());
		    					return Long.toString( file_item2.getFile().lastModified()).compareTo( Long.toString(file_item1.getFile().lastModified()));
		    				}
		    				return 0;
		    			}
		    		});
		    		filelist_reverse_order_by_modyfy = false;
		    	}
		    	layout.removeAllViews();
		    	for (i = 0; i < Array_file_item.length; i++)
		    		layout.addView( Array_file_item[i] );
		    	return true;
		}
		
		if ( item.getItemId() != 0 )
			return super.onOptionsItemSelected(item);
		else
			return true;
	}
	
	MenuItem item_open_file, item_delete_file, item_rename_file, item_select_all_file, item_unselect_all_file;
	MenuItem item_print_result, item_delete_result, item_home, item_normalization, Dummy_menu_item;
	Menu main_menu;
	ImageButton btn_home, btn_delete_result, btn_print_result, btn_normalization;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    if ( activity_use_new_ui == true ) {
		    if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
			    inflater.inflate(R.menu.management_right_side_menu, menu);
			    
			    main_menu = menu;
			    item_print_result = menu.findItem(R.id.item_print);
			    item_print_result.setActionView( R.layout.actionview_item_print );
			    btn_print_result = ( ImageButton ) item_print_result.getActionView();
			    item_delete_result = menu.findItem(R.id.item_delete);
			    item_delete_result.setActionView( R.layout.actionview_item_delete );
			    btn_delete_result = ( ImageButton ) item_delete_result.getActionView();
			    item_home = menu.findItem( R.id.home );
			    item_home.setActionView( R.layout.actionview_item_home );
			    btn_home = ( ImageButton ) item_home.getActionView();
			    btn_home.setOnClickListener( new View.OnClickListener( ) {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.d ( "btn_home", "click" );
						action_id = android.R.id.home; 
						LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
					}
				} );
			    
			    Dummy_menu_item = menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "dummy");
			    Dummy_menu_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			    Dummy_menu_item.setVisible( false );

			    btn_print_result.setOnClickListener( new View.OnClickListener( ) {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.d ( "btn_print_result", "click" );
						int byte_offset = 0, dna_type = 0;
						byte [] byte_array = new byte [8192];
						byte[] bytes;
						if ( mActiveFile.getName().contains( "dsDNA" ) ) {
							dna_type = 0;
						}
						else
							if ( mActiveFile.getName().contains( "ssDNA" ) ) {
								dna_type = 1;
							}
							else
								if ( mActiveFile.getName().contains( "RNA" ) ) {
									dna_type = 2;
								}
						Log.d ( "Tag", Integer.toString( byte_array.length ) );
						//double conc, A260, A260_A280, A260_A230;
						if ( result_listview.getAdapter() instanceof dna_result_adapter ) {
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( dna_type ).array();
							System.arraycopy ( bytes, 0, byte_array, 0, bytes.length );
							byte_offset = byte_offset + bytes.length;
							bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
							System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
							byte_offset = byte_offset + bytes.length;
							
							System.arraycopy ( datetime_data, 0, byte_array, byte_offset, datetime_data.length );
							byte_offset = byte_offset + datetime_data.length;
							for ( HashMap<String, String> map : fillMaps ) {
								if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" ) ) {
									//String[] dna_from = new String[] { "No.", "Conc.", "A260", "A260_A280", "A260_A230" };
									//Integer.toString(i)//map.get(  )
									bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( dna_from [0] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									//Integer.toString(i)//map.get(  )
									bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( dna_from [0] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									//Double.parseDouble( dna_from [0] );
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [1] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									//Double.parseDouble( dna_from [1] );
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [2] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									//Double.parseDouble( dna_from [2] );
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [3] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									//Double.parseDouble( dna_from [3] );
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [4] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									
									if ( map.get( dna_from [5] ) != null ) {
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [5] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									}
									else {
										bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( 2.3 ).array();
										System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
										byte_offset = byte_offset + bytes.length;
									}
									if ( map.get( dna_from [6] ) != null ) {
									bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( dna_from [6] ) ) ).array();
									System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
									byte_offset = byte_offset + bytes.length;
									}
									else {
										bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( 1.8 ).array();
										System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
										byte_offset = byte_offset + bytes.length;
									}
								}
							}
							if ( ( byte_offset % 256 ) != 0)
								mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, 0, ( byte_offset / 256 ) + 1, byte_array, 0);
							else
								mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_DNA_RESULT, 0, ( byte_offset / 256 ), byte_array, 0);
						}
						else
							if ( result_listview.getAdapter() instanceof protein_result_adapter ) {
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								/*bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;*/
								
								System.arraycopy ( datetime_data, 0, byte_array, byte_offset, datetime_data.length );
								byte_offset = byte_offset + datetime_data.length;
								
								bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt(mSelected_items_count).array();
								System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
								byte_offset = byte_offset + bytes.length;
								for ( HashMap<String, String> map : fillMaps ) {
									if ( map.get( "isSelected" ) != null && map.get( "isSelected" ).equals( "true" ) ) {
										//String[] protein_from = new String[] { "No.", "A280" };
										bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( protein_from [0] ) ) ).array();
										System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
										byte_offset = byte_offset + bytes.length;
										bytes = ByteBuffer.allocate(4).order( ByteOrder.LITTLE_ENDIAN ).putInt( Integer.parseInt( map.get( protein_from [0] ) ) ).array();
										System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
										byte_offset = byte_offset + bytes.length;									
										bytes = ByteBuffer.allocate(8).order( ByteOrder.LITTLE_ENDIAN ).putDouble( Double.parseDouble( map.get( protein_from [1] ) ) ).array();
										System.arraycopy ( bytes, 0, byte_array, byte_offset, bytes.length );
										byte_offset = byte_offset + bytes.length;
									}
								}
								if ( ( byte_offset % 256 ) != 0)
									mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_PROTEIN_RESULT, 0, ( byte_offset / 256 ) + 1, byte_array, 0);
								else
									mNano_dev.MN913A_IOCTL(CMD_T.HID_CMD_PRINT_PROTEIN_RESULT, 0, ( byte_offset / 256 ), byte_array, 0);
							}
					}
			    	
			    } );
			    //View v = item_print_result.getActionView();
			    //ActionMenuView.LayoutParams lp = ( ActionMenuView.LayoutParams ) item_print_result.getActionView().getLayoutParams();
			    btn_delete_result.setOnClickListener( new View.OnClickListener( ) {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.d ( "btn_delete_result", "click" );
						Iterator<HashMap<String, String>> it;
						HashMap <String, String> map1;
						it = fillMaps.iterator();
						while ( it.hasNext() ) {
							map1 = it.next();
							if ( map1.get( "isSelected" ) != null && map1.get( "isSelected" ).equals( "true" ) ) {
								it.remove();
								//fillMaps.remove( map1 );
							}
						}

						if ( result_listview.getAdapter() instanceof dna_result_adapter )
							( ( dna_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
						else
							if ( result_listview.getAdapter() instanceof protein_result_adapter )
								( ( protein_result_adapter ) result_listview.getAdapter() ).notifyDataSetChanged();
						mIsFileDirty = true;
						
						//if ( item_print_result.isVisible() == true )
							item_print_result.setVisible( false );
						//if ( item_delete_result.isVisible() == true )
							item_delete_result.setVisible( false );
					}
			    	
			    } );
		    }
		    else 
		    	if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
		    		//item_print_result.setVisible( false );
		    		//item_delete_result.setVisible( false );
				    inflater.inflate(R.menu.analysis_right_side_menu, menu);
				    
				    main_menu = menu;
				    item_normalization = menu.findItem(R.id.item_normalization_analysis);
				    item_normalization.setActionView ( R.layout.actionview_item_normalization_analysis );
				    btn_normalization = ( ImageButton ) item_normalization.getActionView();
				    item_home = menu.findItem( R.id.home );
				    item_home.setActionView( R.layout.actionview_item_home );
				    btn_home = ( ImageButton ) item_home.getActionView();
				    btn_home.setOnClickListener( new View.OnClickListener( ) {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Log.d ( "btn_home", "click" );
							action_id = android.R.id.home; 
							LogFileChooserActivity.this.onOptionsItemSelected( Dummy_menu_item );
						}
					} );
				    
				    Dummy_menu_item = menu.add( Menu.NONE, Menu.NONE, Menu.NONE, "dummy");
				    Dummy_menu_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				    Dummy_menu_item.setVisible( false );

				    item_normalization.setVisible( false );
		    		btn_normalization.setOnClickListener( new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Log.d ( "btn_normalization", "click" );
							Intent intent = new Intent ( LogFileChooserActivity.this, NormalizationActivity.class );
							intent.putExtra( "arraylist" , (Serializable) fillMaps );
							//intent.getExtras().putSerializable("arraylist", (Serializable) fillMaps);
							if ( LogFileChooserActivity.this.getIntent().getExtras().containsKey( "target conc." ) )
								intent.putExtra( "target conc.", LogFileChooserActivity.this.getIntent().getExtras().getDouble( "target conc." ) );
							if ( LogFileChooserActivity.this.getIntent().getExtras().containsKey( "target vol." ) )
								intent.putExtra( "target vol.", LogFileChooserActivity.this.getIntent().getExtras().getDouble( "target vol." ) );
							intent.putExtra( NormalizationActivity.INPUT_ACTIVITY_USE_NEW_UI, true );
							intent.setAction( LogFileChooserActivity.this.getIntent().getAction() );
							if ( LogFileChooserActivity.this.getIntent().getAction().equals( UsbManager.ACTION_USB_DEVICE_ATTACHED ) ) {
								UsbDevice device = (UsbDevice) LogFileChooserActivity.this.getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
								Log.d ( "NanoActivity debug device", device.toString() );
								intent.putExtra( UsbManager.EXTRA_DEVICE, device);
							}
							LogFileChooserActivity.this.startActivityForResult ( intent, 2005 );
						}
					} );
		    	}
		    update_actionbar_optiomenu ();
	    }
	    else {
		    inflater.inflate(R.menu.log_file_chooser_menu, menu);
		    
		    main_menu = menu;
		    item_open_file = menu.findItem(R.id.file_open);
		    item_open_file.setVisible(false);
		    item_delete_file = menu.findItem(R.id.file_delete);
		    item_delete_file.setVisible(false);
		    item_rename_file = menu.findItem(R.id.file_rename);
		    item_rename_file.setVisible(false);

			if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {    		    
			    item_select_all_file = menu.findItem(R.id.file_selection_all);
			    item_select_all_file.setVisible(true);
			    item_unselect_all_file = menu.findItem(R.id.file_unselection_all);
			    item_unselect_all_file.setVisible(true);

				Log.d ( Tag, "management" );
			}
			else
				if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
				    item_select_all_file = menu.findItem(R.id.file_selection_all);
				    item_select_all_file.setVisible( false );
				    item_unselect_all_file = menu.findItem(R.id.file_unselection_all);
				    item_unselect_all_file.setVisible( false );

					Log.d ( Tag, "analysis" );
				}
	    }
		
	    return true;
	}
	
	void update_actionbar_optiomenu() {
		if ( this.activity_use_new_ui == true ) {
			if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
				//btn_rename, btn_delete_files, btn_storage;
				if ( selected_file_items.size() == 0 ) {
					if ( btn_rename.getVisibility() == View.VISIBLE )
						btn_rename.setVisibility( View.INVISIBLE );
					if ( btn_delete_files.getVisibility() == View.VISIBLE )
						btn_delete_files.setVisibility( View.INVISIBLE );
					if ( btn_storage.getVisibility() == View.VISIBLE )
						btn_storage.setVisibility( View.INVISIBLE );
					
					if ( item_print_result.isVisible() == true )
						item_print_result.setVisible( false );
					if ( item_delete_result.isVisible() == true )
						item_delete_result.setVisible( false );
				}
				else
					if ( selected_file_items.size() == 1 ) {
						if ( btn_rename.getVisibility() == View.INVISIBLE )
							btn_rename.setVisibility( View.VISIBLE );
						if ( btn_delete_files.getVisibility() == View.INVISIBLE )
							btn_delete_files.setVisibility( View.VISIBLE );
						if ( btn_storage.getVisibility() == View.INVISIBLE )
							btn_storage.setVisibility( View.VISIBLE );
					}
					else
						if ( selected_file_items.size() > 1 ) {
							if ( btn_rename.getVisibility() == View.INVISIBLE )
								btn_rename.setVisibility( View.VISIBLE );
							if ( btn_delete_files.getVisibility() == View.INVISIBLE )
								btn_delete_files.setVisibility( View.VISIBLE );
							if ( btn_storage.getVisibility() == View.INVISIBLE )
								btn_storage.setVisibility( View.VISIBLE );							
						}
			}
			else
				if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
					
				}
		}
		else {
			if ( activity_use_for.equals( ACTIVITY_USE_FOR_MANAGEMENT ) ) {
				if ( selected_file_items.size() == 0 ) {
					if ( item_open_file.isVisible() == true )
						item_open_file.setVisible( false );

					if ( item_delete_file.isVisible() == true )
						item_delete_file.setVisible( false );
					
					if ( item_rename_file.isVisible() == true )
						item_rename_file.setVisible(false);
				}
				else
					if ( selected_file_items.size() == 1 ) {
						if ( item_open_file.isVisible() == false )
							item_open_file.setVisible( false );

						if ( item_delete_file.isVisible() == false )
							item_delete_file.setVisible( true );
						
						if ( item_rename_file.isVisible() == false )
							item_rename_file.setVisible(true);
					}
					else
						if ( selected_file_items.size() > 1 ) {
							if ( item_open_file.isVisible() == true )
								item_open_file.setVisible( false );

							if ( item_delete_file.isVisible() == false )
								item_delete_file.setVisible( true );
							
							if ( item_rename_file.isVisible() == true )
								item_rename_file.setVisible(false);
						}
			}
			else
				if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) ) {
					if ( selected_file_items.size() == 0 ) {
						
					}
					else
						if ( selected_file_items.size() == 1 ) {
							if ( item_open_file.isVisible() == false )
								item_open_file.setVisible( true );
						}
						else
							if ( selected_file_items.size() > 1 ) {
								
							}
				}
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public void finish() {
	    Intent data = new Intent();
	    setResult(RESULT_OK, data); 

	    super.finish();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2005) {
			Bundle extras = data.getExtras();
	    	if ( extras != null && extras.containsKey ( "target conc." ) )
	    		this.getIntent().putExtra( "target conc.", extras.getDouble( "target conc." ) );
	    	else
	    		if ( this.getIntent().getExtras().containsKey ( "target conc." ) )
	    			this.getIntent().getExtras().remove( "target conc." );
	    	
	    	if ( extras != null && extras.containsKey ( "target vol." ) )
	    		this.getIntent().putExtra( "target vol.", extras.getDouble( "target vol." ) );
	    	else
	    		if ( this.getIntent().getExtras().containsKey ( "target vol." ) )
	    			this.getIntent().getExtras().remove( "target vol." );
    		Log.d ( Tag, "onActivityResult: 2005" );
    		
    		View decorView = getWindow().getDecorView();
    		// Hide both the navigation bar and the status bar.
    		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
    		// a general rule, you should design your app to hide the status bar whenever you
    		// hide the navigation bar.
    		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    		              | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    		decorView.setSystemUiVisibility(uiOptions);
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
    			Log.d ( "LogFileChooser debug device", device.toString() );
    			if (mNano_dev.Enumeration(device)) {
    				Is_MN913A_Online = true;
    			}
    			else {
    				Is_MN913A_Online = false;
    			}
    		}
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
    
    public static boolean isNumeric(String str)  
    {  
      try  
      {  
        double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	EnumerationDevice(getIntent());
    	
    	if ( activity_use_for.equals( ACTIVITY_USE_FOR_ANALYSIS ) )
    		mNano_dev.MN913A_IOCTL ( CMD_T.HID_CMD_PRINTER_POWER_OFF, 0, 0, null, 0 );
    }
    
    public void Show_Toast_Msg(String msg ) {
 		Toast mToastMsg;
 		
 		if (msg.contains("flush log file: "))
 			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
 		else
 			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
     	TextView v = (TextView) mToastMsg.getView().findViewById(android.R.id.message);
     	v.setTextColor(Color.WHITE);
     	v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

    // 	if (msg.equals(I_Tracker_Device_Conn) || msg.equals(I_Tracker_Device_DisCon))
     //	  mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 250);
     //	else
     		mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 125);
 		mToastMsg.show();
     }
}
