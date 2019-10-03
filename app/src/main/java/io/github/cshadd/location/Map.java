package io.github.cshadd.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import java.lang.ref.WeakReference;

public class Map extends Fragment {
    private OnFragmentInteractionListener listener;
    private LocationEngine locationEngine;
    private LocationCallback mapCallback;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionManager pm;

    public Map() {
        super();
        this.pm = PermissionManager.getInstance();
        return;
    }

    private static class LocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<Map> activityWeakReference;

        public LocationCallback(Map activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            return;
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            final Map activity = this.activityWeakReference.get();
            if (activity != null) {
                final Location location = result.getLastLocation();
                if (location != null && activity.mapboxMap != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
            return;
        }

        @Override
        public void onFailure(Exception e) {
            Log.e("KAPLAN", "Could not process map!");
            e.printStackTrace();
            return;
        }
    }

    public interface OnFragmentInteractionListener { }

    public static Map newInstance() {
        final Map fragment = new Map();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void enableMapLocationComponent(Style loadedMapStyle) {
        final LocationComponent locationComponent = this.mapboxMap.getLocationComponent();
        final LocationComponentActivationOptions locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(super.getContext(), loadedMapStyle)
                        .useDefaultLocationEngine(false)
                        .build();
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);

        this.initLocationEngine();
        return;
    }

    private void initLocationEngine() {
        this.locationEngine = LocationEngineProvider.getBestLocationEngine(super.getContext());

        final LocationEngineRequest request = new LocationEngineRequest.Builder(1000L)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(5000L).build();
        if (this.pm.isLocationAllowed()) {
            locationEngine.requestLocationUpdates(request, this.mapCallback, super.getContext().getMainLooper());
            locationEngine.getLastLocation(this.mapCallback);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pm.requestLocationPermission(this);
        Mapbox.getInstance(super.getContext(), super.getString(R.string.mapbox_access_token));
        Log.i("KAPLAN", "I love maps!");
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        this.mapCallback = new LocationCallback(this);
        this.mapView = (MapView)rootView.findViewById(R.id.mapView);
        this.mapView.onCreate(savedInstanceState);

        final Style.OnStyleLoaded mapViewStyleLoaded = new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(Style style) {
                enableMapLocationComponent(style);
                return;
            }
        };

        final OnMapReadyCallback mapViewReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap map) {
                mapboxMap = map;
                mapboxMap.setCameraPosition(new CameraPosition.Builder().zoom(17).build());
                mapboxMap.setStyle(Style.MAPBOX_STREETS, mapViewStyleLoaded);
                return;
            }
        };

        this.mapView.getMapAsync(mapViewReadyCallback);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.listener = (OnFragmentInteractionListener)context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        return;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.locationEngine != null) {
            this.locationEngine.removeLocationUpdates(this.mapCallback);
        }
        this.mapView.onDestroy();
        return;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        this.mapView.onLowMemory();
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.pm.onRequestLocationPermissionsResultHandler(requestCode, permissions, grantResults);
        if (this.pm.isLocationAllowed()) {
            this.initLocationEngine();
        }
        return;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mapView.onResume();
        return;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mapView.onStart();
        return;
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mapView.onStop();
        return;
    }
}
