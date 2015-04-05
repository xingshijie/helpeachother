package com.xingshijie.helpeachother.datamanger;

import android.util.Log;

import com.xingshijie.helpeachother.DataSinglePattern;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Administrator on 2015/3/31 0031.
 */
public class GetIP extends Thread{
    @Override
    public void run(){

        for(InetAddress inetAddress: DataSinglePattern.getDataSinglePattern().getGroupOwner().values()){
            try {
                Socket socket=new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(inetAddress.getHostAddress(),4545) ,5000);
                OutputStream outputStream = socket.getOutputStream();

                new DataOutputStream(outputStream).writeInt(111);
                new DataOutputStream(outputStream).write(DataSinglePattern.getDataSinglePattern().getMacAddress().getBytes());

                Log.e("本机ip" + socket.toString(), "发送给长成功");

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
