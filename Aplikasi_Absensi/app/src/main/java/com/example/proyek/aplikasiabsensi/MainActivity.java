package com.example.proyek.aplikasiabsensi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Data akan dikirim dan menerima respons melalui url berikut:
    public static final String url = "http://ummuhafidzah.sch.id/absensi/home.php";
    TextView tvNama, tvJabatan, hari, waktu, hadir, telat, izin, waktuHadir,waktuPulang, waktuKerja;
    ConstraintLayout absenMasuk, absenKeluar, riwayatAbsen;
    // Handler dan runnable dipakai untuk looping dengan durasi waktu tertentu
    Handler handler;
    Runnable runnable;
    ImageView logout;
    String username,bulan,tahun,formattedDate, today,nama,jabatan;
    boolean fetch = false;
    //Frame dan progressbar untuk UI loading
    ProgressBar progressBar;
    FrameLayout overLayout;
    LinearLayout layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        tvNama = findViewById(R.id.namaUser);
        tvJabatan = findViewById(R.id.jabatanUser);
        hari = findViewById(R.id.today);
        waktu = findViewById(R.id.waktuToday);
        hadir = findViewById(R.id.hadir);
        telat = findViewById(R.id.telat);
        waktuKerja = findViewById(R.id.waktuKerja);
        absenMasuk = findViewById(R.id.absen_masuk);
        absenKeluar = findViewById(R.id.absen_keluar);
        riwayatAbsen = findViewById(R.id.riwayat_absen);
        waktuHadir = findViewById(R.id.waktuHadir);
        waktuPulang = findViewById(R.id.waktuPulang);
        //logout = findViewById(R.id.logout);
        progressBar = findViewById(R.id.progressBar);
        overLayout =  findViewById(R.id.overlayLayout);
        layout = findViewById(R.id.layout);
        logout = findViewById(R.id.logout);


        handler = new Handler(); // Initialize handler disini

        setWaktu();
        setMasuk();
        Log.d("PESAN3", "fetch: " + fetch);

        //Menjalankan looping dengan jarak antar loop 1,5 detik
        //dikarenakan ketika mencoba mengambil data, terkadang data tidak terambil,
        // jadi perlu diloop sampai data benar" terambil
        runnable = new Runnable() {
            @Override
            public void run() {
                //kalau data belum terambil dari server (fetch == false), maka ambil data ke server lagi dan mulai loading
                //jika sudah (fetch == true), data tidak perlu diambil lagi
                if(!fetch)setMasuk(); //setMasuk = function untuk menampilkan data dari server ke activity ini
                else{
                    //kalau data sudah diambil dari server, matikan loading
                    progressBar.setVisibility(View.GONE);
                    overLayout.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1500); // 1.5 seconds
            }
        };
        handler.post(runnable);

        //Button untuk pindah ke AbsenMasukActivity
        absenMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbsenMasuk.class);
                intent.putExtra("username",username);
                startActivity(intent);

            }
        });

        //Button untuk pindah ke AbsenKeluarActivity
        absenKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AbsenKeluar.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });

        //Button untuk pindah ke Acticity Riwayat
        riwayatAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Riwayat.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });

        //Button untuk kembali ke laman login
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

        Intent home = getIntent();
        username = home.getStringExtra("username");

    }
    @Override
    protected void onPause() {
        //Saat kembali memasukan activity ini, handler akan dijalan kembali
        super.onPause();
        Log.d("HANDLER", "onPause: STOP ");
        handler.removeCallbacks(runnable);
    }
    @Override
    protected void onResume(){
        //Saat kembali memasukan activity ini, handler akan dijalan kembali
        super.onResume();
        //memulai loading
        progressBar.setVisibility(View.VISIBLE);
        overLayout.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);
        //fetch=false agar loop handler berjalan, dan melakukan pegambilan data
        fetch = false;
        handler.post(runnable);
        Log.d("PESAN3", "fetch: " + fetch);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(runnable);
    }

    private void setWaktu() {
        //Untuk mengambil waktu dari HP user
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat day = new SimpleDateFormat("EEEE", new Locale("id", "ID"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        SimpleDateFormat month = new SimpleDateFormat("MMMM",new Locale("id", "ID"));
        SimpleDateFormat year = new SimpleDateFormat("yyyy",new Locale("id", "ID"));
        bulan = month.format(calendar.getTime());
        tahun = year.format(calendar.getTime());
        formattedDate = sdf.format(calendar.getTime());
        today = day.format(calendar.getTime());

        hari.setText(today); //hari ini
        waktu.setText(formattedDate); //format tgl: 02 Agustus 2024

        Log.d("setWaktu", "setWaktu: " + today + " " + bulan + " " + tahun );
    }

    void setMasuk(){
        //Function untuk menampilkan data dari server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("anyText",response);
                            Log.i("tagconvertstr", "["+response+"]");
                            // Menerima respons dari server berupa data
                            JSONObject jsonResponse = new JSONObject(response);
                            String pesanHadir = jsonResponse.getString("hadir");
                            String pesanPulang = jsonResponse.getString("pulang");
                            String jumlahHadir = String.valueOf(jsonResponse.getInt("jumlah"));
                            String jumlahTelat = String.valueOf(jsonResponse.getInt("telat"));
                            String jamKerja = jsonResponse.getString("jamKerja");
                            Log.d("PESAN2", "onResponse: " + jumlahHadir + " " + jumlahTelat);
                            nama = jsonResponse.getString("nama");
                            jabatan = jsonResponse.getString("jabatan");

                            //Menampilkan data dari respons dengan TextView
                            tvNama.setText(nama);
                            tvJabatan.setText(jabatan);
                            hadir.setText(jumlahHadir + " hari");
                            telat.setText(jumlahTelat + " Hari");
                            waktuKerja.setText(jamKerja);

                            //Menampilkan Data jumlah hadir pada bulan ini
                            if (pesanHadir.trim().equals("belum hadir")){
                                waktuHadir.setText("Masuk : -");
                            } else {
                                waktuHadir.setText("Masuk : " + pesanHadir);
                            }

                            //Menampilkan Data jumlah telat pada bulan ini
                            if(pesanPulang.trim().equals("belum pulang")){
                                waktuPulang.setText("Pulang : -");
                            }else {
                                waktuPulang.setText("Pulang : " + pesanPulang);
                            }


                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>data = new HashMap<>();
                //Meengirim data ke server untuk diambil datanya
                data.put("waktu", formattedDate);
                data.put("username", username);
                data.put("bulan", bulan);
                data.put("tahun",tahun);
                Log.d("PESAN", "getParams: " + username);
                fetch = true;
                return data;
            }
        };

// Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(stringRequest);
    }
}
