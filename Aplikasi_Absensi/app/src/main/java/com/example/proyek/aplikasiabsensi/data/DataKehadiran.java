package com.example.proyek.aplikasiabsensi.data;

/**
 * Kelas DataKehadiran merepresentasikan data kehadiran karyawan.
 * Kelas ini mencakup informasi seperti nama, jabatan, status kehadiran, waktu, jam hadir, dan jam pulang.
 */

public class DataKehadiran {
    private String waktu, status, jamHadir, jamPulang,nama,jabatan;

    public DataKehadiran() {
    }

    /**
     * Konstruktor dengan parameter.
     * Digunakan untuk membuat objek DataKehadiran dengan data awal.
     *
     * @param nama     Nama karyawan.
     * @param jabatan  Jabatan karyawan.
     * @param status   Status kehadiran karyawan (misalnya hadir, izin, sakit).
     * @param waktu    Waktu kehadiran.
     * @param jamHadir Jam hadir karyawan.
     * @param jamPulang Jam pulang karyawan.
     */

    public DataKehadiran(String nama, String jabatan,String status, String waktu, String jamHadir, String jamPulang){
        this.nama = nama;
        this.jabatan = jabatan;
        this.status = status;
        this.waktu = waktu;
        this.jamHadir = jamHadir;
        this.jamPulang = jamPulang;

    }

    public String getNama() {
        return nama;
    }

    public void setnama(String nama) {
        this.nama = nama;
    }
    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getJamHadir() {
        return jamHadir;
    }

    public void setJamHadir(String jamHadir) {
        this.jamHadir = jamHadir;
    }

    public String getJamPulang() {
        return jamPulang;
    }

    public void setJamPulang(String jamPulang) {
        this.jamPulang = jamPulang;
    }
}
