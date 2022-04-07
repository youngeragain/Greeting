package com.example.greeting

import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import com.example.greeting.GreetingYou.greeting
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var externalFileDir1:File?=null
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.CHINA)
    private var mMediaRecorder:MediaRecorder?=null
    var startTimeMills=0L
    var timer:Timer? = Timer()
    var isPrepared = false
    var duration = 0
    lateinit var btnRecord:AppCompatButton
    val timerTask = object :TimerTask(){
        @RequiresApi(Build.VERSION_CODES.N)
        override fun run() {
            val l = now() - startTimeMills
            val times = l / 3000L
            Log.e("blue", "l:${l}\ttimes:$times")
            if(times in 1..9){
                stopRecordChangeOutputFileThenRestartRecordAudio(externalFileDir1!!.path+"/audio_"+times+"_"+simpleDateFormat.format(Calendar.getInstance().time)+".mp3")
            }else if(times==10L){
                stopRecord()
                releaseRecord()
                timer?.cancel()
                timer = null
                btnRecord?.post {
                    btnRecord?.text = "开始录音"
                }
                isPrepared = false
                Log.e("blue", "stop record by default 10s")
            }


        }
    }
    val stringBuilder = StringBuilder()
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        externalFileDir1 = externalCacheDir
        GreetingYou.putHolder(this)
        findViewById<AppCompatTextView>(R.id.btn1)?.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
        findViewById<AppCompatTextView>(R.id.btn)?.setOnClickListener {
            stringBuilder.clear()
            mapOf(
                android.Manifest.permission.WRITE_SETTINGS to "我们需要修改亮度123",
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to "有些文件需要放到外部存储空间但是又不能放到特定的位置",
                android.Manifest.permission.SYSTEM_ALERT_WINDOW to "给您提供类似画中画的效果",
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "I just want this permission!",
                android.Manifest.permission.READ_EXTERNAL_STORAGE to "I just want this permission!",
                android.Manifest.permission.RECORD_AUDIO to "I just want this permission!",
            ).greeting(
                granted = {permissions->
                    if(permissions.size==6){
                        Toast.makeText(this, "all the permissions are granted!", Toast.LENGTH_SHORT).show()
                    }
                    stringBuilder.append("granted permissions:\n")
                    permissions.forEach {permission->
                        stringBuilder.append(permission).append("\n")
                    }

                },
                denied = {permissions->
                    stringBuilder.append("denied permissions:\n")
                    permissions.forEach {permission->
                        stringBuilder.append(permission).append("\n")
                    }
                    findViewById<AppCompatTextView>(R.id.btn)?.text = stringBuilder.toString()
                }
            )
            //Please do not call the greeting method multiple times synchronously like the following, because it will be ignored
            (android.Manifest.permission.WRITE_SETTINGS).greeting(
                granted = {permission->
                    Log.e("blue", "WRITE_SETTINGS granted")

                },
                denied = {permission->
                    Log.e("blue", "WRITE_SETTINGS denied")
                }
            )

        }
        btnRecord = findViewById<AppCompatButton>(R.id.btn_record)
        btnRecord?.setOnClickListener {

            //has the RECORD_AUDIO permission?

            if(externalFileDir1==null)
                return@setOnClickListener
            else if(externalFileDir1!!.path.isNullOrEmpty())
                return@setOnClickListener
            if(isPrepared){
                (it as AppCompatButton).text = "开始录音"
                stopRecord()
                releaseRecord()
                timer?.cancel()
                timer = null
                durationTimer?.cancel()
                durationTimer = null
                Log.e("blue", "stop record manually")
            }else{
                if(durationTimer==null){
                    durationTimer = Timer()
                }
                if(timer==null){
                    timer = Timer()
                }

                durationTimer?.scheduleAtFixedRate(getDurationUpdateTask(), 0, 1000)
               /* (it as AppCompatButton).text = "录音中 {$duration}s"*/
                startTimeMills = System.currentTimeMillis()
                initMediaRecorder()
                prepareMediaRecorder(externalFileDir1!!.path+"/audio_"+0+"_"+simpleDateFormat.format(Calendar.getInstance().time)+".mp3")
                startRecord()
                timer?.schedule(timerTask, 0, 3000)
            }
        }
    }

    private fun getDurationUpdateTask() = object : TimerTask() {
        override fun run() {
            if (isPrepared) {
                duration++
            }
            btnRecord?.post {
                btnRecord?.text = "录音中 ${duration}s"
            }
        }
    }
    fun now():Long = System.currentTimeMillis()


    fun initMediaRecorder(){
        if(mMediaRecorder==null){
            mMediaRecorder = MediaRecorder()
        }
    }

    //check if has RECORD_AUDIO permission firstly
    fun prepareMediaRecorder(filePath: String){
        mMediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            changeOutputFile(filePath)
            prepare()
            if(!isPrepared)
                isPrepared = !isPrepared
        }
    }
    fun stopRecord(){
        if(!isPrepared)
            mMediaRecorder?.stop()
    }
    fun startRecord(){
        mMediaRecorder?.start()
    }
    fun releaseRecord(){
        mMediaRecorder?.release()
    }
    var durationTimer:Timer? = Timer()


    @RequiresApi(Build.VERSION_CODES.N)
    fun stopRecordChangeOutputFileThenRestartRecordAudio(filePath: String){
        Log.e("blue", "stopRecordChangeOutputFileThenRestartRecordAudio:$filePath")
        /*stopRecord()
        releaseRecord()*/
        mMediaRecorder?.reset()
        prepareMediaRecorder(filePath)
        startRecord()
    }
    fun changeOutputFile(filePath:String){
        mMediaRecorder?.run {
            setOutputFile(filePath)
        }
    }

}