// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.xingshijie.helpeachother;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.ArrayMap;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Referenced classes of package com.xingshijie.wifichat:
//            ChatWindowActivity, WifiChatActivity

public class DataSinglePattern
{

    private static DataSinglePattern dataSinglePattern=new DataSinglePattern();

    private Map<String,MyWifiP2pDevice> myWifiP2pDeviceMap=new HashMap<>();

    //每个组长的device地址与地址表
    private HashMap<String,InetAddress> groupOwner=new HashMap<>();

    private MyWifiP2pDevice myWifiP2pDevice=new MyWifiP2pDevice(new WifiP2pDevice());

    //代表本机mac地址
    private String macAddress=null;

    //设置全局handler的必要性；如果自己的handle为空，就使用全局handle
    private Handler handler=null;


    /**
     * 查看被设备是否可以被发现
     */
    private boolean isDiscoveryEnable=false;

    private DataSinglePattern()
    {

    }

    public static DataSinglePattern getDataSinglePattern()
    {
        return dataSinglePattern;
    }


    public Map<String, MyWifiP2pDevice> getMyWifiP2pDeviceMap() {

        return myWifiP2pDeviceMap;
    }

    public void setMyWifiP2pDeviceMap(Map<String, MyWifiP2pDevice> myWifiP2pDeviceMap) {
        this.myWifiP2pDeviceMap = myWifiP2pDeviceMap;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public HashMap<String, InetAddress> getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(HashMap<String, InetAddress> groupOwner) {
        this.groupOwner = groupOwner;
    }


    public MyWifiP2pDevice getMyWifiP2pDevice() {
        return myWifiP2pDevice;
    }

    public void setMyWifiP2pDevice(MyWifiP2pDevice myWifiP2pDevice) {
        this.myWifiP2pDevice = myWifiP2pDevice;
    }

    public boolean isDiscoveryEnable() {
        return isDiscoveryEnable;
    }

    public void setDiscoveryEnable(boolean isDiscoveryEnable) {
        this.isDiscoveryEnable = isDiscoveryEnable;
    }
}
