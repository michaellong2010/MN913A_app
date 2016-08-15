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
	//private File system_path = new File("//system//etc//");
	private File system_path = new File("//mnt//sdcard//MaestroNano");
	String property_filename = "//MN913A_app_property";
	
	public static final String prop_k1 = "slope_of_conc_linear_eq";
	public static final double def_prop_k1 = ( double ) 0.05;
	
	public static final String prop_k2 = "bias_of_conc_linear_eq";
	public static final double def_prop_k2 = 0.0d;

	public static final String prop_k3 = "A260_2nd_order_polynomial_1st_term";
	public static final double def_prop_k3 = 0.0d;
	
	public static final String prop_k4 = "A260_2nd_order_polynomial_2nd_term";
	public static final double def_prop_k4 = ( double ) 0.05;
	
	public static final String prop_k5 = "A260_2nd_order_polynomial_3rd_term";
	public static final double def_prop_k5 = 0.0d;
	
	public static final String prop_p1 = "A230_transfer_factor";
	public static final double def_prop_p1 = ( double ) 0.05;
	
	public static final String prop_p3 = "A230_2nd_order_polynomial_1st_term";
	public static final double def_prop_p3 = 0.0d;
	
	public static final String prop_p4 = "A230_2nd_order_polynomial_2nd_term";
	public static final double def_prop_p4 = ( double ) 0.05;
	
	public static final String prop_p5 = "A230_2nd_order_polynomial_3rd_term";
	public static final double def_prop_p5 = 0.0d;
	
	public static final String prop_s1 = "A280_transfer_factor";
	public static final double def_prop_s1 = ( double ) 0.05;
	
	public static final String prop_s3 = "A280_2nd_order_polynomial_1st_term";
	public static final double def_prop_s3 = 0.0d;
	
	public static final String prop_s4 = "A280_2nd_order_polynomial_2nd_term";
	public static final double def_prop_s4 = ( double ) 0.05;
	
	public static final String prop_s5 = "A280_2nd_order_polynomial_3rd_term";
	public static final double def_prop_s5 = 0.0d;
	
	public static final String prop_T1 = "A260_transmission_rate1"; //conc.=25
	public static final double def_prop_T1 = ( double ) 0.944;
	
	public static final String prop_T2 = "A260_transmission_rate2"; //conc.=1200
	public static final double def_prop_T2 = ( double ) 0.063;
	MN913A_Properties() {
		
	}
	
	MN913A_Properties ( String external_property_filename ) {
		property_filename = external_property_filename;
	}
	
	void load_property() {
		File f;
		FileInputStream fis;
		FileOutputStream fos;
		
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
		
		if (getProperty(prop_p1) != null) {
			
		}
		else {
			setProperty(prop_p1, Double.toString(def_prop_p1));
			Log.d(Tag, getProperty(prop_p1));
		}
		
		if (getProperty(prop_p3) != null) {
			
		}
		else {
			setProperty(prop_p3, Double.toString(def_prop_p3));
			Log.d(Tag, getProperty(prop_p3));
		}
		
		if (getProperty(prop_p4) != null) {
			
		}
		else {
			setProperty(prop_p4, Double.toString(def_prop_p4));
			Log.d(Tag, getProperty(prop_p4));
		}
		
		if (getProperty(prop_p5) != null) {
			
		}
		else {
			setProperty(prop_p5, Double.toString(def_prop_p5));
			Log.d(Tag, getProperty(prop_p5));
		}
		
		if (getProperty(prop_s1) != null) {
			
		}
		else {
			setProperty(prop_s1, Double.toString(def_prop_s1));
			Log.d(Tag, getProperty(prop_s1));
		}
		
		if (getProperty(prop_s3) != null) {
			
		}
		else {
			setProperty(prop_s3, Double.toString(def_prop_s3));
			Log.d(Tag, getProperty(prop_s3));
		}
		
		if (getProperty(prop_s4) != null) {
			
		}
		else {
			setProperty(prop_s4, Double.toString(def_prop_s4));
			Log.d(Tag, getProperty(prop_s4));
		}
		
		if (getProperty(prop_s5) != null) {
			
		}
		else {
			setProperty(prop_s5, Double.toString(def_prop_s5));
			Log.d(Tag, getProperty(prop_s5));
		}
		
		if (getProperty(prop_T1) != null) {
			
		}
		else {
			setProperty(prop_T1, Double.toString(def_prop_T1));
			Log.d(Tag, getProperty(prop_T1));
		}

		if (getProperty(prop_T2) != null) {
			
		}
		else {
			setProperty(prop_T2, Double.toString(def_prop_T2));
			Log.d(Tag, getProperty(prop_T2));
		}
		
		if (f.exists() == false) {
			try {
				if (f.exists()==false)
				  fos = new FileOutputStream(f.getPath());
				else
					fos = null;
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
