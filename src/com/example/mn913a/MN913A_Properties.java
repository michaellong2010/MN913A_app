package com.example.mn913a;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.util.Log;

public class MN913A_Properties extends Properties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String Tag = "MN913A_Properties";
	private File system_path = new File("//system//etc//");
	String property_filename = "//MN913A_app_property";
	
	public static final String prop_k1 = "slope_of_conc_linear_eq";
	public static final double def_prop_k1 = 1000;
	
	public static final String prop_k2 = "bias_of_conc_linear_eq";
	public static final double def_prop_k2 = 0;

	public static final String prop_k3 = "2nd_order_polynomial_1st_term";
	public static final double def_prop_k3 = 1;
	
	public static final String prop_k4 = "2nd_order_polynomial_2nd_term";
	public static final double def_prop_k4 = 1;
	
	public static final String prop_k5 = "2nd_order_polynomial_3rd_term";
	public static final double def_prop_k5 = 1;
	
	MN913A_Properties() {
		
	}
	
	MN913A_Properties ( String external_property_filename ) {
		property_filename = external_property_filename;
	}
	
	void load_property() {
		File f;
		FileInputStream fis;
		
		f = new File( system_path.getPath() + property_filename );
		if (f.exists()) {
			try {
				fis = new FileInputStream(f);
				this.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
		}
		
		if (getProperty(prop_k1) != null) {
			
		}
		else {
			setProperty(prop_k1, Double.toString(def_prop_k1));
			Log.d(Tag, getProperty(prop_k1));
		}
		
		if (getProperty(prop_k2) != null) {
			
		}
		else {
			setProperty(prop_k2, Double.toString(def_prop_k2));
			Log.d(Tag, getProperty(prop_k2));
		}
		
		if (getProperty(prop_k3) != null) {
			
		}
		else {
			setProperty(prop_k3, Double.toString(def_prop_k3));
			Log.d(Tag, getProperty(prop_k3));
		}
		
		if (getProperty(prop_k4) != null) {
			
		}
		else {
			setProperty(prop_k4, Double.toString(def_prop_k4));
			Log.d(Tag, getProperty(prop_k4));
		}
		
		if (getProperty(prop_k5) != null) {
			
		}
		else {
			setProperty(prop_k5, Double.toString(def_prop_k5));
			Log.d(Tag, getProperty(prop_k5));
		}
	}
	
	void flush() {
		FileOutputStream fos;
		File f;
		
		f = new File( system_path.getPath() + property_filename );
		try {
			if (f.exists())
				fos = new FileOutputStream(f);
			else
				fos = new FileOutputStream(f.getPath());
			if (fos != null) {
				this.store(fos, "");
				fos.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
