package com.ntduc.utils.app_utils.activity

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntduc.apputils.getApps
import com.ntduc.utils.model.MyApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel : ViewModel() {
    var listAllApp: MutableLiveData<List<MyApp>> = MutableLiveData(listOf())
    var isLoadListAllApp = false

    fun loadAllApp(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val apps = context.getApps()
                val temp = apps.sortedWith { o1, o2 ->
                    o2.storage!!.compareTo(o1.storage!!)
                }
                val result = ArrayList<MyApp>()
                temp.forEach {
                    val myApp = MyApp(
                        it.name,
                        it.packageName,
                        it.icon,
                        it.storage
                    )
                    result.add(myApp)
                }
                listAllApp.postValue(result)
                isLoadListAllApp = true
            }
        }
    }
}