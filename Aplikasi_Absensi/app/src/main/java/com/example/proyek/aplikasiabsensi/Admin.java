package com.example.proyek.aplikasiabsensi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyek.aplikasiabsensi.adapter.AdapterUser;
import com.example.proyek.aplikasiabsensi.data.DataUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Admin extends AppCompatActivity {
    public static final String url = "http://ummuhafidzah.sch.id/absensi/dataUser.php";
    public static ArrayList<DataUser> DataUserArrayList = new ArrayList<>();
    ListView list;
    AdapterUser adapter;
    ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        list = findViewById(R.id.myList);
        logout = findViewById(R.id.logout);
        // Inisialisasi adapter
        adapter = new AdapterUser(this, DataUserArrayList);
        list.setAdapter(adapter);
        lihatData();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ambil data nama dari item yang diklik
                String username = DataUserArrayList.get(position).getUsername();

                // Buat Intent untuk berpindah ke DetailActivity
                Intent intent = new Intent(Admin.this, Riwayat.class);
                intent.putExtra("username", username);
                intent.putExtra("user", "admin");
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Admin.this, Login.class);
                startActivity(intent);
            }
        });

    }


    void lihatData(){
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        DataUserArrayList.clear();
                        Log.d("masuk", "MULAI MASUK ");
                        try {
                            Log.d("muali try", "MULAI try ");

                            JSONObject jsonObject = new JSONObject(response);
                                list.setVisibility(View.VISIBLE);
                                JSONArray jsonArray = jsonObject.getJSONArray("dataUser");
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String nama = object.getString("nama");
                                    String jabatan = object.getString("jabatan");
                                    String username = object.getString("username");
                                    Log.d("namaUser:", nama);

                                    DataUser dataUser = new DataUser(nama,jabatan,username);
                                    DataUserArrayList.add(dataUser);
                                    adapter.notifyDataSetChanged();
                                }


                        } catch (JSONException e) {
                            Toast.makeText(Admin.this, "error carch:" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERRORvolley", error.getMessage());
                Toast.makeText(Admin.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}