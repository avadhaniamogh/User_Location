package com.example.amogha.mapbox_app;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * User location displayed in Mapbox
 */
public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mapview);

        mMapView.setStyleUrl(Style.EMERALD);
        mMapView.setZoomLevel(11);
        mMapView.onCreate(savedInstanceState);
        markerOptions = new MarkerOptions();
        mMapView.setMyLocationEnabled(true);
        mMapView.setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
        mMapView.setOnMyLocationChangeListener(new MapView.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location location) {
                Log.d("DUMMY", "Mapbox Location changes");
                mMapView.setCenterCoordinate(new LatLng(location), true);
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.setMyLocationEnabled(false);
    }
}
