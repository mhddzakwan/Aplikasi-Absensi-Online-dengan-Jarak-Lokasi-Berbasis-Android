package com.example.proyek.aplikasiabsensi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    //Data akan dikirim dan menerima respons melalui url berikut:
    public static final String url = "http://ummuhafidzah.sch.id/absensi/login.php";
    EditText editPassword, editUsername;
    AppCompatButton btnLogin;
    String username,password;
    // Handler dan runnable dipakai untuk looping dengan durasi waktu tertentu
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    boolean fetch = true;
    //Frame dan progressbar untuk UI loading
    ProgressBar progressBar;
    FrameLayout overLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editPassword = findViewById(R.id.passwordLogin);
        editUsername = findViewById(R.id.usernameLogin);
        btnLogin = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);
        overLayout = (FrameLayout) findViewById(R.id.overlayLayout);

        //Menjalankan looping dengan jarak antar loop 1,5 detik
        //dikarenakan ketika mencoba mengambil data, terkadang data tidak terambil,
        // jadi perlu diloop sampai data benar" terambil
        runnable = new Runnable() {
            @Override
            public void run() {
                //kalau data belum terambil dari server (fetch == false), maka ambil data ke server lagi dan mulai loading
                //jika sudah (fetch == true), data tidak perlu diambil lagi
                if(!fetch)cekLogin();
                else{
                    //kalau data sudah diambil dari server, matikan loading
                    progressBar.setVisibility(View.INVISIBLE);
                    overLayout.setVisibility(View.INVISIBLE);
                }
                handler.postDelayed(this, 1500); // 1.5 seconds
            }
        };
        handler.post(runnable);



        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Mengambil username dan password dari editText
                username = editUsername .getText().toString();
                password = editPassword .getText().toString();
                if((username.equals("")) ||( password.equals(""))){
                    Toast.makeText(Login.this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }else {
                    //kalau isi editTextnya kosong, mulai cekLogin(ambil data dari server)
                    fetch = false; //agar loop handler berjalan dan trus loop sampai data terambil
                    progressBar.setVisibility(View.VISIBLE); //Munculkan loading
                    overLayout.setVisibility(View.VISIBLE);
                    cekLogin();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        //Saat pindah dari activity ini, handler akan dimatikan
        super.onPause();
        Log.d("HANDLER", "onPause: STOP ");
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume(){
        //Saat kembali memasukan activity ini, handler akan dijalan kembali
        super.onResume();
        handler.post(runnable);
    }

    void cekLogin(){
            //Program Volley
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("Pesan2",   username +" " + password );

                                //Menerima respons dari API
                                JSONObject jsonResponse = new JSONObject(response);
                                String pesan = jsonResponse.getString("status").trim();

                                //pesan, merupakan status apakah username dan pw nya bener, penegcekan dilakukan di API php
                                if (pesan.equals("berhasil")){
                                    if(username.equals("admin")){
                                        //Jika usernamenya admin, maka akan diarahkan ke halaman admin
                                        Intent admin = new Intent(Login.this, Admin.class);
                                        admin.putExtra("username", username);
                                        startActivity(admin);
                                    }else {
                                        //Selain dari admin, akan diarahkan ke pegawai
                                        Intent home = new Intent(Login.this, MainActivity.class);
                                        home.putExtra("username", username);
                                        startActivity(home);
                                    }
                                    finish();
                                }else {
                                    if(fetch)Toast.makeText(Login.this, "salah password", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", "onErrorResponse: " + error.toString());
                    Toast.makeText(Login.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    //Mengirim data username dan password untuk dilakukan pengecekan di API
                    data.put("username", username);
                    data.put("password", password);
                    //fetch = true, artinya data sudah berhasil dikirim dan loop handler tidak perlu dijalankan lagi
                    fetch = true;
                    Log.d("Pesan1", "getParams: onReq" + username +" " + password );
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
    }
}