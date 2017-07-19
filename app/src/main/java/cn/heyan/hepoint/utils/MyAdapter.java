package cn.heyan.hepoint.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import cn.heyan.hepoint.R;
import cn.heyan.hepoint.services.MainService;

/**
 * Created by 13717 on 2017/7/19 0019.
 */

public class MyAdapter extends SimpleAdapter {

    MainService context;


    public MyAdapter(MainService context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = super.getView(position, convertView, parent);

        LinearLayout linearLayout = v.findViewById(R.id.linearLayout);
        ImageButton imageButton = v.findViewById(R.id.item_list_button);

        linearLayout .setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                Toast.makeText(context, "HePoint: 已将" + DataMoniter.list.get(position).get("title").toString() +" 复制到剪切板",
                        Toast.LENGTH_SHORT).show();

                // TODO Auto-generated method stub
                ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", DataMoniter.list.get(position).get("title").toString());
                clip.setPrimaryClip(mClipData);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                context.showFloatDia(position);
            }
        });

        return v;
    }
}
