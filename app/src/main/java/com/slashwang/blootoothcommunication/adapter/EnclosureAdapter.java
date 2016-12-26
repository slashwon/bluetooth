package com.slashwang.blootoothcommunication.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by PICO-USER on 2016/12/26.
 */
public abstract class EnclosureAdapter<T> extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final List<T> mList;
    protected final Context mContext;
    private int mLayoutId;
    private ListView mLv;

    public EnclosureAdapter(Context context, List<T> list){
        mList = list;
        mContext = context;
    }

    public void init(int layoutId, ListView lv){
        mLayoutId = layoutId;
        mLv = lv;
        mLv.setAdapter(this);
        mLv.setOnItemClickListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView==null){
            vh = new ViewHolder();
            convertView = View.inflate(mContext,mLayoutId,null);
            convertView.setTag(vh);
            bindView(convertView,vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        T item = getItem(position);
        bindData(item,vh);
        return convertView;
    }

    protected abstract void bindData(T item, ViewHolder viewHolder);

    protected abstract void bindView(View convertView, ViewHolder viewHolder);

    public class ViewHolder{
        //do nothing
        public TextView[] tv;
    }
}
