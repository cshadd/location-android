package io.github.cshadd.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int RADIUS = 100;

    private Geocoder geocoder;
    private Location lastLocation;
    private List<Float> lightValues;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Resources res;
    private SensorManager sensorManager;
    private TextView textAltitude;
    private TextView textDistance;
    private TextView textLatitude;
    private TextView textLocation;
    private TextView textLight;
    private TextView textLongitude;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public MainActivity() {
        super();
        this.lightValues = new LinkedList<>();
        return;
    }

    private Float calculateAverage(List<Float> values) {
        Float sum = 0f;
        if (!values.isEmpty()) {
            for (Float value : values) {
                sum += value;
            }
            return sum / values.size();
        }
        return sum;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.geocoder = new Geocoder(this, Locale.getDefault());
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.res = getResources();
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        final List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (sensorList.size() > 0) {
            this.lightSensor = sensorList.get(0);
        }
        else {

        }

        this.textAltitude = (TextView)findViewById(R.id.altitude);
        this.textDistance = (TextView)findViewById(R.id.distance);
        this.textLatitude = (TextView)findViewById(R.id.latitude);
        this.textLight = (TextView)findViewById(R.id.light);
        this.textLocation = (TextView)findViewById(R.id.location);
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

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                return;
            }

            @Override
            public void onSensorChanged(SensorEvent arg0) {
                final float light = arg0.values[0];

                // To prevent out of memory?
                if (lightValues.size() > 2000) {
                    lightValues.clear();
                    Log.i("NOGA", "Clearing list of light values for memory!");
                }

                lightValues.add(light);
                final Float lightAverage = calculateAverage(lightValues);
                textLight.setText(res.getString(R.string.light, lightAverage));
            }
        };

        locationListener = new LocationListener() {
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
                textAltitude.setText(res.getString(R.string.altitude, altitude));
                textLatitude.setText(res.getString(R.string.latitude, latitude));
                textLongitude.setText(res.getString(R.string.longitude, longitude));

                if (lastLocation == null) {
                    lastLocation = new Location("Point A");
                    lastLocation.setAltitude(altitude);
                    lastLocation.setLatitude(latitude);
                    lastLocation.setLongitude(longitude);
                }

                final Location currentLocation = new Location("Point B");
                currentLocation.setAltitude(altitude);
                currentLocation.setLatitude(latitude);
                currentLocation.setLongitude(longitude);

                float distance = lastLocation.distanceTo(currentLocation);

                try {
                    final List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        textLocation.setText(res.getString(R.string.location, addresses.get(0).getAddressLine(0)));
                    }
                }
                catch (IOException e) {
                    Log.e("NOGA", "Could not get location!");
                    e.printStackTrace();
                }
                catch (Exception e) {
                    Log.e("NOGA", "Could not get location!");
                    e.printStackTrace();
                }

                if (distance > RADIUS) {
                    distance = 0;
                    lastLocation.setAltitude(altitude);
                    lastLocation.setLatitude(latitude);
                    lastLocation.setLongitude(longitude);
                    lightValues.clear();
                    Log.i("NOGA", "Resetting location!");
                }

                textDistance.setText(res.getString(R.string.distance, distance));

                return;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                return;
            }
        };

        Log.i("NOGA", "I love locations!");
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, locationListener);
        }
        if (this.lightSensor != null) {
            sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
        if (lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
        }
        return;
    }
}
