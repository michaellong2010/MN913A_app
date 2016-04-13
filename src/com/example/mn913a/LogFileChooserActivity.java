package com.example.mn913a;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
	String[] from = new String[] { "No.", "Conc.", "A260", "A260_A230", "A260_A280" };
	int[] to = new int [] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
	ListView result_listview;
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
    	
    	/*20140819 added by michael
    	 * set the activity orientation*/
    	Bundle extras = this.getIntent().getExtras();
    	if (extras.containsKey(INPUT_ACTIVITY_ORIENTATION)) {
    		if (getRequestedOrientation() != extras.getInt(INPUT_ACTIVITY_ORIENTATION))
    			setRequestedOrientation(extras.getInt(INPUT_ACTIVITY_ORIENTATION));
    	}
    	
    	ActionBar abr;
    	abr = this.getActionBar();
    	//abr.setTitle("knight");
    	//Log.d(Tag, (String) abr.getTitle());
    	abr.setDisplayHomeAsUpEnabled(true);
    	
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
				if ( source.isSelected() ) {
					selected_file_items.add( source );
				}
				else
					selected_file_items.removeAll ( Collections.singletonList( source ) );
				
				update_actionbar_optiomenu();
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
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
	}

/*20141022 added by michael
 * allowe home as up arrow to back to previous activity */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		LinearLayout root = this.getRootLayout();
		final LinearLayout layout = (LinearLayout) root.findViewById(ar.com.daidalos.afiledialog.R.id.linearLayoutFiles);
		int i = 0;
		FileItem file_item;
		
		switch ( item.getItemId() ) {
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
			LinearLayout measure_root_layout;
			measure_root_layout = this.getMeasureResultLayout();
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			result_listview = ( ListView ) inflater.inflate ( R.layout.dna_result_listview, measure_root_layout, false );
			SimpleAdapter adapter = null;// = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
			String[] sports = new String [] { "123", "111", "234" };
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.simple_list_item_multiple_choice, sports );
			dna_result_adapter adapter2 = new dna_result_adapter ( this, fillMaps ); 
			
			
			measure_root_layout.removeAllViews();
			if ( selected_file_items.get(0).getFile().getName().contains( "dsDNA" ) == true ||
				 selected_file_items.get(0).getFile().getName().contains( "ssDNA" ) == true ||
				 selected_file_items.get(0).getFile().getName().contains( "RNA" ) == true ) {
				fillMaps.clear();
				for ( int j = 0; j < 50; j++ ) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put( "No.", Integer.toString( j ) );
				map.put( "Conc.", "Conc." );
				map.put( "A260", "A260" );
				map.put( "A260_A230", "A260/A230" );
				map.put( "A260_A280", "A260/A280" );
				fillMaps.add( map );
				}
				adapter = new SimpleAdapter ( this, fillMaps, R.layout.dna_result_listview_item, from, to );
				result_listview.setAdapter( adapter2 );
				//result_listview.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );
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
				result_listview.setDividerHeight( 3 );
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

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						checkbox1 = ( CheckBox ) view.findViewById( R.id.checkbox2 );
						Log.d ( "tag", "click");
						if ( view.isSelected() ) {
							view.setSelected( false );
							Log.d ( "Tag: ", Boolean.toString( view.isSelected() ) );
						}
						else {
							view.setSelected( true );
							Log.d ( "Tag: ", Integer.toString( parent.getChildCount() ) );							
						}
						
						checkbox1.toggle();
					}
					
				});
        	}
			else
				if ( selected_file_items.get(0).getFile().getName().contains( "PROTEIN" ) == true ) {
					
				}
			adapter.notifyDataSetChanged();
			measure_root_layout.addView( result_listview );
			return true;			
		  case R.id.file_rename:
			  file_item = selected_file_items.get( 0 );
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
				  update_actionbar_optiomenu();
			  }
			  return true;
		  case R.id.file_unselection_all:
			  for ( i = 0; i < layout.getChildCount(); i++ ) {
				  file_item = ( FileItem ) layout.getChildAt(i);
				//if ( selected_file_items.contains( file_item ) == false ) {
				  if ( file_item.isSelected() == true ) {
					  file_item.setSelected( false );
					  selected_file_items.removeAll ( Collections.singletonList( file_item ) );
				  }
				  update_actionbar_optiomenu();
			  }
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
		return super.onOptionsItemSelected(item);		
	}
	
	MenuItem item_open_file, item_delete_file, item_rename_file, item_select_all_file, item_unselect_all_file;
	Menu main_menu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.log_file_chooser_menu, menu);
	    
	    main_menu = menu;
	    item_open_file = menu.findItem(R.id.file_open);
	    item_open_file.setVisible(false);
	    item_delete_file = menu.findItem(R.id.file_delete);
	    item_delete_file.setVisible(false);
	    item_rename_file = menu.findItem(R.id.file_rename);
	    item_rename_file.setVisible(false);
	    item_select_all_file = menu.findItem(R.id.file_selection_all);
	    item_select_all_file.setVisible(true);
	    item_unselect_all_file = menu.findItem(R.id.file_unselection_all);
	    item_unselect_all_file.setVisible(true);
	    
	    return true;
	}
	
	void update_actionbar_optiomenu() {
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
					item_open_file.setVisible( true );

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
}
