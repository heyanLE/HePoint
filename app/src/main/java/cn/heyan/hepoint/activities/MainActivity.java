package cn.heyan.hepoint.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import cn.heyan.hepoint.R;
import cn.heyan.hepoint.services.MainService;
import cn.heyan.hepoint.utils.DataMoniter;

import static cn.heyan.hepoint.utils.GetSystem.SYS_EMUI;
import static cn.heyan.hepoint.utils.GetSystem.SYS_MIUI;
import static cn.heyan.hepoint.utils.GetSystem.getSystem;

/**
 * Created by 13717 on 2017/7/12 0012.
 */

public class MainActivity extends AppCompatActivity{

    SwitchCompat switchCompat ;
    SeekBar seekBarA;
    SeekBar seekBarS;
    ImageButton imageButton;

    private static int alpha ;
    private static int size;

    private static Context context;


    Messenger mService = null;
    boolean mBound;
    public void sayUpdate(int T,int TT) {
        if (!mBound) return;
        // 向Service发送一个Message
        Message msg = Message.obtain(null, T, TT, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Activity已经绑定了Service
            // 通过参数service来创建Messenger对象，这个对象可以向Service发送Message，与Service进行通信
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);



        context = this;



        alpha = Integer.parseInt(DataMoniter.read("Alpha",this)) ;
        size = Integer.parseInt(DataMoniter.read("Size",this)) ;



        switchCompat = (SwitchCompat) findViewById(R.id.main_switch);

        if(isServiceRunning()){
            switchCompat.setChecked(true);
            bindService(new Intent(this, MainService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }else{
            switchCompat.setChecked(isServiceRunning());
        }

        imageButton = (ImageButton)findViewById(R.id.main_imageView_shield);

        imageButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                String system = getSystem();
                Intent intent = new Intent();
                if(system.equals(SYS_EMUI)){//华为
                    ComponentName componentName = new ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                    intent.setComponent(componentName);
                    Toast.makeText(getApplicationContext(), "EMUI 请允许HePoint后台运行。",
                            Toast.LENGTH_SHORT).show();
                }else if(system.equals(SYS_MIUI)){//小米
                    ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                    intent.setComponent(componentName);
                    Toast.makeText(getApplicationContext(), "MIUI 请允许HePoint后台运行。",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "不是MIUI和EMUI,不需要允许后台运行",
                            Toast.LENGTH_SHORT).show();
                }
                try{
                    context.startActivity(intent);
                }catch (Exception e){//抛出异常就直接打开设置页面
                    intent=new Intent(Settings.ACTION_SETTINGS);
                    context.startActivity(intent);
                }

            }
        });


        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {




                    if (Build.VERSION.SDK_INT >= 23) {
                        if(!Settings.canDrawOverlays(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "请授予HePoint悬浮窗权限。",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION , Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent,1);
                            switchCompat.setChecked(false);
                        } else {
                            Intent intent = new Intent(MainActivity.this, MainService.class);
                            startService(intent);
                            bindService(new Intent(MainActivity.this, MainService.class), mConnection,
                                    Context.BIND_AUTO_CREATE);
                        }
                    } else {
                        Intent intent = new Intent(MainActivity.this, MainService.class);
                        startService(intent);
                        bindService(new Intent(MainActivity.this, MainService.class), mConnection,
                                Context.BIND_AUTO_CREATE);
                    }

                } else {
                    Intent intent = new Intent(MainActivity.this, MainService.class);
                    stopService(intent);
                    if (mBound) {
                        unbindService(mConnection);
                        mBound = false;
                    }
                }

            }
        });

        seekBarA = (SeekBar) findViewById(R.id.main_seekbar_alpha);
        seekBarS = (SeekBar) findViewById(R.id.main_seekbar_size);

        seekBarA.setProgress(255 - alpha);
        seekBarS.setProgress(size - 50);


        seekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DataMoniter.write("Alpha",alpha + "",context);
            }
            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                alpha = 255 - progress;

                sayUpdate(MainService.UPDATE_FLOAT_BUTTON_A,alpha);

            }
        });
        seekBarS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sayUpdate(MainService.UPDATE_FLOAT_BUTTON_S,size);
                DataMoniter.write("Size",size + "",context);

            }
            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                size = progress + 50;



            }
        });
    }




    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("cn.heyan.hepoint.services.MainService".equals(service.service.getClassName())) {
                return true;
            }
            Log.i("xxxxx",service.service.getClassName());
        }
        return false;
    }
}
