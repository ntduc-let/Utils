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
                val result = ArrayList<MyApp>()
                apps.forEach {
                    val myApp = MyApp(
                        name = it.name,
                        packageName = it.packageName,
                        icon = it.icon,
                        category = it.category,
                        dataDir = it.dataDir,
                        minSdkVersion = it.minSdkVersion,
                        targetSdkVersion = it.targetSdkVersion,
                        nativeLibraryDir = it.nativeLibraryDir,
                        processName = it.processName,
                        publicSourceDir = it.publicSourceDir,
                        sourceDir = it.sourceDir,
                        splitNames = it.splitNames,
                        splitPublicSourceDirs = it.splitPublicSourceDirs,
                        splitSourceDirs = it.splitSourceDirs,
                        storageUuid = it.storageUuid,
                        taskAffinity = it.taskAffinity,
                        uid = it.uid
                    )
                    result.add(myApp)
                }
                listAllApp.postValue(result)
                isLoadListAllApp = true
            }
        }
    }
}