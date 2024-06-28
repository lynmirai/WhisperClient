package com.example.whisperclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperclient.databinding.WhisperAdapterBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class WhisperAdapter(private val dataset: MutableList<WhisperData>) : RecyclerView.Adapter<WhisperAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: WhisperAdapterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WhisperData) {
            binding.userNameText.text = item.userName
            binding.whisperText.text = item.content


            //
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = """
                {
                    "userId":"${item.userId}",
                    "loginUserId":"${item.loginUserId}"
                }
                """.trimIndent()

            val request = Request.Builder()
                .url("https://click.ecc.ac.jp/ecc/whisper24_d/userWhisperInfo.php")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Request failed: ${e.message}")
                    // Handle the failure scenario (e.g., show a toast or log the error)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    println("Response received: $body")

                    val json = JSONObject(body)
                    val goodList = json.optJSONArray("goodList")
                    if (goodList != null) {
                        for (i in 0 until goodList.length()) {
                            val goodItem = goodList.getJSONObject(i)
                            val whisperNo = goodItem.optInt("whisperNo")
                            if (whisperNo == item.whisperNo) {
                                val goodFlg = goodItem.optInt("goodFlg")
                                (binding.root.context as TimelineActivity).runOnUiThread {
                                    if (goodFlg == 1) {
                                        binding.goodImage.setImageResource(R.drawable.ic_action_star_black)
                                    } else {
                                        binding.goodImage.setImageResource(R.drawable.ic_action_star_border_black)
                                    }
                                }
                                break
                            }
                        }
                    }

                }
            })


            binding.goodImage.setOnClickListener {
                sendGoodRequest(item)
            }
        }

        private fun sendGoodRequest(item: WhisperData) {
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = """
                {
                    "userId":"${item.loginUserId}",
                    "whisperNo":"${item.whisperNo}",
                    "goodFlg":true
                }
                """.trimIndent()

            val request = Request.Builder()
                .url("https://click.ecc.ac.jp/ecc/whisper24_d/goodCtl.php")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Request failed: ${e.message}")
                    // Handle the failure scenario (e.g., show a toast or log the error)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    println("Response received: $body")

                    val json = JSONObject(body)
                    val result = json.optString("result")

                    // Ensure UI changes are made on the main thread
                    (binding.root.context as TimelineActivity).runOnUiThread {
                        if (result == "add") {
                            binding.goodImage.setImageResource(R.drawable.ic_action_star_black)
                        } else {
                            binding.goodImage.setImageResource(R.drawable.ic_action_star_border_black)
                        }
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WhisperAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = dataset.size
}
