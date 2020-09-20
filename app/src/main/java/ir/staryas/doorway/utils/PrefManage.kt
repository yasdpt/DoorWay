package ir.staryas.doorway.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PrefManage @SuppressLint("CommitPrefEdits") constructor(context: Context) {
    var pref:SharedPreferences? = null
    var editor:SharedPreferences.Editor? = null
    var _context:Context? = context

    var PRIVATE_MODE = 0
    private val PREF_NAME = "doorway-user"
    private val prefUsername = "username"
    private val prefUserId = "id"
    private val prefEmail = "email"
    private val prefFullName = "fullname"
    private val prefPhone = "phone"
    private val prefisUserLoggedIn = "isUserLoggedIn"
    private val prefCityId = "cityId"
    private val prefCityName = "cityName"


    init {
        pref = _context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref!!.edit()
    }

    fun setUsername(username: String) {
        editor!!.putString(prefUsername, username)
        editor!!.commit()
    }

    fun getUsername(): String? {
        return pref!!.getString(prefUsername, "null")
    }

    fun setCityName(city: String) {
        editor!!.putString(prefCityName, city)
        editor!!.commit()
    }

    fun getCityName(): String? {
        return pref!!.getString(prefCityName, "null")
    }

    fun setCityId(cityId: Int) {
        editor!!.putInt(prefCityId, cityId)
        editor!!.commit()
    }


    fun getCityId(): Int? {
        return pref!!.getInt(prefCityId, 0)
    }




    fun setIsUserLoggedIn(isUserLoggedIn:Boolean) {
        editor!!.putBoolean(prefisUserLoggedIn,isUserLoggedIn)
        editor!!.commit()
    }
    fun getIsUserLoggedIn():Boolean? {
        return pref!!.getBoolean(prefisUserLoggedIn,false)
    }

    fun setUserId(userId: Int) {
        editor!!.putInt(prefUserId, userId)
        editor!!.commit()
    }

    fun getUserId(): Int? {
        return pref!!.getInt(prefUserId, 0)
    }

    fun setEmail(email: String) {
        editor!!.putString(prefEmail, email)
        editor!!.commit()
    }

    fun getEmail(): String? {
        return pref!!.getString(prefEmail, "null")
    }

    fun setFullName(fullName: String) {
        editor!!.putString(prefFullName, fullName)
        editor!!.commit()
    }

    fun getFullName(): String? {
        return pref!!.getString(prefFullName, "null")
    }

    fun setPhone(phone: String) {
        editor!!.putString(prefPhone, phone)
        editor!!.commit()
    }

    fun getPhone(): String? {
        return pref!!.getString(prefPhone, "null")
    }



}