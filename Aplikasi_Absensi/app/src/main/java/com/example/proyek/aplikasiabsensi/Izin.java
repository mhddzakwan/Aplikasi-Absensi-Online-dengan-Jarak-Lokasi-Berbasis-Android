package com.example.proyek.aplikasiabsensi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Izin extends AppCompatActivity {
    public static final String url = "http://ummuhafidzah.sch.id/absensi/izin.php";
    public static final String url2 = "http://ummuhafidzah.sch.id/absensi/cekAlasan.php";
    private EditText inputTanggal;
    private Calendar calendar;
    private String selectedDate;
    Spinner spinnerStatus;
    private String selectedStatus;
    TextView date;
    Button ajukan;
    String username, waktu,alasan,user="";
    EditText etContent;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    boolean fetch = true, fetch2 = false;
    ProgressBar progressBar;
    FrameLayout overLayout;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_izin);

        ajukan = findViewById(R.id.btn_ajukan);
        date = findViewById(R.id.date);
        etContent = findViewById(R.id.etContent);
        progressBar = findViewById(R.id.progressBar);
        overLayout =  findViewById(R.id.overlayLayout);
        layout = findViewById(R.id.layout);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Alasan Keterlambatan");


        // Dropdown
        spinnerStatus = findViewById(R.id.spinnerStatus);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Set listener untuk mendapatkan pilihan pengguna
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedStatus = (String) parent.getItemAtPosition(position); // Simpan pilihan pengguna
                Toast.makeText(Izin.this, selectedStatus,Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });
        Intent home = getIntent();
        username = home.getStringExtra("username");
        waktu = home.getStringExtra("waktu");
        if(home.getStringExtra("user") != null) user = home.getStringExtra("user");


        ajukan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alasan = etContent.getText().toString();
                //Jika saat mengajukan datanya kosong:
                if((selectedStatus.equals("")) ||( alasan.equals(""))){
                    Toast.makeText(Izin.this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }else {
                    //ketika datanya terisi:
                    fetch = false;
                    //Mulai loading
                    progressBar.setVisibility(View.VISIBLE);
                    overLayout.setVisibility(View.VISIBLE);
                    //Mengupdate data alasan ke server
                    updateAlasan();
                }

            }
        });


        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("PESAN3", "run: ");
                //Melakukan updateAlasan jika data belum difetch, sampai data telah berhasil di fetch
                if(!fetch)updateAlasan();
                //cekAlasan() digunakan untuk melihat, apakah user sebelumnya sudah membuat alasan atau belum
                //dan akan menampilkan alasan sebelumna jika sudah ada
                else if(!fetch2)cekAlasan();
                else{
                    //Hilangkan loading
                    progressBar.setVisibility(View.GONE);
                    overLayout.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    Log.d("PESAN4", "run: visibility");
                }
                handler.postDelayed(this, 1500); // 1.5 seconds
            }
        };
        handler.post(runnable);


        cekAlasan();

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("HANDLER", "onPause: STOP ");
        handler.removeCallbacks(runnable);
    }
    @Override
    protected void onResume(){
        super.onResume();
        fetch2 = false;
        handler.post(runnable);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            recreate();
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Function untuk membuat alasan ddan menambahkannya ke server
    void updateAlasan(){
        Log.d("Pesan2", "gmasuk");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Log.d("Pesan2",   username +" " + password );
                            JSONObject jsonResponse = new JSONObject(response);
                            String pesan = jsonResponse.getString("status").trim();

                            //Jika berhasil membuat alasan:
                            if (pesan.equals("berhasil")){
                                Log.d("PESAN", "onResponse: " + pesan);
                                //Kembali ke MainActivity
                                onBackPressed();
                            }
                            //gagal membuat alasan ke server:
                            else{
                                Log.d("PESAN", "onResponse: " + pesan);
                                Toast.makeText(Izin.this, "Gagal", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Izin.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>data = new HashMap<>();
                //Mengirim data ke server
                data.put("waktu", waktu);
                data.put("username", username);
                data.put("alasan", alasan);
                data.put("judul_alasan", selectedStatus);
                Log.d("Pesan1", "getParams: onReq" + alasan+" " + selectedStatus );
                fetch = true;
                return data;
            }
        };

// Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(Izin.this);
        queue.add(stringRequest);
    }

    //function untuk mengecek dan menampilkan alasan sebelumnya yg sudah dibuat
    void cekAlasan(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("PESAN2", "onResponse: Masuk" );

                            //Menerima respon dari server berupa data alasan dan judul alasan
                            JSONObject jsonResponse = new JSONObject(response);
                            String alasanResp = jsonResponse.getString("alasan");
                            String judulResp = jsonResponse.getString("judul");

                            //Jika ternyata user belum membuat alasan:
                            if (alasanResp.equals("0")) etContent.setText("");
                            else etContent.setText(alasanResp); //jika sudah membuat alasan, tampilkan alasan tsb

                            //Menampilkan judul alasan yg dipiih user sebelumnya
                            if(judulResp.equals("Kendaraan bermasalah"))spinnerStatus.setSelection(1);
                            else if(judulResp.equals("Keluarga sakit"))spinnerStatus.setSelection(2);
                            else if(judulResp.equals("Terlambat bangun"))spinnerStatus.setSelection(3);
                            else if(judulResp.equals("Lain-lain"))spinnerStatus.setSelection(4);
                            else spinnerStatus.setSelection(0); //Jika belum memilih maka akan men-set default dropdown

                            date.setText(waktu);
                            Log.d("PESAN", "onResponse: " + user);

                            //Jika ternyata usernya admin, maka tidak dapat melakukan updateAlasan
                            if(user.equals("admin")){
                                spinnerStatus.setEnabled(false);
                                etContent.setEnabled(false);
                                ajukan.setVisibility(View.GONE);
                            }
                            //Selain dari admin dapat melakukan update alasan
                            else{
                                ajukan.setVisibility(View.VISIBLE);
                                spinnerStatus.setEnabled(true);
                                etContent.setEnabled(true);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Izin.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>data = new HashMap<>();
                //Mengirim data ke server
                data.put("waktu", waktu);
                data.put("username", username);
                fetch2 = true;
                Log.d("PESAN", "getParams: " + waktu + " " + username);
                return data;
            }
        };

// Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(Izin.this);
        queue.add(stringRequest);
    }

}