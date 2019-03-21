package com.tw.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Created by wei.tian
 * 2019/3/19
 */
class PermissionsUtil {

    class Permission private constructor() {
        companion object {
            internal val sCalendars = ArrayList<String>(2)
            internal val sCameras = ArrayList<String>(1)
            internal val sContactes = ArrayList<String>(3)
            internal val sLocations = ArrayList<String>(2)
            internal val sMicrophones = ArrayList<String>(1)
            internal val sPhones = ArrayList<String>(7)
            internal val sSensorses = ArrayList<String>(1)
            internal val sSmses = ArrayList<String>(5)
            internal val sStorages = ArrayList<String>(2)

            init {
                //Calendar
                sCalendars.add(Calendar.READ_CALENDAR)
                sCalendars.add(Calendar.WRITE_CALENDAR)
                //Camera
                sCameras.add(Camera.CAMERA)
                //Contacts
                sContactes.add(Contacts.WRITE_CONTACTS)
                sContactes.add(Contacts.READ_CONTACTS)
                sContactes.add(Contacts.GET_ACCOUNTS)
                //Location
                sLocations.add(Location.ACCESS_FINE_LOCATION)
                sLocations.add(Location.ACCESS_COARSE_LOCATION)
                //Microphone
                sMicrophones.add(Microphone.RECORD_AUDIO)
                //Phone
                sPhones.add(Phone.READ_PHONE_STATE)
                sPhones.add(Phone.CALL_PHONE)
                sPhones.add(Phone.READ_CALL_LOG)
                sPhones.add(Phone.WRITE_CALL_LOG)
                sPhones.add(Phone.ADD_VOICEMAIL)
                sPhones.add(Phone.USE_SIP)
                sPhones.add(Phone.PROCESS_OUTGOING_CALLS)
                //Sensors
                sSensorses.add(Sensors.BODY_SENSORS)
                //Sms
                sSmses.add(Sms.SEND_SMS)
                sSmses.add(Sms.RECEIVE_SMS)
                sSmses.add(Sms.READ_SMS)
                sSmses.add(Sms.RECEIVE_WAP_PUSH)
                sSmses.add(Sms.RECEIVE_MMS)
                //Storage
                sStorages.add(Storage.READ_EXTERNAL_STORAGE)
                sStorages.add(Storage.WRITE_EXTERNAL_STORAGE)
            }
        }

        object Calendar {
            const val READ_CALENDAR = Manifest.permission.READ_CALENDAR
            const val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR
            internal const val MSG = "日历"
        }

        object Camera {
            const val CAMERA = Manifest.permission.CAMERA
            internal const val MSG = "相机"
        }

        object Contacts {
            const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
            const val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
            const val GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS
            internal const val MSG = "联系人"
        }

        object Location {
            const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
            const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
            internal const val MSG = "定位"
        }

        object Microphone {
            const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
            internal const val MSG = "麦克风"
        }

        object Phone {
            const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
            const val CALL_PHONE = Manifest.permission.CALL_PHONE
            const val READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
            const val WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG
            const val ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL
            const val USE_SIP = Manifest.permission.USE_SIP
            const val PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS
            internal const val MSG = "电话"
        }

        object Sensors {
            const val BODY_SENSORS = Manifest.permission.BODY_SENSORS
            internal const val MSG = "传感器"
        }

        object Sms {
            const val SEND_SMS = Manifest.permission.SEND_SMS
            const val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
            const val READ_SMS = Manifest.permission.READ_SMS
            const val RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH
            const val RECEIVE_MMS = Manifest.permission.RECEIVE_MMS
            internal const val MSG = "短信"
        }

        object Storage {
            const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
            const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
            internal const val MSG = "存储"
        }


    }

    private val SETTINGS_REQ_CODE = 99
    private val TAG = "PermissionsUtil"
    private val KEY_DENIED_PERMISSIONS = "deniedPermissions"
    private val KEY_REQUEST_CODE = "requestCode"
    private val MSG_UI_HANDLER_DEAL_DENIED_PERMISSION = 100
    private var sDebug = true
    private var mAny: Any
    private lateinit var mPermissions: Array<String>
    private var mRequestCode: Int = 0
    private var mRationaleTitle: String? = null
    private var mPositiveText = "确定"
    private var mNegativeText = "取消"
    private val mUiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UI_HANDLER_DEAL_DENIED_PERMISSION -> {
                    val data = msg.data
                    val requestCode = data.getInt(KEY_REQUEST_CODE)
                    val permissions: Array<String> = data.getStringArray(KEY_DENIED_PERMISSIONS)
                    val any = msg.obj
                    showAlertDialog(any, requestCode, permissions)
                }
            }
        }
    }

    private constructor(any: Any) {
        if (!isObjectLegal(any)) {
            throw IllegalArgumentException("Activity or Fragment must implements IPermissionsCallback")
        }
        mAny = any
    }

    companion object {
        fun with(activity: Activity): PermissionsUtil {
            return PermissionsUtil(activity)
        }

        fun with(fragment: Fragment): PermissionsUtil {
            return PermissionsUtil(fragment)
        }

        fun with(fragment: android.app.Fragment): PermissionsUtil {
            return PermissionsUtil(fragment)
        }
    }

    fun permissions(vararg permissions: String): PermissionsUtil {
        val realPermissions = filterBlank(*permissions)
        if (realPermissions.isEmpty()) {
            throw IllegalArgumentException("permissions are blank")
        }
        mPermissions = realPermissions
        return this
    }

    fun requestCode(requestCode: Int): PermissionsUtil {
        mRequestCode = requestCode
        return this
    }

    fun rationaleTitle(title: String): PermissionsUtil {
        mRationaleTitle = title
        return this
    }

    fun positiveText(positiveText: String): PermissionsUtil {
        mPositiveText = positiveText
        return this
    }

    fun negativeText(negativeText: String): PermissionsUtil {
        mNegativeText = negativeText
        return this
    }

    fun isDebug(isDebug: Boolean): PermissionsUtil {
        sDebug = isDebug
        return this
    }

    fun request(): PermissionsUtil {
        request(mAny, mRequestCode, mPermissions)
        return this
    }

    private fun request(any: Any, requestCode: Int, permissions: Array<String>) {
        val activity = requireActivityNotNull(getActivity(any))
        if (needRequest() && notGrantedAllPermissions(activity, permissions)) {
            val unGrantedPermissionsList = createUnGrantedPermissionsList(any, permissions)
            logD("request---requestCode: $requestCode ---unGrantedPermissionsList: $unGrantedPermissionsList")
            if (unGrantedPermissionsList.isNotEmpty()) {
                requestPermissions(any, requestCode, unGrantedPermissionsList.toTypedArray())
            } else {
                invokePermissionsGranted(any, requestCode, permissions)
            }
        } else {
            logD(
                "request--- requestCode: $requestCode ---permissionsGranted: ${permissions.toList()}"
            )
            invokePermissionsGranted(any, requestCode, permissions)
        }
    }

    private fun createUnGrantedPermissionsList(any: Any, permissions: Array<String>): List<String> {
        val activity = requireActivityNotNull(getActivity(any))
        return permissions.filter { notGrantedPermission(activity, it) }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestPermissions(any: Any, requestCode: Int, permissions: Array<String>) {
        logD(
            "requestPermissions--- requestCode: $requestCode ---requestPermissions: ${permissions.toList()}"
        )
        when (any) {
            is Activity -> ActivityCompat.requestPermissions(any, permissions, requestCode)
            is Fragment -> any.requestPermissions(permissions, requestCode)
            is android.app.Fragment -> any.requestPermissions(permissions, requestCode)
        }
    }

    /**
     * @param `any`
     * @param requestCode
     * @param deniedPermissions denied permissions
     * @return
     */
    private fun showAlertDialog(any: Any, requestCode: Int, deniedPermissions: Array<String>) {
        logD(
            "showAlertDialog --- requestCode: $requestCode --- deniedPermissions: $deniedPermissions"
        )
        val activity = requireActivityNotNull(getActivity(any))
        val builder = AlertDialog.Builder(getActivity(any))
        mRationaleTitle?.isNotBlank().let { builder.setTitle(mRationaleTitle) }
        builder.setMessage(createRationaleMsg(activity, deniedPermissions))
            .setPositiveButton(
                mPositiveText
            ) { dialog, which -> goSetting(any) }
            .setNegativeButton(
                mNegativeText
            ) { dialog, which ->
                invokePermissionsDenied(
                    any,
                    requestCode,
                    deniedPermissions
                )
            }
            .create()
            .show()
    }

    private fun needRequest(): Boolean {
        return Build.VERSION.SDK_INT >= 23
    }

    private fun getActivity(any: Any): Activity? {
        return when (any) {
            is Activity -> any
            is Fragment -> any.activity
            is android.app.Fragment -> any.activity
            else -> null
        }
    }

    private fun requireActivityNotNull(activity: Activity?): Activity {
        return activity ?: let { throw NullPointerException("activity == null") }
    }

    private fun grantedPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) === PackageManager.PERMISSION_GRANTED
    }

    private fun notGrantedPermission(activity: Activity, permission: String): Boolean {
        return !grantedPermission(activity, permission)
    }

    private fun grantedAllPermissions(activity: Activity, permissions: Array<String>): Boolean {
        permissions.forEach {
            if (notGrantedPermission(activity, it)) {
                return false
            }
        }
        return true
    }

    private fun notGrantedAllPermissions(activity: Activity, permissions: Array<String>): Boolean {
        return !grantedAllPermissions(activity, permissions)
    }

    private fun dealDeniedPermissions(any: Any, requestCode: Int, deniedPermissions: Array<String>) {
        logD(
            "dealDeniedPermissions --- requestCode: $requestCode --- deniedPermissions: $deniedPermissions"
        )
        val bundle = Bundle().apply {
            putStringArray(KEY_DENIED_PERMISSIONS, deniedPermissions)
            putInt(KEY_REQUEST_CODE, requestCode)
        }
        val message = Message.obtain().apply {
            data = bundle
            obj = any
            what = MSG_UI_HANDLER_DEAL_DENIED_PERMISSION
        }
        mUiHandler.sendMessage(message)
    }

    private fun isObjectLegal(any: Any): Boolean {
        return any is IPermissionsCallback
    }

    private fun filterBlank(vararg params: String): Array<String> {
        return params.filter { it.isNotBlank() }.toTypedArray()
    }

    private fun getAppName(context: Context): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            packageManager.getApplicationLabel(applicationInfo) as String
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "当前应用"
        }
    }

    private fun createRationaleMsg(context: Context, permissions: Array<String>): String {
        return "${getAppName(context)}需要${createPermissionsMsg(permissions)}权限，是否去设置?"
    }

    private fun createPermissionsMsg(permissions: Array<String>): String {
        val builder = StringBuilder()
        var grantedCalendar = false
        var grantedCamera = false
        var grantedContacts = false
        var grantedLocation = false
        var grantedMicrophone = false
        var grantedPhone = false
        var grantedSensors = false
        var grantedSms = false
        var grantedStorage = false
        permissions.forEach { permission ->
            if (!grantedCalendar && Permission.sCalendars.contains(permission)) {
                builder.append(Permission.Calendar.MSG)
                builder.append("、")
                grantedCalendar = true
            }
            if (!grantedCamera && Permission.sCameras.contains(permission)) {
                builder.append(Permission.Camera.MSG)
                builder.append("、")
                grantedCamera = true
            }
            if (!grantedContacts && Permission.sContactes.contains(permission)) {
                builder.append(Permission.Contacts.MSG)
                builder.append("、")
                grantedContacts = true
            }
            if (!grantedLocation && Permission.sLocations.contains(permission)) {
                builder.append(Permission.Location.MSG)
                builder.append("、")
                grantedLocation = true
            }
            if (!grantedMicrophone && Permission.sMicrophones.contains(permission)) {
                builder.append(Permission.Microphone.MSG)
                builder.append("、")
                grantedMicrophone = true
            }
            if (!grantedPhone && Permission.sPhones.contains(permission)) {
                builder.append(Permission.Phone.MSG)
                builder.append("、")
                grantedPhone = true
            }
            if (!grantedSensors && Permission.sSensorses.contains(permission)) {
                builder.append(Permission.Sensors.MSG)
                builder.append("、")
                grantedSensors = true
            }
            if (!grantedSms && Permission.sSmses.contains(permission)) {
                builder.append(Permission.Sms.MSG)
                builder.append("、")
                grantedSms = true
            }
            if (!grantedStorage && Permission.sStorages.contains(permission)) {
                builder.append(Permission.Storage.MSG)
                builder.append("、")
                grantedStorage = true
            }
        }
        return builder.toString().run { substring(0, length - 1) }
    }

    private fun getPermissionsCallback(any: Any): IPermissionsCallback {
        return any as IPermissionsCallback
    }

    private fun invokePermissionsGranted(any: Any, requestCode: Int, permissions: Array<String>) {
        getPermissionsCallback(any).onPermissionsGranted(requestCode, permissions)
    }

    private fun invokePermissionsDenied(any: Any, requestCode: Int, permissions: Array<String>) {
        getPermissionsCallback(any).onPermissionsDenied(requestCode, permissions)
    }

    interface IPermissionsCallback {
        fun onPermissionsGranted(requestCode: Int, permissions: Array<String>)

        fun onPermissionsDenied(requestCode: Int, permissions: Array<String>)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == mRequestCode) {
            val deniedPermissions = mutableListOf<String>()
            grantResults
                .filter { it != PackageManager.PERMISSION_GRANTED }
                .takeIf { it.isNotEmpty() }
                ?.forEachIndexed { index, _ ->
                    deniedPermissions.add(permissions[index])
                }
            logD("onRequestPermissionsResult --- requestCode: $requestCode --- deniedPermissions: $deniedPermissions")
            if (deniedPermissions.isNotEmpty()) {
                dealDeniedPermissions(mAny, requestCode, deniedPermissions.toTypedArray())
            } else {
                invokePermissionsGranted(mAny, requestCode, permissions)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SETTINGS_REQ_CODE) {
            val unGrantedPermissionsList = createUnGrantedPermissionsList(mAny, mPermissions)
            logD("onActivityResult --- requestCode: $requestCode ---unGrantedPermissionsList: $unGrantedPermissionsList")
            if (unGrantedPermissionsList.isNotEmpty()) {
                invokePermissionsDenied(mAny, mRequestCode, unGrantedPermissionsList.toTypedArray())
            } else {
                invokePermissionsGranted(mAny, mRequestCode, mPermissions)
            }
        }
    }

    private fun goSetting(any: Any) {
        logD("goSetting")
        val activity = requireActivityNotNull(getActivity(any))
        val uri = Uri.fromParts("package", activity.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = uri }
        when (any) {
            is Activity -> any.startActivityForResult(intent, SETTINGS_REQ_CODE)
            is Fragment -> any.startActivityForResult(intent, SETTINGS_REQ_CODE)
            is android.app.Fragment -> any.startActivityForResult(intent, SETTINGS_REQ_CODE)
        }
    }

    private fun logD(msg: String) {
        if (sDebug)
            Log.d(TAG, msg)
    }
}