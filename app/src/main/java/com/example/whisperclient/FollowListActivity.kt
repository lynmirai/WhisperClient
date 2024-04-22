package com.example.whisperclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import com.example.whisperclient.databinding.ActivityFollowListBinding

class FollowListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFollowListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_follow_list)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dang ky menu ngu canh
        registerForContextMenu(binding.imgMenu)
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