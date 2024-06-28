package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.whisperclient.databinding.ActivityLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val userIdEdit = binding.userIdEdit.text.toString().trim()
            val passwordEdit = binding.passwordEdit.text.toString().trim()

            if (passwordEdit.isEmpty()) {
                Toast.makeText(this@LoginActivity, "ユーザーIDとパスワードを入力してください", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if (userIdEdit.isEmpty()){
                Toast.makeText(this@LoginActivity,"ユーザIDが指定されていません",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if (userIdEdit.isEmpty() || passwordEdit.isEmpty()){
                Toast.makeText(this@LoginActivity,"パスワードが指定されていません ",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = "{\"userId\":\"$userIdEdit\",\"password\":\"$passwordEdit\"}"
            val request = Request.Builder()
                .url("https://click.ecc.ac.jp/ecc/whisper24_d/loginAuth.php")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "リクエストに失敗しました", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body.string()
                        val jsonResponse = JSONObject(responseBody ?: "")
                        val result = jsonResponse.optString("result", "error")
                        val list = jsonResponse.optJSONArray("list")

                        runOnUiThread {
                            if (result == "success" && list != null && list.length() == 1) {
                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                val app = application as MyApplication
                                app.loginUserId = userIdEdit
                                val intent = Intent(this@LoginActivity, TimelineActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val errorMessage = jsonResponse.optString("007", "ユーザIDまたはパスワードが違います")
                                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "サーバーエラー: ${response.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }

        binding.createButton.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }
    }
}
