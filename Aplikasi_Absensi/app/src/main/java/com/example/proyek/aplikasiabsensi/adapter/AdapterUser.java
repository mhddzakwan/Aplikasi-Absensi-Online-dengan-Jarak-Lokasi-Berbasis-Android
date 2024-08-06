package com.example.proyek.aplikasiabsensi.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyek.aplikasiabsensi.R;
import com.example.proyek.aplikasiabsensi.data.DataUser;

import java.util.List;

/**
 * AdapterUser adalah kelas adapter yang digunakan untuk menghubungkan data pengguna pada Activity Admin
 * dengan tampilan ListView.
 */

public class AdapterUser extends ArrayAdapter<DataUser> {
    Context context;
    List<DataUser> arrayListUser;

    /**
     * Konstruktor untuk membuat instance dari AdapterUser.
     *
     * @param context Konteks aktivitas yang menggunakan adapter ini.
     * @param arrayListUser Daftar data pengguna yang akan ditampilkan.
     */

    public AdapterUser(@NonNull Context context, List<DataUser> arrayListUser) {
        super(context, R.layout.list_user, arrayListUser);
        this.context = context;
        this.arrayListUser = arrayListUser;
    }

    /**
     * Mengatur tampilan untuk setiap item dalam ListView.
     *
     * @param position Posisi item dalam daftar.
     * @param convertView Tampilan yang dapat digunakan kembali.
     * @param parent Parent view yang berisi tampilan item.
     * @return Tampilan yang diperbarui dengan data pengguna.
     */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        @SuppressLint({"ResourceType", "ViewHolder", "InflateParams"}) View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user, null, true);

        TextView tvNama = view.findViewById(R.id.nama);
        TextView tvJabatan = view.findViewById(R.id.jabatan);
        //TextView tvStatus = view.findViewById(R.id.status);

        //Menampilkan data sesuai dengan format di list_user.xml
        tvNama.setText(arrayListUser.get(position).getNama());
        tvJabatan.setText(arrayListUser.get(position).getJabatan());


        //tvStatus.setText(arrayListUser.get(position).getStatus());
//        if (Objects.equals(arrayListUser.get(position).getStatus(), "Tepat")){
//            tvStatus.setBackgroundResource(R.drawable.data_tepat_badge);
//            tvStatus.setText(arrayListUser.get(position).getStatus());
//        }else if (Objects.equals(arrayListUser.get(position).getStatus(), "Telat")) {
//            tvStatus.setBackgroundResource(R.drawable.data_telat_badge);
//            tvStatus.setText(arrayListUser.get(position).getStatus());
//        }else{
//
//        }

        return view;
    }
}
