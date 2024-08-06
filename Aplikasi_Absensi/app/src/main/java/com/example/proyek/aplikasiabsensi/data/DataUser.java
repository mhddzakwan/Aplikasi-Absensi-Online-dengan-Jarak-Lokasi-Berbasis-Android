package com.example.proyek.aplikasiabsensi.data;

/**
 * Kelas DataUser merepresentasikan data pengguna.
 * Kelas ini mencakup informasi seperti nama, jabatan, dan username.
 */
public class DataUser {
    private String nama, jabatan, username;

    public DataUser() {
    }

    /**
     * Konstruktor dengan parameter.
     * Digunakan untuk membuat objek DataUser dengan data awal.
     *
     * @param nama     Nama pengguna.
     * @param jabatan  Jabatan pengguna.
     * @param username Username pengguna.
     */

    public DataUser(String nama, String jabatan, String username) {
        this.nama = nama;
        this.jabatan = jabatan;
        this.username = username;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
