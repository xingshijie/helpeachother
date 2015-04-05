package com.xingshijie.helpeachother.datamanger;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;

import com.xingshijie.helpeachother.DataSinglePattern;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 定义自己的wifip2pdevice包含必要的信息
 */
public class MyWifiP2pDevice extends WifiP2pDevice{

//    /**
//     *主socket，只要与客户端连接过后就长期保留，负责管理双方通信
//     *groupOwner从serverSocket.accept()获得此socket，client从socket。connect后获得此套接字
//     * 如果此套接字为null，表示未建立，需要组员向组长发送建立套接字请求
//     * 组长用此套接字向组员传递消息，组员时刻监听此套接字的数据
//     */
//    private Socket mainSocket;

    /**
     * 与此设备的聊天数据保存在这个地方
     */
    private ArrayList<String> arrayList=new ArrayList<>();

    //定义此设备的ip地址
    private String ip=null;

    //每个设备聊天的handle
    private Handler handler=null;

    public MyWifiP2pDevice(WifiP2pDevice wifiP2pDevice){
        //super=wifiP2pDevice;
        super(wifiP2pDevice);
    }
    /**
     * Update device details. This will be throw an exception if the device address
     * does not match.
     * @param device to be updated
     * @throws IllegalArgumentException if the device is null or device address does not match
     * @hide
     */
    public void update(WifiP2pDevice device) {
        updateSupplicantDetails(device);
        status = device.status;
    }

    /** Updates details obtained from supplicant @hide */
    public void updateSupplicantDetails(WifiP2pDevice device) {
        deviceName = device.deviceName;
        deviceAddress=device.deviceAddress;
        primaryDeviceType = device.primaryDeviceType;
        secondaryDeviceType = device.secondaryDeviceType;
    }


    /**
     * 次函数把所有临时数据置为null；
     * 如mainSocket，handle,groupOwner,isNearby,isconnect,status
     */
    public void clearData(){

        handler=null;
        status=WifiP2pDevice.UNAVAILABLE;

    }

    public ArrayList<String> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }


    /**
     * 如果handler为空，返回一个系统notification handle
     */
    public Handler getHandler() {
        if(handler==null){
               handler= DataSinglePattern.getDataSinglePattern().getHandler();
        }
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
