package com.button.notice.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.button.notice.Button.buttonLogin;
import com.button.notice.Jellyrefresh.JellyRefreshLayout;
import com.button.notice.Notice.noticeDetailActivity;
import com.button.notice.Notice.noticeNew;


import com.button.notice.Notice.noticeSearch;


import com.button.notice.R;
import com.button.notice.service.CommonRequest;
import com.button.notice.service.CommonResponse;
import com.button.notice.service.ResponseHandler;
import com.button.notice.util.ACache;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jack on 2017/8/22.
 */


public class noticeFragment extends ListFragment  {
    private ListView listView;
    private JellyRefreshLayout mJellyLayout;
    private SimpleAdapter simpleAdapter;
    Button toNewNotice;
    TextView Search;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable
        ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_notice, null);//以上 常规操作 常规操作

    ACache aCache =ACache.get(getActivity());//键键说这个是用来存储任意类型数据的本地数据库 棒棒吧！

    ArrayList<HashMap<String,String>> arrayList =(ArrayList<HashMap<String,String>>) aCache.getAsObject("listacache1");

    if(arrayList!=null) {
        SimpleAdapter adapter = new SimpleAdapter(getContext(),
                arrayList, R.layout.notice_listviewitem,
                new String[]{"noticeTitle", "noticeText","noticeDate","noticeTime"},
                new int[]{R.id.title, R.id.info,R.id.date,R.id.time});
        setListAdapter(adapter);

    }


    listView= view.findViewById(android.R.id.list);
    Search=view.findViewById(R.id.search);
    mJellyLayout = view.findViewById(R.id.jelly_refresh);
    mJellyLayout.setPullToRefreshListener(pullToRefreshLayout -> {


                CommonRequest request = new CommonRequest();
                request.setTable("table_notice_info");
                request.Query(new ResponseHandler() {
                    @Override
                    public void success(CommonResponse response) {
                        mJellyLayout.setRefreshing(false);
                        ArrayList<HashMap<String, String>> arrayList1 = response.getDataList();
                        ACache aCache1 = ACache.get(getActivity());
                        aCache1.put("listacache1", arrayList1);
                        SimpleAdapter adapter =new SimpleAdapter(getContext(), arrayList1,R.layout.notice_listviewitem,new String[]{"noticeTitle", "noticeText","noticeDate","noticeTime"},new int[]{R.id.title, R.id.info,R.id.date,R.id.time});
                        setListAdapter(adapter);
                    }

                    @Override
                    public void fail(String failCode, String failMsg) {

                    }
                });

                //在这个里面写查询功能，然后通过适配器给Listview             加油哦
              //如果 成功就调用这个来使刷新中断 我猜的
            });  //超过多少毫秒就停止刷新，还是我猜的。


    View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.view_loading, null);
    mJellyLayout.setLoadingView(loadingView);

    {   //发送新通知跳转
        toNewNotice = view.findViewById (R.id.toNewNotice);
        toNewNotice.setOnClickListener((v -> {
        Intent intent = new Intent(getActivity(),noticeNew.class );
        startActivity(intent);
        })
        );
    }


    Search.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(),noticeSearch.class );
            startActivity(intent);
        }
    });

    return view;
}


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SharedPreferences sp = getActivity().getSharedPreferences("DODODO", Context.MODE_PRIVATE);



        if( sp.getBoolean("sign",true)) {

            Intent intent = getActivity().getIntent();
            String autosignin = intent.getStringExtra("autosignin");

            if (!(autosignin == null || autosignin.isEmpty())) {

                if (autosignin.equals("true")) {

                    CommonRequest request = new CommonRequest();
                    request.setUserName(sp.getString("account", ""));
                    request.setPassWord(sp.getString("password", ""));
                    request.Login(new ResponseHandler() {
                        @Override
                        public void success(CommonResponse response) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("sign", false);
                            editor.commit();
                        }

                        @Override
                        public void fail(String failCode, String failMsg) {
                            Toast.makeText(getActivity(), "登录失效请重新登录", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("Id", null);
                            editor.commit();
                            Intent intent = new Intent(getActivity(), buttonLogin.class);
                            startActivity(intent);
                            getActivity().finish();

                        }
                    });


                }
            }


        }
    }

    public static noticeFragment newInstance(String content) {
        Bundle args = new Bundle();
        args.putString("ARGS", content);
        noticeFragment fragment = new noticeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HashMap<String,String> map=(HashMap<String,String>) l.getItemAtPosition(position);
        String Text=map.get("Id");
        Intent intent = new Intent();
        intent.putExtra("id", Text);
        intent.setClass(getActivity(), noticeDetailActivity.class);
        getActivity().startActivity(intent);
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
    }


}