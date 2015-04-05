package com.xingshijie.helpeachother;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class WifiP2pServiceFragment extends Fragment {

    Button startButton;
    Button addButton;
    Button flushButton;
    View rootView;
    WifiP2pServiceListener mListener;

    public WifiP2pServiceFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WifiP2pServiceListener) activity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_wifi_p2p_service, container, false);
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        startButton=(Button)rootView.findViewById(R.id.button_start_service_discovery);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startServiceDiscovery();
            }
        });
        addButton=(Button)rootView.findViewById(R.id.button_add_service);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.addService();
            }
        });

        flushButton=(Button)rootView.findViewById(R.id.button_flush_serviceRequest);
        flushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.flushServiceRequest();
            }
        });


    }

    public interface WifiP2pServiceListener{
        public void addService();
        public void startServiceDiscovery();
        public void flushServiceRequest();
    }


}
