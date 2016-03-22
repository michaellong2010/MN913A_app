package com.example.mn913a;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

public class NanoApplication extends Application {
	private List<Activity> activityList = new LinkedList<Activity>();
	private static NanoApplication instance;
	
	//單例模式中獲取唯一的MyApplication實例
	public static NanoApplication getInstance() {
	    if(null == instance) {
	        instance = new NanoApplication ( );
	    }
	    
	    return instance;
	}
	
	//添加Activity到容器中
		public void addActivity(Activity activity) {
		    activityList.add(activity);
		}
		
	//遍歷所有Activity並finish
	public void exit() {
	    for( Activity activity:activityList ) {
	    	if ( activity.isDestroyed() == false )
	    		activity.finish();
	    }
	    
	    //System.exit(0);
	}
}
