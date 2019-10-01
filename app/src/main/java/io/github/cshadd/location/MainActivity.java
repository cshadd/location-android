package io.github.cshadd.location;

import android.content.Context;
import android.content.Intent;
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
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int MAX_LIGHT_STORAGE = 5000;
    private static final float RADIUS = 100;

    private Location currentLocation;
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
    private TextView textLastAltitude;
    private TextView textLastLatitude;
    private TextView textLastLocation;
    private TextView textLastLongitude;
    private TextView textLatitude;
    private TextView textLight;
    private TextView textLocation;
    private TextView textLongitude;
    private Vibrator vibrator;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public MainActivity() {
        super();
        this.lightValues = new LinkedList<>();
        return;
    }

    private Float computeAverage(List<Float> values) {
        Float sum = 0f;
        if (!values.isEmpty()) {
            for (Float value : values) {
                sum += value;
            }
            return sum / values.size();
        }
        return sum;
    }

    private String computeLocationName(Location loc) {
        try {
            final List<Address> addresses = this.geocoder.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
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
        return "NaN";
    }

    private void resetLastLocation(Location loc) {
        this.lastLocation = null;
        if (loc != null) {
            this.lastLocation = new Location("Point A");
            this.lastLocation.setAltitude(loc.getAltitude());
            this.lastLocation.setLatitude(loc.getLatitude());
            this.lastLocation.setLongitude(loc.getLongitude());
        }
        this.vibrate(500);
        return;
    }

    private void vibrate(int milliseconds) {
        if (this.vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else {
                this.vibrator.vibrate(milliseconds);
            }
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.currentLocation = new Location("Point B");
        this.geocoder = new Geocoder(this, Locale.getDefault());
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.res = getResources();
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        this.vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        final List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (sensorList.size() > 0) {
            this.lightSensor = sensorList.get(0);
        }

        this.textAltitude = (TextView)findViewById(R.id.altitude);
        this.textDistance = (TextView)findViewById(R.id.distance);
        this.textLastAltitude = (TextView)findViewById(R.id.last_altitude);
        this.textLastLatitude = (TextView)findViewById(R.id.last_latitude);
        this.textLastLocation = (TextView)findViewById(R.id.last_location);
        this.textLastLongitude = (TextView)findViewById(R.id.last_longitude);
        this.textLatitude = (TextView)findViewById(R.id.latitude);
        this.textLight = (TextView)findViewById(R.id.light);
        this.textLocation = (TextView)findViewById(R.id.location);
        this.textLongitude = (TextView)findViewById(R.id.longitude);

        if (ContextCompat.checkSelfPermission(this, this.PERMISSIONS[0])
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, this.PERMISSIONS[1])
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    this.PERMISSIONS[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    this.PERMISSIONS[1])) {
                Log.w("NOGA", "Requesting permissions!");
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                Log.w("NOGA", "Requesting permissions!");
            }
        }

        this.lightSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                return;
            }

            @Override
            public void onSensorChanged(SensorEvent arg0) {
                final float light = arg0.values[0];

                // To prevent out of memory?
                if (lightValues.size() > MAX_LIGHT_STORAGE) {
                    lightValues.clear();
                    Log.i("NOGA", "Clearing list of light values for memory!");
                }

                lightValues.add(light);
                final Float lightAverage = computeAverage(lightValues);
                textLight.setText(res.getString(R.string.light, lightAverage));
            }
        };

        this.locationListener = new LocationListener() {
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

                currentLocation.setAltitude(altitude);
                currentLocation.setLatitude(latitude);
                currentLocation.setLongitude(longitude);

                final String locationName = computeLocationName(currentLocation);
                textAltitude.setText(res.getString(R.string.altitude, altitude));
                textLatitude.setText(res.getString(R.string.latitude, latitude));
                textLocation.setText(res.getString(R.string.location, locationName));
                textLongitude.setText(res.getString(R.string.longitude, longitude));

                if (lastLocation == null) {
                    resetLastLocation(currentLocation);
                }

                final double lastAltitude = lastLocation.getAltitude();
                final double lastLatitude = lastLocation.getLatitude();
                final String lastLocationName = computeLocationName(lastLocation);
                final double lastLongitude = lastLocation.getLongitude();

                textLastAltitude.setText(res.getString(R.string.last_altitude, lastAltitude));
                textLastLatitude.setText(res.getString(R.string.last_latitude, lastLatitude));
                textLastLocation.setText(res.getString(R.string.last_location, lastLocationName));
                textLastLongitude.setText(res.getString(R.string.last_longitude, lastLongitude));

                float distance = lastLocation.distanceTo(currentLocation);

                if (distance >= RADIUS) {
                    distance = RADIUS;
                    resetLastLocation(currentLocation);
                    lightValues.clear();
                    Log.i("NOGA", "Resetting location!");
                }

                textDistance.setText(res.getString(R.string.distance, distance, RADIUS));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final int id = item.getItemId();
        if (id == R.id.action_close) {
            this.vibrate(100);
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else if (id == R.id.action_reset) {
            this.resetLastLocation(this.currentLocation);
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED) {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500, 1, this.locationListener);
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    500, 1, this.locationListener);
        }
        if (this.lightSensor != null) {
            this.sensorManager.registerListener(this.lightSensorListener, this.lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), this.PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED) {
            this.locationManager.removeUpdates(locationListener);
        }
        if (this.lightSensorListener != null) {
            this.sensorManager.unregisterListener(this.lightSensorListener);
        }
        return;
    }
}
