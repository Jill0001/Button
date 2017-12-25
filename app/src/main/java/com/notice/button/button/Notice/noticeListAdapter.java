package com.notice.button.button.Notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.notice.button.button.Notice.noticeDetail;
import com.notice.button.button.R;

import java.util.List;

/**
 * Created by bzdell on 2017/12/14.
 */

public class noticeListAdapter extends ArrayAdapter {

        private final int resourceId;

        public noticeListAdapter(Context context, int textViewResourceId, List<noticeDetail> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            noticeDetail noticeDetail = (noticeDetail) getItem(position); // 获取当前项的实例
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象

            TextView notice_title = (TextView) view.findViewById(R.id.notice_title);
            TextView notice_lable = (TextView) view.findViewById(R.id.notice_label);
            TextView notice_deadline_date = (TextView) view.findViewById(R.id.notice_deadline_date);
            TextView notice_release_date = (TextView) view.findViewById(R.id.notice_release_date);
            TextView notice_publisher = (TextView) view.findViewById(R.id.notice_publisher);//获取该布局内的文本视图

            notice_title.setText(noticeDetail.getTitle());//为文本视图设置文本内容

            return view;
        }
    }
