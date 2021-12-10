package br.com.areatecbrasil.androidfirebaseproject;

import static java.time.LocalDate.now;

import java.util.Calendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import br.com.areatecbrasil.androidfirebaseproject.Activity.Login;
import br.com.areatecbrasil.androidfirebaseproject.model.DataModel;

public class MainActivity extends AppCompatActivity {
    private Ponto p1, p2;
    private String PROVIDER;
    private FirebaseAuth mAuth;
    protected static Random random = new Random();
    private Object Date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        PROVIDER = LocationManager.GPS_PROVIDER;
        zerarPontos();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser;
        currentuser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentuser == null){
            Intent intent = new Intent( MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private void zerarPontos(){
        p1 = new Ponto();
        p2 = new Ponto();
    }

    public void reset(View v) {
        zerarPontos();
        ((EditText)findViewById(R.id.edtPonto1)).setText("");
        ((EditText)findViewById(R.id.edtPonto2)).setText("");
    }

    public void lerPonto1(View v) {
        p1 = this.getPonto();
        ((EditText)findViewById(R.id.edtPonto1)).setText(p1.toString());
    }

    public void lerPonto2(View v) {
        p2 = new Ponto();

        String latLng = getPointFromGoogleMaps();
        if(latLng.length() > 0){
            String lat = latLng.split(",")[0];
            String lng = latLng.split(",")[1];
            p2.setLatitude(Double.parseDouble(lat));
            p2.setLongitude(Double.parseDouble(lng));
            p2.setAltitude(0.0);
        }



        ((EditText)findViewById(R.id.edtPonto2)).setText(p2.toString());
    }

    public Ponto getPonto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return null;
        }

        LocationManager mLocManager  = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
        LocationListener mLocListener = new MinhaLocalizacaoListener();

        mLocManager.requestLocationUpdates(PROVIDER, 0, 0, mLocListener);

        //Location localAtual = mLocManager.getLastKnownLocation(PROVIDER);

        if (! mLocManager.isProviderEnabled(PROVIDER)) {
            Toast.makeText(MainActivity.this, "GPS DESABILITADO.", Toast.LENGTH_LONG).show();
        }

        //return new Ponto(localAtual.getLatitude, localAtual.getLongitude, localAtual.getAltitude);
        return new Ponto(MinhaLocalizacaoListener.latitude,
                MinhaLocalizacaoListener.longitude,
                MinhaLocalizacaoListener.altitude);
    }

    public void verPonto1(View v) { mostrarGoogleMaps(p1.getLatitude(), p1.getLongitude());}

    public void verPonto2(View v) { mostrarGoogleMaps(p2.getLatitude(), p2.getLongitude());}

    @SuppressLint("SetJavaScriptEnabled")
    public void mostrarGoogleMaps(double latitude, double longitude) {
        WebView wv = findViewById(R.id.webv);
        wv.loadUrl("https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude);
        // wv.loadUrl("https://www.google.com/maps/search/?api=1&query=-23.1621736,-45.7959114");

    }
    @SuppressLint("SetJavaScriptEnabled")
    public String getPointFromGoogleMaps(){
        try{

            WebView wv = findViewById(R.id.webv);
            wv.getSettings().setJavaScriptEnabled(true);
             String url = wv.getUrl();
              url = url.split("@")[1];
             String latLng = url.split(",")[0];
             latLng += ", " + url.split(",")[1];
             return latLng;
        }
        catch (Exception ex)
        {
            return "";
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void calcularDistancia(View v) {
        //LocationManager mLocManager  = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
        float[] resultado = new float[1];

        double LatMin = randomInRange( -23.164195, -23.252862);
        double LatMax = randomInRange( -23.164195, -23.252862);
        double LonMin = randomInRange( -45.790585, -45.912293);
        double LonMax = randomInRange( -45.790585, -45.912293);

        String hora;
        String data;
        float Distancia; // = new Float[1];

        // Location.distanceBetween(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude(), resultado);

        Location.distanceBetween(LatMin, LonMin, LatMax, LonMax, resultado);

        Distancia= resultado[0];

        Toast.makeText(MainActivity.this, "DISTÂNCIA: " + resultado[0] + "m  ", Toast.LENGTH_LONG).show();

        DataModel datamodel =  new DataModel();

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        datamodel.setHora(currentTime); // getInstance().getDate()
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        datamodel.setData(currentDate);

        datamodel.setId(mAuth.getUid()+" "+currentDate+" "+currentTime);
        datamodel.setP1Lat( (float) LatMin);
        datamodel.setP1Lon( (float) LonMin);
        datamodel.setP2Lat( (float) LatMax);
        datamodel.setP2Lon( (float) LonMax);

        datamodel.setDistancia((float ) Distancia);

        datamodel.Salvar();

    }

    public static double randomInRange(double min, double max) {
        double range = max - min;
        double scaled = random.nextFloat() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextFloat() * (max-min)) + min;
    }

}



