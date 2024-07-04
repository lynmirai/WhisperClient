package com.example.whisperclient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoodAdapter(private val context: Context, private val goods: List<Good>) : RecyclerView.Adapter<GoodAdapter.ViewHolder>() {

    private val app = context.applicationContext as MyApplication
    private val loginUserId = app.loginUserId

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val userImageView: ImageView = item.findViewById(R.id.userImage)
        val userNameText: TextView = item.findViewById(R.id.userNameText)
        val whisperText: TextView = item.findViewById(R.id.whisperText)
        val goodCntText: TextView = item.findViewById(R.id.goodCntText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.good_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val good = goods[position]
        holder.userImageView.setImageResource(good.userImg)
        holder.userNameText.text = good.userName
        holder.whisperText.text = good.content
        holder.goodCntText.text = good.gcnt.toString()

        // userImageのクリックイベント
        holder.userImageView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserInfoActivity::class.java)
            intent.putExtra("userId", loginUserId)
            // ユーザー情報画面に遷移
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return goods.size
    }
}
