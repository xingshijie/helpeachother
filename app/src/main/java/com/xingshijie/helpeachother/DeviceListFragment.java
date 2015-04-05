package com.xingshijie.helpeachother;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.xingshijie.helpeachother.datamanger.MyWifiP2pDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the
 * interface.
 * 负责显示wifipeerlist
 * 显示附近的设备列表
 * 使用此fragment需要实现onDeviceListListener
 */
public class DeviceListFragment extends ListFragment {


    private OnDeviceListListener mListener;

    Collection<MyWifiP2pDevice> myWifiP2pDeviceCollection=DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().values();

    //次函数创建的list底层不依赖collection里的元素，
    List<MyWifiP2pDevice> myWifiP2pDeviceList=new ArrayList<>(myWifiP2pDeviceCollection);

    public DeviceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter arrayAdapter=new WifiP2pDeviceArrayAdapter(getActivity(),android.R.layout.simple_list_item_2,android.R.id.text1,myWifiP2pDeviceList);

        setListAdapter(arrayAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeviceListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(((LinearLayout)v.findViewById(R.id.linearLayout_device_information)).getVisibility()==View.VISIBLE){
            v.findViewById(R.id.linearLayout_device_information).setVisibility(View.GONE);
        }else {
            v.findViewById(R.id.linearLayout_device_information).setVisibility(View.VISIBLE);
        }

//        if (null != mListener) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            MyWifiP2pDevice wifiP2pDevice= myWifiP2pDeviceList.get(position);
//
//            //!!!!!!此数据传回去的是list里的数据，不管如何修改都不会改变collection里的数据
//            //mListener.onDeviceListener(wifiP2pDevice);
//            mListener.onDeviceListener(DataSinglePattern.getDataSinglePattern().getMyWifiP2pDeviceMap().get(wifiP2pDevice.deviceAddress));
//        }
    }

    public void showAllDevice(){


//        for(WifiP2pDevice wifiP2pDevice:myWifiP2pDeviceCollection){
//            Map<String,Object> map=new HashMap<>();
//            map.put("deviceName",wifiP2pDevice.deviceName);
//            map.put("name",wifiP2pDevice.deviceAddress);
//            map.put("state","未连接");
//            map.put("device",wifiP2pDevice);
//
//            list.add(map);
//        }

        //退而求其次，每次改变时再重新设置list；
        //缺点，会不会导致刷新太多导致卡频现象
        myWifiP2pDeviceList.clear();
        myWifiP2pDeviceList.addAll(myWifiP2pDeviceCollection);
//        for(MyWifiP2pDevice myWifiP2pDevice:myWifiP2pDeviceCollection){
//            Log.e("",myWifiP2pDevice.deviceName);
//        }

        ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
    }

    public class WifiP2pDeviceArrayAdapter extends ArrayAdapter<MyWifiP2pDevice> {
        private List<MyWifiP2pDevice> list;

        public WifiP2pDeviceArrayAdapter(Context context, int resource,
                                  int textViewResourceId, List<MyWifiP2pDevice> items) {
            super(context, resource, textViewResourceId,items);
            this.list= items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.fragment_device_list, null);
            }
            final MyWifiP2pDevice myWifiP2pDevice = list.get(position);
            if( myWifiP2pDevice!= null) {
                TextView nameText = (TextView) v
                        .findViewById(R.id.textView);

                if (nameText != null) {
                    nameText.setText(myWifiP2pDevice.deviceName+ " - " + myWifiP2pDevice.deviceAddress);
                }
                TextView statusText = (TextView) v
                        .findViewById(R.id.textView2);
                statusText.setText(getDeviceStatus(myWifiP2pDevice.status));

                TextView ownerText = (TextView) v
                        .findViewById(R.id.textView3);

                ownerText.setText(myWifiP2pDevice.getIp());

                Button buttonConnectPeer=(Button)v.findViewById(R.id.button_connect_peer);
                buttonConnectPeer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onConnectDevice(myWifiP2pDevice);
                    }
                });


                Button buttonStartChat=(Button)v.findViewById(R.id.button_start_chat);
                buttonStartChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onStartChat(myWifiP2pDevice);
                    }
                });

                Button buttonSendFile=(Button)v.findViewById(R.id.button_send_file);
                buttonSendFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onStartSendFile(myWifiP2pDevice);
                    }
                });

                //listview里加入按钮会使listvi的点击事件无效
//                Button button=(Button)v.findViewById(R.id.button_disconnect);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mListener.disconnectPeer(myWifiP2pDevice);
//                    }
//                });
            }
            return v;
        }
    }

    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDeviceListListener {
        // TODO: Update argument type and name
        public void onStartChat(WifiP2pDevice wifiP2pDevice);
        public void onStartSendFile(WifiP2pDevice wifiP2pDevice);
        public void onConnectDevice(WifiP2pDevice wifiP2pDevice);
    }

}
