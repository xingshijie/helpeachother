package com.xingshijie.helpeachother;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;


public class DeviceDetailActivity extends ActionBarActivity {

    public static final String DEVICE_ADDRESS ="device_address";

    MyWifiP2pDevice myWifiP2pDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Intent intent=getIntent();
        String  string=intent.getStringExtra(DEVICE_ADDRESS);
        myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(string);
        if(myWifiP2pDevice==null){
            if(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDevice().deviceAddress.equals(string)){
                myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDevice();
            }
            else return;
        }

        ((TextView)findViewById(R.id.textView_device_name)).setText(myWifiP2pDevice.deviceName);
        ((TextView)findViewById(R.id.textView_device_address)).setText(myWifiP2pDevice.deviceAddress);
        //((TextView)findViewById(R.id.textView_device_group_ip)).setText(myWifiP2pDevice.);
        ((TextView)findViewById(R.id.textView_device_ip)).setText(myWifiP2pDevice.getIp());
        ((TextView)findViewById(R.id.textView_device_allDetail)).setText(myWifiP2pDevice.toString());
        ((TextView)findViewById(R.id.textView_device_status)).setText(DeviceListFragment.getDeviceStatus(myWifiP2pDevice.status));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
