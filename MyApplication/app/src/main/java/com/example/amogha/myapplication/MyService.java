package com.example.amogha.myapplication;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The service listens for user location.
 */
public class MyService extends Service implements LocationListener {

	private LocationManager mLocationManager;
	private Location currentLocation;
	private Messenger mClients = null;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_VALUE = 3;

	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler());

	public MyService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		subscribeToLocationUpdates();
		return START_STICKY;
	}

	private void subscribeToLocationUpdates() {
		// TODO Auto-generated method stub
		this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		this.mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, this);
		this.mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		sendMessageToUI(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	private void sendMessageToUI(Location valuetosend) {
		if (mClients != null) {
			try {
				Bundle bundle = new Bundle();
				bundle.putParcelable("location", valuetosend);
				Message msg = Message.obtain(null, MSG_SET_VALUE);
				msg.setData(bundle);
				mClients.send(msg);

			} catch (RemoteException e) {
			}
		}
	}

	private class IncomingMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_REGISTER_CLIENT :
					mClients = msg.replyTo;
					break;
				case MSG_UNREGISTER_CLIENT :
					mClients = null;
					break;
				case MSG_SET_VALUE :
					break;
				default :
					super.handleMessage(msg);
			}
		}
	}
}
