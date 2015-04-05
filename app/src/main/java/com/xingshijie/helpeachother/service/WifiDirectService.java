package com.xingshijie.helpeachother.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.xingshijie.helpeachother.ChatManager;
import com.xingshijie.helpeachother.DataSinglePattern;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 使用服务监听4545端口，并使用线程处理连接信息
 */
public class WifiDirectService extends IntentService {


    public WifiDirectService() {
        super("WifiDirectService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            ServerSocket serverSocket=null;
            try {
                serverSocket=new ServerSocket(4545);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true){
                Socket socket=null;
                if(serverSocket!=null) {
                    try {
                        socket=serverSocket.accept();
                        Log.e("","有连接到来");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new Thread(new ChatManager(socket)).start();
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
