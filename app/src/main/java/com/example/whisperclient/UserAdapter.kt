package com.example.whisperclient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val app: MyApplication, private val users:List<User>): RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    private val loginUserId = app.loginUserId


    class ViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val userImageView: ImageView
        val userNameText: TextView
        val followCntText: TextView
        val followerCntText: TextView

        init {
            userImageView = item.findViewById(R.id.userImage)
            userNameText = item.findViewById(R.id.userNameText)
            followCntText = item.findViewById(R.id.followCntText)
            followerCntText = item.findViewById(R.id.followerCntText)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        //ユーザ行情報の画面デザインを設定する
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_adapter,parent,false)
        //設定した画面デザインに戻り値をセットする
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        //viewHolderのオブジェクトに対象行のデータをセットする
        holder.userImageView.setImageResource(users[position].userImg)
        holder.userNameText.text = users[position].userName
        holder.followCntText.text = users[position].follow.toString()
        holder.followerCntText.text = users[position].follower.toString()
        //userImageのclickイベント
        holder.userImageView.setOnClickListener{
            val intent = Intent(holder.itemView.context,UserInfoActivity::class.java)
            intent.putExtra("userId", loginUserId)
            //ユーザ情報画面に遷移
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        //行リストの件数を戻り値にセットする
        return users.size
    }

}