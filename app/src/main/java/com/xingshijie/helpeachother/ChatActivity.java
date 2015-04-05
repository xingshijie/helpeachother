package com.xingshijie.helpeachother;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

import java.util.List;


public class ChatActivity extends ActionBarActivity implements ChatFragment.OnChatListener,Handler.Callback{

    public static final String DEVICE_ADDRESS="1";

    String deviceAddress;
    ChatFragment chatFragment;
    MyWifiP2pDevice myWifiP2pDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatFragment=new ChatFragment();

        //
        Intent intent=getIntent();
        deviceAddress=intent.getStringExtra(DEVICE_ADDRESS);
        myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceAddress);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, chatFragment)
                    .commit();
        }

        //如果没连接，设置按钮无效
        if(myWifiP2pDevice.status!= WifiP2pDevice.CONNECTED){

        }
    }

    //此处获得当前聊天的设备信息，并设置此设备的handle；
    @Override
    public void onStart(){
        super.onStart();
        myWifiP2pDevice.setHandler(new Handler(this));
        if(myWifiP2pDevice.status!= WifiP2pDevice.CONNECTED){
            findViewById(R.id.button_send).setEnabled(false);
        }

    }
    @Override
    public void onStop(){
        super.onStop();

        myWifiP2pDevice.setHandler(null);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        //menuItem不能使用findviewbyitem找到
        menu.findItem(R.id.action_chat_name).setTitle(myWifiP2pDevice.deviceName);
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

    @Override
    public void write(byte[] bytes) {
        new Thread(new ChatManager(deviceAddress, bytes)).start();
    }

    @Override
    public List<String> getListString() {
        return myWifiP2pDevice.getArrayList();
    }

    @Override
    public boolean handleMessage(Message msg) {
        byte[] bytes=(byte[])msg.obj;
        String deviceMac=new String(bytes,0,17);
        String string=new String(bytes,17,msg.arg1);
        //Log.d("",string);
        chatFragment.pushMessage(myWifiP2pDevice.deviceName+":"+string);
        return true;
    }
}
