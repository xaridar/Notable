package com.xaridar.notable.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xaridar.notable.R;
import com.xaridar.notable.common.Utils;
import com.xaridar.notable.models.Note;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapFragment extends Fragment implements LocationListener, GoogleMap.OnMarkerClickListener {

    private Activity ctx;
    private LatLng location;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference geoFireRef = db.getReference("geofire");
    private final GeoFire geoFire = new GeoFire(geoFireRef);
    private GeoQuery geoQuery;

    LocationManager locMan;
    private GoogleMap map;
    private Map<String, Marker> markers;

    @SuppressLint("MissingPermission")
    ActivityResultLauncher<String[]> locPermReq = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
        if ((fine != null && fine) || (coarse != null && coarse)) {
            map.setMyLocationEnabled(true);
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    });

    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Drawable d = ContextCompat.getDrawable(ctx, R.drawable.ic_note);
            if (d != null) {
                d.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.note_color, ctx.getTheme()), PorterDuff.Mode.SRC_IN));
            }
            Marker marker = map.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.latitude, location.longitude))
                            .icon(Utils.getBitmapDescriptorFromDrawable(Objects.requireNonNull(d)))
            );
            if (marker == null) {
                Log.w("GeoFire", "Error adding marker.");
            } else {
                marker.setTag(key);
                markers.put(key, marker);
            }
        }

        @Override
        public void onKeyExited(String key) {
            Objects.requireNonNull(markers.get(key)).remove();
            markers.remove(key);
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Objects.requireNonNull(markers.get(key)).setPosition(new LatLng(location.latitude, location.longitude));
        }

        @Override
        public void onGeoQueryReady() {
            Log.i("GeoFire", "Query Ready");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.w("GeoFire", "Query error: " + error.getMessage());
        }
    };

    @SuppressLint("MissingPermission")
    private final OnMapReadyCallback callback = googleMap -> {
        this.map = googleMap;
        map.setOnMarkerClickListener(this);
        if ((ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_dark);
            map.setMapStyle(style);
        }
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locPermReq.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        googleMap.setMyLocationEnabled(true);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location gpsLoc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (gpsLoc != null) {
            LatLng loc = new LatLng(gpsLoc.getLatitude(), gpsLoc.getLongitude());
            map.moveCamera(CameraUpdateFactory.zoomTo(15));
            map.moveCamera(CameraUpdateFactory.newLatLng(loc));
        }
    };

    public static MapFragment newInstance(Activity ctx) {
        MapFragment mf = new MapFragment();
        mf.ctx = ctx;
        mf.locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return mf;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("loc", "status");
        if (provider.equals(LocationManager.GPS_PROVIDER) && status != LocationProvider.AVAILABLE) {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locPermReq.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                return;
            }
            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Location netLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (netLoc != null) {
                LatLng loc = new LatLng(netLoc.getLatitude(), netLoc.getLongitude());
                map.moveCamera(CameraUpdateFactory.zoomTo(15));
                map.moveCamera(CameraUpdateFactory.newLatLng(loc));
            }
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d("loc", "enable");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("loc", "disable");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        markers = new HashMap<>();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("LOCATION", "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude());
        if (this.location != null && this.location.latitude == location.getLatitude() && this.location.longitude == location.getLongitude()) {
            return;
        }
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.location == null) {
            map.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
        this.location = loc;
        map.moveCamera(CameraUpdateFactory.newLatLng(loc));
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.latitude, loc.longitude), Utils.GEOFIRE_QUERY_RADIUS);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setCenter(new GeoLocation(loc.latitude, loc.longitude));
        }
    }

    public void addNote(String msg, FirebaseUser user) {
        String key = geoFireRef.push().getKey();
        geoFire.setLocation(key, new GeoLocation(location.latitude, location.longitude), (geoKey, error) -> {
            if (error != null) {
                Log.w("GeoFire", "Error saving location to GeoFire: " + error.getMessage());
            } else {
                Log.i("GeoFire", "Location saved successfully!");
                new Note(key, msg, user.getDisplayName(), user.getUid(), user.getPhotoUrl(), location, LocalDateTime.now()).addToDB();
                Intent noteViewIntent = new Intent(ctx, NoteActivity.class);
                noteViewIntent.putExtra("NoteID", key);
                startActivity(noteViewIntent);
                ctx.overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (geoQuery != null) geoQuery.removeAllListeners();
        clearMarkers();
    }

    @Override
    public void onStop() {
        super.onStop();
        locMan.removeUpdates(this);
        if (geoQuery != null) geoQuery.removeAllListeners();
        clearMarkers();
    }

    private void clearMarkers() {
        for (Marker m : markers.values()) {
            m.remove();
        }
        markers.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
        String id = (String) marker.getTag();
        map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        Intent noteIntent = new Intent(ctx, NoteActivity.class);
        noteIntent.putExtra("NoteID", id);
        startActivity(noteIntent);
        ctx.overridePendingTransition(R.anim.slide_in_bottom, R.anim.none);
        return true;
    }
}