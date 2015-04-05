package com.xingshijie.helpeachother;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用chatfragment来处理信息界面，此类至于实现接口的类交流
 * 高度内聚
 * 使用此fragment需要实现onChatListener接口
 */
public class ChatFragment extends Fragment {

    OnChatListener mListener=null;
    ListView listView=null;
    EditText editText=null;

    //如果外界不提供listString，就使用自带的listString
    public List<String> listString=new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_chat, container,false);
        if(view==null){
            Log.e("","view null");
        }
        listView=(ListView)view.findViewById(R.id.listView_chat);

        //设置listString
        if(mListener.getListString()!=null){
            listString=mListener.getListString();
        }
        arrayAdapter=new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,listString);
        listView.setAdapter(arrayAdapter);

        editText=(EditText)view.findViewById(R.id.editText_txtChatLine);
        Button button=(Button)view.findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string=editText.getText().toString();
                mListener.write(string
                        .getBytes());
                pushMessage("Me: " + editText.getText().toString());
                editText.setText("");

            }
        });

        return view;
    }


    public void pushMessage(String s) {
        arrayAdapter.add(s);
        arrayAdapter.notifyDataSetChanged();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnChatListener) activity;
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
    public interface OnChatListener {
        // TODO: Update argument type and name
        public void write(byte[] bytes);
        public List<String> getListString();
    }

}
