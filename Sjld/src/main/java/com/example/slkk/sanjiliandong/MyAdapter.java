package com.example.slkk.sanjiliandong;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by slkk on 2016/5/11.
 */
public class MyAdapter extends BaseAdapter {
    public static final String TagG = "MyAdapter";
    private Context context;
    private List<InfoBean> list;
    private TextView mTv;

    public MyAdapter(){
        super();
    }

    public MyAdapter(Context context,List<InfoBean> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(context,R.layout.item,null);
        }
        mTv = (TextView) convertView.findViewById(R.id.tv);
        InfoBean info = (InfoBean) getItem(position);
        Log.d(TagG, "getView: " +info.isChecked);
        if(info.isChecked()){
            mTv.setBackgroundResource(R.drawable.choose_item_selected);
        }else{
            mTv.setBackgroundResource(android.R.color.transparent);
        }
        mTv.setText(info.getName());
        return convertView;
    }
}
