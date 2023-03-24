package com.img.audition.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.img.audition.R
import com.img.audition.dataModel.LanguageResponse
import com.img.audition.dataModel.Languages
import com.img.audition.databinding.LanguageSelectDailogBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LanguageSelecteDialog() : BottomSheetDialogFragment() {
    val TAG = "LanguageSelecteDialog"
    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    private val myApplication by lazy {
        MyApplication(requireActivity().applicationContext)
    }

    var listLang: ArrayList<Languages> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    lateinit var view: LanguageSelectDailogBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         view = LanguageSelectDailogBinding.inflate(inflater,container,false)

        view.langConfirmBtn.setOnClickListener {
            var language = ""
            for (zz in listLang) {
                if (zz.isSelected) language = zz.language!!
            }
            if (language.equals("")){
                myApplication.showToast("Please select Language..")
            }else{
                sessionManager.setSelectedLanguage(language)
                dismiss()
            }
        }

        getLanguages()
        return view.root
    }

    private fun getLanguages() {
        val getLangRequest = apiInterface.getLanguages(sessionManager.getToken())

        getLangRequest.enqueue(object : Callback<LanguageResponse?>{
            override fun onResponse(call: Call<LanguageResponse?>, response: Response<LanguageResponse?>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val listRes = response.body()!!.data[0].languages
                     if (listRes.size>0){
                         listLang = ArrayList()
                         for (zz in listRes) {
                             val ob = Languages()
                             ob.language = zz
                             ob.isSelected = sessionManager.getSelectedLanguage().equals(zz)
                             listLang.add(ob)
                         }
                         val langAdapter = LanguageAdapter(requireContext().applicationContext,listLang)
                            view.languageRecyclerView.adapter = langAdapter
                        }
                }else{
                    myApplication.printLogE("No Data ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<LanguageResponse?>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}


