package com.xingshijie.helpeachother;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileSelecterActivity extends Activity {
    public ListView listView;
    public Button returnButton;
    public Button okButton;
    public File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selecter);

        listView=(ListView)findViewById(R.id.listView_file);
        returnButton=(Button)findViewById(R.id.button_return_file);
        okButton=(Button)findViewById(R.id.button_ok);

        currentFile=Environment.getExternalStorageDirectory();
        inflateFileList(currentFile);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentFile.equals(Environment.getExternalStorageDirectory())){
                    currentFile=currentFile.getParentFile();
                    inflateFileList(currentFile);
                }else{
                    finish();
                }
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentFile.listFiles()[position].isDirectory()){
                    currentFile=currentFile.listFiles()[position];
                    inflateFileList(currentFile);
                }else{
                    Intent intent=new Intent();
                    intent.putExtra("fileName",currentFile.listFiles()[position].getPath());
                    setResult(1,intent);
                    finish();
                }
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_selecter, menu);
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

    //负责显示此文件下的文件
    public void inflateFileList(File file) {
        if(file.isFile()) return;

        File[] files=file.listFiles();
        List<Map<String,Object>> hashMapList=new ArrayList<Map<String,Object>>();

        for(File file1:files){
            Map<String,Object> stringObjectMap=new HashMap<>();
            stringObjectMap.put("fileName",file1.getName());
            stringObjectMap.put("fileIcon",file1.isFile()?R.drawable.file:R.drawable.folder);
            hashMapList.add(stringObjectMap);
        }

        SimpleAdapter simpleAdapter=new SimpleAdapter(this,hashMapList,R.layout.line_file,new String[]{"fileName","fileIcon"},
        new int[]{R.id.textView_file_name,R.id.imageView_file});

        listView.setAdapter(simpleAdapter);
    }
}
