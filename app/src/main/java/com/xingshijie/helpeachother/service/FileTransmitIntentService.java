package com.xingshijie.helpeachother.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * s.
 */
public class FileTransmitIntentService extends IntentService {

    public static final int FILE_TRANSMIT_PORT=5858;
    String header;
    String MIMEType="application/octet-stream";
    long length;
    InputStream inputStream;
    Intent intent;
    public FileTransmitIntentService() {
        super("FileTransmitIntentService");
    }
    private void constructHttpHeader(){
        header = "HTTP/1.0 200 OK\r\n"
                + "Server:OneFile 1.0\r\n"
                + "Content-length:" + length + "\r\n"
                + "Content-type:" + MIMEType + "\r\n\r\n";
        //this.header = header.getBytes("ASCII");
    }
    private void getFile(){
        File file=new File(Environment.getExternalStorageDirectory().getPath()+"/fqrouter-latest.apk");
        try {
            length=file.length();
            inputStream=new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            Log.e("","文件打开错误");
            e.printStackTrace();
        }
    }
    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("", e.toString());
            return false;
        }
        return true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.intent=intent;

        try {
            new HttpFileServer().startFileServer();
        } catch (Exception e) {
            Log.e("","创建文件服务器出错");
            e.printStackTrace();
        }
    }

    public void myFileServer(){
        getFile();
        constructHttpHeader();

        if (intent != null) {
            try {
                //创建服务器socket
                ServerSocket server = new ServerSocket(FILE_TRANSMIT_PORT);
                System.out.println("接收来自端口为" + server.getLocalPort() + "的客户端的连接");
                while (true) {
                    Socket conn = null;
                    try {
                        conn = server.accept();
                        //定义一个输出流，用来向客户端发送数据
                        Log.e("", "准备发送");
                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                        //定义一个输入流，用来读取客户端发来的数据
                        BufferedInputStream in = new BufferedInputStream(conn.getInputStream());


                        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));
                        String string;
                        Log.e("","到这了");
                        //                    while(bufferedReader.ready()) {
                        string = bufferedReader.readLine();
                        //                   Log.e("",string);
                        //                  }

                        StringBuffer request=new StringBuffer();
                        while (true) {
                            int c=in.read();
                            if (c=='\r'||c=='\n'||c==-1) {
                                break;
                            }
                            request.append((char)c);

                        }
                        //如果检测到是HTTP/1.0及以后的协议，按照规范，需要发送一个MIME首部

                        out.write(header.getBytes("ASCII"));


                        //发送context
                        copyFile(inputStream, out);
                        //涮新

                    } catch (IOException ex) {
                    }
                }//end while
            } catch (IOException ex) {
                ex.printStackTrace();
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
