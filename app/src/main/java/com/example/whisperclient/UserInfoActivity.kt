package com.example.whisperclient

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperclient.databinding.ActivityUserInfoBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private var followFlg: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_user_info)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        //ラジオグループの取得
        val radioGroup = binding.radioGroup

        //グローバル変数の取得
        val app = application as MyApplication
        val loginUserId = app.loginUserId

        //渡されてきたユーザIDを受け取る
        var userId = intent.getStringExtra("userId")
        //userIdが渡されてこなかったら自分のuserIdを入れる
        if(userId.isNullOrEmpty()){
            userId = loginUserId
        }
        println("受け取ったゆーざID:$userId")

        //ユーザささやき情報取得API共通実行メソッドを呼び出す
        fetchUserInfo(
            myapp = app,
            userId = userId,
            loginUserId = loginUserId,
            userNameTx = binding.userNameText,
            userProfileTx = binding.profileText,
            followCountTx = binding.followCntText,
            followerCountTx = binding.followerCntText,
            followBtn = binding.followButton,
            userRecycle = binding.userRecycle,
            radioGroup = binding.radioGroup
        )

        //ラジオグループのチェック変更イベントリスナー
        radioGroup.setOnCheckedChangeListener { group, checked ->
            fetchUserInfo(
                myapp = app,
                userId = userId,
                loginUserId = loginUserId,
                userNameTx = binding.userNameText,
                userProfileTx = binding.profileText,
                followCountTx = binding.followCntText,
                followerCountTx = binding.followerCntText,
                followBtn = binding.followButton,
                userRecycle = binding.userRecycle,
                radioGroup = binding.radioGroup
            )
        }

        binding.followCntText.setOnClickListener{
            val intent = Intent(this, FollowListActivity::class.java)
            val followCount = binding.followCntText.text.toString()
            intent.putExtra("follow", "$followCount")
            intent.putExtra("userId", "$userId")
            startActivity(intent)
        }

        binding.followerCntText.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            val followerCount = binding.followerCntText.text.toString()
            intent.putExtra("follower", "$followerCount")
            intent.putExtra("userId", "$userId")
            startActivity(intent)
        }

        binding.followButton.setOnClickListener {
            //フォロー管理APIをリクエスト
            // HTTP接続用インスタンス生成
            val client = OkHttpClient()
            // JSON形式でパラメータを送るようデータ形式を設定
            val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
            // Bodyのデータ(APIに渡したいパラメータを設定)
            val newFollowFlg = !followFlg
            val requestBody = """
                {
                    "userId":"$loginUserId",
                    "followUserId":"$userId",
                    "followFlg":"$newFollowFlg"
                }
                """.trimIndent()
            println("Request Body: $requestBody")
            // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
            val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/followCtl.php").post(requestBody.toRequestBody(mediaType)).build()
            println("Request: $request")
            //コールバック処理
            client.newCall(request!!).enqueue(object  : Callback {
                //リクエストが失敗したとき
                override fun onFailure(call: Call, e: IOException) {
                    //エラーメッセージ表示
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
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
                            val intent = Intent(this@UserInfoActivity, UserInfoActivity::class.java)
                            intent.putExtra("userId", "$userId")
                            startActivity(intent)
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
                            Toast.makeText(this@UserInfoActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

    }

    fun fetchUserInfo(
        myapp: Application,
        userId: String?,
        loginUserId: String,
        userNameTx: TextView,
        userProfileTx: TextView,
        followCountTx: TextView,
        followerCountTx: TextView,
        followBtn: Button,
        userRecycle: RecyclerView,
        radioGroup: RadioGroup
    ){

        userRecycle.layoutManager = LinearLayoutManager(this)

        //ユーザー情報取得APIをリクエスト
        // HTTP接続用インスタンス生成
        val client = OkHttpClient()
        // JSON形式でパラメータを送るようデータ形式を設定
        val mediaType : MediaType = "application/json; charset=utf-8".toMediaType()
        // Bodyのデータ(APIに渡したいパラメータを設定)
        val requestBody = """
                    {
                        "userId":"$userId",
                        "loginUserId":"$loginUserId"
                    }
                    """.trimIndent()
        println("Request Body: $requestBody")
        // Requestを作成(先ほど設定したデータ形式とパラメータ情報をもとにリクエストデータを作成)
        val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/userWhisperInfo.php").post(requestBody.toRequestBody(mediaType)).build()
        println("Request: $request")
        //コールバック処理
        client.newCall(request!!).enqueue(object  : Callback {
            //リクエストが失敗したとき
            override fun onFailure(call: Call, e: IOException) {
                //エラーメッセージ表示
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
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
                        //取得したデータを各オブジェクトにセットする
                        val followFlg = jsonResponse.optBoolean("followflg")
                        this@UserInfoActivity.followFlg = followFlg
                        val profileList = jsonResponse.optJSONArray("profile")
                        println("Profile List: $profileList")
                        if(profileList.length() > 0){
                            val profileObject = profileList.getJSONObject(0)
                            val userName = profileObject.optString("userName")
                            val userProfile = profileObject.optString("profile")
                            val followCount = profileObject.optInt("followcnt")
                            val followerCount = profileObject.optInt("followercnt")
                            val iconPath = profileObject.optString("iconPath")
                            println("値を受け取りました:$userName")
                            //受け取った値をオブジェクトに格納してUIを更新する
                            runOnUiThread {
                                followerCountTx.text = followerCount.toString()
                                followCountTx.text = followCount.toString()
                                userNameTx.text = userName
                                userProfileTx.text = userProfile
                                println("UIを変更しました:$userName,$followCount")
                            }
                            println("loginUserId:$loginUserId,userId:$userId")
                            //対象ユーザがログインユーザと同じならボタンを非表示にする
                            runOnUiThread {
                                if (loginUserId.trim() == userId) {
                                    followBtn.visibility = View.GONE
                                } else if (followFlg) {
                                    followBtn.setText("フォロー中")
                                } else {
                                    //もしフォローユーザならボタンにフォローすると表示
                                    followBtn.setText("フォローする")
                                }
                            }

                            //ささやき情報一覧が存在する間、ささやき情報をリストに格納する
                            // RecyclerViewに設定するリストを作成
                            val whisperInfoList = mutableListOf<WhisperData>()
                            // JSONオブジェクトの中からKey値がlistのValue値を文字列として取得(Value値のイメージ：{"list" : [{"???" : "xxx"}, {"???" : "yyy"} ...]})
                            val whisperList = jsonResponse.getString("whisperList")
                            // 取得したValue値(文字列)は配列の構成になっているので、JSON配列に変換
                            val jsonArray = JSONArray(whisperList)
                            // 配列をfor文で回して中身を取得
                            for (i in 0 until jsonArray.length()) {
                                val userName =
                                    jsonArray.getJSONObject(i).getString("userName") // 商品番号を取得
                                val content =
                                    jsonArray.getJSONObject(i).getString("content")          // 商品名を取得
//                        val price =
//                            jsonArray.getJSONObject(i).getString("price")          // 価格を取得
                                whisperInfoList.add(
                                    WhisperData(
                                        userName,
                                        content,
                                        loginUserId,
                                        jsonArray.getJSONObject(i).getString("userId"),
                                        jsonArray.getJSONObject(i).getInt("whisperNo")
                                    )
                                ) // 1行分のデータに商品番号、商品名、価格を設定してリストに追加
                            }

                            //いいね情報を格納するリストを作成
                            val goods = mutableListOf<Good>()
                            //いいね情報をリストに格納する
                            val goodList = jsonResponse.optJSONArray("goodList")
                            if(goodList != null){
                                for (i in 0 until goodList.length()){
                                    val goodJson = goodList.getJSONObject(i)
                                    goods.add(
                                        Good(
                                            content = goodJson.optString("content"),
                                            userName = goodJson.optString("userName"),
                                            userImg = R.drawable.ic_launcher_background,
                                            gcnt = goodJson.optInt("goodCount"),
                                            userId = goodJson.optString("userId")
                                        )
                                    )
                                }
                            }

                            //ラジオグループで選択されたラジオボタンのIDを取得
                            val section = when (radioGroup.checkedRadioButtonId){
                                R.id.whisperRadio -> "1"
                                R.id.goodInfoRadio -> "2"
                                else -> ""
                            }
                            println("section:$section")
                            runOnUiThread {
//                                val app = application as MyApplication
                                //ラジオボタンがuserRadioを選択している場合
                                if (section == "1") {
                                    userRecycle.adapter = WhisperAdapter(whisperInfoList)
                                    println("whisperList:$whisperInfoList")
                                    //ラジオボタンがwhisperRadioを選択している場合
                                } else if (section == "2") {
                                    userRecycle.adapter = GoodAdapter(myapp,goods)
                                    println("goodList:$goodList")
                                }
                            }

                        }else{
                            println("Profile list is empty or null")  // 追加
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
                        Toast.makeText(this@UserInfoActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
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
