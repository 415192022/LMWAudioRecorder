package com.lmw.audiorecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lmw.audiorecorder.widget.RecordDialog
import com.lmw.ijkplayer.listener.AudioPlayListener
import com.lmw.ijkplayer.service.AudioPlayService
import com.lmw.ijkplayer.service.AudioPlayService.AudioPlayBinder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mConnection: AloneServiceConnection? = null

    private var mPlayService: AudioPlayService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService()

        setListener()

        recordButton?.recorderDialog = RecordDialog(this, R.style.theme_audioDialog)
        recordButton?.setFileDir(Environment.getExternalStorageDirectory().absolutePath + File.separator + "Hi" + File.separator)
        recordButton?.setRecorderFinishListener { time, filePath ->
            Toast.makeText(this, "$time    $filePath", Toast.LENGTH_LONG).show()
            mPlayService?.initDataSource(filePath)
        }
    }

    private fun setListener() {
        mPlayService?.setAudioPlayListener(object : AudioPlayListener {
            override fun onComplete() {
            }

            override fun onBufferingUpdate(percent: Int) {
            }

            override fun onPause() {
            }

            override fun onProgress(currentPosition: Long, duration: Long) {
                println("$currentPosition   $duration")
            }

            override fun onError(msg: String?) {
                println("$msg   $ ")
            }

            override fun onPreparing() {
            }

            override fun onPlaying() {
            }
        })
    }


    private fun startService() {
        val intent = Intent(this, AudioPlayService::class.java)
        mConnection = AloneServiceConnection()
        bindService(intent, mConnection!!, Context.BIND_AUTO_CREATE)
    }

    inner class AloneServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //获取service对象
            mPlayService = (service as AudioPlayBinder).audioPlayService
            mPlayService?.setAutoPlaying(true)
            Toast.makeText(
                this@MainActivity,
                " " + name + "   " + service + "  ",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }
}
