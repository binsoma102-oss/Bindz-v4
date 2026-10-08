package com.bindz.v3

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*

class OverlayService:Service(){
    private var wm:WindowManager?=null
    private var root:FrameLayout?=null
    private var video:VideoView?=null

    override fun onCreate(){
        super.onCreate()
        channel()
        startForeground(31,Notification.Builder(this,"bindz")
            .setContentTitle("Bin Dz V3")
            .setContentText("Video landscape overlay đang chạy")
            .setSmallIcon(android.R.drawable.ic_media_play).build())
        if(!Settings.canDrawOverlays(this)){stopSelf();return}

        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        root=FrameLayout(this)

        video=VideoView(this)
        val vp=FrameLayout.LayoutParams(dp(640),dp(360),Gravity.CENTER)
        root!!.addView(video,vp)

        val close=TextView(this).apply{
            text="×";textSize=26f;setTextColor(Color.WHITE)
            setBackgroundColor(0x88000000.toInt());setPadding(12,0,12,0)
            setOnClickListener{stopSelf()}
        }
        root!!.addView(close,FrameLayout.LayoutParams(dp(52),dp(52),
            Gravity.TOP or Gravity.END))

        val type=if(Build.VERSION.SDK_INT>=26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        val lp=WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT)
        lp.gravity=Gravity.CENTER
        wm!!.addView(root,lp)

        video!!.setVideoURI(android.net.Uri.parse(
            "android.resource://$packageName/${R.raw.effect_landscape}"))
        video!!.setOnPreparedListener{mp->
            mp.isLooping=true;mp.setVolume(1f,1f);video!!.start()
        }
    }

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun channel(){
        if(Build.VERSION.SDK_INT>=26){
            val n=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            n.createNotificationChannel(NotificationChannel(
                "bindz","Bin Dz Overlay",NotificationManager.IMPORTANCE_LOW))
        }
    }
    override fun onDestroy(){
        try{root?.let{wm?.removeView(it)}}catch(_:Exception){}
        root=null;video=null;super.onDestroy()
    }
    override fun onBind(i:Intent?)=null
}
