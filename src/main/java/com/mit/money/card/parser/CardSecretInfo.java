package com.mit.money.card.parser;

/**
 * Created by huangxiaodong on 16/3/29.
 */
public class CardSecretInfo {
    private String clipboard;
    private String pastePrompt;
    private String intent;
    private String packageName;
    private String title;
    private String msg;
    private String apkUrl;

    public void setClipboard(String clipboard) {
        this.clipboard = clipboard;
    }

    public void setPastePrompt(String pastePrompt) {
        this.pastePrompt = pastePrompt;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getClipboard() {
        return clipboard;
    }

    public String getPastePrompt() {
        return pastePrompt;
    }

    public String getIntent() {
        return intent;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public String getMsg() {
        return msg;
    }

    public String getApkUrl() {
        return apkUrl;
    }
}
