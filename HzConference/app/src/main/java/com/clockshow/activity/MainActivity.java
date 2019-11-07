package com.clockshow.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.clockshow.R;
import com.clockshow.logic.login.ILoginContract;
import com.clockshow.logic.login.LoginPresenter;
import com.clockshow.ui.base.MVPBaseActivity;
import com.clockshow.ui.base.MVPBasePresenter;
import com.clockshow.ui.conference.CreateConfActivity;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.loginmgr.LoginConstant;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends MVPBaseActivity <ILoginContract.LoginBaseView, LoginPresenter>
        implements ILoginContract.LoginBaseView, LocBroadcastReceiver,EasyPermissions.PermissionCallbacks  {

    private Button btnClick;
    private Button btnAddConference;




    /**
     * 布局初始化操作
     */
    @Override
    public void initializeComposition() {

        setContentView(R.layout.activity_main);
        btnClick = findViewById(R.id.login);
        btnAddConference = findViewById(R.id.start_conference);
        getPermission();



        /**
         * 登录操作
         */
        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new Thread(runnable).start();
            }
        });


        /**
         * 添加会议
         */
        btnAddConference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, CreateConfActivity.class);
                startActivity(intent);
            }
        });
    }


    //获取应用需要的权限
    private void getPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            //读取sd卡的权限
            String[] mPermissionList = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.RECORD_AUDIO
            };


            if (EasyPermissions.hasPermissions(this, mPermissionList)) {
                //已经同意过
//--------------------------------------------权限通过进行账户信息配置----------------------------------------------------------------
                //1、配置登陆个人账号
                String account="01000402";
                String password="a1234567";
                //2、设置服务器相关配置
                SharedPreferences  dataSharePreference= getSharedPreferences(LoginConstant.FILE_NAME, Activity.MODE_PRIVATE);
                dataSharePreference.edit().putBoolean(LoginConstant.TUP_VPN, false)
                        .putString(LoginConstant.TUP_REGSERVER, "222.88.225.23")
                        .putString(LoginConstant.TUP_PORT, "5061")
                        .commit();


                dataSharePreference.edit().putInt(LoginConstant.TUP_SRTP, 0)   //2 mandatory  1 optional 0 disable
                        .putInt(LoginConstant.TLS_PORT, 1)           // 0 udp  1 tls 2 tcp
                        .commit();

                dataSharePreference.edit()
                        .putInt(LoginConstant.VC_TYPE, 2)                        //1  vc_hosted  2、vc_smc
                        .commit();
                //3、进行登录操作
                mPresenter.onLoginParams();
//--------------------------------------------权限通过进行账户信息配置end----------------------------------------------------------------

            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        this,  //上下文
                        "需要读内存卡的权限、照相机等权限权限", //提示文言
                        10, //请求码
                        mPermissionList //权限列表
                );
            }
        } else {
            //SDK小于23,检查不到权限，只能说全部权限都有
        }
    }

    ////////权限问题//////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //把申请权限的回调交由EasyPermissions处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case 10:
                Log.i("获取成功的权限", "获取成功的权限" + perms);
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case 10:
                Log.i("获取失败的权限", "获取失败的权限" + perms);
                break;
        }
    }







    /*最新的API要求需要在子线程里面判断网络信息，和一些耗时操作*/
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mPresenter.doLogin("01000402", "a1234567");
        }
    };





    @Override
    public void initializeData() {
        mPresenter.initServerData();
    }



    @Override
    protected ILoginContract.LoginBaseView createView()
    {
        return this;
    }

    @Override
    protected LoginPresenter createPresenter()
    {
        return new LoginPresenter(this);
    }

    @Override
    public void dismissLoginDialog() {

    }

    @Override
    public void setEditText(String account, String password) {

    }

    @Override
    public void onReceive(String broadcastName, Object obj) {

    }
}
