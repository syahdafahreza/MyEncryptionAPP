package com.myproject.myencryptionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        startsecondactivity()
    }
    fun startsecondactivity () {

        if(!isDestroyed){
            val intent = Intent(this, MainMenu::class.java)
            val tmtask = timerTask {
                if (!isDestroyed){
                    startActivity(intent)
                    finish()
                }
            }
            val timer = Timer()
            timer.schedule(tmtask, 2000)
        }
    }
}