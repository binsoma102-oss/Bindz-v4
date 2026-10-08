package com.bindz.v3

import android.app.*
import android.content.*
import android.graphics.Color
import android.hardware.*
import android.net.*
import android.os.*
import android.provider.Settings
import android.net.Uri
import android.view.*
import android.widget.*
import java.net.InetAddress
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var sensorManager: SensorManager

    private fun tv(s:String,size:Float=15f)=TextView(this).apply{
        text=s; textSize=size; setTextColor(Color.WHITE); setPadding(8,8,8,8)
    }
    private fun btn(s:String, action:()->Unit)=Button(this).apply{
        text=s; setOnClickListener{action()}
    }

    override fun onCreate(b:Bundle?) {
        super.onCreate(b)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        sensorManager=getSystemService(SENSOR_SERVICE) as SensorManager

        val root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(18,14,18,14)
            setBackgroundColor(Color.rgb(18,18,22))
        }
        root.addView(tv("BIN DZ • GAME CORNER V3",25f))
        root.addView(tv("Overlay + máy + công cụ chơi game an toàn",13f))

        status=tv("Đang đọc thông tin…")
        root.addView(status)

        val row1=LinearLayout(this)
        listOf(
            "↻ Refresh Rate" to {refreshRate()},
            "ⓘ Information" to {information()},
            "⌁ DNS Tuner" to {dnsInfo()},
            "◎ Crosshair" to {crosshair()}
        ).forEach{(t,a)-> row1.addView(btn(t,a),LinearLayout.LayoutParams(0,-2,1f))}
        root.addView(row1)

        val row2=LinearLayout(this)
        listOf(
            "⚡ Quick Boost" to {quickBoost()},
            "◎ Gyro" to {gyro()},
            "⛶ Rotation" to {rotation()},
            "☾ DND" to {dnd()}
        ).forEach{(t,a)-> row2.addView(btn(t,a),LinearLayout.LayoutParams(0,-2,1f))}
        root.addView(row2)

        val row3=LinearLayout(this)
        listOf(
            "☀ Brightness" to {brightness()},
            "✋ Touch" to {touchInfo()},
            "▣ Screenshot" to {screenshotSettings()},
            "◉ Show Taps" to {showTapsSettings()}
        ).forEach{(t,a)-> row3.addView(btn(t,a),LinearLayout.LayoutParams(0,-2,1f))}
        root.addView(row3)

        root.addView(btn("🎞 BẬT VIDEO OVERLAY",::startVideo))
        root.addView(btn("× TẮT OVERLAY",::stopVideo))
        root.addView(btn("🎮 MỞ FREE FIRE",::openFF))
        root.addView(btn("🎮 MỞ FREE FIRE MAX",::openFFMax))
        root.addView(btn("🔄 LÀM MỚI MONITOR",::information))

        setContentView(root)
        information()
    }

    private fun information(){
        val dm=resources.displayMetrics
        val hz=if(Build.VERSION.SDK_INT>=30) display.refreshRate else 60f
        val cm=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val online=cm.activeNetwork!=null
        val battery=registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp=battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)?.let{it/10.0} ?: -1.0
        val ram=ActivityManager.MemoryInfo().also{
            (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(it)
        }
        status.text="📱 ${Build.MANUFACTURER} ${Build.MODEL}  |  ${dm.widthPixels}×${dm.heightPixels}  |  ${dm.densityDpi} dpi  |  ${hz.roundToInt()} Hz\\n"+
                "🌡 ${if(temp>=0) String.format("%.1f°C",temp) else "N/A"}  |  🧠 RAM còn ${ram.availMem/1024/1024} MB  |  📶 ${if(online)"Online" else "Offline"}"
    }

    private fun refreshRate(){
        val hz=if(Build.VERSION.SDK_INT>=30) display.refreshRate else 60f
        toast("Refresh rate hiện tại: ${hz.roundToInt()} Hz")
    }

    private fun dnsInfo(){
        val cm=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val n=cm.activeNetwork
        val lp=if(n!=null) cm.getLinkProperties(n) else null
        val dns=lp?.dnsServers?.joinToString{" ${it.hostAddress}"} ?: "Không đọc được"
        toast("DNS hiện tại: $dns")
    }

    private fun quickBoost(){
        val cm=getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val online=cm.activeNetwork!=null
        val b=registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val t=b?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)?.let{it/10.0} ?: -1.0
        val msg=when{
            !online->"Mạng chưa kết nối"
            t>=40->"Máy ${String.format("%.1f",t)}°C — nên hạ nhiệt"
            else->"Kiểm tra OK: mạng + nhiệt độ ổn"
        }
        toast("⚡ $msg")
    }

    private fun gyro(){
        val s=sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        toast(if(s!=null) "Gyroscope: ${s.name} — cảm biến sẵn sàng" else "Thiết bị không có Gyroscope")
    }

    private fun rotation(){
        requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        toast("Bin Dz đã khóa giao diện ngang")
    }

    private fun dnd(){
        if(Build.VERSION.SDK_INT>=23) startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    private fun brightness(){
        if(Build.VERSION.SDK_INT>=23 && !Settings.System.canWrite(this)){
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
        } else toast("Quyền độ sáng đã sẵn sàng; độ sáng vẫn do Android quản lý.")
    }

    private fun touchInfo(){
        toast("Android không cho app thường tăng độ nhạy cảm ứng của game khác. Có thể mở cài đặt màn hình/cảm ứng để chỉnh thủ công.")
        try{startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))}catch(_:Exception){}
    }

    private fun screenshotSettings(){
        try{startActivity(Intent("android.settings.ACTION_SCREEN_CAPTURE_SETTINGS"))}
        catch(_:Exception){toast("Screenshot cần quyền MediaProjection của Android.")}
    }

    private fun showTapsSettings(){
        try{startActivity(Intent(Settings.ACTION_SETTINGS))}
        catch(_:Exception){}
        toast("Show Taps cần bật trong Developer options của Android.")
    }

    private fun crosshair(){
        if(Settings.canDrawOverlays(this)){
            startService(Intent(this, CrosshairService::class.java))
            toast("Crosshair overlay đã bật")
        }else{
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
        }
    }

    private fun startVideo(){
        if(Settings.canDrawOverlays(this)) startService(Intent(this,OverlayService::class.java))
        else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
    }
    private fun stopVideo(){stopService(Intent(this,OverlayService::class.java))}
    private fun openFF(){openWithVideo("com.dts.freefireth")}
    private fun openFFMax(){openWithVideo("com.dts.freefiremax")}
    private fun openWithVideo(p:String){
        if(!Settings.canDrawOverlays(this)){
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
            toast("Hãy cấp quyền hiển thị trên ứng dụng khác rồi bấm MỞ FREE FIRE lại.")
            return
        }
        startService(Intent(this,OverlayService::class.java))
        Handler(Looper.getMainLooper()).postDelayed({ open(p) }, 250)
    }
    private fun open(p:String){
        val i=packageManager.getLaunchIntentForPackage(p)
        if(i!=null)startActivity(i) else toast("Chưa cài game này.")
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
