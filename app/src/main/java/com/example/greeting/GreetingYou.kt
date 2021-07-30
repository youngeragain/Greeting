package com.example.greeting

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.File
import java.lang.ref.WeakReference

object GreetingYou {
    private const val tag = "special_permission_fragment"

    /**
     * 危险权限, 需要到特定页面手动授予
     *
     * INSTANT_APP_FOREGROUND_SERVICE api 26
     * LOADER_USAGE_STATS api 30
     * MANAGE_EXTERNAL_STORAGE api 30
     * MANAGE_MEDIA api 31
     * MANAGE_ONGOING_CALLS api 31
     * PACKAGE_USAGE_STATS api 23
     * SMS_FINANCIAL_TRANSACTIONS add in api 29 (android 10), deprecate in api 31 (android 12)
     * SYSTEM_ALERT_WINDOW api 1 (android 1.0)
     * USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER api 31 (android 12)
     *
     */
    private val constantsSpecialPermissions: List<String> = listOf(
        INSTANT_APP_FOREGROUND_SERVICE,
        MANAGE_EXTERNAL_STORAGE,
        ACCESS_MEDIA_LOCATION,
        PACKAGE_USAGE_STATS,
        SYSTEM_ALERT_WINDOW,
        WRITE_SETTINGS
    )

    private lateinit var specialPermissionDialog:SpecialPermissionDialog

    private val deniedPermissions: MutableList<String> = mutableListOf()
    private val grantedPermissions: MutableList<String> = mutableListOf()

    private lateinit var mHandler: Handler
    private lateinit var ctx: WeakReference<Context>

    private var holder: Any? = null
        private set(value) {
            mHandler = Handler(Looper.getMainLooper())
            if (value is AppCompatActivity) {
                ctx = WeakReference(value)
                field = value
            } else if (value is Fragment && value.requireContext() != null) {
                ctx = WeakReference(value.requireContext())
                field = value
            } else {
                Log.e("blue", "current only support AppcompatActivity and Fragment")
            }
        }

    private lateinit var emptyPermissionFragment:EmptyPermissionFragment

    internal var canDrawOverlays = false

    internal var canWrite = false

    internal var canManageExternalStorage = false

    private var grantedCallback:((List<String>)->Unit)? = null

    private var deniedCallback:((List<String>)->Unit)? = null

    private var runtimePermissions:List<String> = mutableListOf()

    private var specialPermissions:List<String> = mutableListOf()

    private val sdcard0File:File? by lazy {
        Environment.getExternalStorageDirectory()?.path?.let {
            File(it)
        }
    }

    fun putHolder(any: Any): GreetingYou {
        holder = any
        return this
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmName("GreetingYou_greeting")
    fun greeting(
        map: Map<String, String>,
        granted: (List<String>) -> Unit,
        denied: (List<String>) -> Unit
    ) {
        map.greeting(granted, denied)
    }

    fun addGrantedSpecialPermission(specialPermission: String) {
        if (!grantedPermissions.contains(specialPermission)) {
            grantedPermissions.add(specialPermission)
        }
        if(deniedPermissions.contains(specialPermission)){
            deniedPermissions.remove(specialPermission)
        }

    }

    private fun addDeniedSpecialPermission(specialPermission: String) {
        if (!deniedPermissions.contains(specialPermission))
            deniedPermissions.add(specialPermission)
        if(grantedPermissions.contains(specialPermission)){
            grantedPermissions.remove(specialPermission)
        }
    }

    fun showNextSpecialPermission(){
        if(!::specialPermissionDialog.isInitialized){
            return
        }else{
            if(specialPermissionDialog.isShowing){
                specialPermissionDialog.showNextPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSpecialPermissionDialog(specialPermissions: List<String>, reasons: Map<String, String?>){
        ctx.get()?.let {context->
            //过滤掉已经拥有的特殊权限
            val list = specialPermissions.toMutableList()
            updateSpecialPermissionIfNeeded()
            if(canWrite){//过滤掉修改设置
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.removeIf {
                        if(it== WRITE_SETTINGS){
                            addGrantedSpecialPermission(it)
                            true
                        }else{
                            false
                        }
                    }
                }else{
                    for(specialPermission in list){
                        if(specialPermission == WRITE_SETTINGS){
                            addGrantedSpecialPermission(specialPermission)
                            list.remove(specialPermission)
                            break
                        }
                    }
                }

            }else{
                val contains = list.contains(WRITE_SETTINGS)
                if(contains)
                    addDeniedSpecialPermission(WRITE_SETTINGS)
            }
            if(canDrawOverlays){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.removeIf {
                        if(it==SYSTEM_ALERT_WINDOW){
                            addGrantedSpecialPermission(it)
                            true
                        }else{
                            false
                        }
                    }
                }else{
                    for(specialPermission in list){
                        if(specialPermission == SYSTEM_ALERT_WINDOW){
                            addGrantedSpecialPermission(specialPermission)
                            list.remove(specialPermission)
                            break
                        }
                    }
                }
            }else{
                val contains = list.contains(SYSTEM_ALERT_WINDOW)
                if(contains)
                    addDeniedSpecialPermission(SYSTEM_ALERT_WINDOW)
            }
            if(canManageExternalStorage){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.removeIf {
                        if(it== MANAGE_EXTERNAL_STORAGE){
                            addGrantedSpecialPermission(MANAGE_EXTERNAL_STORAGE)
                            true
                        }else{
                            false
                        }
                    }
                }else{
                    for(specialPermission in list){
                        if(specialPermission == MANAGE_EXTERNAL_STORAGE){
                            addGrantedSpecialPermission(specialPermission)
                            list.remove(specialPermission)
                            break
                        }
                    }
                }
            }else{
                val contains = list.contains(MANAGE_EXTERNAL_STORAGE)
                if(contains){
                    addDeniedSpecialPermission(MANAGE_EXTERNAL_STORAGE)
                }
            }
            if(list.isEmpty()){
                Log.e("blue", "all special permissions is granted!")
                dispatchRequestCallback()
            }else{
                emptyPermissionFragment.specialPermissions = list
                specialPermissionDialog= SpecialPermissionDialog(context, list, reasons).apply {
                    setOnDismissListener { //消失的时候
                        dispatchRequestCallback()
                    }
                }
                specialPermissionDialog.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun Map<String, String?>.greeting(
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) {
        holder ?: return
        this.entries.map { it.key }.greeting(this, granted, denied)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun Pair<String, String?>.greeting(
        granted: ((String?) -> Unit)? = null,
        denied: ((String?) -> Unit)? = null){
        mapOf<String, String?>(this).greeting({
            if(!it.isNullOrEmpty()){
                if(it.size==1){
                    granted?.invoke(it[0])
                }else{
                    granted?.invoke(null)
                }
            }

        },{
            if(!it.isNullOrEmpty()){
                if(it.size==1){
                    denied?.invoke(it[0])
                }else{
                    denied?.invoke(null)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun List<String>.greeting(
        reasons: Map<String, String?>,
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) {
        holder ?: return
        grantedCallback = granted
        deniedCallback = denied
        val activity = (holder as? AppCompatActivity) ?: ((holder as? Fragment)?.requireActivity())
        if(!::emptyPermissionFragment.isInitialized){
            emptyPermissionFragment = EmptyPermissionFragment()
        }
        if(!emptyPermissionFragment.isAdded){
            activity?.supportFragmentManager?.let { fm ->
                fm.beginTransaction()
                    .add(emptyPermissionFragment, tag)
                    .commit()
            }
        }
        grantedPermissions.clear()
        deniedPermissions.clear()
        runtimePermissions = this.filterNot {
            getBoolean(it)
        }
        specialPermissions = this.filter {
            getBoolean(it)
        }

        if (runtimePermissions.isEmpty() && specialPermissions.isEmpty()) {
            //do nothing
        } else if(runtimePermissions.isNotEmpty() && specialPermissions.isNotEmpty()){//既有运行时权限，又有危险权限

                if(emptyPermissionFragment.isResumed){
                    //first 处理运行时权限
                    resolveRuntimePermissions(runtimePermissions, reasons){
                        //then 处理特殊权限
                        resolveSpecialPermissions(specialPermissions, reasons)
                    }
                }else{
                    mHandler.postDelayed({
                        //first 处理运行时权限
                        resolveRuntimePermissions(runtimePermissions, reasons){
                            //then 处理特殊权限
                            resolveSpecialPermissions(specialPermissions, reasons)
                        }
                    }, 0)
                }
        } else if (runtimePermissions.isEmpty()) {//只有危险权限
            resolveSpecialPermissions(specialPermissions, reasons)
        } else {//只有运行时权限
            if(emptyPermissionFragment.isResumed){
                resolveRuntimePermissions(runtimePermissions, reasons)
            }else{
                mHandler.postDelayed({
                    resolveRuntimePermissions(runtimePermissions, reasons)
                }, 0)
            }
        }
    }

    private fun getBoolean(permissionName: String):Boolean {
        return run{
            var b = false
            for (c in constantsSpecialPermissions) {
                b = b || (permissionName == c)
            }
            b
        }
    }

    private fun removeEmptyFragment(){
        val activity = (holder as? AppCompatActivity) ?: ((holder as? Fragment)?.requireActivity())
        activity?.supportFragmentManager?.let {fm->
            fm.findFragmentByTag(tag)?.let { fragment ->
                fm.beginTransaction()
                    .remove(fragment)
                    .commit()
            }
        }
    }

    private fun resolveRuntimePermissions(runtimePermissions: List<String>, reasons: Map<String, String?>, then:((then:(()->Unit)?)->Unit)?=null) {
        runtimePermissions?.let {
            emptyPermissionFragment.runtimePermissions = it
            emptyPermissionFragment.runtimePermissionCallback = { permissions, results ->
                for (i in permissions.indices) {
                    if (results[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissions.add(permissions[i])
                    } else if (results[i] == PackageManager.PERMISSION_DENIED) {
                        deniedPermissions.add(permissions[i])
                    }
                }

                then?: run {
                    dispatchRequestCallback()
                }
                then?.invoke(null)
            }
            emptyPermissionFragment.requestRuntimePermissions()
        }
    }

    private fun dispatchRequestCallback(){
        grantedCallback?.invoke(grantedPermissions)
        deniedCallback?.invoke(deniedPermissions)
        removeEmptyFragment()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resolveSpecialPermissions(specialPermissions: List<String>, reasons: Map<String, String?>) {
        showSpecialPermissionDialog(specialPermissions, reasons)
    }

    fun getCurrentSpecialPermissionShowPermissionIndex():Int? {
        return if(!::specialPermissionDialog.isInitialized){
            null
        }else{
            if(!specialPermissionDialog.isShowing){
                null
            }else{
                specialPermissionDialog.currentSpecialPermissionIndex()
            }
        }
    }

    fun currentSpecialPermissionShowPermissionIndexDecrement() {
        if(!::specialPermissionDialog.isInitialized){
            return
        }else{
            if(!specialPermissionDialog.isShowing){
                return
            }else{
                specialPermissionDialog.currentSpecialPermissionIndexDecrement()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateSpecialPermissionIfNeeded() {
        if(!::ctx.isInitialized)
            return
        if(ctx.get()==null){
            return
        }
        canDrawOverlays = Settings.canDrawOverlays(ctx.get())
        canWrite = Settings.System.canWrite(ctx.get())
        //只有android 10之后才会为null, 如果开发者最高适配了安卓10，并且Manifests.xml里面请求了legacy storage,这里不会为null
        canManageExternalStorage = sdcard0File !=null&&(sdcard0File?.canWrite())?:false
    }

}