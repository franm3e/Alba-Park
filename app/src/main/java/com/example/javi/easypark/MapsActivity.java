package com.example.javi.easypark;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationManager locationManager;
    Location location;
    double distanciaSensor;
    int estado;
    MarkerOptions marker2 = new MarkerOptions();
    Marker mark2;
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;
    EditText locationSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Obtener informacion de sensores desde el server
        //Creamos conexion, obtenemos JSON y sacabamos distancia
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                URL sensorServer = null;
                try {
                    sensorServer = new URL("https://testsmart.eu-gb.mybluemix.net/sensor1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                try {
                    HttpsURLConnection myConnection = (HttpsURLConnection) sensorServer.openConnection();
                    int responseCode = myConnection.getResponseCode();

                    if (myConnection.getResponseCode() == 200) {
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                            System.out.println(inputLine);
                        }
                        /*ArrayList<String> cadena = new ArrayList<String>();
                        for(int i=1; i<response.length();i++)
                        {
                            if(response.charAt(i) == '}'){
                                cadena.add(response.substring(1,i+1));
                                response.delete(1,i+1);
                            }
                        }*/


                        JSONObject obj = new JSONObject(response.substring(1,response.length()-1));
                        //JSONObject obj1 = new JSONObject(response.substring(131,response.length()-1));
                        System.out.println(obj.toString());
                        String distancia = obj.getString("distance");
                        distanciaSensor =  Double.parseDouble(distancia);
                        System.out.println("DISTANCIA: " + distanciaSensor);
                        in.close();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationSearch = (EditText) findViewById(R.id.editText);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Boton Zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Como llegar
        mMap.getUiSettings().setMapToolbarEnabled(true);

        //Boton Ubicacion actual
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        //Ejemplos Markers Zona Universidad. Se cambiara
        LatLng plaza1 = new LatLng(38.978647, -1.855814);
        mMap.addMarker(new MarkerOptions().position(plaza1).title("Estado: Ocupado").snippet("Paseo de los Estudiantes, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo)));

        //Este funciona con el sensor, los demas NO.
        LatLng plaza2 = new LatLng(38.978991, -1.854389);
        marker2.position(plaza2).snippet("Cronista Francisco Ballesteros Gómez, Albacete");
        estado = estadoAparcamiento(distanciaSensor);
        if(estado == 1){
            marker2.title("Estado: Libre");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde));
            mark2 = mMap.addMarker(marker2);
        }
        if(estado == 2){
            marker2.title("Estado: Ocupado");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo));
            mark2 = mMap.addMarker(marker2);
        }
        if(estado == 3){
            marker2.title("Estado: Desconocido");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_interrogacion));
            mark2 = mMap.addMarker(marker2);
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(plaza2));

        LatLng plaza3 = new LatLng(38.978977, -1.854585);
        mMap.addMarker(new MarkerOptions().position(plaza3).title("Estado: Libre").snippet("Cronista Francisco Ballesteros Gómez, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde)));

        LatLng plaza4 = new LatLng(38.978481, -1.855781);
        mMap.addMarker(new MarkerOptions().position(plaza4).title("Estado: Desconocido").snippet("Paseo de los Estudiantes, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_interrogacion)));

        LatLng plaza5 = new LatLng(38.978865, -1.855517);
        mMap.addMarker(new MarkerOptions().position(plaza5).title("Estado: Libre").snippet("Cronista Francisco Ballesteros Gómez, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde)));

        LatLng plaza6 = new LatLng(38.977565, -1.853042);
        mMap.addMarker(new MarkerOptions().position(plaza6).title("Estado: Ocupado").snippet("Avenida España, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo)));

        LatLng plaza7 = new LatLng(38.978854, -1.855728);
        mMap.addMarker(new MarkerOptions().position(plaza7).title("Estado: Ocupado").snippet("Cronista Francisco Ballesteros Gómez, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo)));

        //Ejemplos Marker. Calle Ciudad Real 22, Albacete
        LatLng plaza8 = new LatLng(38.983931, -1.854548);
        mMap.addMarker(new MarkerOptions().position(plaza8).title("Estado: Ocupado").snippet("Calle Ciudad Real, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo)));

        LatLng plaza9 = new LatLng(38.983912, -1.854667);
        mMap.addMarker(new MarkerOptions().position(plaza9).title("Estado: Libre").snippet("Calle Ciudad Real, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde)));

        LatLng plaza10 = new LatLng(38.983899, -1.854780);
        mMap.addMarker(new MarkerOptions().position(plaza10).title("Estado: Libre").snippet("Calle Ciudad Real, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde)));

        LatLng plaza11 = new LatLng(38.983885, -1.854871);
        mMap.addMarker(new MarkerOptions().position(plaza11).title("Estado: Libre").snippet("Calle Ciudad Real, Albacete").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde)));



        //Zoom a la ubicacion actual
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng actual = new LatLng(location.getLatitude(),location.getLongitude()); //Coordenadas ubicacion actual
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 17.0f));


        //Para mover el boton de ubicacion actual
        View mapView = mapFragment.getView();
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 325);

    }

    //Reconocimiento de voz
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECOGNIZE_SPEECH_ACTIVITY:
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String strSpeech2Text = speech.get(0);
                    locationSearch.setText(strSpeech2Text);
                }
                break;
            default:
                break;
        }
    }

    public void reconocimientoVoz(View v) {

        Intent intentActionRecognizeSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Configura el Lenguaje (Español)
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        try {
            startActivityForResult(intentActionRecognizeSpeech, RECOGNIZE_SPEECH_ACTIVITY);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Tú dispositivo no soporta el reconocimiento por voz", Toast.LENGTH_SHORT).show();
        }

    }

    //Funcion que se ejecuta despues de darle al boton Buscar. Pasa direccion postales a coordenadas
    public void onMapSearch(View view){
        //locationSearch.setText("");
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try{
                addressList = geocoder.getFromLocationName(location, 1);
                Address direccion = addressList.get(0);
                LatLng coordenadas = new LatLng(direccion.getLatitude(), direccion.getLongitude());
                mMap.addMarker(new MarkerOptions().position(coordenadas).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
            }catch (IOException e){
                //e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Introduce una direccion postal correcta.", Toast.LENGTH_SHORT);
                toast.show();
            }catch (IndexOutOfBoundsException e){
                //e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Introduce una direccion postal correcta.", Toast.LENGTH_SHORT);
                toast.show();
            }

        }

        //Para dejar el EditText en blanco despues de pulsar el boton
        locationSearch.setText("");
        //Para ocultar el teclado despues de pulsar el boton
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(locationSearch.getWindowToken(),0);

    }

    public void Refrescar(View view){

        Toast toast = Toast.makeText(getApplicationContext(), "Actualizando aparcamientos.", Toast.LENGTH_SHORT);
        toast.show();
        //Obtener informacion de sensores desde el server
        //Creamos conexion, obtenemos JSON y sacabamos distancia
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                URL sensorServer = null;
                try {
                    sensorServer = new URL("https://testsmart.eu-gb.mybluemix.net/sensor1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                try {
                    HttpsURLConnection myConnection = (HttpsURLConnection) sensorServer.openConnection();
                    int responseCode = myConnection.getResponseCode();

                    if (myConnection.getResponseCode() == 200) {
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        JSONObject obj = new JSONObject(response.substring(1,response.length()-1));

                        System.out.println(obj.toString());
                        String distancia = obj.getString("distance");
                        distanciaSensor =  Double.parseDouble(distancia);
                        System.out.println("DISTANCIA: " + distanciaSensor);
                        in.close();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        estado = estadoAparcamiento(distanciaSensor);
        mark2.remove();
        LatLng plaza2 = new LatLng(38.978991, -1.854389);
        marker2.position(plaza2).snippet("Cronista Francisco Ballesteros Gómez, Albacete");
        if(estado == 1){
            marker2.title("Estado: Libre");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_verde));
            mark2 = mMap.addMarker(marker2);
        }
        if(estado == 2){
            marker2.title("Estado: Ocupado");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_minusvalido_rojo));
            mark2 = mMap.addMarker(marker2);
        }
        if(estado == 3){
            marker2.title("Estado: Desconocido");
            marker2.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_interrogacion));
            mark2 = mMap.addMarker(marker2);
        }
    }

    //Devolvera 1 si esta libre, 2 si esta ocupado y 3 si es error.
    public int estadoAparcamiento(double distancia){
        if(distancia<0.15){
            return 3;
        }else{
            if(distancia>0.15 && distancia<=0.9){
                return 2;
            }else{
                return 1;
            }
        }
    }


}
