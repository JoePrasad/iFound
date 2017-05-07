package com.example.josephp.project;


import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.graphics.Color;
import android.view.View;

import android.app.Activity;
import android.widget.Toast;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;

import android.os.Bundle;

import android.bluetooth.BluetoothDevice;

import android.app.ProgressDialog;

import java.util.Set;

import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// Joseph Prasad Engr 697 Senior Project

public class SeniorProject extends Activity {

    private TextView statusText;
    private Button enableBtn;
    private Button paired;
    private Button scan;
    private Button home;
    UsbDeviceConnection connection;



    BluetoothAdapter bluetooth;

    private static final String TAG = "JP";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;



    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Arduino Uno
    private static String address = "20:16:09:18:66:35";




    private ProgressDialog progressDialogBox;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter bluetoothAdapt;


    MediaPlayer mySound; // variable for the sound file in the raw folder

    int paused;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState); // super is used to call the parents constructor (Inheritance)

        setContentView(R.layout.activity_main);

        this.setTitle("ifound");



        mySound = MediaPlayer.create(this, R.raw.ringtone); // this creates the sound file called "ringtone"


        statusText =       (TextView) findViewById(R.id.tv_status);
        enableBtn =     (Button) findViewById(R.id.btn_enable);
        paired =        (Button) findViewById(R.id.btn_view_paired);
        scan =          (Button) findViewById(R.id.btn_scan);    // these are the button variables created in the app


        bluetoothAdapt = BluetoothAdapter.getDefaultAdapter();

        progressDialogBox = new ProgressDialog(this);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        progressDialogBox.setMessage("Scanning..."); // the progress button generates a scanning text field upon activation
        progressDialogBox.setCancelable(false);

        progressDialogBox.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                bluetoothAdapt.cancelDiscovery();
            }



        });




        if (bluetoothAdapt == null) {
            showUnsupported();
        } else {
            paired.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Set<BluetoothDevice> pairedDevices = bluetoothAdapt.getBondedDevices();

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Devices Found");
                    } else {
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                        list.addAll(pairedDevices);

                    }
                }
            });

            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    bluetoothAdapt.startDiscovery();




                }
            });

            enableBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (bluetoothAdapt.isEnabled()) {
                        bluetoothAdapt.disable();

                        showDisabled();


                    } else {

                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                        startActivityForResult(intent, 1000);
                    }


                }
            });

            if (bluetoothAdapt.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();

            }
        }


        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        registerReceiver(mReceiver, filter);


        IntentFilter filter3 = new IntentFilter();
        filter3.addAction((BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));


        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(mReceiver3, filter3);

        registerReceiver(mReceiver3, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Attempting client connect...");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);


        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }





    @Override
    public void onPause() {
        if (bluetoothAdapt != null) {
            if (bluetoothAdapt.isDiscovering()) {
                bluetoothAdapt.cancelDiscovery();
            }
        }


        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }


        super.onPause();
        mySound.stop();
        mySound.release(); // this super function is used to stop the sound once the application closes
        mySound=null;
    }

    private void checkBTState() {

        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public void onDestroy() {


        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver3);
        Toast.makeText(this, "BlueDetect Stopped", Toast.LENGTH_LONG).show();
    }

    private void showEnabled() {
        statusText.setText("Bluetooth is On"); // enables bluetooth within the smart phone
        statusText.setTextColor(Color.BLUE); // text is blue to indicate bluetooth is active
        statusText.setTextSize(30f);

        enableBtn.setText("Disable");
        enableBtn.setEnabled(true);

        paired.setEnabled(true);
        scan.setEnabled(true);


    }




    private void showDisabled() {
        statusText.setText("Bluetooth is Off"); // turns off the bluetooth
        statusText.setTextColor(Color.RED); // color is red to indicate bluetooth is off

        enableBtn.setText("Enable");
        enableBtn.setEnabled(true);

        paired.setEnabled(false);
        scan.setEnabled(false);


        if(mySound==null) {


            mySound = MediaPlayer.create(this, R.raw.ringtone);
            mySound.start();
        } else if (!mySound.isPlaying()){
            mySound.seekTo(paused);
            mySound.start();

        }


    }

    private void showUnsupported() {
        statusText.setText("Bluetooth is unsupported by this device");

        enableBtn.setText("Enable");
        enableBtn.setEnabled(false);

        paired.setEnabled(false);
        scan.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }





    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Toast.makeText(SeniorProject.this, "received !", Toast.LENGTH_LONG).show();


            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");

                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                progressDialogBox.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                progressDialogBox.dismiss();



                Set<BluetoothDevice> pairedDevices = bluetoothAdapt.getBondedDevices();

                if (pairedDevices == null || pairedDevices.size() == 0) {
                    //showToast("No Paired Devices Found");
                } else {
                    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                    list.addAll(pairedDevices);

                    Intent newintent = new Intent(SeniorProject.this, ListActivity.class);

                    newintent.putParcelableArrayListExtra("device.list", list);

                    startActivity(newintent);
                }




            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {



                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            }


        } // broadcast receiver is used to activate the bluetooth function and to find nearby devices. This is an universal function
    };



    private final BroadcastReceiver mReceiver3 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


            Toast.makeText(SeniorProject.this, "Data received !", Toast.LENGTH_LONG).show();



            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(SeniorProject.this, device.getName() + " Device found", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Toast.makeText(SeniorProject.this, device.getName() + " Device is now connected", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Toast.makeText(SeniorProject.this, device.getName() + " Device is about to disconnect", Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Toast.makeText(SeniorProject.this, device.getName() + " Device has disconnected", Toast.LENGTH_LONG).show();

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

    } // this function plays the sound button from the file .


    public void pause(View view) {
        mySound.pause();
        paused = mySound.getCurrentPosition();
    }


    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }


}
