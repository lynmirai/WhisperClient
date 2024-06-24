package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.whisperclient.databinding.ActivityUserEditBinding
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

class UserEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserEditBinding


    private var userId: String? = null
    private  var profile: String? = null
    private var userName: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_user_edit)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        //グローバル変数の取得
        val app = application as MyApplication
        val loginUserId = app.loginUserId

//        val userIdText = binding.userIdText.text.toString()
        var userNameEdit = binding.userNameEdit.text.toString()
        var profileEdit = binding.profileEdit.text.toString()

        //ユーザー情報取得APIをリクエスト
        // HTTP接続用インスタンス生成
        val client = OkHttpClient()
        // JSON形式でパラメータを送るようデータ形式を設定
        val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
        // Bodyのデータ(APIに渡したいパラメータを設定)
        val requestBody = """
                    {
                        "userId":"$loginUserId"
                    }
                    """.trimIndent()
        println("Request Body: $requestBody")
        // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
        val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/userInfo.php").post(requestBody.toRequestBody(mediaType)).build()
        println("Request: $request")
        //コールバック処理
        client.newCall(request!!).enqueue(object  : Callback {
            //リクエストが失敗したとき
            override fun onFailure(call: Call, e: IOException) {
                //エラーメッセージ表示
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
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
                        //リストを変数に入れる
                        val userInfoList = jsonResponse.optJSONArray("list")
                        //受け取ったリストから中身を変数に入れる
                        if(userInfoList.length() > 0){
                            val userObject = userInfoList.getJSONObject(0)
                            userId = userObject.optString("userId")
                            profile = userObject.optString("profile")
                            userName = userObject.optString("userName")
                            val iconPath = userObject.optString("iconPath")
                            //受け取った値をオブジェクトに格納してUIを更新する
                            runOnUiThread {
                                binding.userIdText.text = userId
                                binding.profileEdit.setText(profile)
                                binding.userNameEdit.setText(userName)
                                println("UIを更新しました: userId=$userId")
                            }
                        }
                    } else {
                        //エラーメッセージ表示
                        val errorMessage = jsonResponse.optString("errorMessage")
                        throw IOException(errorMessage)
                    }
                }catch (e:Exception){
                    //JSONデータがエラーの場合
                    val errorCode = jsonResponse.optString("errCode")
                    val errorMessage = jsonResponse.optString("errMsg")
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        //change button
        binding.changeButton.setOnClickListener {
            //入力されたテキストを変数に入れる
            userName = binding.userNameEdit.text.toString().trim()
            profile = binding.profileEdit.text.toString().trim()

            // HTTP接続用インスタンス生成
            val client = OkHttpClient()
            // JSON形式でパラメータを送るようデータ形式を設定
            val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
            // Bodyのデータ(APIに渡したいパラメータを設定)
            val requestBody = """
                    {
                        "userId":"$userId",
                        "userName":"$userName",
                        "password":" ",
                        "profile":"$profile"
                    }
                    """.trimIndent()
            println("Request Body: $requestBody")
            // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
            val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/userUpd.php").post(requestBody.toRequestBody(mediaType)).build()
            println("Request: $request")
            //コールバック処理
            client.newCall(request!!).enqueue(object  : Callback {
                //リクエストが失敗したとき
                override fun onFailure(call: Call, e: IOException) {
                    //エラーメッセージ表示
                    runOnUiThread {
                        Toast.makeText(this@UserEditActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
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
                            val intent = Intent(this@UserEditActivity,UserInfoActivity::class.java)
                            intent.putExtra("userId",loginUserId)
                            //ユーザ情報画面に遷移
                            startActivity(intent)
                            //画面を閉じる
                            finish()
                        } else {
                            //エラーメッセージ表示
                            val errorMessage = jsonResponse.optString("errorMessage")
                            throw IOException(errorMessage)
                        }
                    }catch (e:Exception){
                        //JSONデータがエラーの場合
                        val errorCode = jsonResponse.optString("errCode")
                        val errorMessage = jsonResponse.optString("errMsg")
                        runOnUiThread {
                            Toast.makeText(this@UserEditActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        //cancel button
        binding.cancelButton.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
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