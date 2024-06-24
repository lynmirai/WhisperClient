package com.example.whisperclient

import android.app.Application

class MyApplication: Application() {
    var loginUserId: String = ""
    //    var apiUrl: String = "https://click.ecc.ac.jp/ecc/whisper24_d/"
    var apiUrl: String = "http://click.ecc.ac.jp/ecc/whisperSystem/"


    override fun onCreate() {
        super.onCreate()

    }
}