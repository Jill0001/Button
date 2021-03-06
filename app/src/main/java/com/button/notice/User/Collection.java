package com.button.notice.User;

import android.app.ListActivity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.button.notice.R;
import com.button.notice.service.CommonRequest;
import com.button.notice.service.CommonResponse;
import com.button.notice.service.ResponseHandler;
import com.button.notice.util.ACache;
import com.button.notice.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class Collection extends ListActivity {
    private ListView a;
    com.button.notice.util.ProgressBar b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_collection);
        a=findViewById(android.R.id.list);
        b=findViewById(R.id.progressBar);
        noticeget();
        noticegeto();


    }

    public void noticeget(){

        ACache aCache = ACache.get(Collection.this);
        ArrayList<HashMap<String, String>> list =  (ArrayList<HashMap<String,String>>) aCache.getAsObject("collection");
        if(list!=null){
            SimpleAdapter adapter =new SimpleAdapter(Collection.this, list,R.layout.notice_listviewitem,new String[]{"noticeTitle", "noticeText","noticeDate","noticeTime"},new int[]{R.id.title, R.id.info,R.id.date,R.id.time});
            a.setAdapter(adapter);


        }
    }

    public void noticegeto(){

        CommonRequest request = new CommonRequest();
        request.setTable("table_user_info");
        request.setWhereEqualTo("userId",request.getCurrentId(Collection.this));
        request.Query(new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {

                ArrayList<HashMap<String, String>> list = response.getDataList();

                if(list.size()>0) {


                    HashMap<String, String> map = list.get(0);
                    String[] collection = StringUtil.ChangetoString(map.get("userCollection"));


                        if(collection[0]!=""){

                    CommonRequest request1 = new CommonRequest();
                    request1.setTable("table_notice_info");
                    request1.setWhereEqualMoreTo("Id", collection);
                    request1.Query(new ResponseHandler() {
                        @Override
                        public void success(CommonResponse response) {
                            if (response.getDataList().size() > 0) {
                                ACache aCache = ACache.get(Collection.this);
                                ArrayList<HashMap<String, String>> list = response.getDataList();

                                if (aCache.getAsObject("collection") != null) {

                                    if (((ArrayList<HashMap<String, String>>) aCache.getAsObject("collection")).size() != list.size()) {

                                        aCache.put("collection", list);
                                        SimpleAdapter adapter = new SimpleAdapter(Collection.this, list, R.layout.notice_listviewitem, new String[]{"noticeTitle", "noticeText", "noticeDate", "noticeTime"}, new int[]{R.id.title, R.id.info, R.id.date, R.id.time});
                                        a.setAdapter(adapter);
                                        new Handler().postDelayed(() -> b.setVisibility(View.GONE), 1000);  //延时操作，使动画消失
                                    }
                                    else{
                                        new Handler().postDelayed(() -> b.setVisibility(View.GONE), 1000);  //延时操作，使动画消失
                                    }

                                } else {

                                    aCache.put("collection", list);
                                    SimpleAdapter adapter = new SimpleAdapter(Collection.this, list, R.layout.notice_listviewitem, new String[]{"noticeTitle", "noticeText", "noticeDate", "noticeTime"}, new int[]{R.id.title, R.id.info, R.id.date, R.id.time});
                                    a.setAdapter(adapter);
                                        new Handler().postDelayed(() -> b.setVisibility(View.GONE), 1000);
                                }

                            }
                        }

                        @Override
                        public void fail(String failCode, String failMsg) {

                        }
                    });
                        }
                        else {
                            ArrayList<HashMap<String, String>> list2 = null;
                            ACache aCache = ACache.get(Collection.this);
                            aCache.put("collection", list2);
                            a.setAdapter(null);
                        }
                }
            }
            @Override
            public void fail(String failCode, String failMsg) {

            }
        });


    }

    public void back(View view){
        finish();
    }
}
