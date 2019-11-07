package com.clockshow;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.clockshow.common.UIConstants;
import com.clockshow.logic.call.CallFunc;
import com.clockshow.logic.conference.ConfFunc;
import com.clockshow.logic.login.LoginFunc;
import com.clockshow.util.FileUtil;
import com.huawei.application.BaseApp;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.util.CrashUtil;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.contactmgr.ContactMgr;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.servicemgr.ServiceMgr;
import com.huawei.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MyApplication extends Application {

    private static final int EXPECTED_FILE_LENGTH = 7;

    private static final String FRONT_PKG = "com.clockshow";

    String[] allpermissions=new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ,Manifest.permission.CAMERA
                                        ,Manifest.permission.CALL_PHONE};
    @Override
    public void onCreate() {
        super.onCreate();
        //设置初始化页面
        //applypermission();
        BaseApp.setApp(this);
        if (!isFrontProcess(this,FRONT_PKG))
        {
            LocContext.init(this);
            CrashUtil.getInstance().init(this);
            Log.i("SDKDemo", "onCreate: PUSH Process.");
            return;
        }
        String appPath = getApplicationInfo().dataDir + "/lib";
        ServiceMgr.getServiceMgr().startService(this, appPath);
        Log.i("SDKDemo", "onCreate: MAIN Process.");
        LoginMgr.getInstance().regLoginEventNotification(LoginFunc.getInstance());      //初始化登录
        CallMgr.getInstance().regCallServiceNotification(CallFunc.getInstance());       //初始化呼叫
        MeetingMgr.getInstance().regConfServiceNotification(ConfFunc.getInstance());    //初始化会议
        // ContactMgr.getInstance().setContactNotification(ContactFunc.getInstance());
        initResourceFile();
    }


    /*public void applypermission(){
        if(Build.VERSION.SDK_INT>=23){
            boolean needapply=false;
            for(int i=0;i<allpermissions.length;i++){
                int chechpermission= ContextCompat.checkSelfPermission(getApplicationContext(),
                        allpermissions[i]);
                if(chechpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply=true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions((Activity) getApplicationContext(),allpermissions,1);
            }
        }
    }*/



    private void initResourceFile()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                initDataConfRes();
            }
        }).start();
    }



    private void initDataConfRes()
    {
        String path = LocContext.
                getContext().getFilesDir() + "/AnnoRes";
        File file = new File(path);
        if (file.exists())
        {
            LogUtil.i(UIConstants.DEMO_TAG,  file.getAbsolutePath());
            File[] files = file.listFiles();
            if (null != files && EXPECTED_FILE_LENGTH == files.length)
            {
                return;
            }
            else
            {
                FileUtil.deleteFile(file);
            }
        }

        try
        {
            InputStream inputStream = getAssets().open("AnnoRes.zip");
            ZipUtil.unZipFile(inputStream, path);
        }
        catch (IOException e)
        {
            LogUtil.i(UIConstants.DEMO_TAG,  "close...Exception->e" + e.toString());
        }
    }





    private static boolean isFrontProcess(Context context, String frontPkg)
    {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        if (infos == null || infos.isEmpty())
        {
            return false;
        }

        final int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : infos)
        {
            if (info.pid == pid)
            {
                Log.i(UIConstants.DEMO_TAG, "processName-->"+info.processName);
                return frontPkg.equals(info.processName);
            }
        }

        return false;
    }












}
