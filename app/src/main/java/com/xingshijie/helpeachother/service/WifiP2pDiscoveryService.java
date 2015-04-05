package com.xingshijie.helpeachother.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.xingshijie.helpeachother.ChatActivity;
import com.xingshijie.helpeachother.DataSinglePattern;
import com.xingshijie.helpeachother.MainActivity;
import com.xingshijie.helpeachother.R;
import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WifiP2pDiscoveryService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    public static final String TAG="WifiP2pDiscoveryService";
    public static final int PERIOD=10*1000;
    public static ArrayList<Map<String,String>> arrayListRecord=new ArrayList<>();
    public static final String TITLE="title";
    public static final String CONTENT="content";

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
//        HandlerThread thread = new HandlerThread("ServiceStartArguments",
//                Process.THREAD_PRIORITY_BACKGROUND);
//        thread.start();
//
//        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper =getMainLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        wifiP2pManager=(WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        channel=wifiP2pManager.initialize(this,mServiceLooper,new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.e("","wifip2pmanager channel disconnected");
            }
        });
        startRegistration();
        initializeServiceDiscover();

        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                discoverService();
            }
        };
        Timer timer=new Timer();
        timer.scheduleAtFixedRate(timerTask,1000,PERIOD);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
//        Message msg = mServiceHandler.obtainMessage();
//        msg.arg1 = startId;
//        mServiceHandler.sendMessage(msg);


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        Log.e("","WifiP2pDiscoveryService destroy");
    }

    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistration() {
        Map<String, String> record = new HashMap<>();
        record.put("isVisible", "visible");
        record.put("name","sos");
        record.put(TITLE,"xingshijie");
        record.put(CONTENT,"hahahahha");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        wifiP2pManager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.e("","Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.e(""+error,"Failed to add a service");
            }
        });
    }

    public WifiP2pDnsSdServiceRequest serviceRequest;
    private void initializeServiceDiscover(){
        wifiP2pManager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.


                                /*WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                                        .getListAdapter());
                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;*/



                        }
                        Log.e("intanceName",instanceName);
                        Log.e("registrationType",registrationType);
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        //遍历已存在的记录，如果存在相同记录，不通知

                        boolean isContain=false;
                        for(Map<String,String> myRecord:arrayListRecord){
                            for(String value:record.values()){
                               if(!myRecord.containsValue(value)){
                                   isContain=false;
                                  break;
                               }else {
                                   isContain=true;
                               }
                            }
                            if(isContain){break;}
                        }
                        if(!isContain){
                            arrayListRecord.add(record);
                            notification(device,record);
                        }

                        Log.d(TAG,
                                device.deviceName + " is "
                                        + record.get("name"));
                        Log.e("fullDomainName",fullDomainName);
                        Log.e("record",record.toString());
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiP2pManager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.e("","Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.e(""+arg0,"Failed adding service discovery request");
                    }
                });
    }
    private void notification(WifiP2pDevice wifiP2pDevice,Map<String,String> stringMap){


        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(intent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );

        Notification notification=new Notification.Builder(this)
                .setAutoCancel(true).setTicker(wifiP2pDevice.deviceName + ":" )
                .setSmallIcon(R.drawable.file)
                .setContentTitle(stringMap.get(TITLE))
                .setContentText(stringMap.get(CONTENT))
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();
        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(100,notification);

    }
    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.e("", "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.e(""+arg0, "Service discovery failed");

            }
        });
    }


    public void addService() {
        startRegistration();
    }

    public void startServiceDiscovery() {
        discoverService();
    }

    public void flushServiceRequest() {
        wifiP2pManager.removeServiceRequest(channel,serviceRequest,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("","remove sevice request success");
            }

            @Override
            public void onFailure(int reason) {
                Log.e("","remove sevice request fail");
            }
        });

        initializeServiceDiscover();
    }


    /**
     * 在service fragment里填充数据
     */
    public void influateSeviceFragment(){

    }
}
