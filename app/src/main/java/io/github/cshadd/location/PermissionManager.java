package io.github.cshadd.location;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public final class PermissionManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String[] LOCATION_PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static PermissionManager instance;

    private boolean locationAllowed;

    private PermissionManager() {
        super();
        this.locationAllowed = false;
        return;
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

    public boolean isLocationAllowed() {
        return this.locationAllowed;
    }

    public void requestLocationPermission(Activity activity) {
        this.locationAllowed = false;

        if (ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSIONS[1])
                        == PackageManager.PERMISSION_GRANTED) {
            this.locationAllowed = true;
            Log.i("WIEGLEY", "Location permission granted!");
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    LOCATION_PERMISSIONS[0])
                    && ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    LOCATION_PERMISSIONS[1])) {
                Log.w("WIEGLEY", "Requesting location permissions!");
            }
            else {
                ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
                Log.w("WIEGLEY", "Requesting location permissions!");
            }
        }
        return;
    }


    public void requestLocationPermission(Fragment fragment) {
        this.locationAllowed = false;

        fragment.requestPermissions(LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        Log.w("WIEGLEY", "Requesting location permissions!");
        return;
    }

    public void onRequestLocationPermissionsResultHandler(int requestCode,
                                                                 String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for(int i = 0; i < grantResults.length; i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        this.locationAllowed = false;
                        Log.w("WIEGLEY", "Location permission denied!");
                        return;
                    }
                }
                this.locationAllowed = true;
                Log.i("WIEGLEY", "Location permission granted!");
            }
        }
        return;
    }
}