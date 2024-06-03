package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whisperclient.databinding.ActivityLoginBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login button を押す時
        binding.loginButton.setOnClickListener {
            val userIdEdit = binding.userIdEdit.text.toString().trim()  //ユーザーID
            val passwordEdit = binding.passwordEdit.text.toString().trim() //パスワード
            val client = OkHttpClient() //HTTP接続用インスタンス
            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()  //JSON型パラメーター
            //bodyデータAPIに渡したいデータ
            val requestBody = "{\"userId\":\"$userIdEdit\",\"password\":\"$passwordEdit\"}"
            // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
            val request = Request.Builder()
                .url("http://10.200.2.79/whisper/loginAuth.php")
                .post(requestBody.toRequestBody(mediaType)).build()

            Toast.makeText(this,"受信", Toast.LENGTH_SHORT).show()

            // リクエスト送信（非同期処理）
            client.newCall(request!!).enqueue(object : Callback {
                // リクエストが失敗した場合の処理を実装
                override fun onFailure(call: Call, e: IOException) {
                    this@LoginActivity.runOnUiThread {
                        Toast.makeText(this@LoginActivity,"リクエストに失敗しました",Toast.LENGTH_SHORT).show()
                    }
                }
                // リクエストが成功した場合の処理を実装
                override fun onResponse(call: Call, response: Response) {
                    Toast.makeText(this@LoginActivity,"レスポンスを受信しました",Toast.LENGTH_SHORT).show()
                }
            })
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