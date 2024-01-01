package com.img.audition.network

import android.content.Context
import com.img.audition.globalAccess.ConstValFile

class SessionManager(context: Context) {

    val sharedPrefMain = context.getSharedPreferences(ConstValFile.PREFER_MAIN, Context.MODE_PRIVATE)
    val sharedPrefLang = context.getSharedPreferences(ConstValFile.PREFER_LANG, Context.MODE_PRIVATE)
    val sharedPrefContest = context.getSharedPreferences(ConstValFile.PREFER_CONTEST, Context.MODE_PRIVATE)
    val sharedPrefAudioVideoSession = context.getSharedPreferences(ConstValFile.PREFER_VIDEO, Context.MODE_PRIVATE)
    val sharedPrefDuetVideoSession = context.getSharedPreferences(ConstValFile.DUET_VIDEO, Context.MODE_PRIVATE)
    val prefEditorMain = sharedPrefMain.edit()
    val prefEditorLang = sharedPrefLang.edit()
    val prefEditorContest = sharedPrefContest.edit()
    val prefEditorVideoSession = sharedPrefAudioVideoSession.edit()
    val prefEditorDuetVideoSession = sharedPrefDuetVideoSession.edit()


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

    fun setMobileNumber(number:String){
        prefEditorMain.putString(ConstValFile.NUMBER,number)
        prefEditorMain.commit()
    }

    fun getMobileNumber():String?{
       return sharedPrefMain.getString(ConstValFile.NUMBER,"")
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

    fun getEmailVerified(): Boolean {
        return sharedPrefMain.getBoolean(ConstValFile.EmailVerified,false)
    }

    fun setEmailVerified(mVerify:Boolean){
        prefEditorMain.putBoolean(ConstValFile.EmailVerified,mVerify)
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

    fun setUserAuditionID(v: String?) {
        prefEditorMain.putString(ConstValFile.AuditionID,v)
        prefEditorMain.commit()
    }

    fun getUserAuditionID(): String? {
        return sharedPrefMain.getString(ConstValFile.AuditionID,"")
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

    fun clearDuetSession(){
        prefEditorDuetVideoSession.clear()
        prefEditorDuetVideoSession.commit()
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

    fun setIsFromContest(isFromContest: Boolean){
        prefEditorContest.putBoolean(ConstValFile.IsFromContest,isFromContest)
        prefEditorContest.commit()

    }

    fun setCreateVideoSession(videoTempUrl: String?, videoSpeedState: String, videoDuration: Long) {
        prefEditorVideoSession.putString(ConstValFile.VideoFilePath,videoTempUrl)
        prefEditorVideoSession.putString(ConstValFile.VideoOriginalPath,videoTempUrl)
        prefEditorVideoSession.putString(ConstValFile.VideoSpeedState,videoSpeedState)
        prefEditorVideoSession.putLong(ConstValFile.VideoDuration,videoDuration)
        prefEditorVideoSession.commit()
    }

    fun setCreateVideoSpeedState(VideoFrom: String?) {
        prefEditorVideoSession.putString(ConstValFile.VideoSpeedState,VideoFrom)
        prefEditorVideoSession.commit()
    }

    fun setIsVideoFromGallery(VideoFrom: Boolean) {
        prefEditorVideoSession.putBoolean(ConstValFile.isFromGallery,VideoFrom)
        prefEditorVideoSession.commit()
    }

    fun getIsVideoFromGallery(): Boolean {
        return sharedPrefAudioVideoSession.getBoolean(ConstValFile.isFromGallery,false)
    }
    fun setCreateAudioSession(trimAudioUrl: String?) {
        prefEditorVideoSession.putString(ConstValFile.TrimAudioUrl,trimAudioUrl)
        prefEditorVideoSession.commit()
    }

    fun setVideoOriginalPath(videoOriginalPath:String) {
        prefEditorVideoSession.putString(ConstValFile.VideoOriginalPath,videoOriginalPath)
        prefEditorVideoSession.commit()
    }

    fun getVideoOriginalPath(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.VideoOriginalPath,"")
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

    fun setCreateVideoDuration(videoDuration : Long) {
        prefEditorVideoSession.putLong(ConstValFile.VideoDuration,videoDuration)
        prefEditorVideoSession.commit()
    }

    fun setCreateVideoPath(videoTempUrl: String?) {
        prefEditorVideoSession.putString(ConstValFile.VideoFilePath,videoTempUrl)
        prefEditorVideoSession.commit()
    }

    fun clearVideoSession(){
        prefEditorVideoSession.clear()
        prefEditorVideoSession.commit()
    }

    fun setVideoHashTag(hashTag: String?) {
        prefEditorVideoSession.putString(ConstValFile.VideoHashTag,hashTag)
        prefEditorVideoSession.commit()
    }

    fun getVideoHashTag(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.VideoHashTag,"")
    }

    fun setVideoSongID(songID: String?) {
        prefEditorVideoSession.putString(ConstValFile.SongID,songID)
        prefEditorVideoSession.commit()
    }

    fun getVideoSongID(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.SongID,"")
    }


    fun setAppSongID(appSongID: String?) {
        prefEditorVideoSession.putString(ConstValFile.AppSongID,appSongID)
        prefEditorVideoSession.commit()
    }

    fun getAppSongID(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.AppSongID,"")
    }

    fun setAudioDuration(audioDuration: Int) {
        prefEditorVideoSession.putInt(ConstValFile.AudioDuration,audioDuration)
        prefEditorVideoSession.commit()
    }

    fun getAudioDuration(): Int {
        return sharedPrefAudioVideoSession.getInt(ConstValFile.AudioDuration,0)
    }



    fun setVideoSongUrl(songUrl: String?) {
        prefEditorVideoSession.putString(ConstValFile.SongUrl,songUrl)
        prefEditorVideoSession.commit()
    }

    fun setIsFromTryAudio(isFromTryAudio: Boolean) {
        prefEditorVideoSession.putBoolean(ConstValFile.isFromTryAudio,isFromTryAudio)
        prefEditorVideoSession.commit()
    }

    fun getIsFromTryAudio(): Boolean {
        return sharedPrefAudioVideoSession.getBoolean(ConstValFile.isFromTryAudio,false)
    }

    fun setIsAppAudio(appAudio: Boolean) {
        prefEditorVideoSession.putBoolean(ConstValFile.AppAudio,appAudio)
        prefEditorVideoSession.commit()
    }

    fun getIsAppAudio(): Boolean {
        return sharedPrefAudioVideoSession.getBoolean(ConstValFile.AppAudio,false)
    }

    fun getVideoSongUrl(): String? {
        return sharedPrefAudioVideoSession.getString(ConstValFile.SongUrl,"")
    }

    fun setDuetVideoUrl(duetVideoUrl: String) {
        prefEditorDuetVideoSession.putString(ConstValFile.DuetVideoUrl,duetVideoUrl)
        prefEditorDuetVideoSession.commit()
    }

    fun setDuetVideoSession(createVideoUrl: String, duetVideoUrl: String, duetWithId: String, isFromDuet: Boolean) {
        prefEditorDuetVideoSession.putString(ConstValFile.VideoFilePath,createVideoUrl)
        prefEditorDuetVideoSession.putString(ConstValFile.DuetVideoUrl,duetVideoUrl)
        prefEditorDuetVideoSession.putString(ConstValFile.DuetCaption,duetWithId)
        prefEditorDuetVideoSession.putBoolean(ConstValFile.isFromDuet,isFromDuet)
        prefEditorDuetVideoSession.commit()
    }

    fun getDuetVideoUrl():String?{
        return sharedPrefDuetVideoSession.getString(ConstValFile.DuetVideoUrl,"")
    }

    fun getDuetCaption():String?{
        return sharedPrefDuetVideoSession.getString(ConstValFile.DuetCaption,"")
    }

    fun getIsFromDuet():Boolean{
        return sharedPrefDuetVideoSession.getBoolean(ConstValFile.isFromDuet,false)
    }

    fun getCreateDuetVideoUrl():String?{
        return sharedPrefDuetVideoSession.getString(ConstValFile.VideoFilePath,"")
    }
}