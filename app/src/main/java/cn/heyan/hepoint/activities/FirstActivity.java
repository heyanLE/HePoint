package cn.heyan.hepoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import cn.heyan.hepoint.R;
import cn.heyan.hepoint.utils.DataMoniter;

/**
 * Created by 13717 on 2017/7/13 0013.
 */

public class FirstActivity extends AppCompatActivity {

    String [] dataName = {"x","y","Alpha","Size"};
    private void checkData(){
        for(String s : dataName){
            if(DataMoniter.read(s,this).equals("")){
                firstWrite();
                return;
            }
        }
    }

    private void firstWrite(){
        String [] dData = {"0","0","255","120"};
        for(int i = 0 ; i < 4 ; i ++){
            DataMoniter.write(dataName[i],dData[i],this);
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.layout_first);

        checkData();


        new Handler().postDelayed(new Runnable(){
            public void run() {
                Intent intent = new Intent(FirstActivity.this,MainActivity.class);
                startActivity(intent);
                FirstActivity.this.finish();
            }
        }, 3000);
    }
}
