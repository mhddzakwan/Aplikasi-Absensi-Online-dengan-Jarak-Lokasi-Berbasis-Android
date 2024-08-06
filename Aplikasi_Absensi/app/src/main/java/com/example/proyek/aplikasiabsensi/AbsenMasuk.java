package com.example.proyek.aplikasiabsensi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AbsenMasuk extends AppCompatActivity implements OnMapReadyCallback{
    //url untuk akses data di server
    public static final String url = "http://ummuhafidzah.sch.id/absensi/tampilJarak.php";
    public static final String url2 = "http://ummuhafidzah.sch.id/absensi/cekHadir.php";
    private GoogleMap mMap;
    AppCompatImageButton back, refresh, sekolah, user;
    AppCompatButton hadir;
    TextView tanggal, jarak, akurasiTv;
    MapView map;
    private FusedLocationProviderClient locationProviderClient;
    double latitude, longitude, akurasi,tempLatitude, tempLongitude;
    String username,waktu,jarakUser, btn,bulan,tahun,hour,minute,second,jam, maksJarak;
    boolean fetch = true, fetch2 = false,isFocus=false;
    ProgressBar progressBar;
    FrameLayout overLayout;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    private com.google.android.gms.maps.MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_absen_masuk);
        mapView = findViewById(R.id.mapM);
        Bundle mapViewBundle = null;

        back = findViewById(R.id.btnBackM);
        refresh = findViewById(R.id.btnRefreshM);
        hadir = findViewById(R.id.absensi);
        tanggal = findViewById(R.id.tanggalM);
        jarak = findViewById(R.id.jarakM);
        akurasiTv = findViewById(R.id.accuracyM);
        progressBar = findViewById(R.id.progressBarM);
        overLayout = findViewById(R.id.overlayLayout);
        sekolah = findViewById(R.id.btnSekolahM);
        user = findViewById(R.id.btnLokasiM);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(AbsenMasuk.this);

        Intent home = getIntent();
        username = home.getStringExtra("username");

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(AbsenMasuk.this);

        getLocation();
        setTanggalLengkap();

        //Saat tombol hadir diklik, maka akan mulai loading dan mengabil
        // data(fetchnya di falsekan biar handlernya bekerja)
        hadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch = false;
                progressBar.setVisibility(View.VISIBLE);
                overLayout.setVisibility(View.VISIBLE);
                btn = "hadir";
                //Cek kehadiran
                cek();
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if(!fetch)cek();
//                else if(!fetch2)ambilJarak();
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    overLayout.setVisibility(View.INVISIBLE);
                }
                handler.postDelayed(this, 1500); // 1.5 seconds
                //Lokasi pengguna akan selalu dicek tiap 1,5 detik
                getLocation();
            }
        };
        handler.post(runnable);

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("HANDLER", "onPause: STOP ");
        handler.removeCallbacks(runnable);
    }
    public void onResume(){
        super.onResume();
        fetch2 = false;
        ambilJarak();
        handler.post(runnable);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10){
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Izin lokasi tidak di aktifkan!", Toast.LENGTH_SHORT).show();
            }else{
                getLocation();
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void setTanggalLengkap() {
        // Format tanggal lengkap
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        java.text.SimpleDateFormat month = new java.text.SimpleDateFormat("MMMM",new Locale("id", "ID"));
        java.text.SimpleDateFormat year = new SimpleDateFormat("yyyy",new Locale("id", "ID"));
        waktu = sdf.format(calendar.getTime());
        bulan = month.format(calendar.getTime());
        tahun = year.format(calendar.getTime());
        Log.d("waktu", waktu);
        tanggal.setText(waktu);

        hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        minute = String.format("%02d", calendar.get(Calendar.MINUTE));
        second = String.format("%02d", calendar.get(Calendar.SECOND));
        jam = hour + ":" + minute + ":" + second;
    }

    void getLocation() {
        //Mengambil lokasi pengguna
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // get Permission
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 10);
        }else{
            // get Location
            locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        akurasi = location.getAccuracy()/100;
                        akurasiTv.setText(String.valueOf(akurasi) + " %");

                        LatLng schoolLocation = new LatLng(3.604839, 98.643812);

                        //kalau posisi marker sebelumnya tidak sama dengan yg skrg
                        //pindahkan marker tsb ke posisi saat ini (Marker Dinamis)
                        if(latitude != tempLatitude || longitude != tempLongitude){
                            tempLongitude = longitude;
                            tempLatitude = latitude;
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(schoolLocation).title("Marker in Medan")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        }
                        LatLng userLocation = new LatLng(latitude,longitude);
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Lokasi Pengguna"));


                        float zoomLevel = 16.0f;
                        if(!isFocus){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
                            isFocus = true;
                        }

                        user.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel), 1000, null);
                            }
                        });

                        sekolah.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(schoolLocation, zoomLevel), 1000, null);
                            }
                        });
                        //Hitung jarak dari titk user skrg ke sekolah (dilakukan diserver)
                        ambilJarak();

                    } else {
                        Toast.makeText(getApplicationContext(), "Lokasi tidak aktif!", Toast.LENGTH_SHORT).show();
                        jarak.setText("-");
                        akurasiTv.setText("-");
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void ambilJarak(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //menerima respons dari server berupa jarak antara user dan sekolah
                            JSONObject jsonResponse = new JSONObject(response);
                            jarakUser  = String.valueOf(jsonResponse.getDouble("jarakUser"));
                            maksJarak = String.valueOf(jsonResponse.getInt("minimal_jarak"));

                            //Menampilkan jarak user
                            jarak.setText(jarakUser + " Meter (" + "max. " + maksJarak + " m)");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", "onErrorResponse: " + error.toString());
                Toast.makeText(AbsenMasuk.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("latitude", String.valueOf(latitude));
                data.put("longitude", String.valueOf(longitude));
                fetch2=true;
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    void cek(){
        //function ini berfungsi untuk melakukan absensi kehadiran
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (fetch){
                                JSONObject jsonResponse = new JSONObject(response);
                                String pesan = jsonResponse.getString("status").trim();
                                String kondisi = jsonResponse.getString("kondisi").trim();
                                Log.d("res", pesan);

                                //Kondisi ketika user sudah berhasil melakukan absensi sebelumnya,
                                // tetapi malah menekan absensi lagi
                                if (pesan.equals("sudah hadir")){
                                    Toast.makeText(AbsenMasuk.this, "Anda sudah mengisi", Toast.LENGTH_SHORT).show();
                                }

                                else if (pesan.equals("berhasil hadir")){
                                    //user berhasil melakukan absensi, tetapi telat, maka akan diarahkan
                                    //ke Activity Izin, untuk memberikan alasan keterlambatan
                                    if (kondisi.equals("Telat") && fetch){
                                        Toast.makeText(AbsenMasuk.this, "Anda telat hadir", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(AbsenMasuk.this, Izin.class);
                                        intent.putExtra("username",username);
                                        intent.putExtra("waktu", waktu);
                                        startActivity(intent);
                                        finish();
                                    }
                                    //Jika tidak telat:
                                    else {
                                        Toast.makeText(AbsenMasuk.this, "Terimakasih sudah mengisi hadir", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(AbsenMasuk.this, MainActivity.class);
                                        intent.putExtra("username",username);
                                        startActivity(intent);
                                        finish();
                                    }
                                //Jika jarak melampaui jarak maksimal
                                } else if (pesan.equals("jauh")){
                                    Toast.makeText(AbsenMasuk.this, "Lokasi anda terlalu jauh", Toast.LENGTH_SHORT).show();
                                }
                                //Jika user menekan tombol absensi, namun belum mulai waktu kerja
                                else if (pesan.equals("belum waktunya")){
                                    Toast.makeText(AbsenMasuk.this, "Belum waktunya bekerja", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AbsenMasuk.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        }){
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>data = new HashMap<>();
                //Mengirim data ke server
                data.put("username", username);
                data.put("waktu", waktu);
                data.put("latitude", String.valueOf(latitude));
                data.put("longitude", String.valueOf(longitude));
                data.put("jarakUser", String.valueOf(jarakUser));
                data.put("absensi", btn);
                data.put("jam", jam);
                data.put("hour", String.valueOf(hour));
                data.put("minute", String.valueOf(minute));
                data.put("second", String.valueOf(second));
                data.put("bulan",bulan);
                data.put("tahun", tahun);
                fetch = true;
                return data;
            }
        };

// Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mMap = googleMap;
        getLocation();
    }
}