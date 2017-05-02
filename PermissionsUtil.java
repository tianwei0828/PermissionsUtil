package com.tian.wei.permissionsdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动态申请权限工具类
 * Created by tian.wei on 2017/4/26.
 */

public class PermissionsUtil {

    private PermissionsUtil() {
    }

    /**
     * 危险权限，targetSdkVersion >=23必须动态申请
     */
    public static final class Permission {
        private static final List<String> sCalendars = new ArrayList<>(2);
        private static final List<String> sCameras = new ArrayList<>(1);
        private static final List<String> sContactes = new ArrayList<>(3);
        private static final List<String> sLocations = new ArrayList<>(2);
        private static final List<String> sMicrophones = new ArrayList<>(1);
        private static final List<String> sPhones = new ArrayList<>(7);
        private static final List<String> sSensorses = new ArrayList<>(1);
        private static final List<String> sSmses = new ArrayList<>(5);
        private static final List<String> sStorages = new ArrayList<>(2);

        static {
            //Calendar
            sCalendars.add(Calendar.READ_CALENDAR);
            sCalendars.add(Calendar.WRITE_CALENDAR);
            //Camera
            sCameras.add(Camera.CAMERA);
            //Contacts
            sContactes.add(Contacts.WRITE_CONTACTS);
            sContactes.add(Contacts.READ_CONTACTS);
            sContactes.add(Contacts.GET_ACCOUNTS);
            //Location
            sLocations.add(Location.ACCESS_FINE_LOCATION);
            sLocations.add(Location.ACCESS_COARSE_LOCATION);
            //Microphone
            sMicrophones.add(Microphone.RECORD_AUDIO);
            //Phone
            sPhones.add(Phone.READ_PHONE_STATE);
            sPhones.add(Phone.CALL_PHONE);
            sPhones.add(Phone.READ_CALL_LOG);
            sPhones.add(Phone.WRITE_CALL_LOG);
            sPhones.add(Phone.ADD_VOICEMAIL);
            sPhones.add(Phone.USE_SIP);
            sPhones.add(Phone.PROCESS_OUTGOING_CALLS);
            //Sensors
            sSensorses.add(Sensors.BODY_SENSORS);
            //Sms
            sSmses.add(Sms.SEND_SMS);
            sSmses.add(Sms.RECEIVE_SMS);
            sSmses.add(Sms.READ_SMS);
            sSmses.add(Sms.RECEIVE_WAP_PUSH);
            sSmses.add(Sms.RECEIVE_MMS);
            //Storage
            sStorages.add(Storage.READ_EXTERNAL_STORAGE);
            sStorages.add(Storage.WRITE_EXTERNAL_STORAGE);
        }

        public static final class Calendar {
            public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
            public static final String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;
            private static final String MSG = "日历";
        }

        public static final class Camera {
            public static final String CAMERA = Manifest.permission.CAMERA;
            private static final String MSG = "相机";
        }

        public static final class Contacts {
            public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
            public static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
            public static final String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
            private static final String MSG = "联系人";
        }

        public static final class Location {
            public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
            public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
            private static final String MSG = "定位";
        }

        public static final class Microphone {
            public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
            private static final String MSG = "麦克风";
        }

        public static final class Phone {
            public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
            public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
            public static final String READ_CALL_LOG = Manifest.permission.READ_CALL_LOG;
            public static final String WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG;
            public static final String ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL;
            public static final String USE_SIP = Manifest.permission.USE_SIP;
            public static final String PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS;
            private static final String MSG = "电话";
        }

        public static final class Sensors {
            public static final String BODY_SENSORS = Manifest.permission.BODY_SENSORS;
            private static final String MSG = "传感器";
        }


        public static final class Sms {
            public static final String SEND_SMS = Manifest.permission.SEND_SMS;
            public static final String RECEIVE_SMS = Manifest.permission.RECEIVE_SMS;
            public static final String READ_SMS = Manifest.permission.READ_SMS;
            public static final String RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH;
            public static final String RECEIVE_MMS = Manifest.permission.RECEIVE_MMS;
            private static final String MSG = "短信";
        }

        public static final class Storage {
            public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
            public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            private static final String MSG = "存储";
        }
    }

    public static final int SETTINGS_REQ_CODE = 99;
    private static final String TAG = "PermissionsUtil";
    private static final String KEY_DENIED_PERMISSIONS = "deniedPermissions";
    private static final String KEY_REQUEST_CODE = "requestCode";
    private static final int MSG_UI_HANDLER_DEAL_DENIED_PERMISSION = 100;
    private static boolean sDebug = true;
    private Object mObject;
    private String[] mPermissions;
    private int mRequestCode;
    private String mRationaleTitle;
    private String mPositiveText = "确定";
    private String mNegativeText = "取消";
    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UI_HANDLER_DEAL_DENIED_PERMISSION:
                    Bundle data = msg.getData();
                    int requestCode = data.getInt(KEY_REQUEST_CODE);
                    String[] permissions = data.getStringArray(KEY_DENIED_PERMISSIONS);
                    Object object = msg.obj;
                    showAlertDialog(object, requestCode, permissions);
                    break;
            }
        }
    };


    private PermissionsUtil(@NonNull Object object) {
        if (isObjectIllegality(object))
            throw new IllegalArgumentException("Activity or Fragment must implements IPermissionsCallback");
        mObject = object;
    }

    public static PermissionsUtil with(@NonNull Activity activity) {
        return new PermissionsUtil(activity);
    }

    public static PermissionsUtil with(@NonNull Fragment fragment) {
        return new PermissionsUtil(fragment);
    }

    public static PermissionsUtil with(@NonNull android.app.Fragment fragment) {
        return new PermissionsUtil(fragment);
    }

    public PermissionsUtil permissions(@NonNull String... permissions) {
        if (hasEmpty(permissions))
            throw new IllegalArgumentException("permissions can't contain null");
        mPermissions = permissions;
        return this;
    }

    public PermissionsUtil requestCode(int requestCode) {
        mRequestCode = requestCode;
        return this;
    }

    public PermissionsUtil rationaleTitle(String title) {
        mRationaleTitle = title;
        return this;
    }

    public PermissionsUtil positiveText(String positiveText) {
        mPositiveText = positiveText;
        return this;
    }

    public PermissionsUtil negativeText(String negativeText) {
        mNegativeText = negativeText;
        return this;
    }

    public PermissionsUtil isDebug(boolean isDebug) {
        sDebug = isDebug;
        return this;
    }

    public PermissionsUtil request() {
        request(mObject, mRequestCode, mPermissions);
        return this;
    }

    public void request(Object object, int requestCode, String... permissions) {
        if (needRequest() && notGrantedAllPermissions(getActivity(object), permissions)) {
            List<String> unGrantedPermissionsList = createUnGrantedPermissionsList(object, permissions);
            PLog.d("request---" + "requestCode : " + requestCode + "---unGrantedPermissionsList : " + unGrantedPermissionsList);
            if (unGrantedPermissionsList.size() > 0) {
                requestPermissions(object, requestCode, listToStringArray(unGrantedPermissionsList));
                unGrantedPermissionsList.clear();
            } else {
                invokePermissionsGranted(object, requestCode, permissions);
            }
            unGrantedPermissionsList = null;
        } else {
            PLog.d("request---" + "requestCode : " + requestCode + "---permissionsGranted : " + stringArrayToList(permissions));
            invokePermissionsGranted(object, requestCode, permissions);
        }
    }

    private List<String> createUnGrantedPermissionsList(Object object, String... permissions) {
        List<String> unGrantedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (notGrantedPermission(getActivity(object), permission)) {
                unGrantedPermissionsList.add(permission);
            }
        }
        return unGrantedPermissionsList;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(Object object, int requestCode, String... permissions) {
        PLog.d("requestPermissions---" + "requestCode : " + requestCode + "---requestPermissions : " + stringArrayToList(permissions));
        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, permissions, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(permissions, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(permissions, requestCode);
        }
    }

    /**
     * @param object
     * @param requestCode
     * @param deniedPermissions denied permissions
     * @return
     */
    private void showAlertDialog(final Object object, final int requestCode, final String... deniedPermissions) {
        PLog.d("showAlertDialog --- " + "requestCode : " + requestCode + "--- deniedPermissions : " + stringArrayToList(deniedPermissions));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(mObject));
        if (!TextUtils.isEmpty(mRationaleTitle)) {
            builder.setTitle(mRationaleTitle);
        }
        builder.setMessage(createRationaleMsg(getActivity(object), deniedPermissions))
                .setPositiveButton(mPositiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goSetting(object);
                    }
                })
                .setNegativeButton(mNegativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        invokePermissionsDenied(object, requestCode, deniedPermissions);
                    }
                })
                .create()
                .show();
    }

    private boolean needRequest() {
        return Build.VERSION.SDK_INT >= 23;
    }

    private Activity getActivity(Object object) {
        Activity activity = null;
        if (object instanceof Activity) {
            activity = (Activity) object;
        } else if (object instanceof Fragment) {
            activity = ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            activity = ((android.app.Fragment) object).getActivity();
        }
        return activity;
    }

    public boolean grantedPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean notGrantedPermission(Activity activity, String permission) {
        return !grantedPermission(activity, permission);
    }

    public boolean grantedAllPermissions(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (notGrantedPermission(activity, permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean notGrantedAllPermissions(Activity activity, String... permissions) {
        return !grantedAllPermissions(activity, permissions);
    }

    private void dealDeniedPermissions(Object object, int requestCode, String... deniedPermissions) {
        PLog.d("dealDeniedPermissions --- " + "requestCode : " + requestCode + "--- deniedPermissions : " + stringArrayToList(deniedPermissions));
        Message message = mUiHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putStringArray(KEY_DENIED_PERMISSIONS, deniedPermissions);
        bundle.putInt(KEY_REQUEST_CODE, requestCode);
        message.setData(bundle);
        message.obj = object;
        message.what = MSG_UI_HANDLER_DEAL_DENIED_PERMISSION;
        mUiHandler.sendMessage(message);
    }

    private boolean isObjectLegal(Object object) {
        return (object instanceof IPermissionsCallback);
    }

    private boolean isObjectIllegality(Object object) {
        return !isObjectLegal(object);
    }

    private boolean hasEmpty(String... strings) {
        boolean hasEmpty = false;
        if (strings != null && strings.length > 0) {
            for (String s : strings) {
                if (TextUtils.isEmpty(s)) {
                    hasEmpty = true;
                    break;
                }
            }
        } else {
            hasEmpty = true;
        }
        return hasEmpty;
    }

    private String[] listToStringArray(List<String> stringList) {
        return stringList.toArray(new String[stringList.size()]);
    }

    private List<String> stringArrayToList(String[] strings) {
        return Arrays.asList(strings);
    }

    private String getAppName(Context context) {
        String appName = "";
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    private String createRationaleMsg(Context context, String... permissions) {
        String permissionsMsg = createPermissionsMsg(permissions);
        permissionsMsg = permissionsMsg.substring(0, permissionsMsg.length() - 1);
        return getAppName(context) + "需要" + permissionsMsg + "权限，是否去设置";
    }

    private String createPermissionsMsg(String... permissions) {
        StringBuilder builder = new StringBuilder();
        boolean grantedCalendar = false;
        boolean grantedCamera = false;
        boolean grantedContacts = false;
        boolean grantedLocation = false;
        boolean grantedMicrophone = false;
        boolean grantedPhone = false;
        boolean grantedSensors = false;
        boolean grantedSms = false;
        boolean grantedStorage = false;
        for (String permission : permissions) {
            if (!grantedCalendar && Permission.sCalendars.contains(permission)) {
                builder.append(Permission.Calendar.MSG);
                builder.append("、");
                grantedCalendar = true;
            }
            if (!grantedCamera && Permission.sCameras.contains(permission)) {
                builder.append(Permission.Camera.MSG);
                builder.append("、");
                grantedCamera = true;
            }
            if (!grantedContacts && Permission.sContactes.contains(permission)) {
                builder.append(Permission.Contacts.MSG);
                builder.append("、");
                grantedContacts = true;
            }
            if (!grantedLocation && Permission.sLocations.contains(permission)) {
                builder.append(Permission.Location.MSG);
                builder.append("、");
                grantedLocation = true;
            }
            if (!grantedMicrophone && Permission.sMicrophones.contains(permission)) {
                builder.append(Permission.Microphone.MSG);
                builder.append("、");
                grantedMicrophone = true;
            }
            if (!grantedPhone && Permission.sPhones.contains(permission)) {
                builder.append(Permission.Phone.MSG);
                builder.append("、");
                grantedPhone = true;
            }
            if (!grantedSensors && Permission.sSensorses.contains(permission)) {
                builder.append(Permission.Sensors.MSG);
                builder.append("、");
                grantedSensors = true;
            }
            if (!grantedSms && Permission.sSmses.contains(permission)) {
                builder.append(Permission.Sms.MSG);
                builder.append("、");
                grantedSms = true;
            }
            if (!grantedStorage && Permission.sStorages.contains(permission)) {
                builder.append(Permission.Storage.MSG);
                builder.append("、");
                grantedStorage = true;
            }
        }
        return builder.toString();
    }

    private IPermissionsCallback getPermissionsCallback(Object object) {
        return (IPermissionsCallback) object;
    }

    private void invokePermissionsGranted(Object object, int requestCode, String... permissions) {
        getPermissionsCallback(object).onPermissionsGranted(requestCode, permissions);
    }

    private void invokePermissionsDenied(Object object, int requestCode, String... permissions) {
        getPermissionsCallback(object).onPermissionsDenied(requestCode, permissions);
    }

    public interface IPermissionsCallback {
        void onPermissionsGranted(int requestCode, String... permission);

        void onPermissionsDenied(int requestCode, String... permission);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    deniedPermissions.add(permissions[i]);
            }
            PLog.d("onRequestPermissionsResult--- " + "requestCode : " + requestCode + "--- deniedPermissions : " + deniedPermissions);
            if (deniedPermissions.size() > 0) {
                dealDeniedPermissions(mObject, requestCode, listToStringArray(deniedPermissions));
            } else {
                invokePermissionsGranted(mObject, requestCode, permissions);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQ_CODE) {
            List<String> unGrantedPermissionsList = createUnGrantedPermissionsList(mObject, mPermissions);
            PLog.d("onActivityResult --- " + "requestCode : " + requestCode + "---" + "unGrantedPermissionsList : " + unGrantedPermissionsList);
            if (unGrantedPermissionsList.size() > 0) {
                invokePermissionsDenied(mObject, mRequestCode, listToStringArray(unGrantedPermissionsList));
            } else {
                invokePermissionsGranted(mObject, mRequestCode, mPermissions);
            }
        }
    }

    private void goSetting(Object object) {
        PLog.d("goSetting");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity(object).getPackageName(), null);
        intent.setData(uri);
        if (object instanceof Activity) {
            ((Activity) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        } else if (object instanceof Fragment) {
            ((Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
        }
    }

    static final class PLog {
        private static void d(String msg) {
            if (sDebug)
                Log.d(TAG, msg);
        }

        private static void e(String msg) {
            if (sDebug)
                Log.e(TAG, msg);
        }
    }
}
