package com.xingshijie.helpeachother;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Handler;
import android.util.Log;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/3/26 0026.
 * 管理数据的输入和输出，只负责向某一socket写入数据或读取数据
 * 使用此类只需构造函数然后start就可以
 */
public class ChatManager implements Runnable {

    private Socket socket = null;
    private String readOrWrite=null;
    private Handler handler=DataSinglePattern.getDataSinglePattern().getHandler();
    private byte[] bytes=new byte[10240];
    private byte[] b;
    //将要发送的地址
    private InetAddress inetAddress;
    private String deviceAddress;


    public ChatManager(Socket socket) {
        this.socket = socket;

        readOrWrite="read";
    }

    public ChatManager(String deviceAddress,byte[] bytes){
        readOrWrite="write";
        this.deviceAddress=deviceAddress;
        this.b=bytes;
        this.socket=new Socket();
        try {
            this.socket.bind(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("","mac地址多长"+deviceAddress.getBytes().length);
    }

    private InputStream inputStream;
    private OutputStream outputStream;
    private static final String TAG = "ChatHandler";

    @Override
    public void run() {
        //如果是读取数据
        if(readOrWrite.equals("read")){
            read();
        }else {
            //每次写入数据前加入int
            if (readOrWrite.equals("write")) {
                write();
            }
        }
    }
    public void write(){
        if(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceAddress).getIp()==null){
            Log.e("","套接字为空，无法发送信息");
            return;
        }
        try {
            String ip=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceAddress).getIp();
            socket.connect(new InetSocketAddress(ip,4545) ,5000);
            outputStream = socket.getOutputStream();

            new DataOutputStream(outputStream).writeInt(125);
            new DataOutputStream(outputStream).write(DataSinglePattern.getDataSinglePattern().getMacAddress().getBytes());

            outputStream.write(b);
            Log.e("本机ip"+socket.toString(),"写入成功");

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void read(){
        int n=0,a=0;
        String deviceMac=null;
        try {
            inputStream=socket.getInputStream();
            n=new DataInputStream(inputStream).readInt();
            //byte[] bytes1=new byte[1024];
            int b=DataSinglePattern.getDataSinglePattern().getMacAddress().getBytes().length;
            a=inputStream.read(bytes,0,b);
            deviceMac=new String(bytes,0,a);

            deviceMac=getWifiP2pMac(deviceMac);

            //wifi-direct和wifi地址不一样
            //如果在表里找不到此设备，就从中找一个相似度最高的

            //这句话很关键，关于string hash
            //string=GetString.get(string);

            Log.e("",n+" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (n) {
            //获得组员的ip地址
            case 111:
                MyWifiP2pDevice myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceMac);
                //myWifiP2pDevice=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().values().iterator().next();
                String ip="";
                try {
                    ip=new BufferedReader(new InputStreamReader(inputStream)).readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(myWifiP2pDevice==null){
                    Log.e("", "获取ip地址失败"+deviceMac);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {

                    myWifiP2pDevice.setIp(ip);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("", "获取组员ip成功");
                }
                return;

            //此代码表示从作为组长正常收到发来的信息数据
            case 125:
                int n1 = 0, i = a;
                try {
                    while (true) {
                        n1 = inputStream.read(bytes, i, 1024);
                        if (n1 == -1) {
                            break;
                        } else {
                            i = n + i;
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                Log.e("本机ip" + new String(bytes,0,a) + new String(bytes,0,i), "读取成功");
                handler=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(deviceMac).getHandler();

                handler.obtainMessage(1, i, -1, bytes).sendToTarget();
                return;

            //此代码表示向此写入发送给组员的数据
            //此处代码比较诡异
            case 138:
                try {
                    //outputStream.write(DataSinglePattern.getDataSinglePattern().getSendString().getBytes());

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e("本机ip"  + socket.toString(), "循环套接字发送成功");
                return;
            default:
                return;

        }
    }
    public static String getWifiP2pMac(String wifiMac){

        //找一个相似度最大的字符串
        char[] a=wifiMac.toCharArray();
        int max=0,mount=0;

        WifiP2pDevice maxDevice=new WifiP2pDevice();
        for(WifiP2pDevice wifiP2pDevice:DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().values()){

            char[] b=wifiP2pDevice.deviceAddress.toCharArray();
            for(int i=0;i<wifiMac.length();i++){
                if(a[i]==b[i]){
                    mount++;
                }
            }
            if(mount>max){
                max=mount;
                maxDevice=wifiP2pDevice;
            }
        }

        return maxDevice.deviceAddress;
    }

}
