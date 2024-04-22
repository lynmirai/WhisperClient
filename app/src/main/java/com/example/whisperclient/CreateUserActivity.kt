package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whisperclient.databinding.ActivityCreateUserBinding

class CreateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_create_user)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Create User button を押す時
        binding.createButton.setOnClickListener {

            //User作成できたら、、Timeline画面へ
            val i = Intent(this, TimelineActivity::class.java)
            startActivity(i)
        }

        //Cancel button を押す時
        binding.cancelButton.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
    }
}