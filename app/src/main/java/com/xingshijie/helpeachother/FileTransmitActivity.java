package com.xingshijie.helpeachother;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xingshijie.helpeachother.service.FileTransmitIntentService;
import com.xingshijie.helpeachother.service.HttpFileServer;


public class FileTransmitActivity extends ActionBarActivity {
    //指明当前的二维码图片
    int n=0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transmit);

        final Intent intentService=new Intent(FileTransmitActivity.this, FileTransmitIntentService.class);
        Button button=(Button)findViewById(R.id.button_open_server);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("","下载服务打开");
                ((TextView)findViewById(R.id.textView_server)).setText("下载服务器已打开");
                //Intent intent=new Intent(FileTransmitActivity.this, FileTransmitIntentService.class);
                startService(intentService);
            }
        });
        Button button2=(Button)findViewById(R.id.button_file_select);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("","选择文件");
                Intent intent = new Intent(FileTransmitActivity.this,FileSelecterActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        Button button3=(Button)findViewById(R.id.button_close_server);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("","关闭服务器");
                stopService(intentService);
            }
        });

        final ImageView imageView=(ImageView)findViewById(R.id.imageView_QR_code);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n==0) {
                    imageView.setImageResource(R.drawable.qrcode_2);
                    ((TextView)findViewById(R.id.textView_qrCode_content)).setText("当前是直接使用wifi热点连接的图片,点击图片切换");
                    n=1;
                }else{
                    imageView.setImageResource(R.drawable.qr_code);
                    ((TextView)findViewById(R.id.textView_qrCode_content)).setText("如果使用Wifi-Direct连接扫描此图片，否则点击切换");
                    n=0;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null){
            ((TextView)findViewById(R.id.textView_file_path)).setText(data.getStringExtra("fileName"));
            HttpFileServer.filePath=data.getStringExtra("fileName");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_transmit, menu);
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
