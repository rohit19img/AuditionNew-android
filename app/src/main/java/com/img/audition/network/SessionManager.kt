package com.img.audition.network

import android.content.Context
import com.img.audition.globalAccess.ConstValFile

class SessionManager(context: Context) {

    val sharedPrefMain = context.getSharedPreferences(ConstValFile.PREFER_MAIN, Context.MODE_PRIVATE)
    val sharedPrefLang = context.getSharedPreferences(ConstValFile.PREFER_LANG, Context.MODE_PRIVATE)
    val prefEditorMain = sharedPrefMain.edit()
    val prefEditorLang = sharedPrefLang.edit()


    fun createUserLoginSession(isLogin: Boolean, Token: String?, mNumber: String?) {
        prefEditorMain.putBoolean(ConstValFile.IS_LOGIN,isLogin)
        prefEditorMain.putString(ConstValFile.TOKEN,"Bearer "+Token)
        prefEditorMain.putString(ConstValFile.NUMBER,mNumber)
        prefEditorMain.commit()
    }

    fun setToken(Token:String){
        prefEditorMain.putString(ConstValFile.TOKEN,"Bearer "+Token)
        prefEditorMain.commit()
    }

    fun setUserSelfID(userID:String){
        prefEditorMain.putString(ConstValFile.USER_ID,userID)
        prefEditorMain.commit()
    }

    fun getUserSelfID():String?{
       return sharedPrefMain.getString(ConstValFile.USER_ID,"");
    }

    fun getToken(): String? {
        return sharedPrefMain.getString(ConstValFile.TOKEN,"")
    }

    fun isUserLoggedIn():Boolean{
        return sharedPrefMain.getBoolean(ConstValFile.IS_LOGIN,false)
    }

    fun isGuestLoggedIn():Boolean{
        return sharedPrefMain.getBoolean(ConstValFile.IS_GUEST,false)
    }

    fun setGuestLogin(isGuestLogin:Boolean){
        prefEditorMain.putBoolean(ConstValFile.IS_GUEST,isGuestLogin)
        prefEditorMain.commit()
    }

    fun clearLogoutSession(){
        //here to Transfer TO Home and Login Activity

        prefEditorMain.clear()
        prefEditorMain.commit()
       /* prefEditorLang.clear()
        prefEditorLang.commit()*/
    }

    fun setSelectedLanguage(selected_Lang:String){
        prefEditorLang.putString(ConstValFile.SELECTED_LANG,selected_Lang)
        prefEditorLang.commit()
    }

    fun getSelectedLanguage():String?{
        return sharedPrefLang.getString(ConstValFile.SELECTED_LANG,"")
    }

    fun setNotificationToken(notificationToken:String){
        prefEditorMain.putString(ConstValFile.NOTIFICATION_TOKEN,notificationToken)
        prefEditorMain.commit()
    }

    fun getNotificationToken():String?{
       return sharedPrefMain.getString(ConstValFile.NOTIFICATION_TOKEN,"")
    }

    fun getMobileVerified(): Boolean {
        return sharedPrefMain.getBoolean(ConstValFile.MobileVerified,false)
    }

    fun setMobileVerified(mVerify:Boolean){
        prefEditorMain.putBoolean(ConstValFile.MobileVerified,mVerify)
        prefEditorMain.commit()
    }

    fun setPANVerified(v: String?) {
        prefEditorMain.putString(ConstValFile.PanVerified,v)
        prefEditorMain.commit()
    }

    fun setBankVerified(v: String?) {
        prefEditorMain.putString(ConstValFile.BankVerified,v)
        prefEditorMain.commit()
    }

    fun getPANVerified(): String? {
        return sharedPrefMain.getString(ConstValFile.PanVerified,"")
    }

    fun getBankVerified(): String? {
        return sharedPrefMain.getString(ConstValFile.BankVerified,"")
    }


}