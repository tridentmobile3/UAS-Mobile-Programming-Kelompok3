package com.feisal.workingreport.model

data class Lembur(
    var id: String = "",
    val userId: String = "",
    val namaKaryawan: String = "",
    val nip: String = "",
    val tanggal: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val alasanLembur: String = "",
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)