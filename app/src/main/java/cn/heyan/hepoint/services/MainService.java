package cn.heyan.hepoint.services;

import android.app.Application;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.heyan.hepoint.R;
import cn.heyan.hepoint.activities.FirstActivity;
import cn.heyan.hepoint.activities.MainActivity;
import cn.heyan.hepoint.utils.DataMoniter;
import cn.heyan.hepoint.utils.MyAdapter;

/**
 *
 * Created by 13717 on 2017/7/12 0012.
 */

public class MainService extends Service implements SearchView.OnQueryTextListener{

    public static final int UPDATE_FLOAT_BUTTON_A = 1;
    public static final int UPDATE_FLOAT_BUTTON_S = 2;

    public void showDia(int positionP){
/**
        final int position = positionP;

        AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(context);

        normalDialog.setIcon(R.mipmap.planet);
        normalDialog.setTitle(DataMoniter.list.get(position).get("title").toString());
        normalDialog.setMessage("用法:" + DataMoniter.use.get(DataMoniter.list.get(position).get("title").toString()) + "\n说明：" + DataMoniter.list.get(position).get("subTitle").toString() );
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        normalDialog.setNegativeButton("复制",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Toast.makeText(context, "HePoint" + DataMoniter.list.get(position).get("title").toString() +" 已复制到剪切板",
                                Toast.LENGTH_SHORT).show();

                        // TODO Auto-generated method stub
                        ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", DataMoniter.list.get(position).get("title").toString());
                        clip.setPrimaryClip(mClipData);
                    }
                });
        // 显示
        AlertDialog ad = normalDialog.create();
        ad.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        ad.setCanceledOnTouchOutside(false);                                   //点击外面区域不会让dialog消失
        ad.show();
**/

    }



    String [] dataName = {"x","y","Alpha","Size"};

    String [] title = new String[]{""};
    String [] subTitle = new String[]{""};



    List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    String json ;
    JSONObject jsonObject = null;
    // JSONObject[] jsonObjectS = {};

    private ListView listView;
    private MyAdapter myAdapter;

    private static final String TAG = "MainService";


    private static int floatX = 0;
    private static int floatY = 0;
    private static int floatAlpha = 255;
    private static int floatSize = 120;

    private static boolean ifClick = false;

    private Context context;

    WindowManager mWindowManager;

    RelativeLayout floatButtonLayout;
    WindowManager.LayoutParams floatButtonParams;
    ImageButton floatButton;

    SearchView searchView ;

    RelativeLayout floatWindowLayout;
    WindowManager.LayoutParams floatWindowParams;

    RelativeLayout floatDiaLayout;
    WindowManager.LayoutParams floatDiaParams;
    TextView floatDiaTitle;
    TextView floatDiaSubTitle;
    TextView floatDiaSubTitleUse;
    Button floatDiaOne;
    Button floatDiaTwo;


    final Messenger mMessenger = new Messenger(new IncomingHandler());


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_FLOAT_BUTTON_A:
                    floatAlpha = msg.arg1;
                    floatButton.getBackground().setAlpha(floatAlpha);
                    mWindowManager.updateViewLayout(floatButtonLayout, floatButtonParams);
                    break;
                case UPDATE_FLOAT_BUTTON_S:
                    floatSize = msg.arg1;
                    floatButton.getLayoutParams().width = floatSize;
                    floatButton.getLayoutParams().height = floatSize;
                    floatButtonParams.width = floatSize;
                    floatButtonParams.height = floatSize;
                    mWindowManager.updateViewLayout(floatButtonLayout, floatButtonParams);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchItem(newText);
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");

        context = this;

        floatX = Integer.parseInt(DataMoniter.read(dataName[0],this)) ;
        floatY = Integer.parseInt(DataMoniter.read(dataName[1],this)) ;
        floatAlpha = Integer.parseInt(DataMoniter.read(dataName[2],this)) ;
        floatSize = Integer.parseInt(DataMoniter.read(dataName[3],this)) ;


        try{
            json =  DataMoniter.getJson("command.json",MainService.this);
            jsonObject = new JSONObject(json);
            JSONArray jsonArray =  jsonObject.getJSONArray("command");

            title = new String[jsonArray.length() ];
            subTitle = new String[jsonArray.length() ];

            for(int i = 0 ; i < jsonArray.length() ; i ++){
                Log.i(TAG,i + "  Title ->" + new JSONObject(jsonArray.get(i).toString()).getString("指令") + "    SubTitle ->" + new JSONObject(jsonArray.get(i).toString()).getString("说明"));

                title[i] = new JSONObject(jsonArray.get(i).toString()).getString("指令");
                subTitle[i] = new JSONObject(jsonArray.get(i).toString()).getString("说明");
                DataMoniter.use.put(title[i],new JSONObject(jsonArray.get(i).toString()).getString("用法"));

            }




        }catch(org.json.JSONException e){
            e.printStackTrace();
        }




        mWindowManager = (WindowManager)getApplication().getSystemService(Application.WINDOW_SERVICE);
        Log.i(TAG,"mWindowManager ->" + mWindowManager.toString());

        showFloatButton();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        if(floatButtonLayout != null)
        {
            //移除悬浮窗口
            mWindowManager.removeView(floatButtonLayout);
        }
    }

    public void showFloatDia(int i){
        final int position = i;
        floatDiaParams = new WindowManager.LayoutParams();
        floatDiaParams.windowAnimations = android.R.style.Animation_Dialog;
        floatDiaParams.type = WindowManager.LayoutParams. TYPE_PHONE  ;
        floatDiaParams.format = PixelFormat.RGBA_8888;
        Log.i(TAG,"xxxxxxxxxxxxxxxxxxxxxx" + PixelFormat.RGBA_8888);
        floatDiaParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        floatDiaParams.gravity = Gravity.START|Gravity.TOP;
        floatDiaParams.x = 0;
        floatDiaParams.y = 0;
        floatDiaParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        floatDiaParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        floatDiaLayout = (RelativeLayout) inflater.inflate(R.layout.layout_float_window_dia,null);


        mWindowManager.addView(floatDiaLayout,floatDiaParams);

        floatDiaLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mWindowManager.removeView(floatDiaLayout);
            }
        });

        floatDiaTitle = floatDiaLayout.findViewById(R.id.float_dia_title);
        floatDiaSubTitle = floatDiaLayout.findViewById(R.id.float_dia_subtitle);
        floatDiaSubTitleUse = floatDiaLayout.findViewById(R.id.float_dia_subtitle_use);
        floatDiaOne = floatDiaLayout.findViewById(R.id.float_dia_one);
        floatDiaTwo = floatDiaLayout.findViewById(R.id.float_dia_two);


        floatDiaTitle.setText(DataMoniter.list.get(i).get("title").toString());

        floatDiaSubTitle.setText(DataMoniter.list.get(i).get("subTitle").toString() );
        floatDiaSubTitleUse.setText(DataMoniter.use.get(DataMoniter.list.get(position).get("title").toString()));

        floatDiaOne.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                mWindowManager.removeView(floatDiaLayout);

            }
        });

        floatDiaTwo.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Toast.makeText(context, "HePoint: 已将" + DataMoniter.list.get(position).get("title").toString() +" 复制到剪切板",
                        Toast.LENGTH_SHORT).show();

                // TODO Auto-generated method stub
                ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", DataMoniter.list.get(position).get("title").toString());
                clip.setPrimaryClip(mClipData);

            }
        });

    }


    private void showFloatWindow(){
        floatWindowParams = new WindowManager.LayoutParams();
        floatWindowParams.windowAnimations = android.R.style.Animation_Translucent;
        floatWindowParams.type = WindowManager.LayoutParams. TYPE_PHONE  ;
        floatWindowParams.format = PixelFormat.RGBA_8888;
        floatWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        floatWindowParams.gravity = Gravity.START|Gravity.TOP;
        floatWindowParams.x = 0;
        floatWindowParams.y = 0;
        floatWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        floatWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        floatWindowLayout = (RelativeLayout) inflater.inflate(R.layout.layout_float_window,null);


        mWindowManager.removeView(floatButtonLayout);

        mWindowManager.addView(floatWindowLayout,floatWindowParams);

        floatWindowLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mWindowManager.addView(floatButtonLayout,floatButtonParams);

                mWindowManager.removeView(floatWindowLayout);



            }
        });


        for (int i = 0; i < title.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("title", title[i]);
            listem.put("subTitle", subTitle[i]);
            listems.add(listem);
        }

        DataMoniter.list.clear();
        DataMoniter.list.addAll(listems);

        myAdapter = new MyAdapter(MainService.this,listems,R.layout.item_listview_window,new String[] { "title", "subTitle"},    new int[] {R.id.item_list_title,R.id.item_list_sub_title});
        listView =  floatWindowLayout.findViewById(R.id.main_listView);
        listView.setAdapter(myAdapter);
        listView.setTextFilterEnabled(true);
        listView.findFocus();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(listView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


        searchView = floatWindowLayout.findViewById(R.id.window_searchview);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint("请输入搜索内容");
        searchView.setFocusable(false);
        searchView.clearFocus();

        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null,null);
//获取到TextView的控件
        TextView textView = searchView.findViewById(id);
//设置字体大小为14sp
        //textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);//14sp
//设置字体颜色
        textView.setTextColor(Color.WHITE);
//设置提示文字颜色
        textView.setHintTextColor( Color.argb(255, 172, 172, 172));

        int iId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn",null,null);

        ImageView imageButton = searchView.findViewById(iId);
        imageButton.setImageResource(R.mipmap.ic_close);


        searchView.setOnTouchListener(new View.OnTouchListener()
        {


            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // TODO Auto-generated method stub
                //searchView.setFocusable(true);
                Log.i(TAG,"SearchView -> onTouch");
                return true;
            }
        });

        searchView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
        }


    }

    public void searchItem(String name) {
        ArrayList<String> mSearchListTitle = new ArrayList<String>();
        ArrayList<String> mSearchListSubTitle = new ArrayList<String>();
        for (int i = 0; i < title.length; i++) {
            int index = title[i].indexOf(name);
            int index2 = subTitle[i].indexOf(name);
            if (index != -1 || index2 != -1) {
                mSearchListTitle.add(title[i]);
                mSearchListSubTitle.add(subTitle[i]);
            }
        }
        listems.clear();

        for (int i = 0; i < mSearchListTitle.size(); i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("title", mSearchListTitle.get(i));
            listem.put("subTitle", mSearchListSubTitle.get(i));
            listems.add(listem);
        }

        DataMoniter.list.clear();
        DataMoniter.list.addAll(listems);

        myAdapter.notifyDataSetChanged();
        listView.invalidateViews();

    }




    private void showFloatButton()
    {
        floatButtonParams = new WindowManager.LayoutParams();
        //获取的是WindowManagerImpl.CompatModeWrapper

        floatButtonParams.windowAnimations = android.R.style.Animation_Translucent;

        //设置window type
        floatButtonParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        floatButtonParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        floatButtonParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        floatButtonParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        floatButtonParams.x = floatX;
        floatButtonParams.y = floatY;

        //设置悬浮窗口长宽数据
        floatButtonParams.width = floatSize;
        floatButtonParams.height = floatSize;

         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        floatButtonLayout = (RelativeLayout) inflater.inflate(R.layout.layout_float_button, null);
        //添加mFloatLayout
        mWindowManager.addView(floatButtonLayout, floatButtonParams);

        //浮动窗口按钮
        floatButton = floatButtonLayout.findViewById(R.id.float_button);

        floatButton.getLayoutParams().width = floatSize;
        floatButton.getLayoutParams().height = floatSize;

        floatButtonLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.i(TAG, "Width/2--->" + floatButton.getMeasuredWidth()/2);
        Log.i(TAG, "Height/2--->" + floatButton.getMeasuredHeight()/2);

        floatButton.getBackground().setAlpha(floatAlpha);

        //设置监听浮动窗口的触摸移动
        floatButton.setOnTouchListener(new View.OnTouchListener()
        {


            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // TODO Auto-generated method stub

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        floatButton.getLayoutParams().width = floatSize - 20;
                        floatButton.getLayoutParams().height = floatSize - 20;
                        mWindowManager.updateViewLayout(floatButtonLayout, floatButtonParams);
                        ifClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        floatButtonParams.x =(int) event.getRawX() - floatButton.getMeasuredWidth()/2;
                        floatButtonParams.y =(int) event.getRawY() - floatButton.getMeasuredWidth()/2 - 25;
                        floatX = floatButtonParams.x;
                        floatY = floatButtonParams.y;
                        Log.i(TAG,"x -->" + floatButtonParams.x);
                        Log.i(TAG,"y -->" + floatButtonParams.y);
                        mWindowManager.updateViewLayout(floatButtonLayout, floatButtonParams);
                        ifClick = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        floatButton.getLayoutParams().width = floatSize;
                        floatButton.getLayoutParams().height = floatSize;
                        mWindowManager.updateViewLayout(floatButtonLayout, floatButtonParams);
                        DataMoniter.write("x",floatX + "" , context);
                        DataMoniter.write("x",floatX + "" , context);
                        saveXY();
                }
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        floatButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                if(ifClick){
                    ifClick = false;
                    Log.i(TAG,"onClick");
                    showFloatWindow();
                }

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return START_STICKY;//super.onStartCommand(intent, flags, startId);
    }

    private void saveXY(){
        DataMoniter.write("x",floatX + "",this);
        DataMoniter.write("y",floatY + "",this);
    }


}
