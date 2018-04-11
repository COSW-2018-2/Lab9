package co.edu.escuelaing.is.lab9_salinas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private final int ACCESS_LOCATION_PERMISSION_CODE = 1;
    private TextView address;
    private boolean showMyLocation = true;
    @SuppressLint("RestrictedApi")
    private final LocationRequest locationRequest = new LocationRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        address = (TextView) findViewById( R.id.address );
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Create an instance of GoogleAPIClient.
        /* JHORDY WAY
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/
        // TEACHER WAY
        googleApiClient =
            new GoogleApiClient.Builder( this ).addConnectionCallbacks( this ).addOnConnectionFailedListener(
                this ).addApi( LocationServices.API ).build();
        locationRequest.setInterval( 10000 );
        locationRequest.setFastestInterval( 5000 );
        locationRequest.setPriority( LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY );
        googleApiClient.connect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        showMyLocation();
    }

    @SuppressLint("MissingPermission")
    public void showMyLocation() {
        if (googleMap != null && showMyLocation) {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
            if (hasPermissions(this, permissions)) {
                googleMap.setMyLocationEnabled(true);
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (lastLocation != null) addMarkerAndZoom(lastLocation, "My Location", 15);
            } else {
                ActivityCompat.requestPermissions(this, permissions, ACCESS_LOCATION_PERMISSION_CODE);
            }
        }
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void addMarkerAndZoom(Location location, String title, int zoom) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(myLocation).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == -1) {
                return;
            }
        }
        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_CODE:
                showMyLocation();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {startLocationUpdates();}

    @Override
    public void onConnectionSuspended(int i) {stopLocationUpdates();}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {startLocationUpdates();}

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates( googleApiClient, new LocationListener() {
            @Override
            public void onLocationChanged( Location location ) {}
        } );
    }

    @SuppressWarnings( "MissingPermission" )
    public void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest,
                new LocationListener() {
                    @Override
                    public void onLocationChanged( Location location ) {
                        showMyLocation();
                        stopLocationUpdates();
                    }
                });
    }


    // Action to button

    public void onFindAddressClicked(View view) {
        startFetchAddressIntentService();
    }

    @SuppressLint("MissingPermission")
    private void startFetchAddressIntentService() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if ( lastLocation != null ) {
            AddressResultReceiver addressResultReceiver = new AddressResultReceiver( new Handler() );
            addressResultReceiver.setAddressResultListener( new AddressResultListener() {
                @Override
                public void onAddressFound( final String address ){
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            MapsActivity.this.address.setText( address );
                            MapsActivity.this.address.setVisibility( View.VISIBLE );
                        }
                    });
                }
            });
            Intent intent = new Intent( this, FetchAddressIntentService.class );
            intent.putExtra( FetchAddressIntentService.RECEIVER, addressResultReceiver );
            intent.putExtra( FetchAddressIntentService.LOCATION_DATA_EXTRA, lastLocation );
            startService( intent );
        }
    }

    public void onNewLocation(View view) {
        Intent newLocation = new Intent(this, NewLocation.class);
        startActivityForResult(newLocation,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == NewLocation.RESULT_OK) {
                showMyLocation = false;
                Location newLocation = (Location) data.getExtras().get("newLocation");
                if (newLocation != null) addMarkerAndZoom(newLocation, "New Location", 15);
            }
        }
    }
}
