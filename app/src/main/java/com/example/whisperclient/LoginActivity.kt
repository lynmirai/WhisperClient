package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whisperclient.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login button を押す時
        binding.loginButton.setOnClickListener {

            //User存在する場合、Timeline画面へ
            val i = Intent(this, TimelineActivity::class.java)
            startActivity(i)

        }

        //Create User button を押す時
        binding.createButton.setOnClickListener {
            val i = Intent(this, CreateUserActivity::class.java)
            startActivity(i)
        }
    }
}