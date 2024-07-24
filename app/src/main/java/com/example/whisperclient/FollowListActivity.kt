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
import com.example.whisperclient.databinding.ActivityFollowListBinding
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

class FollowListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFollowListBinding
    //recyclerViewの宣言
    lateinit var srv : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_follow_list)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        //recyclerViewをレイアウトと紐づける
        srv = binding.followRecycle
        srv.layoutManager = LinearLayoutManager(this)

        //渡されてきたユーザIDとフォローステータスを受け取る
        var userId = intent.getStringExtra("userId")
        val followStatus = intent.getStringExtra("followStatus")
        println("受け取ったゆーざID:$userId,フォロー状態:$followStatus")

        //フォローならfollowListにテキストを変える
        if(followStatus == "follow"){
            binding.followListText.text = "followList"
        }else if(followStatus == "follower"){
            binding.followListText.text = "followerList"
        }

        // HTTP接続用インスタンス生成
        val client = OkHttpClient()
        // JSON形式でパラメータを送るようデータ形式を設定
        val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
        // Bodyのデータ(APIに渡したいパラメータを設定)
        val requestBody = """
                    {
                        "userId":"$userId"
                    }
                    """.trimIndent()
        println("Request Body: $requestBody")
        // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
        val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/followerInfo.php").post(requestBody.toRequestBody(mediaType)).build()
        println("Request: $request")
        //コールバック処理
        client.newCall(request!!).enqueue(object  : Callback {
            //リクエストが失敗したとき
            override fun onFailure(call: Call, e: IOException) {
                //エラーメッセージ表示
                runOnUiThread {
                    Toast.makeText(this@FollowListActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            //リクエストが成功したとき
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)
                try{
                    println("レスポンスを受信しました:$responseBody")
                    val result = jsonResponse.optString("result")
//                    if (result == "success") {
                    if(followStatus == "follow"){
                        //ユーザ情報を格納するリストを作成
                        val users = mutableListOf<User>()
                        //ユーザ情報をリストに格納する
                        val userList = jsonResponse.optJSONArray("followList")
                        if(userList != null){
                            //ユーザ情報が存在する間ループ
                            for(i in 0 until userList.length()){
                                //
                                val userJson = userList.getJSONObject(i)
                                users.add(
                                    User(
                                        userName = userJson.optString("userName"),
                                        follow = userJson.optInt("followcnt"),
                                        follower = userJson.optInt("followercnt"),
                                        userImg = R.drawable.ic_launcher_background,
                                        userId = userJson.optString("userId")
                                    )
                                )
                            }
                        }
                        //リサイクラービューにセットする
                        runOnUiThread {
                            val app = application as MyApplication
                            srv.adapter = UserAdapter(app, users)
                            srv.adapter?.notifyDataSetChanged() // これを追加してアダプタの変更を通知
                        }
                    }else{
                        //ユーザ情報を格納するリストを作成
                        val users = mutableListOf<User>()
                        //ユーザ情報をリストに格納する
                        val userList = jsonResponse.optJSONArray("followerList")
                        if(userList != null){
                            //ユーザ情報が存在する間ループ
                            for(i in 0 until userList.length()){
                                //
                                val userJson = userList.getJSONObject(i)
                                users.add(
                                    User(
                                        userName = userJson.optString("followerUser"),
                                        follow = userJson.optInt("followcnt"),
                                        follower = userJson.optInt("followercnt"),
                                        userImg = R.drawable.ic_launcher_background,
                                        userId = userJson.optString("userId")
                                    )
                                )
                            }
                        }
                        //リサイクラービューにセットする
                        runOnUiThread {
                            val app = application as MyApplication
                            srv.adapter = UserAdapter(app, users)
                            srv.adapter?.notifyDataSetChanged() // これを追加してアダプタの変更を通知
                        }
                    }


//                    } else {
//                        //エラーメッセージ表示
//                        val errorMessage = jsonResponse.optString("errorMessage")
//                        throw IOException(errorMessage)
//                    }
                }catch (e:Exception){
                    //JSONデータがエラーの場合
                    val errorCode = jsonResponse.optString("errCode")
                    val errorMessage = jsonResponse.optString("errMsg")
                    runOnUiThread {
                        Toast.makeText(this@FollowListActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

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
