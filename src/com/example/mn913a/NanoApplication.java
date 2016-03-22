package com.example.mn913a;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

public class NanoApplication extends Application {
	private List<Activity> activityList = new LinkedList<Activity>();
	private static NanoApplication instance;
	
	//��ҼҦ�������ߤ@��MyApplication���
	public static NanoApplication getInstance() {
	    if(null == instance) {
	        instance = new NanoApplication ( );
	    }
	    
	    return instance;
	}
	
	//�K�[Activity��e����
		public void addActivity(Activity activity) {
		    activityList.add(activity);
		}
		
	//�M���Ҧ�Activity��finish
	public void exit() {
	    for( Activity activity:activityList ) {
	    	if ( activity.isDestroyed() == false )
	    		activity.finish();
	    }
	    
	    //System.exit(0);
	}
}
