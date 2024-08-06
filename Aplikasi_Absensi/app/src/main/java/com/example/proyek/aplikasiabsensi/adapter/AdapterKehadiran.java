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
import com.example.proyek.aplikasiabsensi.data.DataKehadiran;

import java.util.List;
import java.util.Objects;

/**
 * AdapterKehadiran adalah kelas adapter yang digunakan untuk menghubungkan data kehadiran
 * dan menampilkan ListView di Activity Riwayat.
 */

public class AdapterKehadiran extends ArrayAdapter<DataKehadiran> {
    Context context;
    List<DataKehadiran> arrayListKehadiran;

    /**
     * Konstruktor untuk membuat instance dari AdapterKehadiran.
     *
     * @param context Konteks aktivitas yang menggunakan adapter ini.
     * @param arrayListKehadiran Daftar data kehadiran yang akan ditampilkan.
     */

    public AdapterKehadiran(@NonNull Context context, List<DataKehadiran> arrayListKehadiran) {
        super(context, R.layout.list, arrayListKehadiran);
        this.context = context;
        this.arrayListKehadiran = arrayListKehadiran;
    }

    /**
     * Mengatur tampilan untuk setiap item dalam ListView.
     *
     * @param position Posisi item dalam daftar.
     * @param convertView Tampilan yang dapat digunakan kembali.
     * @param parent Parent view yang berisi tampilan item.
     * @return Tampilan yang diperbarui dengan data kehadiran.
     */

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        @SuppressLint({"ResourceType", "ViewHolder", "InflateParams"}) View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, null, true);

        //Inisialisasi variabel dengan id sesuai di layout list.xml
        TextView tvNama = view.findViewById(R.id.nama);
        TextView tvJabatan = view.findViewById(R.id.jabatan);
        TextView tvWaktu = view.findViewById(R.id.waktu);
        TextView tvJam = view.findViewById(R.id.jam);
        TextView tvTepat = view.findViewById(R.id.tepat);

        //tampilkan belum jika ternyata user belum melakukan absensi pulang
        if(Objects.equals(arrayListKehadiran.get(position).getJamPulang(), "0")){
            arrayListKehadiran.get(position).setJamPulang("belum");
        }

        //menampilkan data sesuai dengan card yg dibuat di list.xml
        tvNama.setText(arrayListKehadiran.get(position).getNama());
        tvJabatan.setText(arrayListKehadiran.get(position).getJabatan());
        tvWaktu.setText(arrayListKehadiran.get(position).getWaktu());
        tvJam.setText(arrayListKehadiran.get(position).getJamHadir() + "-" + arrayListKehadiran.get(position).getJamPulang());

        //Memeberi background hijau jika user hadir tepat waktu
        if (Objects.equals(arrayListKehadiran.get(position).getStatus(), "Tepat")){
            tvTepat.setBackgroundResource(R.drawable.data_tepat_badge);
            tvTepat.setText(arrayListKehadiran.get(position).getStatus());
        }
        //Memeberi background merah jika user hadir telat
        else if (Objects.equals(arrayListKehadiran.get(position).getStatus(), "Telat")) {
            tvTepat.setBackgroundResource(R.drawable.data_telat_badge);
            tvTepat.setText(arrayListKehadiran.get(position).getStatus());
        }

        return view;
    }

}
