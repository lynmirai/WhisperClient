package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperclient.databinding.ActivitySearchBinding
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

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    //recyclerViewの宣言
    lateinit var srv : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //recyclerViewをレイアウトと紐づける
        srv = findViewById(R.id.searchRecycle)
        srv.layoutManager = LinearLayoutManager(applicationContext)

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        var goodList = mutableListOf<String>()

        binding.searchButton.setOnClickListener{
            //検索するテキストを取得
            val searchText = binding.searchEdit.text.toString().trim()
            println("$searchText")
            //ラジオグループで選択されたラジオボタンのIDを取得
            val section = when (binding.radioGroup.checkedRadioButtonId){
                R.id.userRadio -> "1"
                R.id.whisperRadio -> "2"
                else -> ""
            }
            println("$section")
            if(searchText.isEmpty()){
                runOnUiThread {
                    //エラーToast表示
                    Toast.makeText(this,"空白エラー", Toast.LENGTH_SHORT).show()
                }
            }else{
                // HTTP接続用インスタンス生成
                val client = OkHttpClient()
                // JSON形式でパラメータを送るようデータ形式を設定
                val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
                // Bodyのデータ(APIに渡したいパラメータを設定)
                val requestBody = """
                    {
                        "section":"${section.toInt()}",
                        "string":"${binding.searchEdit.text}"
                    }
                    """.trimIndent()
                println("requestbody:$requestBody")
                // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
                val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/search.php").post(requestBody.toRequestBody(mediaType)).build()
                println("request:$request")
                //ささやき登録処理APIをリクエストして入力したささやきの登録処理を行う
                //リクエスト送信
                client.newCall(request!!).enqueue(object  : Callback {
                    //リクエストが失敗したとき
                    override fun onFailure(call: Call, e: IOException) {
                        //エラーメッセージ表示
                        runOnUiThread{
                            Toast.makeText(
                                this@SearchActivity,
                                "リクエスト失敗:${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    //リクエストが成功したとき
                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        try {
                            println("レスポンスを受信しました:$responseBody")
                            val result = jsonResponse.optString("result")
                            if (result == "success") {
                                //ユーザ情報を格納するリストを作成
                                val users = mutableListOf<User>()
                                //ユーザ情報をリストに格納する
                                val userList = jsonResponse.optJSONArray("list")
                                if(userList != null){
                                    //ユーザ情報が存在する間ループ
                                    for(i in 0 until userList.length()){
                                        //
                                        val userJson = userList.getJSONObject(i)
                                        users.add(
                                            User(
                                                userName = userJson.optString("userName"),
                                                follow = userJson.optInt("followCnt"),
                                                follower = userJson.optInt("followerCnt"),
                                                userImg = R.drawable.ic_launcher_background,
                                                userId = userJson.optString("userId")
                                            )
                                        )
                                    }
                                }
                                //いいね情報を格納するリストを作成
                                val goods = mutableListOf<Good>()
                                //いいね情報をリストに格納する
                                val goodList = jsonResponse.optJSONArray("list")
                                if(goodList != null){
                                    for (i in 0 until goodList.length()){
                                        val goodJson = goodList.getJSONObject(i)
                                        goods.add(
                                            Good(
                                                content = goodJson.optString("content"),
                                                userName = goodJson.optString("userName"),
                                                userImg = R.drawable.ic_launcher_background,
                                                gcnt = goodJson.optInt("cnt"),
                                                userId = goodJson.optString("userId")
                                            )
                                        )
                                    }
                                }

                                runOnUiThread {
                                    val app = application as MyApplication
                                    //ラジオボタンがuserRadioを選択している場合
                                    if (section == "1") {
                                        srv.adapter = UserAdapter(app,users)
                                        //ラジオボタンがwhisperRadioを選択している場合
                                    } else if (section == "2") {
                                        srv.adapter = GoodAdapter(app,goods)
                                    }
                                }
                            } else {
                                val errorMessage = jsonResponse.optString("errorMessage")
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity,errorMessage,Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            //
                            val errorCode = jsonResponse.optString("errCode")
                            val errorMessage = jsonResponse.optString("errMsg")
                            Toast.makeText(
                                this@SearchActivity,
                                "$errorMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
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
