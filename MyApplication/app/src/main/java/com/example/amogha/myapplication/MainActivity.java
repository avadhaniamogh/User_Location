package com.example.amogha.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity shows user location in an OSM map using osmdroid
 */
public class MainActivity extends AppCompatActivity {

	private MapView mMapView;
	private Button mBindButton;
	private Button mUnbindButton;
	private Messenger mServiceMessenger = null;
	boolean mIsBound = false;
	Location mCurrentLocation = null;
	private static final int ZOOM_LEVEL = 15;
	private ItemizedOverlay<OverlayItem> mCurrentLocationLocationOverlay;
	private ResourceProxy mResourceProxy;
	ArrayList<OverlayItem> items;

	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler());

	private class IncomingMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d("REALLY?", "It really works here in activity");
			switch (msg.what) {
				case MyService.MSG_SET_VALUE :
					Bundle bundle = msg.getData();
					Location loc = bundle.getParcelable("location");
					Log.d("REALLY?",
							"It really works Lat: " + loc.getLatitude()
									+ "  Lon: " + loc.getLongitude());
					if (loc != null) {
						GeoPoint location = new GeoPoint(loc);
						if (mCurrentLocation != null) {
							clearOverlay(mCurrentLocationLocationOverlay);
						} else {
							mMapView.getController().setCenter(location);
						}
						mCurrentLocation = loc;
						displayCurrentUserLocationDrawable(location);
					}
					break;
				default :
					super.handleMessage(msg);
			}
		}
	}

	public void displayCurrentUserLocationDrawable(GeoPoint loc) {
		mResourceProxy = new DefaultResourceProxyImpl(this);
		items = new ArrayList<OverlayItem>();
		// Put overlay icon a little way from map center
		OverlayItem overlayForCurrentLocationItem = new OverlayItem(
				"Here you are", "we will keep track of you", loc);
		items.add(overlayForCurrentLocationItem);
		/* OnTapListener for the Markers, shows a simple Toast. */
		this.mCurrentLocationLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
				items,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					@Override
					public boolean onItemSingleTapUp(final int index,
							final OverlayItem item) {
						Toast.makeText(getApplicationContext(),
								"single tap" + item.getTitle(),
								Toast.LENGTH_LONG).show();
						return true; // We 'handled' this event.
					}
					@Override
					public boolean onItemLongPress(final int index,
							final OverlayItem item) {
						Toast.makeText(getApplicationContext(),
								"longPress '" + item.getTitle(),
								Toast.LENGTH_LONG).show();
						return false;
					}
				}, mResourceProxy);
		this.mMapView.getOverlays().add(this.mCurrentLocationLocationOverlay);
		mMapView.invalidate();
	}

	public void clearOverlay(Overlay overlay) {
		if (overlay != null) {
			List<Overlay> overlays = mMapView.getOverlays();
			if (overlays != null) {
				overlays.remove(overlay);
			}
		}
	}

	private MyServiceConnection mConnection = new MyServiceConnection();
	class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mServiceMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						MyService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mServiceMessenger.send(msg);
			} catch (RemoteException e) {
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mServiceMessenger = null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMapView = (MapView) findViewById(R.id.mapview);
		mBindButton = (Button) findViewById(R.id.bindBtn);
		mUnbindButton = (Button) findViewById(R.id.unbindBtn);

		MapController mapController = (MapController) mMapView.getController();
		mapController.setZoom(ZOOM_LEVEL);
		mMapView.setMultiTouchControls(true);
		mMapView.setBuiltInZoomControls(true);
		/*
		 * GeoPoint gp = new GeoPoint(15, 75); mapController.setCenter(gp);
		 */

		mBindButton.setOnClickListener(bindClickListener);
		mUnbindButton.setOnClickListener(unbindClickListener);

		Intent trackerIntent = new Intent(this, MyService.class);
		startService(trackerIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private View.OnClickListener bindClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			doBindService();
			mUnbindButton.setVisibility(View.VISIBLE);
			mBindButton.setVisibility(View.INVISIBLE);
		}
	};

	private View.OnClickListener unbindClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			doUnbindService();
			mBindButton.setVisibility(View.VISIBLE);
			mUnbindButton.setVisibility(View.INVISIBLE);
		}
	};

	private void doBindService() {
		bindService(new Intent(this, MyService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			if (mServiceMessenger != null) {
				try {
					Message msg = Message.obtain(null,
							MyService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mServiceMessenger.send(msg);
				} catch (RemoteException e) {
				}
			}
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(this, MyService.class);
		this.stopService(intent);
	}
}
