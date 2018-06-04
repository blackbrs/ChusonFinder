package grupo14.chusonfinder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PantallaUsuarioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,DirectionFinderListener {
/////////////////////////VARIABLES PARA GOOGLE MAPS API///////////////////////////////
    private GoogleMap mMap;
    GoogleApiClient nGoogleApiClient;
    Location nLastLocation;
    LocationRequest nLocationRequest;

    private Button btnFindPath;
    private EditText etOrigin, etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;

    /////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_usuario);
////////////////////???????????EL SETEADOR DEL MAP EN LA LAYOUT//////////////////////////////////////////
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//----------------------------------------SENSOR EVENT PARA LIMPIAR EL MAPA----------------------------------------

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensor == null){ finish(); }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float y = sensorEvent.values[1];
                if(y<-5){
                    limpiar();
                    mMap.setTrafficEnabled(false);

                    onResume();
                }//else if(y>5){//}

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
/////////////////////////////////////////////////////////////////////////////////////////////////

       Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

      //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
          //  public void onClick(View view) {
          //      Snackbar.make(view, "Mapa Limpio", Snackbar.LENGTH_LONG);
            //    limpiar();
              //  mMap.setTrafficEnabled(false);
           // }
        //});

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ////////////////////////////////////////creador de rutas///////////////////////////
        btnFindPath =(Button)findViewById(R.id.btnFindPath);
        etOrigin = (EditText)findViewById(R.id.etOrigin);
        etDestination = (EditText)findViewById(R.id.etDestination);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu ubicacion actual!! ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa a donde quieres llegar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }


    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions().title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()

                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.CYAN).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pantalla_usuario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
                onClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Blog) {
            Intent intent = new Intent(PantallaUsuarioActivity.this, BlogActivity.class);
            startActivity(intent);

        } else if (id == R.id.seguras) {
        } else if (id == R.id.inseguras) {
        } else if (id == R.id.Route53) {
           ruta53();
        } else if (id == R.id.Route1) {
            ruta1();
        } else if (id == R.id.Route44) {
        } else if (id == R.id.Trafico) {
            mMap.setTrafficEnabled(true);
        } else if (id == R.id.Satilital) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.Hybrido) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (id == R.id.Normal) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.cerrar) {
            Logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



/////////////////////////////////////// FUNCIONES DE GOOGLE MAPS///////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


    protected synchronized  void buildGoogleApiClient(){
        nGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        nGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        nLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        //mMap.setTrafficEnabled(true);
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(30));

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Se define el intervalo de actualizacion del gps en la aplicacion y su exactitud
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(1000);
        nLocationRequest.setFastestInterval(1000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(nGoogleApiClient, nLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    private void goMainScreen(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void Logout(){
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        goMainScreen();
    }


    public  void onClick(){
        Intent intent = new Intent(PantallaUsuarioActivity.this, DatosPersonalesActivity.class);
        startActivity(intent);
    }
/////////////////////////////////////////////////////// ASIGANANDO RUTAS/////////////////////////////////////////////////////
    public void ruta53(){
         LatLng Punto1 = new LatLng(13.742818, -89.221449);
         LatLng Punto2 = new LatLng(13.739358, -89.217147);
         LatLng Punto3 = new LatLng(13.738556, -89.212995);
         LatLng Punto4 = new LatLng(13.737165, -89.210720);
         LatLng Punto5 = new LatLng(13.736081, -89.207947);
         LatLng Punto6 = new LatLng(13.733236, -89.206198);
         LatLng Punto7 = new LatLng(13.732447, -89.204691);
         LatLng Punto8 = new LatLng(13.729940, -89.202344);
         LatLng Punto9 = new LatLng(13.727228, -89.202186);
         LatLng Punto10 = new LatLng(13.721014, -89.204031);
         LatLng Punto11 = new LatLng(13.718630, -89.205911);
         LatLng Punto12 = new LatLng(13.715667, -89.210503);
         LatLng Punto13 = new LatLng(13.714928, -89.205602);
         LatLng Punto14 = new LatLng(13.715220, -89.204379);
         LatLng Punto15 = new LatLng(13.714001, -89.203585);
         LatLng Punto16 = new LatLng(13.708915, -89.203049);
         LatLng Punto17 = new LatLng(13.705531, -89.196333);
         LatLng Punto18 = new LatLng(13.703405, -89.196413);
         LatLng Punto19 = new LatLng(13.701279, -89.196610);
         LatLng Punto20 = new LatLng(13.699903, -89.196106);
         //LatLng Punto21 = new LatLng();
         //LatLng Punto22 = new LatLng();
         //LatLng Punto23 = new LatLng();
         //LatLng Punto24 = new LatLng();
         //LatLng Punto25 = new LatLng();
         //LatLng Punto26 = new LatLng();
         //LatLng Punto27 = new LatLng();
         //LatLng Punto28 = new LatLng();
         //LatLng Punto29 = new LatLng();
        // LatLng Punto30 = new LatLng();


            mMap.addMarker(new MarkerOptions().position(Punto1).title("Punto de salida ruta 53").snippet("Primera Salida 5:30 am, Ultima Salida 5:30 pm"));
            mMap.addMarker(new MarkerOptions().position(Punto2).title("Parada Mister Pan"));
            mMap.addMarker(new MarkerOptions().position(Punto3).title("Parada Col San Pedro"));
            mMap.addMarker(new MarkerOptions().position(Punto4).title("Parada Villa Olimpica"));
            mMap.addMarker(new MarkerOptions().position(Punto5).title("Parada Calle El Bambu"));
            mMap.addMarker(new MarkerOptions().position(Punto6).title("Parada Calle al Volcan"));
            mMap.addMarker(new MarkerOptions().position(Punto7).title("Unidad De Salud zacamil"));
            mMap.addMarker(new MarkerOptions().position(Punto8).title("Parada Los 400"));
            mMap.addMarker(new MarkerOptions().position(Punto9).title("Parada Comerciales zacamil"));
            mMap.addMarker(new MarkerOptions().position(Punto10).title("Parada Polideportivo UES"));
            mMap.addMarker(new MarkerOptions().position(Punto11).title("Parada Oficinas Anda"));
            mMap.addMarker(new MarkerOptions().position(Punto12).title("Parada Colegio Cristobsl Colon"));
            mMap.addMarker(new MarkerOptions().position(Punto13).title("Parada INAC"));
            mMap.addMarker(new MarkerOptions().position(Punto14).title("Parada Minerva Ues"));
            mMap.addMarker(new MarkerOptions().position(Punto15).title("Parada Hospital Bloom"));
            mMap.addMarker(new MarkerOptions().position(Punto16).title("Parada Club de Leones"));
            mMap.addMarker(new MarkerOptions().position(Punto17).title("Parada Correos de El salvador"));
            mMap.addMarker(new MarkerOptions().position(Punto18).title("Parada Banco Agricola Centro de Gobierno"));
            mMap.addMarker(new MarkerOptions().position(Punto19).title("Parada Motel El OSO"));
            mMap.addMarker(new MarkerOptions().position(Punto20).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("Parada Final 53"));
    }

    public void ruta1(){
        LatLng Punto1 = new LatLng(13.738544, -89.213012);
        LatLng Punto2 = new LatLng(13.737085, -89.210739);
        LatLng Punto3 = new LatLng(13.736315, -89.208089);
        LatLng Punto4 = new LatLng(13.736570, -89.204427);
        LatLng Punto5 = new LatLng(13.736268, -89.203236);
        LatLng Punto6 = new LatLng(13.733548, -89.202681);
        LatLng Punto7 = new LatLng(13.729888, -89.202331);
        LatLng Punto8 = new LatLng(13.727199, -89.202158);
        LatLng Punto9 = new LatLng(13.721050, -89.204014);
        LatLng Punto10 = new LatLng(13.718640, -89.205926);
        LatLng Punto11 = new LatLng(13.718640, -89.205926);
        LatLng Punto12 = new LatLng(13.715604, -89.210535);
        LatLng Punto13 = new LatLng(13.714946, -89.205657);
        LatLng Punto14 = new LatLng(13.715308, -89.204262);
        LatLng Punto15 = new LatLng(13.715601, -89.202780);
        LatLng Punto16 = new LatLng(13.716120, -89.200745);
        LatLng Punto17 = new LatLng(13.715577, -89.192604);
        LatLng Punto18 = new LatLng(13.710154, -89.190408);
        LatLng Punto19 = new LatLng(13.708403, -89.190553);
        LatLng Punto20 = new LatLng(13.695561, -89.191844);
        LatLng Punto21 = new LatLng(13.695561, -89.191844);
        LatLng Punto22 = new LatLng(13.690328, -89.189216);
        LatLng Punto23 = new LatLng(13.689150, -89.187102);
        //LatLng Punto24 = new LatLng();
        //LatLng Punto25 = new LatLng();
        //LatLng Punto26 = new LatLng();
        //LatLng Punto27 = new LatLng();
        //LatLng Punto28 = new LatLng();
        //LatLng Punto29 = new LatLng();
        // LatLng Punto30 = new LatLng();


        mMap.addMarker(new MarkerOptions().position(Punto1).title("Punto de salida ruta 1").snippet("Primera Salida 4 am, Ultima Salida 8 pm"));
        mMap.addMarker(new MarkerOptions().position(Punto2).title("Parada villa olimpica").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto3).title("Parada El Bambu").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto4).title("Parada Caes").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto5).title("Parada Ayutuxtepeque").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto6).title("Parada la comunidad").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto7).title("Parada Los 400").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto8).title("Parada Centro comercial Zacamil").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto9).title("Parada Polideportivo UES").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto10).title("Parada Anda").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto11).title("Parada Economia UES").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto12).title("Parada Cristobal colon").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto13).title("Parada INAC").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto14).title("Parada Minerva").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto15).title("Parada Dollar city").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto16).title("Parada Autopista Norte").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto17).title("Parada la Luz del mundo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto18).title("Parada Maria Auxiliadora").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto19).title("Parada San Miguelito").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto20).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("Punto de control ruta 1"));
        mMap.addMarker(new MarkerOptions().position(Punto21).title("Parada el castillo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto22).title("Parada Distrito 5").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.addMarker(new MarkerOptions().position(Punto23).title("Parada Instituto nacional de comercio").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).snippet("Parada para retorno"));



    }

    public void limpiar(){
        mMap.clear();
    }

//------------------------------------- metodos sensor------------------------------------------------
private void start(){
    sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
}
    private void stop(){ }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        start();
        super.onResume();
    }
}



