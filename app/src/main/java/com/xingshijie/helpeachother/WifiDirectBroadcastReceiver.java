package com.xingshijie.helpeachother;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;
    public static final  String TAG="broadcastreceiver";

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.w("","");
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled

            } else {


            }
            Log.e(TAG,"WIFI_P2P_STATE_CHANGED_ACTION");
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel,activity);
            }

            Log.e(TAG,"WIFI_P2P_PEERS_CHANGED_ACTION");
            //如果由一方直接关闭wifi，会触发此广播connection_changed_acttion,可以在这里加入requespeers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.w("",networkInfo.toString());
            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel,
                        activity);

            } else {
                // It's a disconnect

            }

            WifiP2pGroup wifiP2pGroup=intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            Log.w("",wifiP2pGroup.toString());
            if(wifiP2pGroup!=null) {
                manager.requestGroupInfo(channel, activity);
            }

            Log.e(TAG,"WIFI_P2P_CONNECTION_CHANGED_ACTION");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice wifiP2pDevice=intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            DataSinglePattern.getDataSinglePattern().getMyWifiP2pDevice().update(wifiP2pDevice);
            //此处可以获得自己的设备
            Log.e(TAG,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

        }else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            //发现状态改变
            if(intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,250)==WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                DataSinglePattern.getDataSinglePattern().setDiscoveryEnable(true);
            }else {
                DataSinglePattern.getDataSinglePattern().setDiscoveryEnable(false);
                //设置程序永远处于discovery状态
                //只有在dicover状态的前几秒钟才能发现，虽然处于discovery状态，但仍旧不可发现
                activity.discoverPeers();
            }
            Log.e(TAG,"WIFI_P2P_DISCOVERY_CHANGED_ACTION"+intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,250));
        }
    }
}