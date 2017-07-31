package com.tencent.mm;

import com.mit.state.Task;

/**
 * Created by hxd on 16-3-8.
 */
public class MMTask extends Task {
    private double money;
    private boolean startFromList;
    private boolean firstEnterChat;
    private boolean firstEnterPacket;
    private boolean firstEnterDetail;
    private boolean unPacketSuccess;
    private boolean saveDataSuccess;
    private boolean autoInFlag;

    private String sender;
    private String content;
    private String time;
    private String contentDescription;
    private String replyString;
    private boolean others;

    private int count;

    public MMTask(String nextState, String finishState) {
        super(nextState, finishState);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public boolean isAutoInFlag() {
        return autoInFlag;
    }

    public void setAutoInFlag(boolean autoInFlag) {
        this.autoInFlag = autoInFlag;
    }

    public boolean isFirstEnterChat() {
        return firstEnterChat;
    }

    public void setFirstEnterChat(boolean value) {
        this.firstEnterChat = value;
    }

    public boolean isFirstEnterPacket() {
        return firstEnterPacket;
    }

    public void setFirstEnterPacket(boolean value) {
        this.firstEnterPacket = value;
    }

    public boolean isFirstEnterDetail() {
        return firstEnterDetail;
    }

    public void setFirstEnterDetail(boolean firstEnterDetail) {
        this.firstEnterDetail = firstEnterDetail;
    }

    public boolean isUnPacketSuccess() {
        return unPacketSuccess;
    }

    public void setUnPacketSuccess(boolean value) {
        this.unPacketSuccess = value;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public String getReplyString() {
        return replyString;
    }

    public void setReplyString(String replyString) {
        this.replyString = replyString;
    }

    public boolean isOthers() {
        return others;
    }

    public void setOthers(boolean others) {
        this.others = others;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isStartFromList() {
        return startFromList;
    }

    public void setStartFromList(boolean startFromList) {
        this.startFromList = startFromList;
    }

    public boolean isSaveDataSuccess() {
        return saveDataSuccess;
    }

    public void setSaveDataSuccess(boolean saveDataSuccess) {
        this.saveDataSuccess = saveDataSuccess;
    }
}
