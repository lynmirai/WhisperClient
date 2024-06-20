package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperclient.databinding.ActivityWhisperBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class WhisperActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWhisperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWhisperBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //グローバル変数の取得
        val app = application as MyApplication
        val loginUserId = app.loginUserId

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        //whisper登録
        binding.whisperButton.setOnClickListener {
            //ささやきを取得
            val whisper = binding.whisperEdit.text.toString().trim()

            //ささやき入力欄が空白ならToast表示
            if(whisper.isEmpty()){
                //エラーToast表示
                Toast.makeText(this@WhisperActivity,"空白エラー",Toast.LENGTH_SHORT).show()
            }else{
                // HTTP接続用インスタンス生成
                val client = OkHttpClient()
                // JSON形式でパラメータを送るようデータ形式を設定
                val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
                // Bodyのデータ(APIに渡したいパラメータを設定)
                val requestBody = """
                    {
                        "content":"${binding.whisperEdit.text.toString().trim()}",
                        "userId":"$loginUserId"
                    }
                    """.trimIndent()
                println("Request Body: $requestBody")

                // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
                val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/whisperAdd.php")
                    .post(requestBody.toRequestBody(mediaType)).build()
                println("Request: $request")


                //ささやき登録処理APIをリクエストして入力したささやきの登録処理を行う
                //リクエスト送信
                client.newCall(request!!).enqueue(object  : Callback {
                    //リクエストが失敗したとき
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@WhisperActivity, "リクエスト失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //リクエストが成功したとき
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        try{
                            println("レスポンスを受信しました:$responseBody")
                            val result = jsonResponse.optString("result")
                            if (result == "success") {
                                //インテントにログインユーザIDをセット
                                val intent = Intent(this@WhisperActivity,UserInfoActivity::class.java)
                                intent.putExtra("userId",loginUserId)
                                //ユーザ情報画面に遷移
                                startActivity(intent)
                                //画面を閉じる
                                finish()
                            } else {
                                val errorMessage = jsonResponse.optString("errorMessage")
                                throw java.io.IOException(errorMessage)
                            }
                        }catch (e:Exception){
                            //
                            val errorCode = jsonResponse.optString("errCode")
                            runOnUiThread {
                                val errorMessage = jsonResponse.optString("errMsg")
                                Toast.makeText(this@WhisperActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }

        //cancel
        binding.cancelButton.setOnClickListener {
            val intent = Intent(this@WhisperActivity, WhisperActivity::class.java)
            startActivity(intent)
        }

    }

    //
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add(3, 31, 1, "TimeLine")
        menu?.add(3, 32, 2, "Search")
        menu?.add(3, 33, 3, "Whisper")
        menu?.add(3, 34, 4, "Profile")
        menu?.add(3, 35, 5, "UserEdit")
        menu?.add(3, 36, 6, "Logout")
    }

    //
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            31 -> {
                val i = Intent(this, TimelineActivity::class.java)
                startActivity(i)
            }
            32 -> {
                val i = Intent(this, SearchActivity::class.java)
                startActivity(i)
            }
            33 -> {
                val i = Intent(this, WhisperActivity::class.java)
                startActivity(i)
            }
            34 -> {
                val i = Intent(this, UserInfoActivity::class.java)
                startActivity(i)
            }
            35 -> {
                val i = Intent(this, UserEditActivity::class.java)
                startActivity(i)
            }
            36 -> {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            }
        }
        return super.onContextItemSelected(item)
    }
}