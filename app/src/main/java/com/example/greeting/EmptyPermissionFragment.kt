package com.example.greeting

import android.Manifest
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import java.io.File

typealias PermissionCallback = (
    permissions: Array<out String>,
    grantResults: IntArray
)->Unit

internal class EmptyPermissionFragment : Fragment(){
    internal var runtimePermissionCallback: PermissionCallback?=null
    internal var runtimePermissions:List<String>?=null
    internal var specialPermissions:List<String>?=null
    private val mRequestCode = 65530

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        specialPermissions?.let {
            GreetingYou.updateSpecialPermissionIfNeeded()
            GreetingYou.getCurrentSpecialPermissionShowPermissionIndex()?.let {currentShowingSpecialPermissionIndex->
                if(it.size>currentShowingSpecialPermissionIndex&&currentShowingSpecialPermissionIndex>=0){
                    if(GreetingYou.canDrawOverlays&&
                        it.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)&&
                        it[currentShowingSpecialPermissionIndex] == Manifest.permission.SYSTEM_ALERT_WINDOW
                    ){
                        GreetingYou.apply {
                            addGrantedSpecialPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                            currentSpecialPermissionShowPermissionIndexDecrement()
                            showNextSpecialPermission()
                        }
                    }

                    if(GreetingYou.canWrite&&
                        it.contains(Manifest.permission.WRITE_SETTINGS)&&
                        it[currentShowingSpecialPermissionIndex]==Manifest.permission.WRITE_SETTINGS
                    ){
                        GreetingYou.apply {
                            addGrantedSpecialPermission(Manifest.permission.WRITE_SETTINGS)
                            currentSpecialPermissionShowPermissionIndexDecrement()
                            showNextSpecialPermission()
                        }
                    }
                    if(GreetingYou.canManageExternalStorage&&
                        it.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)&&
                        it[currentShowingSpecialPermissionIndex]==Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    ){
                        GreetingYou.apply {
                            addGrantedSpecialPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                            currentSpecialPermissionShowPermissionIndexDecrement()
                            showNextSpecialPermission()
                        }
                    }
                }
            }
        }
    }

    fun requestRuntimePermissions(){
        runtimePermissions?.let {
            requestPermissions(it.toTypedArray(), mRequestCode)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(mRequestCode == requestCode)
            runtimePermissionCallback?.invoke(permissions, grantResults)
    }
}