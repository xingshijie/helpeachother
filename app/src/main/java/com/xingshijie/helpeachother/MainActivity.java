package com.xingshijie.helpeachother;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;
import com.xingshijie.helpeachother.service.WifiDirectService;
import com.xingshijie.helpeachother.service.WifiP2pDiscoveryService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements WifiP2pManager.PeerListListener,DeviceListFragment.OnDeviceListListener, Handler.Callback,
        WifiP2pManager.ConnectionInfoListener,WifiP2pManager.GroupInfoListener{

    //DeviceListFragment deviceListFragment;
    public static final String TAG = "MainActivity";
    private DeviceListFragment deviceListFragment;
    //在设备列表中显示所有设备
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

        Map<String ,MyWifiP2pDevice> myWifiP2pDeviceHashMap=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap();

        //填充数据
        //在这里出现了很久了的错误，此处会覆盖修改的数据
        for(WifiP2pDevice wifiP2pDevice:peers.getDeviceList()){
            //如果本身的列表包含此设备
            if(myWifiP2pDeviceHashMap.containsKey(wifiP2pDevice.deviceAddress)) {
                //更新设备数据
               myWifiP2pDeviceHashMap.get(wifiP2pDevice.deviceAddress).update(wifiP2pDevice);
            }else {
                //不包含此设备就把设备放入里面
                myWifiP2pDeviceHashMap.put(wifiP2pDevice.deviceAddress,new MyWifiP2pDevice(wifiP2pDevice));
            }
        }

        //遍历已存在的设备表，如果设备表里有不包含在peers里，表明设备已经远离，则q清空临时数据
        for(MyWifiP2pDevice myWifiP2pDevice:myWifiP2pDeviceHashMap.values()){
            boolean exist=false;
            for(WifiP2pDevice wifiP2pDevice:peers.getDeviceList()){
                if(wifiP2pDevice.deviceAddress.equals(myWifiP2pDevice.deviceAddress)){
                    exist=true;
                    break;
                }
            }
            if(!exist){
                myWifiP2pDevice.clearData();
            }
        }
        //显示单例模式里的数据
        if (deviceListFragment!=null){
            deviceListFragment.showAllDevice();
        }
    }

    private final IntentFilter intentFilter=new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver broadcastReceiver = null;
    private WifiP2pManager wifiP2pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().replace(R.id.fragment_device_list,new DeviceListFragment()).commit();


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel =wifiP2pManager.initialize(this, getMainLooper(), null);

        Intent intent=new Intent(this, WifiDirectService.class);
        startService(intent);
        Intent intent2=new Intent(this, WifiP2pDiscoveryService.class);
        startService(intent2);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        DataSinglePattern.getDataSinglePattern().setMacAddress(info.getMacAddress());

        DataSinglePattern.getDataSinglePattern().setHandler(new Handler(this));

    }



    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver= new WifiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        registerReceiver(broadcastReceiver, intentFilter);
        deviceListFragment=(DeviceListFragment)getFragmentManager().findFragmentById(R.id.fragment_device_list);
        
        //防止已连接的设备不显示列表
        wifiP2pManager.requestPeers(channel,this);

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * 开启wifiP2p dicovery
     */
    public void discoverPeers(){
        wifiP2pManager.discoverPeers(channel,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
//                Toast.makeText(MainActivity.this,
//                        "打开wifiDirect发现成功",
//                        Toast.LENGTH_SHORT).show();
//                    //每次发现都请求一下新的列表，因为broadcast注册在activity
//                    wifiP2pManager.requestPeers(channel,MainActivity.this);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,
                        "打开wifiDirect发现失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
        }else if(id==R.id.action_discover){
//            deviceListFragment.showAllDevice();
           discoverPeers();

        }else if(id==R.id.action_file){
            //如果选择传送文件的话
            Intent intent=new Intent(this,FileTransmitActivity.class);

            startActivity(intent);
        }else if(id==R.id.action_disconnect){
            disconnectPeers();
        }else if(id==R.id.action_myDevice){
            Intent intent=new Intent();
            intent.setClass(this,DeviceDetailActivity.class);
            intent.putExtra(DeviceDetailActivity.DEVICE_ADDRESS,DataSinglePattern.getDataSinglePattern().getMyWifiP2pDevice().deviceAddress);
            startActivity(intent);
        }else if(id==R.id.action_about){
            Intent intent=new Intent(this,AboutActivity.class);
            startActivity(intent);
        }else if(id==R.id.action_stop_discovery){
            wifiP2pManager.stopPeerDiscovery(channel,new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this,
                            "设备发现已停止",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this,
                            "停止失败"+reason,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * @param wifiP2pDevice
     */
    public void onDeviceListener(final WifiP2pDevice wifiP2pDevice) {
        MyWifiP2pDevice myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress);
        if(myWifiP2pDevice==null){
            Log.e("","找不到此设备");
        }

        //如果已经连接过了，直接打开聊天窗口
        if(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress).status==WifiP2pDevice.CONNECTED){
            Log.e("","已经连接过了");
            Intent intent=new Intent(MainActivity.this,ChatActivity.class);
            intent.putExtra("1",wifiP2pDevice.deviceAddress);
            startActivity(intent);
            return;
        }
        final WifiP2pConfig wifiP2pConfig=new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;

        //不在连接处保存。。
        //如果已经连接之后继续连接，会产生延迟时间才会连接成功，用户体验不好
        wifiP2pManager.connect(channel,wifiP2pConfig,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,
                        "连接成功",
                        Toast.LENGTH_SHORT).show();
                //暂时在此处打开chatActivity
                Intent intent=new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("1",wifiP2pDevice.deviceAddress);
                startActivity(intent);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,
                        "连接失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 无法实现停止现在的连接
     */
    public void disconnectPeers() {


        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {                          }

        });
    }

    //全局handle处理数据
    @Override
    public boolean handleMessage(Message msg) {
        byte[] bytes=(byte[])msg.obj;
        String deviceMac=new String(bytes,0,17);
        /** 获得真实mac地址*/
        deviceMac=ChatManager.getWifiP2pMac(deviceMac);
        String string=new String(bytes,17,msg.arg1);
        MyWifiP2pDevice myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceMac);
        myWifiP2pDevice.getArrayList().add(myWifiP2pDevice.deviceName+":"+string);

        Log.e("",deviceMac);

        Intent intent=new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra(ChatActivity.DEVICE_ADDRESS,deviceMac);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification notification=new Notification.Builder(this)
                .setAutoCancel(true).setTicker(myWifiP2pDevice.deviceName + ":" + string)
                .setSmallIcon(R.drawable.file)
                .setContentTitle(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceMac).deviceName)
                .setContentText(string)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(resultPendingIntent)
                .build();
        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(250,notification);

        return true;
    }

    //查看每个info的groupowener，如果当前发现的所有设备有此设备，
    //则把此设备的grpowener设置为他的mac，
    //否则设备地址设备自己
    WifiP2pInfo wifiInfo;
    //wifip2pinfo会在函数完成时自动清空，加final为了保持在函数内部不变。。。卧槽
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        //如果当前设备是组长
        //DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(info.groupOwnerAddress)
        //new表示分配空间
        this.wifiInfo=new WifiP2pInfo(info);
        if(info.isGroupOwner){
        }

        Log.e("",info.toString());
    }

    //组信息
    @Override
    public void onGroupInfoAvailable(final WifiP2pGroup group) {

        if(group==null){
            return;
        }
        if(group.isGroupOwner()){

            DataSinglePattern.getDataSinglePattern().setMacAddress(group.getOwner().deviceAddress);

        }else{

            //如果是组员,就这样设置ip地址
            DataSinglePattern.getDataSinglePattern().getGroupOwner().put(group.getOwner().deviceAddress,wifiInfo.groupOwnerAddress);
            DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(group.getOwner().deviceAddress).setIp(wifiInfo.groupOwnerAddress.getHostAddress());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket=new Socket();
                        socket.bind(null);
                        socket.connect(new InetSocketAddress(wifiInfo.groupOwnerAddress,4545) ,5000);
                        //获得本机ip地址
                        DataSinglePattern.getDataSinglePattern().getMyWifiP2pDevice().setIp(socket.getLocalAddress().getHostAddress());

                        OutputStream outputStream = socket.getOutputStream();
                        Log.e("本机ip", DataSinglePattern.getDataSinglePattern().getMacAddress());
                        new DataOutputStream(outputStream).writeInt(111);
                        new DataOutputStream(outputStream).write(DataSinglePattern.getDataSinglePattern().getMacAddress().getBytes());
                        new DataOutputStream(outputStream).write(socket.getLocalAddress().getHostAddress().getBytes());
                        outputStream.flush();

                        Log.e("本机ip" + socket.toString(), "发送给长成功");

                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }





    @Override
    public void onStartChat(WifiP2pDevice wifiP2pDevice) {
        MyWifiP2pDevice myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress);
        Intent intent=new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra(ChatActivity.DEVICE_ADDRESS,wifiP2pDevice.deviceAddress);
        startActivity(intent);

    }

    @Override
    public void onStartSendFile(WifiP2pDevice wifiP2pDevice) {

    }

    @Override
    public void onConnectDevice(WifiP2pDevice wifiP2pDevice) {
        MyWifiP2pDevice myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress);
        if(myWifiP2pDevice==null){
            Log.e("","找不到此设备");
        }
        //如果已经连接过了，直接打开聊天窗口
        if(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress).status==WifiP2pDevice.CONNECTED){
            Log.e("","已经连接过了");
            Intent intent=new Intent(MainActivity.this,ChatActivity.class);
            intent.putExtra("1",wifiP2pDevice.deviceAddress);
            startActivity(intent);
            return;
        }
        final WifiP2pConfig wifiP2pConfig=new WifiP2pConfig();
        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
        wifiP2pConfig.wps.setup = WpsInfo.PBC;

        //不在连接处保存。。
        //如果已经连接之后继续连接，会产生延迟时间才会连接成功，用户体验不好
        wifiP2pManager.connect(channel,wifiP2pConfig,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,
                        "连接成功",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,
                        "连接失败",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*@Override
    public void connectP2p(WiFiP2pService service) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            manager.removeServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Connecting to service");
            }

            @Override
            public void onFailure(int errorCode) {
                appendStatus("Failed connecting to service");
            }
        });
    }*/
}
