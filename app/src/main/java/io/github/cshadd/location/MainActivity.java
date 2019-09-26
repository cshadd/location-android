package io.github.cshadd.location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private boolean locationAllowed;
    private LocationManager locationManager;
    private double radius;
    private Resources res;
    private TextView textLatitude;
    private TextView textLongitude;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public MainActivity() {
        super();
        this.locationAllowed = false;
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.res = getResources();
        this.textLatitude = (TextView)findViewById(R.id.latitude);
        this.textLongitude = (TextView)findViewById(R.id.longitude);

        if (ContextCompat.checkSelfPermission(this, this.PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, this.PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, this.PERMISSIONS[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, this.PERMISSIONS[1])) {
                Log.w("NOGA", "Requesting permissions!");
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                Log.w("NOGA", "Requesting permissions!");
            }
        }

        Log.i("NOGA", "I love locations!");
        return;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onProviderEnabled(String s) {
            return;
        }

        @Override
        public void onProviderDisabled(String s) {
            return;
        }

        @Override
        public void onLocationChanged(Location location) {
            final double altitude = location.getAltitude();
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            textLatitude.setText(res.getString(R.string.latitude, latitude));
            textLongitude.setText(res.getString(R.string.longitude, longitude));
            return;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            return;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, locationListener);
        }
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        return;
    }
}
