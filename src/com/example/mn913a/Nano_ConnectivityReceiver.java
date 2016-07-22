package com.example.mn913a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;
import android.widget.Toast;

public class Nano_ConnectivityReceiver extends BroadcastReceiver {
	/*20160627 add by JanChen*/
    public static interface OnNetworkAvailableListener {
        public void onNetworkAvailable();
        public void onNetworkUnavailable();
    }
    
    private final ConnectivityManager connectivityManager;
    private OnNetworkAvailableListener onNetworkAvailableListener;
    private boolean connection = false;
    
    public Nano_ConnectivityReceiver(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        checkConnectionOnDemand();
    }
    
    public Nano_ConnectivityReceiver() {
    	connectivityManager = null;
    }
    
    public void bind(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
        checkConnectionOnDemand();
    }
    
    public void unbind(Context context) {
        context.unregisterReceiver(this);
    }

    private void checkConnectionOnDemand() {
        final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null || info.getState() != State.CONNECTED) {
            if (connection == true) {
                connection = false;
                if (onNetworkAvailableListener != null) onNetworkAvailableListener.onNetworkUnavailable();
            }
        }
        else {
            if (connection == false) {
                connection = true;
                if (onNetworkAvailableListener != null) onNetworkAvailableListener.onNetworkAvailable();
            }
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Toast.makeText(context, "network connection change", Toast.LENGTH_LONG).show();
    	//Log.d("connection", "" + intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));

        if ( intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == true ) {
            connection = false;
            Toast.makeText(context, "network disconnection", Toast.LENGTH_SHORT).show();
            if (onNetworkAvailableListener != null) {
                onNetworkAvailableListener.onNetworkUnavailable();
            }
        }
        else if ( intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) == false ) {
            connection = true;
            Toast.makeText(context, "network connection", Toast.LENGTH_SHORT).show();
            if (onNetworkAvailableListener != null) {
                onNetworkAvailableListener.onNetworkAvailable();
            }
        }
    }
    
    public boolean hasConnection() {
        return connection;
    }
    
    public void setOnNetworkAvailableListener(OnNetworkAvailableListener listener) {
        this.onNetworkAvailableListener = listener;
    }

}