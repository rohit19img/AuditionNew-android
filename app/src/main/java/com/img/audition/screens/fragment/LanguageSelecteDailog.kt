package com.img.audition.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment.*
import com.img.audition.R
import com.img.audition.dataModel.LanguageResponse
import com.img.audition.dataModel.Languages
import com.img.audition.databinding.LanguageSelectDailogBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LanguageSelecteDialog() : BottomSheetDialogFragment() {
    private  var langAdapter: LanguageAdapter? = null
    private lateinit var listRes: java.util.ArrayList<String>
    private val TAG = "LanguageSelecteDialog"
    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
    }
    private var listLang: ArrayList<Languages> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    lateinit var view: LanguageSelectDailogBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         view = LanguageSelectDailogBinding.inflate(inflater,container,false)

        return view.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        view.langConfirmBtn.setOnClickListener {
            var language = ""
            for (zz in listLang) {
                if (zz.isSelected) language = zz.language!!
            }
            if (language == ""){
                Toast.makeText(view1.context,"Please select Language..", Toast.LENGTH_SHORT).show()
            }else{
                sessionManager.setSelectedLanguage(language)
                dismiss()
            }
        }

        getLanguages(view1.context)
    }

    private fun getLanguages(context: Context) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        val getLangRequest = apiInterface.getLanguages(sessionManager.getToken())

        getLangRequest.enqueue(object : Callback<LanguageResponse?>{
            override fun onResponse(call: Call<LanguageResponse?>, response: Response<LanguageResponse?>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                     listRes = response.body()!!.data[0].languages
                     if (listRes.size>0){
                         listLang = ArrayList()
                         for (zz in listRes) {
                             val ob = Languages()
                             ob.language = zz
                             ob.isSelected = sessionManager.getSelectedLanguage().equals(zz)
                             listLang.add(ob)
                         }
                         langAdapter = LanguageAdapter(requireContext().applicationContext,listLang)
                            view.languageRecyclerView.adapter = langAdapter
                        }
                }else{
                    Log.d(TAG, "No Data")
                }
            }

            override fun onFailure(call: Call<LanguageResponse?>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            listLang.clear()
            listRes.clear()
            langAdapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}


