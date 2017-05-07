package com.example.josephp.project;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;


public class ListActivity extends AppCompatActivity {
    private ListView mListView;
    private ListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();


    MediaPlayer mySound;


    int paused;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_paired_devices);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        mListView = (ListView) findViewById(R.id.lv_paired);

        mAdapter = new ListAdapter(this);

        mySound = MediaPlayer.create(this, R.raw.ringtone);




        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


        IntentFilter filter1 = new IntentFilter();

        filter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter1.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        this.registerReceiver(mReceiver, filter1);





        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter3.addAction((BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
        registerReceiver(mReceiver3, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(mReceiver3, filter3);

        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new ListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {

                BluetoothDevice device = mDeviceList.get(position);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    showToast("Pairing...");

                    pairDevice(device);

                }

            }


        });


        mListView.setAdapter(mAdapter);

        registerReceiver(mReceiver3, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }






    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver3);
        Toast.makeText(this, "BlueDetect Stopped", Toast.LENGTH_LONG).show();

        super.onDestroy();

        mySound.stop();
        mySound.release(); // this super function is used to stop the sound once the application closes
        mySound = null;
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mySound == null) {


            mySound = MediaPlayer.create(this, R.raw.ringtone);
            mySound.start();
        } else if (!mySound.isPlaying()) {
            mySound.seekTo(paused);
            mySound.start();

        }

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();



            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);




            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired");
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //do something
                    showToast("Connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(device)) {
                    showToast("BT requested");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    try {
                        Method method = device.getClass().getMethod("removeBond", (Class[]) null);
                        method.invoke(device, (Object[]) null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                mAdapter.notifyDataSetChanged();



            }

    }


    };

    private final BroadcastReceiver mReceiver3 = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();





            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(ListActivity.this, device.getName() + " Device found", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(ListActivity.this, device.getName() + " Device is now connected", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Toast.makeText(ListActivity.this, device.getName() + " Device is about to disconnect", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(ListActivity.this, device.getName() + " Device has disconnected", Toast.LENGTH_LONG).show();
            }

            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

                    mySound.start();


            }


        }
    };


    public void playMusic(View view) {

        if(mySound==null) {


            mySound = MediaPlayer.create(this, R.raw.ringtone);
            mySound.start();
        } else if (!mySound.isPlaying()){
            mySound.seekTo(paused);
            mySound.start();

        }

    }

    public void pause(View view) {
        mySound.pause();
        paused = mySound.getCurrentPosition();
    }



}

