package com.bindz.v3
import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*

class CrosshairService:Service(){
    private var wm:WindowManager?=null
    private var v:View?=null
    override fun onCreate(){
        super.onCreate()
        if(!Settings.canDrawOverlays(this)){stopSelf();return}
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        v=object:View(this){
            val p=Paint().apply{style=Paint.Style.STROKE;strokeWidth=3f}
            override fun onDraw(c:Canvas){super.onDraw(c);val x=width/2f;val y=height/2f;p.color=Color.WHITE;c.drawLine(x-28,y,x+28,y,p);c.drawLine(x,y-28,x,y+28,p);c.drawCircle(x,y,8f,p)}
        }
        val type=if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val lp=WindowManager.LayoutParams(120,120,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT)
        lp.gravity=Gravity.CENTER
        wm!!.addView(v,lp)
    }
    override fun onDestroy(){try{v?.let{wm?.removeView(it)}}catch(_:Exception){};super.onDestroy()}
    override fun onBind(i:Intent?)=null
}
