package com.example.josephp.project;


import java.lang.reflect.Method;
import java.util.List;

import com.example.josephp.project.R;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import android.widget.BaseAdapter;

import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;
import android.content.Context;
import android.os.Bundle;
import java.lang.reflect.Method;
import android.view.Window;
import android.support.v7.app.AppCompatActivity;



// this class allows the user to pair and unpair a bluetooth device from the list items screen


public class ListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;

    public ListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.list_item_device, null);

            holder 				= new ViewHolder();

            holder.nameTv		= (TextView) convertView.findViewById(R.id.tv_name);
            holder.addressTv 	= (TextView) convertView.findViewById(R.id.tv_address);
            holder.pairBtn		= (Button) convertView.findViewById(R.id.btn_pair);
            holder.home         =(Button) convertView.findViewById(R.id.home);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
        holder.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPairButtonClick(position);
                }

            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        TextView pairBtn;
        TextView home;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position);
    }

}
