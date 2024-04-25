package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whisperclient.databinding.ActivityCreateUserBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CreateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_create_user)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Create User button を押す時
        binding.createButton.setOnClickListener {
            if (binding.passwordEdit.text.toString() != binding.rePasswordEdit.text.toString()) {
                // Hiển thị thông báo nếu mật khẩu không khớp
                Toast.makeText(
                    this@CreateUserActivity,
                    "パスワードが一致しません",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // HTTP接続用インスタンス生成
                val client = OkHttpClient()
                // JSON形式でパラメータを送るようデータ形式を設定
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                // Bodyのデータ(APIに渡したいパラメータを設定)
//            val requestBody = "{\"userId\":\"${binding.userIdEdit.text}\",\n\"userName\":\"${binding.userNameEdit.text}\",\n\"password\":\"${binding.passwordEdit.text}\"}"
                val requestBody = """
                {
                    "userId" : "${binding.userIdEdit.text}",
                    "userName" : "${binding.userNameEdit.text}",
                    "password" : "${binding.passwordEdit.text}"
                }
            """.trimIndent()
                println("Request Body: $requestBody")
                // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
                val request = Request.Builder().url("http://10.200.2.86/whisper/userAdd.php")
                    .post(requestBody.toRequestBody(mediaType)).build()
                println("Request: $request")
                // リクエスト送信（非同期処理）
                client.newCall(request).enqueue(object : Callback {
                    // リクエストが失敗した場合の処理を実装
                    override fun onFailure(call: Call, e: IOException) {
                        // runOnUiThreadメソッドを使うことでUIを操作することができる。(postメソッドでも可)
                        this@CreateUserActivity.runOnUiThread {
//                        binding.tvJson.text = "リクエストが失敗しました: ${e.message}"
                            println("Request failed: ${e.message}")
                            Toast.makeText(
                                this@CreateUserActivity,
                                "\"Request failed: ${e.message}\"",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // リクエストが成功した場合の処理を実装
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        try {
                            val result = jsonResponse.optString("result")
                            if (result == "success") {
                                val intent =
                                    Intent(this@CreateUserActivity, TimelineActivity::class.java)
                                startActivity(intent)
                            } else {
                                val errorMessage = jsonResponse.optString("errorMessage")
                                throw IOException(errorMessage)
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                val errorCode = jsonResponse.optString("errCode")
                                val errorMessage = jsonResponse.optString("errMsg")
//                            throw IOException("$errorCode: $errorMessage")
                                Toast.makeText(
                                    this@CreateUserActivity,
                                    "$errorMessage",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
            }
        }

        //Cancel button を押す時
        binding.cancelButton.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
    }
}