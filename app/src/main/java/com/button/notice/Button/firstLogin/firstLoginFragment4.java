package com.button.notice.Button.firstLogin;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.button.notice.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class firstLoginFragment4 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_firstlogin_3, null);


        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {   //在Activity创建后再获取信息
        super.onActivityCreated(savedInstanceState);


    }



    public static firstLoginFragment4 newInstance(String content) {
        Bundle args = new Bundle();
        args.putString("ARGS", content);
        firstLoginFragment4 fragment = new firstLoginFragment4();
        fragment.setArguments(args);
        return fragment;
    }

}
