package com.img.audition.network

import android.content.Context
import com.img.audition.globalAccess.ConstValFile

class SessionManager(context: Context) {

    val sharedPrefMain = context.getSharedPreferences(ConstValFile.PREFER_MAIN, Context.MODE_PRIVATE)
    val sharedPrefLang = context.getSharedPreferences(ConstValFile.PREFER_LANG, Context.MODE_PRIVATE)
    val sharedPrefContest = context.getSharedPreferences(ConstValFile.PREFER_CONTEST, Context.MODE_PRIVATE)
    val sharedPrefAudioVideoSession = context.getSharedPreferences(ConstValFile.PREFER_CONTEST, Context.MODE_PRIVATE)
    val prefEditorMain = sharedPrefMain.edit()
    val prefEditorLang = sharedPrefLang.edit()
    val prefEditorContest = sharedPrefContest.edit()
    val prefEditorVideoSession = sharedPrefContest.edit()


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

    fun setUserName(v: String?) {
        prefEditorMain.putString(ConstValFile.UserName,v)
        prefEditorMain.commit()
    }

    fun getUserName(): String? {
        return sharedPrefMain.getString(ConstValFile.UserName,"")
    }
    fun setUserProfileImage(v: String?) {
        prefEditorMain.putString(ConstValFile.UserImage,v)
        prefEditorMain.commit()
    }

    fun getUserProfileImage(): String? {
        return sharedPrefMain.getString(ConstValFile.UserImage,"")
    }



    fun getBankVerified(): String? {
        return sharedPrefMain.getString(ConstValFile.BankVerified,"")
    }

    fun createContestSession(contestEntryFee: Int,contestID: String?, contestType: String?, contestFile: String?, isFromContest: Boolean) {
        prefEditorContest.putInt(ConstValFile.ContestEntryFee,contestEntryFee)
        prefEditorContest.putString(ConstValFile.ContestID,contestID)
        prefEditorContest.putString(ConstValFile.ContestType, contestType)
        prefEditorContest.putString(ConstValFile.ContestFile,contestFile)
        prefEditorContest.putBoolean(ConstValFile.IsFromContest,isFromContest)
        prefEditorContest.commit()
    }

    fun clearContestSession(){
        prefEditorContest.clear()
        prefEditorContest.commit()
    }

    fun getContestEntryFee(): Int {
        return sharedPrefContest.getInt(ConstValFile.ContestEntryFee,0)
    }

    fun getContestID(): String? {
        return sharedPrefContest.getString(ConstValFile.ContestID,"")
    }

    fun getContestType(): String? {
        return  sharedPrefContest.getString(ConstValFile.ContestType,"")
    }

    fun getContestFile(): String? {
        return  sharedPrefContest.getString(ConstValFile.ContestFile,"")
    }

    fun getIsFromContest(): Boolean {
        return sharedPrefContest.getBoolean(ConstValFile.IsFromContest,false)
    }


    fun setCreateVideoSession(videoTempUrl: String?, videoSpeedState: String, videoDuration: Long) {
        prefEditorVideoSession.putString(ConstValFile.VideoFilePath,videoTempUrl)
        prefEditorVideoSession.putString(ConstValFile.VideoSpeedState,videoSpeedState)
        prefEditorVideoSession.putLong(ConstValFile.VideoDuration,videoDuration)
        prefEditorVideoSession.commit()
    }

    fun setCreateAudioSession(trimAudioUrl: String?) {
        prefEditorVideoSession.putString(ConstValFile.TrimAudioUrl,trimAudioUrl)
        prefEditorVideoSession.commit()
    }

    fun getTrimAudioPath(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.TrimAudioUrl,"")
    }

    fun getCreateVideoPath(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.VideoFilePath,"")
    }
    fun getCreateVideoSpeedState(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.VideoSpeedState,"")
    }

    fun getCreateVideoDuration(): Long {
        return sharedPrefAudioVideoSession.getLong(ConstValFile.VideoDuration,0)
    }

    fun setCreateVideoPath(videoTempUrl: String?) {
        prefEditorVideoSession.putString(ConstValFile.VideoFilePath,videoTempUrl)
        prefEditorVideoSession.commit()
    }

    fun clearVideoSession(){
        prefEditorVideoSession.clear()
        prefEditorVideoSession.commit()
    }





}