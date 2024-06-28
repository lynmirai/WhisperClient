package com.example.whisperclient

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisperclient.databinding.ActivityTimelineBinding
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

class TimelineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimelineBinding

    private var userName: String? = null
    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_timeline)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //グローバル変数の取得
        val app = application as MyApplication
        val loginUserId = app.loginUserId
        println("$loginUserId")

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)

        //
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
        val request = Request.Builder().url("https://click.ecc.ac.jp/ecc/whisper24_d/timelineInfo.php").post(requestBody.toRequestBody(mediaType)).build()
        println("Request: $request")
        // リクエスト送信（非同期処理）
        client.newCall(request).enqueue(object : Callback {
            // リクエストが失敗した場合の処理を実装
            override fun onFailure(call: Call, e: IOException) {
                // runOnUiThreadメソッドを使うことでUIを操作することができる。(postメソッドでも可)
                this@TimelineActivity.runOnUiThread {
                    Toast.makeText(this@TimelineActivity,"リクエスト失敗:${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // リクエストが成功した場合の処理を実装
            override fun onResponse(call: Call, response: Response) {
                // APIから受け取ったデータを文字列で取得
                val body = response.body?.string()
                println("レスポンスを受信しました: $body")
                // APIから取得してきたJSON文字列をJSONオブジェクトに変換
                val json = JSONObject(body)
                try {
                    // RecyclerViewに設定するリストを作成
                    val list = mutableListOf<WhisperData>()
                    // JSONオブジェクトの中からKey値がlistのValue値を文字列として取得(Value値のイメージ：{"list" : [{"???" : "xxx"}, {"???" : "yyy"} ...]})
                    val productList = json.getString("whisperList")
                    // 取得したValue値(文字列)は配列の構成になっているので、JSON配列に変換
                    val jsonArray = JSONArray(productList)
                    // 配列をfor文で回して中身を取得
                    for (i in 0 until jsonArray.length()) {
                        val userName =
                            jsonArray.getJSONObject(i).getString("userName") // 商品番号を取得
                        val content =
                            jsonArray.getJSONObject(i).getString("content")          // 商品名を取得
//                        val price =
//                            jsonArray.getJSONObject(i).getString("price")          // 価格を取得
                        list.add(
                            WhisperData(
                                userName,
                                content,
                                loginUserId,
                                jsonArray.getJSONObject(i).getString("userId"),
                                jsonArray.getJSONObject(i).getInt("whisperNo")
                            )
                        ) // 1行分のデータに商品番号、商品名、価格を設定してリストに追加
                    }
                    // UIスレッドでRecyclerViewにリストを設定
                    this@TimelineActivity.runOnUiThread {
                        // LinearLayoutManagerを設定し、RecyclerViewを初期化する（productRecyclerViewはonCreateメソッドでfindViewByIdを使い、取得しておきましょう。）
                        binding.timelineRecycle.layoutManager =
                            LinearLayoutManager(applicationContext)
                        // 作成したlistをアダプターに渡し、RecyclerViewにアダプターを設定する
                        val adapter = WhisperAdapter(list)
                        binding.timelineRecycle.adapter = adapter
                    }
                } catch (e: Exception) {
                    //JSONデータがエラーの場合
                    val errorCode = json.optString("errCode")
                    val errorMessage = json.optString("errMsg")
                    runOnUiThread {
                        Toast.makeText(this@TimelineActivity,"$errorMessage", Toast.LENGTH_SHORT).show()
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