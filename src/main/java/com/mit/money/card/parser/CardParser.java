package com.mit.money.card.parser;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.dsc.core.DscCoreAgent;
import com.google.gson.Gson;
import com.mit.money.activity.HomeFragment;
import com.mit.money.activity.WebViewActivity;
import com.mit.money.card.DialogAgentActivity;
import com.mit.money.card.FuliCardInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


/**
 * Created by hxd on 16-1-21.
 */
public abstract class CardParser {
    private Object tag;
    private static Gson gson;

    protected static DisplayImageOptions sRoundOptions;
    protected static DisplayImageOptions sNormalOptions;

    public CardParser(Object tag) {
        this.tag = tag;
        if (null == sRoundOptions) {
            sRoundOptions = new DisplayImageOptions.Builder()
//                    .showImageOnLoading(R.drawable.ic_load_default)          // 设置图片下载期间显示的图片
//                    .showImageForEmptyUri(R.drawable.ic_load_default)  // 设置图片Uri为空或是错误的时候显示的图片
//                    .showImageOnFail(R.drawable.ic_load_default)       // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                    .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(20))
                    .build(); // 创建配置过得DisplayImageOption对象
        }
        if (null == sNormalOptions) {
            sNormalOptions = new DisplayImageOptions.Builder()
//                    .showImageOnLoading(R.drawable.ic_load_default)          // 设置图片下载期间显示的图片
//                    .showImageForEmptyUri(R.drawable.ic_load_default)  // 设置图片Uri为空或是错误的时候显示的图片
//                    .showImageOnFail(R.drawable.ic_load_default)       // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                    .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build(); // 创建配置过得DisplayImageOption对象
        }
    }

    public boolean launchIntent(Context context, FuliCardInfo cardInfo) {
        boolean success = true;
        Intent intent = null;
        try {
            String action = cardInfo.getAction();
            if (!TextUtils.isEmpty(action) && action.startsWith("http://")) {
                intent = WebViewActivity.getIntent(context, action, "详情");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (!TextUtils.isEmpty(action) && action.indexOf("#Intent;") >= 0) {
                if (null == gson) {
                    gson = new Gson();
                }
                CardSecretInfo secretInfo = gson.fromJson(action.substring(action.indexOf("{"), action.lastIndexOf("}") + 1), CardSecretInfo.class);
                if (null != secretInfo) {
                    //处理粘贴板
                    if (!TextUtils.isEmpty(secretInfo.getClipboard())) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setPrimaryClip(ClipData.newPlainText(null, secretInfo.getClipboard()));
                        } else {
                            android.text.ClipboardManager cmb = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(secretInfo.getClipboard());
                        }
                        Toast.makeText(context, secretInfo.getPastePrompt(), Toast.LENGTH_SHORT).show();
                    } else if (action.indexOf("#Clipboard") >= 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setPrimaryClip(ClipData.newPlainText(null, cardInfo.getDesc()));
                        } else {
                            android.text.ClipboardManager cmb = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(cardInfo.getDesc());
                        }
                        Toast.makeText(context, secretInfo.getPastePrompt(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        //获取服务端传来的intent
                        intent = Intent.parseUri(secretInfo.getIntent(), 0);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        try {
                            //获取包名对应的intent
                            context.getPackageManager().getPackageInfo(secretInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                            intent = context.getPackageManager().getLaunchIntentForPackage(secretInfo.getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (PackageManager.NameNotFoundException e1) {
                            //获取对话框intent
                            intent = DialogAgentActivity.getIntent(context,
                                    secretInfo.getTitle(),
                                    secretInfo.getMsg(),
                                    WebViewActivity.getIntent(context, secretInfo.getApkUrl(), "详情"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    public abstract void bindViewHolder(Context context, HomeFragment.CardViewAdapter.ViewHolder holder);

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
