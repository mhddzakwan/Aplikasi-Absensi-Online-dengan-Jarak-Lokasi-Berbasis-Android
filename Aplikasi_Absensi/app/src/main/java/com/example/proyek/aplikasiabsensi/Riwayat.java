package com.example.proyek.aplikasiabsensi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyek.aplikasiabsensi.adapter.AdapterKehadiran;
import com.example.proyek.aplikasiabsensi.data.DataKehadiran;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Riwayat extends AppCompatActivity {
    //url untuk akses data di server
    public static final String url = "http://ummuhafidzah.sch.id/absensi/tampilData.php";
    public static final String url2 = "http://ummuhafidzah.sch.id/absensi/hapusData.php";
    public static ArrayList<DataKehadiran> DataKehadiranArrayList = new ArrayList<>();
    ListView list;
    String username, Stahun = "", Sbulan = "",user="", tempWaktu;

    String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
            "Agustus", "September", "Oktober", "November", "Desember"};

    String[] tahun = {"2024", "2025", "2026"};
    AutoCompleteTextView autoCompleteTextView, autoCompleteTextView2;

    ArrayAdapter<String> adapterItems, adapterItems2;
    AdapterKehadiran adapter;
    TextView TvNoData;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;
    boolean fetch = true, fetch2 = true;
    ProgressBar progressBar;
    FrameLayout overLayout;
    View rootView;
    LinearLayout search;
    ImageView imgNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_riwayat);

        username = getIntent().getStringExtra("username");
        if(getIntent().getStringExtra("user") != null) user = getIntent().getStringExtra("user");
        Log.d("username", "onCreate: " + username);

        // Inisialisasi ListView sebelum mengatur adapter
        list = findViewById(R.id.myList);
        progressBar = findViewById(R.id.progressBar);
        overLayout = (FrameLayout) findViewById(R.id.overlayLayout);
        TvNoData = findViewById(R.id.noData);
        rootView = findViewById(android.R.id.content).getRootView();
        autoCompleteTextView = findViewById(R.id.auto_complete_text);
        autoCompleteTextView2 = findViewById(R.id.auto_complete_text2);
        search = findViewById(R.id.search);
        imgNoData = findViewById(R.id.imgNoData);

        TvNoData.setVisibility(View.GONE);
        imgNoData.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Riwayat Absen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inisialisasi adapter setelah ListView
        adapter = new AdapterKehadiran(this, DataKehadiranArrayList);
        list.setAdapter(adapter);

        //fungsi ketika item diklik
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ambil data nama dari item yang diklik
                Log.d("cekTelat", "onItemClick: masuk " + DataKehadiranArrayList.get(position).getStatus());

                //Jika statusnya telat, maka akan diarahkan ke Izin activity
                // dan user dapat mengubah alasan keterlambatannya
                if(Objects.equals(DataKehadiranArrayList.get(position).getStatus(), "Telat")){
                    String waktu = DataKehadiranArrayList.get(position).getWaktu();
                    Log.d("cekTelat2", "onItemClick: " + waktu);
                    Intent intent = new Intent(Riwayat.this, Izin.class);
                    intent.putExtra("username", username);
                    intent.putExtra("waktu", waktu);
                    if(!user.isEmpty())intent.putExtra("user", user);
                    startActivity(intent);
                }

            }
        });
        //Function Saat diklik lama
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Jika ternyata usernya admin, hanya admin yang dapat menghapus data
                if(user.equals("admin")){
                    tempWaktu = DataKehadiranArrayList.get(position).getWaktu();
                    tampilkanDialog(position);
                }
                return true;
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("HANDLER", "run: ");
                if(!fetch)lihatData();
                else if(!fetch2)hapusData();
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    overLayout.setVisibility((View.INVISIBLE));
                }
                handler.postDelayed(this, 1500); // 1.5 seconds
            }
        };
        handler.post(runnable);
        dropDown();
        //Tombol search untuk mencari data
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                overLayout.setVisibility((View.VISIBLE));
                fetch = false;
                lihatData();
            }
        });
        //Mula-mula list kosong
        clearData();

    }
    //Function untuk menampilkan konfirmasi hapus list
    void tampilkanDialog(int position){
        new AlertDialog.Builder(this)
                .setTitle("Hapus Absensi ini")
                .setMessage("Apakah Anda yakin ingin menghapus absensi pada " + tempWaktu + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fetch2 = false;
                        DataKehadiranArrayList.remove(position);
                        adapter.notifyDataSetChanged();
                        //function hapus list item
                        hapusData();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    void hapusData(){
        StringRequest request = new StringRequest(Request.Method.POST, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("masuk", "MULAI MASUK ");
                        //Tidak menerima respon apapun dari server, karena hanya
                        //menghapus data

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.getMessage());
                Toast.makeText(Riwayat.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                //mengirimkan username dan waktu untuk disesuaikan dengan
                //baris data yg akan dihapus
                data.put("username", username);
                data.put("waktu", tempWaktu);
                Log.d("Pesan3", "onResponse: " + username + " " + tempWaktu);
                fetch2 = true;
                return data;
            }
        };
        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    void dropDown(){
        //Menampilkan drop down untuk memilih bulan
        adapterItems = new ArrayAdapter<>(Riwayat.this, R.layout.list_item,bulan);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Ketika item list di klik, datanya akan di simpan di variabel
                Sbulan = parent.getItemAtPosition(position).toString();
                Toast.makeText(Riwayat.this, "item : " + Sbulan , Toast.LENGTH_SHORT).show();

            }
        });
        //Menampilkan dropdown untuk meilih tahun
        adapterItems2 = new ArrayAdapter<>(Riwayat.this, R.layout.list_item_2,tahun);
        autoCompleteTextView2.setAdapter(adapterItems2);

        autoCompleteTextView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Ketika item list di klik, datanya akan di simpan di variabel
                Stahun = parent.getItemAtPosition(position).toString();
                Toast.makeText(Riwayat.this, "item : " + Stahun , Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("HANDLER", "onPause: STOP ");
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(runnable);
    }

    //function untuk menghapus isi list agar kosong saat ditampilkan di UI
    private void clearData() {
        DataKehadiranArrayList.clear();
        adapter.notifyDataSetChanged();
        TvNoData.setVisibility(View.GONE);
        imgNoData.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
    }

    //Function untuk menampilkan data sesuai dengan bulan dan tahun yang dipilih
    void lihatData() {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("masuk", "MULAI MASUK ");
                        clearData();
                        try {
                            Log.d("muali try", "MULAI try ");

                            JSONObject jsonObject = new JSONObject(response);
                            String pesan = jsonObject.getString("status");
                            Log.d("Pesan", "onResponse: " + pesan);

                            //Jika data pada bulan dan tahun tsb ditemukan:
                            if (pesan.trim().equals("berhasil")) {
                                list.setVisibility(View.VISIBLE);
                                TvNoData.setVisibility(View.GONE);
                                imgNoData.setVisibility(View.GONE);

                                //Menerima respon array dari server
                                JSONArray jsonArray = jsonObject.getJSONArray("kehadiran");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    //Mulai memasukkan tiap data dalam array ke list
                                    //data dibawah ini akan berbeda tiap listnya tergantung isi arraynya
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String waktu = object.getString("waktu");
                                    String status = object.getString("status");
                                    String jamhadir = object.getString("jam_hadir");
                                    String jamPulang = object.getString("jam_pulang");

                                    //Tiap list akan berisikan nama dan jabatan yg sama
                                    String nama = jsonObject.getString("nama");
                                    String jabatan = jsonObject.getString("jabatan");

                                    Log.d("nama", "onResponse: " + jamPulang);
                                    //Memasukkan tiap data dalam array ke list
                                    DataKehadiran dataKehadiran = new DataKehadiran(nama, jabatan, status, waktu, jamhadir, jamPulang);
                                    DataKehadiranArrayList.add(dataKehadiran);
                                    adapter.notifyDataSetChanged();
                                    Log.d("arrayData", "onResponse: " + waktu);
                                }
                            }
                            //Jika data pada bulan dan tahun tsb tidak ditemukan:
                            else if (pesan.trim().equals("gagal")) {
                                //Tampilkan pesan data tidak ditemukan dan hilangkan listview
                                TvNoData.setVisibility(View.VISIBLE);
                                imgNoData.setVisibility(View.VISIBLE);
                                TvNoData.setText("Data tidak ditemukan pada " + Sbulan + " " + " " + Stahun);
                                list.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", error.getMessage());
                Toast.makeText(Riwayat.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                //Mengirim data ke server
                data.put("username", username);
                data.put("bulan", Sbulan);
                data.put("tahun", Stahun);
                Log.d("Pesan3", "onResponse: " + username);
                fetch = true;
                return data;
            }
        };
        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        Log.d("Pesan4", "onResponse: " + "addReq");
    }
}
