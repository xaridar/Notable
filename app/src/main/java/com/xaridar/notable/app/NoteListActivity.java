package com.xaridar.notable.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xaridar.notable.R;
import com.xaridar.notable.common.ChildActivity;
import com.xaridar.notable.common.NoteAdapter;
import com.xaridar.notable.common.Utils;

public class NoteListActivity extends ChildActivity implements LocationListener {
    private LatLng location;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference geoFireRef = db.getReference("geofire");
    private final GeoFire geoFire = new GeoFire(geoFireRef);
    private GeoQuery geoQuery;
    private LocationManager locMan;

    private NoteAdapter na;

    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            na.addNoteId(key);
        }

        @Override
        public void onKeyExited(String key) {
            na.removeNoteId(key);
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
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

    @Override
    protected int getContentView() {
        return R.layout.activity_note_list;
    }

    @Override
    protected ButtonEnum getBackType() {
        return ButtonEnum.EXIT;
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location gpsLoc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        na = new NoteAdapter(this);
        RecyclerView rv = findViewById(R.id.notesRV);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(na);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (this.location != null && this.location.latitude == location.getLatitude() && this.location.longitude == location.getLongitude()) {
            return;
        }
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        this.location = loc;
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.latitude, loc.longitude), Utils.GEOFIRE_QUERY_RADIUS);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setCenter(new GeoLocation(loc.latitude, loc.longitude));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (geoQuery != null) geoQuery.removeAllListeners();
        na.clearNoteIds();
    }

    @Override
    public void onStop() {
        super.onStop();
        locMan.removeUpdates(this);
        if (geoQuery != null) geoQuery.removeAllListeners();
        na.clearNoteIds();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (geoQuery != null) geoQuery.addGeoQueryEventListener(geoQueryEventListener);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER) && status != LocationProvider.AVAILABLE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Location netLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }
}
