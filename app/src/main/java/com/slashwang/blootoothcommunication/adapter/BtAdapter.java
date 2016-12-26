package com.slashwang.blootoothcommunication.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.slashwang.blootoothcommunication.MainActivity;
import com.slashwang.blootoothcommunication.R;

import java.util.List;

/**
 * Created by PICO-USER on 2016/12/26.
 */
public class BtAdapter extends EnclosureAdapter<BluetoothDevice> {

    public BtAdapter(Context context, List<BluetoothDevice> list) {
        super(context, list);
    }

    @Override
    protected void bindData(BluetoothDevice item, ViewHolder viewHolder) {
        viewHolder.tv[0].setText(item.getName());
        viewHolder.tv[1].setText(item.getAddress());
    }

    @Override
    protected void bindView(View convertView, ViewHolder viewHolder) {
        viewHolder.tv = new TextView[2];
        viewHolder.tv[0] = (TextView) convertView.findViewById(R.id.tv_device);
        viewHolder.tv[1] = (TextView) convertView.findViewById(R.id.tv_address);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((MainActivity)mContext).getBtHelper().connectDevice(getItem(position));
    }
}
