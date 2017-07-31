package com.mit.money.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dsc.core.DscCoreAgent;
import com.android.dsc.core.DscCoreListener;
import com.android.dsc.core.DscRequestBase;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.utils.PacketData;
import com.mit.money.utils.PacketInfo;
import com.mit.money.utils.PayProto;
import com.mit.money.utils.SpUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.pingplusplus.android.PaymentActivity;

import java.lang.ref.WeakReference;
import java.util.List;

import com.mit.money.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by langduan on 16/3/29.
 */
public class DetailsActivity extends UmengAgentActivity implements View.OnClickListener {

    private Context mContext;
    private TextView mDetailTotalNum;
    private TextView mDetailTotalSize;

    private TextView mPayTitle;

    private ListView detailListView;

    private static RecordAdapter recordAdapter;
    private static List<PacketInfo> packetInfos;

    private final String URL = "http://www.fuli365.net/app_interface/app_main_interface.php";
    private final int REQUEST_CODE_PAYMENT = 1;
    public final String CHANNEL_WECHAT = "wx";  //微信支付渠道
    private String payCommodity = "";
    private boolean paySuccess = false;
    private String mSuccess = "";
    private String mFail = "";
    private String mCancel = "";
    private String mInvalid = "";
    private String webname = "";
    private String weburl = "";

    private MyHandler handler;

    private static class MyHandler extends Handler {
        WeakReference<DetailsActivity> activityWeakReference;

        public MyHandler(DetailsActivity activity) {
            activityWeakReference = new WeakReference<DetailsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailsActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.detailListView.setAdapter(recordAdapter);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        initView();
        init();
        getUmengOnlineParams();
        setData();
    }

    private void setData() {
        String payTitle = OnlineConfigAgent.getInstance().
                getConfigParams(this, SpUtil.KEY_ROB_PAY_TITLE);
        if (payTitle.equals("null")) {
            mPayTitle.setText(payTitle);
            mPayTitle.setVisibility(View.INVISIBLE);
        } else {
            mPayTitle.setText(payTitle);
            mPayTitle.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        mContext = getApplicationContext();

        handler = new MyHandler(this);
        /* 从db文件中读取所有记录 */
        PacketData PacketData = new PacketData(this);
        packetInfos = PacketData.getAll();

        recordAdapter = new RecordAdapter();
    }

    private void initView() {
        ImageView mRobpay = (ImageView) this.findViewById(R.id.rob_pay);
        mRobpay.setOnClickListener(this);

        mDetailTotalNum = (TextView) this.findViewById(R.id.rob_total_num_text);
        mDetailTotalSize = (TextView) this.findViewById(R.id.rob_detail_total_size);
        detailListView = (ListView) findViewById(R.id.record_detail_list);
        mPayTitle = (TextView) this.findViewById(R.id.rob_detail_pay_title);
    }

    private void getUmengOnlineParams() {
        OnlineConfigAgent.getInstance().updateOnlineConfig(this);
        OnlineConfigAgent.getInstance().setDebugMode(false);        //在线参数Debug模式
        mSuccess = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_SUCCESS);
        mFail = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_FAIL);
        mCancel = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_CANCEL);
        mInvalid = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_INVALID);
        webname = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_WEB_NAME);
        weburl = OnlineConfigAgent.getInstance().getConfigParams(this, SpUtil.KEY_PAYMENT_WEB_URL);
    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长

        String size = (String) SpUtil.getValue(mContext, SpUtil.KEY_TOTAL_VALUE, "0.00");
        int num = (int) SpUtil.getValue(mContext, SpUtil.KEY_TOTAL_NUM, 0);

        /* 设置统计的红包数量 */
        mDetailTotalNum.setText(String.valueOf(num));

        /* 设置统计的金额大小 */
        if (Double.valueOf(size) == 0) {
            mDetailTotalSize.setText("¥ 0.00");
        } else {
            mDetailTotalSize.setText("¥ " + size);
        }
        loadList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadList() {
        AsyncTask.execute(mLoadDetailRunnable);
    }

    private final Runnable mLoadDetailRunnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 使用SSO授权必须添加如下代码
//        SocialShareAPI.getInstance().ShareSSO(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
                if (result != null) {
                    if (result.equals("success")) {
                        result = "打赏成功";
                        paySuccess = true;
                        if (!mSuccess.isEmpty()) {
                            errorMsg = mSuccess;
                        } else {
                            errorMsg = "谢谢打赏，我们会更加努力的改进！";
                        }
                    } else if (result.equals("fail")) {
                        result = "打赏失败";
                        if (!mFail.isEmpty()) {
                            errorMsg = mFail;
                        } else {
                            errorMsg = "打赏失败了，重新来一次吧～";
                        }
                    } else if (result.equals("cancel")) {
                        result = "打赏取消";
                        if (!mCancel.isEmpty()) {
                            errorMsg = mCancel;
                        } else {
                            errorMsg = "不要这么残忍嘛，你看我们做了这么多,给一点点鼓励吧～";
                        }
                    } else if (result.equals("invalid")) {
                        result = "打赏失效";
                        if (!mInvalid.isEmpty()) {
                            errorMsg = mInvalid;
                        } else {
                            errorMsg = "该支付订单失效，重新尝试一次吧～";
                        }
                    }
                }
                showMsg(result, errorMsg, extraMsg);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rob_pay) {
            payCommodity = "gold_reward";
            new PaymentTask().execute();
        }
    }

    public void postPayReq(final Context context, String commodity) {
        try {
            DscCoreAgent.add(context, new DscRequestBase(Request.Method.POST,
                    URL,
                    new PayProto(context, CHANNEL_WECHAT, commodity),
                    null,
                    new DscCoreListener() {
                        @Override
                        public void onDscResponse(Object response) {
                            onPostExecute((String) response);
                        }

                        @Override
                        public void onDscErrorResponse(VolleyError error) {

                        }
                    }));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void onPostExecute(String response) {
        try {
            JSONObject object = new JSONObject(response);
            int app_key = object.getInt("app_key");
            if (app_key == 0) {
                Toast.makeText(this, "请求出错", Toast.LENGTH_SHORT).show();
            } else {
                String fuli_pay_response = object.getString("fuli_pay_response");
                if (null == fuli_pay_response) {
                    showMsg("请求出错", "请检查URL", "URL无法获取charge");
                    return;
                }
                Intent intent = new Intent();
                String packageName = getPackageName();
                ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
                intent.setComponent(componentName);
                intent.putExtra(PaymentActivity.EXTRA_CHARGE, fuli_pay_response);
                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null != msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null != msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DetailsActivity.this.finish();
                if (paySuccess && !webname.equals("null") && !weburl.equals("null")) {
                    paySuccess = false;
                    Intent intent = WebViewActivity.getIntent(DetailsActivity.this, weburl, webname);
                    startActivity(intent);
                }
            }
        });
        builder.create().show();
    }


    class PaymentTask extends AsyncTask<String, Integer, String> {

        protected void onPreExecute() {
            //TODO
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected String doInBackground(String... params) {
            if (payCommodity != null && !payCommodity.isEmpty()) {
                postPayReq(DetailsActivity.this, payCommodity);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }


    private class RecordAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return packetInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return packetInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if (null == convertView) {
                view = View.inflate(getApplicationContext(), R.layout.rob_record_list_item, null);
                holder = new ViewHolder();
                holder.rob_type = (ImageView) view.findViewById(R.id.rob_type);
                holder.rob_name = (TextView) view.findViewById(R.id.rob_name);
                holder.rob_time = (TextView) view.findViewById(R.id.rob_time);
                holder.rob_size = (TextView) view.findViewById(R.id.rob_size);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            PacketInfo info = packetInfos.get(position);

            holder.rob_type.setImageResource(R.drawable.rob_detail_weixin);
            holder.rob_name.setText(info.getName());
            holder.rob_time.setText(info.getTime());
            holder.rob_size.setText(info.getSize() + "元");

            return view;
        }

        private class ViewHolder {
            ImageView rob_type;
            TextView rob_name;
            TextView rob_time;
            TextView rob_size;
        }
    }


}
