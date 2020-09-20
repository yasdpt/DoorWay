package ir.staryas.doorway.utils

import android.app.Application

class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        TypefaceUtil.setDefaultFont(this, "SANS_SERIF", "fonts/IRANSans.ttf")
    }
}