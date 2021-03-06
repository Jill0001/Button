package com.button.notice.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.button.notice.util.ACache;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import droidninja.filepicker.utils.FileUtils;

import static com.button.notice.service.Constant.URL_Download;

/**
 * Created by Jack on 2017/12/7.
 */

public class CommonRequest {

    private String userName;
    private String passWord;


    /**
     * 查找对应的表
     **/
    private String Table;
    /**
     * 查找对应的行
     **/
    private String Id = null;
    /**
     * 查找对应的列
     **/
    private String List;

    /**
     * 圈子
     */
    private String Community;

    /**
     * 这个是当你用Connect方法的时候，鬼知道你要传什么的Id进来，所以就有了他。
     */
    private String Text;
    /**
     * 判断是否推送
     *
     */
    private boolean ispush;

    /**
     * 用于判断类型
     */


    private String type;

    /**
     * 条件查询参数
     **/
    private String WhereEqualTo = null;
    private String WhereEqualMoreTo = null;
    private String WhereEqualAndTo = null;
    private String WhereEqualAndTovalue = null;
    private String[] WhereEqualMoreTovalue = null;
    private String WhereEqualTovalue = null;
    private String WhereNotEqualTo = null;
    private String WhereNotEqualTovalue = null;

    /**
     * 模糊查询
     */
    private String LikeEqualTo = null;

    /**
     * 请求码，类似于接口号（在本文中用Servlet做服务器时暂时用不到）
     */
    private String requestCode;
    /**
     * 请求参数
     * （说明：这里只用一个简单map类封装请求参数，对于请求报文需要上送一个数组的复杂情况需要自己再加一个ArrayList类型的成员变量来实现）
     */
    private HashMap<String, String> requestParam;

    public CommonRequest() {
        requestCode = "";
        requestParam = new HashMap<>();
    }

    /**
     * 设置请求代码，即接口号
     */
    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    /**
     * 为请求报文设置参数
     *
     * @param paramKey   参数名
     * @param paramValue 参数值
     */
    public void addRequestParam(String paramKey, String paramValue) {
        requestParam.put(paramKey, paramValue);
    }

    /**
     * 将请求报文体组装成json形式的字符串，以便进行网络发送
     *
     * @return 请求报文的json字符串
     */
    public String getJsonStr() {

        JSONObject object = new JSONObject();
        JSONObject param = new JSONObject(requestParam);
        try {
            // 下边2个"requestCod'e"、"requestParam"是和服务器约定好的请求体字段名称，在本文接下来的服务端代码会说到
//            object.put("","{");
            object.put("requestCode", requestCode);
            object.put("requestParam", param);
        } catch (JSONException e) {

        }
        return object.toString();
    }

    /**
     * 用于登录
     * 用setUserName和setPassWord设置账户密码
     *
     * @param rHandler 返回成功或失败的值
     */
    public void Login(ResponseHandler rHandler) {       //登录

        final CommonRequest request = new CommonRequest();
        request.addRequestParam("userAccount", this.getUserName());
        request.addRequestParam("userPassword", this.getPassWord());

        new HttpPostTask(Constant.URL_Login, request, rHandler).execute();

    }

    /**
     * 用于注册
     * 用setUserName和setPassWord设置账户密码
     *
     * @param rHandler 返回成功或失败的值
     */
    public void Signup(ResponseHandler rHandler) {   //注册

        final CommonRequest request = new CommonRequest();
        request.addRequestParam("userAccount", this.getUserName());
        request.addRequestParam("userPassword", this.getPassWord());
        new HttpPostTask(Constant.URL_Register, request, rHandler).execute();

    }

    /**
     * 用于上传信息（用户，通知，等等）
     * 上传前首先用setTable方法设置要上传到那个表，然后用addRequestParam添加要上传的列的名字和列的值
     *
     * @param request  需要上传的信息
     * @param rHandler 返回成功或失败的值
     */
    public void Updata(CommonRequest request, ResponseHandler rHandler) {   //更新用户信息
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("Id", this.getId());
        new HttpPostTask(Constant.URL_Updata, request, rHandler).execute();
    }

    /**
     * 用于创建表（发通知，发公告）
     * 创建前首先用setTable方法设置要上传到那个表，然后用addRequestParam添加要上传的列的名字和列的值
     *
     * @param rHandler 返回成功或失败的值
     */
    public void Create(CommonRequest request, ResponseHandler rHandler) {  //创建

        request.addRequestParam("Table", this.getTable());
        request.setRequestCode(this.isIspush()+"");
        request.addRequestParam("ispush",this.isIspush()+"");
        new HttpPostTask(Constant.URL_Create, request, rHandler).execute();

    }

    /**
     * 用于删除表（删除公告，撤回）
     * 删除前首先用setTable方法选择删除的表，然后用setId方法设置要删除哪一行。
     *
     * @param rHandler 返回成功或失败的值
     */
    public void Delete(ResponseHandler rHandler) {            //删除
        CommonRequest request = new CommonRequest();
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("Id", this.getId());
        new HttpPostTask(Constant.URL_Delete, request, rHandler).execute();

    }

    /**
     * 用于查询
     * 查询前首先用setTable方法设置查询哪个表
     * 1、条件查询 用setWhereEqualTo设置，哪一列满足什么条件。用setWhereNotEqualTo设置那一列不满足什么条件。用setWhereEqualMoreTo设置多条件查询 先创建一个String[]数组，然后传进来。
     * 2、模糊查询 用setLikeEqualTo设置，模糊查询的关键字。然后调用此方法。
     * @param rHandler 返回成功或失败的值
     */


    public void Query(ResponseHandler rHandler) {

        final CommonRequest request = new CommonRequest();
        request.addRequestParam("Table", this.getTable());

        request.addRequestParam("ISMORE","2");  //用于判断是否是多条件查询
        request.addRequestParam("ISLIKE","2");  //用于判断是否是模糊查询


        if (this.getWhereEqualTo() != null) {
            request.addRequestParam(this.getWhereEqualTo() + " ", this.getWhereEqualTovalue());
            request.addRequestParam("ISMORE","0");  //用于判断是否是多条件查询
        }
        if (this.getWhereEqualMoreTo() != null){
            request.addRequestParam("ISMORE","1");  //用于判断是否是多条件查询
            String[] va = this.getWhereEqualMoreTovalue();
            for(int len=0;len<this.getWhereEqualMoreTovalue().length;len++) {
                request.addRequestParam(this.getWhereEqualMoreTo() + len,va[len]);
            }

        }
//        if(this.getWhereEqualAndTo()!=null){
//            request.addRequestParam("ISAND","3");
//            request.addRequestParam(this.getWhereEqualAndTo()+" ",this.getWhereEqualAndTovalue());
//        }

        if (this.getWhereNotEqualTo() != null){
            request.addRequestParam(this.getWhereNotEqualTo() + " !", this.getWhereNotEqualTovalue());
        }

        if(this.getLikeEqualTo() !=null){
            request.addRequestParam("ISLIKE","1");  //用于判断是否是模糊查询
            request.addRequestParam("LikeKey",this.getLikeEqualTo());
        }

        request.addRequestParam("List", this.getList());

        new HttpPostTask(Constant.URL_Query, request, rHandler).execute();
    }


    /**
     * 用于添加用户
     * 添加前 先设置表，列，行 具体到一个格。
     * @param c 当前的活动名称
     */

    public void Adduser(Context c) {
        CommonRequest request = new CommonRequest();
        request.addRequestParam("ifadd","1");
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("List", this.getList());
        request.addRequestParam("Id", getCurrentId(c));
        request.addRequestParam("Community", this.getCommunity());
        new HttpPostTask(Constant.URL_Connect, request).execute();
    }

    /**
     *
     * @param x 传入“0”为收藏功能，传入“1”为加入圈子功能，传入“2”为评论功能，传入“3”为用户表的普通连接功能，传入“4”为其他表的普通连接功能。
     *          使用前用setTable ，setList ，setId 设置表中的指定一格。
     * @param rHandler 回调
     */
    public void Connect(String x,ResponseHandler rHandler){
        CommonRequest request = new CommonRequest();
        request.setRequestCode("Connect");//Connect
        request.addRequestParam("ifadd",x);
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("List", this.getList());
        request.addRequestParam("Id", this.getId());
        request.addRequestParam("Text", this.getText());
        new HttpPostTask(Constant.URL_Connect, request,rHandler).execute();
    }

    /**
     *
     * @param x 传入“1”为退出圈子功能，传入“2”为删除评论功能，传入“3”为用户表的普通取消连接功能，传入“4”为其他表的普通取消连接功能。
     *          使用前用setTable ，setList ，setId 设置表中的指定一格。
     * @param rHandler 回调
     */

    public void Disconnect(String x,ResponseHandler rHandler){
        CommonRequest request = new CommonRequest();
        request.setRequestCode("Disconnect");//Disconnect
        request.addRequestParam("ifadd",x);
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("List", this.getList());
        request.addRequestParam("Id", this.getId());
        request.addRequestParam("Text", this.getText());
        new HttpPostTask(Constant.URL_Connect, request,rHandler).execute();
    }




    public void Connect(Context c){
        CommonRequest request = new CommonRequest();
        request.addRequestParam("ifadd","0");
        request.addRequestParam("Table", this.getTable());
        request.addRequestParam("List", this.getList());
        request.addRequestParam("Id", getCurrentId(c));
        request.addRequestParam("Community", this.getText());
        new HttpPostTask(Constant.URL_Connect, request).execute();
    }







    /**
     * 下载前，设置文件在云服务器中的URL，然后写下面的句子，逗号前面是你要下载的地方，逗号后面不用管，那个Envionment什么的是获取SD卡目录。后面我随便设置的文件夹。
     *   File file=new File(Environment.getExternalStorageDirectory()+"/ServiceTest",path.substring(path.lastIndexOf("/")+1));
     * @param rHandler
     */

    /**
    CommonRequest request = new CommonRequest();
    String path =Constant.URL_Database+"报名表.doc";
    File file=new File(Environment.getExternalStorageDirectory()+"/ServiceTest",path.substring(path.lastIndexOf("/")+1));
                request.Download(path, new FileAsyncHttpResponseHandler(file) {
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            Toast.makeText(this, "文件下载失败"+statusCode,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, File file) {

            Toast.makeText(this, "文件下载成功"+file.getPath(),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            // TODO Auto-generated method stub
            super.onProgress(bytesWritten, totalSize);
            int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
            // 下载进度显示
            bar.setProgress(count);

        }


    });

    **/

    public void Download(String x,FileAsyncHttpResponseHandler rHandler){

        //下载之后存放的路径 获取SD卡的路径
        AsyncHttpClient client=new AsyncHttpClient();
        client.get(x, rHandler);

    }


    public void Download(String path,String file,ResponseHandler rHandler){
        new HttpPostTaskDown(path,file,rHandler).execute();

    }



    /**
     * 单文件上传
     * 传入URL 然后设置用户的ID，设置类型type（必不可少的环节，最好加判断Id是否为空，有可能会出现长时间登录失效的问题。）
     *
     * 类型 type 的值代表什么：
     *
     * 1,用户头像
     * 2,活动海报
     * 3,活动附件
     * 4,通知附件
     *
     * @param x
     * @param rHandler
     */

    public void Upload(String x,AsyncHttpResponseHandler rHandler) {
        // new Thread(new Runnable() {////不能使用线程
        //
        // @Override
        // public void run() {
        // TODO 自动生成的方法存根
        // 服务器端地址
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(60 * 60 * 1000);
        RequestParams param = new RequestParams();
        try {
            param.put("file0", new File(x));
            param.put("Id", this.getId());
            param.put("Type",this.getType());
            httpClient.post(Constant.URL_Upload, param,rHandler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // }
        // }).start();
    }

    /**
     * 多文件夹上传
     * 传入URL数组 String[] URL= xxxxxxxx;  然后设置用户的ID（必不可少的环节，最好加判断Id是否为空，有可能会出现长时间登录失效的问题。）
     * @param x
     * @param rHandler
     */
    public void Upload(String[] x,AsyncHttpResponseHandler rHandler){
        //创建异步请求对象
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for(int len=0;len<x.length;len++){
            File file =new File(x[len]);
            if(file.exists() && file.length()>0){
                System.out.println("文件找到！");
                //将要上传的文件放入RequestParams对象中
                try {
                    params.put("file"+len, file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                params.put("Id", this.getId());
        }
        }

        client.post(Constant.URL_Upload, params,rHandler);

    }






    public void CreateExecl(String x,String y,String z,Context c,ResponseHandler rHandler){
        CommonRequest request = new CommonRequest();
        request.addRequestParam("Id", getCurrentId(c));
        request.addRequestParam("name", z);
        request.addRequestParam("AId", y);
        request.addRequestParam("info",x);
        new HttpPostTask(Constant.URL_Excel, request,rHandler).execute();
    }





    /**
     * 用于获取当前用户的Id
     * @param c 当前的Activity名称
     * @return 当前用户的Id
     **/

    public String getCurrentId(Context c) {
        final SharedPreferences sp = c.getSharedPreferences("DODODO", Context.MODE_PRIVATE);
        String Id = sp.getString("Id", "");
        return Id;
    }












    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getTable() {
        return Table;
    }

    public void setTable(String table) {
        Table = table;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getList() {
        return List;
    }

    public void setList(String list) {
        List = list;
    }


    public String getWhereEqualTo() {
        return WhereEqualTo;
    }

    public void setWhereEqualTo(String key, String value) {
        WhereEqualTo = key;
        WhereEqualTovalue = value;
    }

    public String getWhereNotEqualTo() {
        return WhereNotEqualTo;
    }

    public void setWhereNotEqualTo(String key, String value) {
        WhereNotEqualTo = key;
        WhereNotEqualTovalue = value;
    }

    public String getWhereEqualTovalue() {
        return WhereEqualTovalue;
    }

    public String getWhereNotEqualTovalue() {
        return WhereNotEqualTovalue;
    }

    public String getCommunity() {
        return Community;
    }

    public void setCommunity(String community) {
        Community = community;
    }

    public String getWhereEqualMoreTo() {
        return WhereEqualMoreTo;
    }

    public void setWhereEqualMoreTo(String key, String[] value) {
        WhereEqualMoreTo = key;
        WhereEqualMoreTovalue =value;
    }

    public String[] getWhereEqualMoreTovalue() {
        return WhereEqualMoreTovalue;
    }

    public boolean isIspush() {
        return ispush;
    }

    public void setIspush(boolean ispush) {
        this.ispush = ispush;
    }

    public String getLikeEqualTo() {
        return LikeEqualTo;
    }

    public void setLikeEqualTo(String likeEqualTo) {
        LikeEqualTo = likeEqualTo;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getWhereEqualAndTo() {
        return WhereEqualAndTo;
    }

    public void setWhereEqualAndTo(String key,String vlaue) {
        WhereEqualAndTo = key;
        WhereEqualAndTovalue =vlaue;
    }

    public String getWhereEqualAndTovalue() {
        return WhereEqualAndTovalue;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
