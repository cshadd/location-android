package io.github.cshadd.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.mapbox.mapboxsdk.geometry.LatLng;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity
        extends AppCompatActivity
        implements Map.OnFragmentInteractionListener {
    private static final int MAX_LIGHT_STORAGE = 5000;
    private static final float RADIUS = 20;

    private Location currentLocation;
    private Geocoder geocoder;
    private LinkedList<HistoricalElement> history;
    private Location lastLocation;
    private List<Float> lightValues;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private PermissionManager pm;
    private Resources res;
    private SensorManager sensorManager;
    private TextView textAltitude;
    private TextView textDistance;
    private TextView textLastAverageLight1;
    private TextView textLastAverageLight2;
    private TextView textLastAverageLight3;
    private TextView textLastAverageLight4;
    private TextView textLastAverageLight5;
    private TextView textLastLocation1;
    private TextView textLastLocation2;
    private TextView textLastLocation3;
    private TextView textLastLocation4;
    private TextView textLastLocation5;
    private TextView textLatitude;
    private TextView textLight;
    private TextView textLocation;
    private TextView textLongitude;
    private Vibrator vibrator;

    public MainActivity() {
        super();
        this.lightValues = new LinkedList<>();
        this.history = new LinkedList<>();
        this.pm = PermissionManager.getInstance();
        return;
    }

    private void addLocation() {
        if (this.history.size() >= 5) {
            this.history.removeLast();
        }
        this.history.addFirst(new HistoricalElement(this.computeLightAverage(this.lightValues),
                this.computeLocationName(this.currentLocation)));
        this.resetLastLocation(this.currentLocation);
        this.lightValues.clear();
        return;
    }

    private void clearHistory() {
        this.history.clear();
        this.history.add(new HistoricalElement());
        this.history.add(new HistoricalElement());
        this.history.add(new HistoricalElement());
        this.history.add(new HistoricalElement());
        this.history.add(new HistoricalElement());
        return;
    }

    private void computeHistory() {
        final HistoricalElement h1 = history.get(0);
        final HistoricalElement h2 = history.get(1);
        final HistoricalElement h3 = history.get(2);
        final HistoricalElement h4 = history.get(3);
        final HistoricalElement h5 = history.get(4);

        this.textLastAverageLight1.setText(res.getString(R.string.last_average_light, 1, h1.light));
        this.textLastAverageLight2.setText(res.getString(R.string.last_average_light, 2, h2.light));
        this.textLastAverageLight3.setText(res.getString(R.string.last_average_light, 3, h3.light));
        this.textLastAverageLight4.setText(res.getString(R.string.last_average_light, 4, h4.light));
        this.textLastAverageLight5.setText(res.getString(R.string.last_average_light, 5, h5.light));

        this.textLastLocation1.setText(res.getString(R.string.last_location, 1, h1.location));
        this.textLastLocation2.setText(res.getString(R.string.last_location, 2, h2.location));
        this.textLastLocation3.setText(res.getString(R.string.last_location, 3, h3.location));
        this.textLastLocation4.setText(res.getString(R.string.last_location, 4, h4.location));
        this.textLastLocation5.setText(res.getString(R.string.last_location, 5, h5.location));
        return;
    }

    private Float computeLightAverage(List<Float> values) {
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
        Log.i("NOGA", "Resetting location!");
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
        super.setContentView(R.layout.activity_main);

        this.pm.requestLocationPermission(this);

        this.currentLocation = new Location("Point B");
        this.geocoder = new Geocoder(this, Locale.getDefault());
        this.clearHistory();

        this.locationManager = (LocationManager)super.getSystemService(Context.LOCATION_SERVICE);

        this.res = super.getResources();
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        final List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (sensorList.size() > 0) {
            this.lightSensor = sensorList.get(0);
        }

        this.textAltitude = (TextView)super.findViewById(R.id.altitude);
        this.textDistance = (TextView)super.findViewById(R.id.distance);
        this.textLastAverageLight1 = (TextView)super.findViewById(R.id.last_average_light_1);
        this.textLastAverageLight2 = (TextView)super.findViewById(R.id.last_average_light_2);
        this.textLastAverageLight3 = (TextView)super.findViewById(R.id.last_average_light_3);
        this.textLastAverageLight4 = (TextView)super.findViewById(R.id.last_average_light_4);
        this.textLastAverageLight5 = (TextView)super.findViewById(R.id.last_average_light_5);
        this.textLastLocation1 = (TextView)super.findViewById(R.id.last_location_1);
        this.textLastLocation2 = (TextView)super.findViewById(R.id.last_location_2);
        this.textLastLocation3 = (TextView)super.findViewById(R.id.last_location_3);
        this.textLastLocation4 = (TextView)super.findViewById(R.id.last_location_4);
        this.textLastLocation5 = (TextView)super.findViewById(R.id.last_location_5);
        this.textLatitude = (TextView)super.findViewById(R.id.latitude);
        this.textLight = (TextView)super.findViewById(R.id.light);
        this.textLocation = (TextView)super.findViewById(R.id.location);
        this.textLongitude = (TextView)super.findViewById(R.id.longitude);

        this.vibrator = (Vibrator)super.getSystemService(Context.VIBRATOR_SERVICE);

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
                textLight.setText(res.getString(R.string.light, light));
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

                final LatLng mapViewLocation = new LatLng();
                mapViewLocation.setAltitude(altitude);
                mapViewLocation.setLatitude(latitude);
                mapViewLocation.setLongitude(longitude);

                final String locationName = computeLocationName(currentLocation);
                textAltitude.setText(res.getString(R.string.altitude, altitude));
                textLatitude.setText(res.getString(R.string.latitude, latitude));
                textLocation.setText(res.getString(R.string.location, locationName));
                textLongitude.setText(res.getString(R.string.longitude, longitude));

                if (lastLocation == null) {
                    resetLastLocation(currentLocation);
                }

                float distance = lastLocation.distanceTo(currentLocation);
                if (distance >= RADIUS) {
                    distance = 0;
                    addLocation();
                }

                computeHistory();

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
        final MenuInflater inflater = super.getMenuInflater();
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
            super.startActivity(intent);
        }
        else if (id == R.id.action_manual) {
            this.addLocation();
            this.computeHistory();
        }
        else if (id == R.id.action_reset) {
            this.resetLastLocation(this.currentLocation);
            this.clearHistory();
            this.computeHistory();
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if (pm.isLocationAllowed()) {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500, 1, this.locationListener);
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    500, 1, this.locationListener);
        }
        if (this.lightSensor != null) {
            this.sensorManager.registerListener(this.lightSensorListener,
                    this.lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pm.isLocationAllowed()) {
            this.locationManager.removeUpdates(locationListener);
        }
        if (this.lightSensorListener != null) {
            this.sensorManager.unregisterListener(this.lightSensorListener);
        }
        return;
    }
}
