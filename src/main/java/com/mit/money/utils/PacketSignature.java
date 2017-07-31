package com.mit.money.utils;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.SimpleDateFormat;

/**
 * Created by android on 2/26/16.
 */
public class PacketSignature {
    public String sender;
    public String content;
    public String time;
    public String contentDescription = "";
    public String replyString;
    public boolean others;

    public void cleanSignature() {
        this.sender = "";
        this.content = "";
        this.time = "";
    }

    public String getContentDescription() {
        return this.contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    private String getSignature(String... strings) {
        String signature = "";
        for (String str : strings) {
            if (str == null) return null;
            signature += str + "|";
        }
        return signature.substring(0, signature.length() - 1);
    }

    @Override
    public String toString() {
        return this.getSignature(this.sender, this.time, this.content);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean generateSignature(AccessibilityNodeInfo nodeInfo, String type) {
        try {
            if ("wechat".equals(type)) {
                AccessibilityNodeInfo packetNode = nodeInfo.getParent();
                if (!"android.widget.LinearLayout".equals(packetNode.getClassName()))
                    return false;
                String packetState = packetNode.getChild(1).getText().toString();
                if ("查看红包".equals(packetState)) return false;
                String packetContent = packetNode.getChild(0).getText().toString();

                AccessibilityNodeInfo messageNode = packetNode.getParent();

                Rect bounds = new Rect();
                messageNode.getBoundsInScreen(bounds);
                if (bounds.top < 0) return false;

                String[] packetInfo = getSenderContentDescriptionFromNode(messageNode, type);
                if (this.toString().equals(this.getSignature(packetInfo[0], packetInfo[1], packetContent)))
                    return false;

                this.sender = packetInfo[0];
                this.time = packetInfo[1];
                this.content = packetContent;
                return true;
            } else if ("qq".equals(type)) {
                AccessibilityNodeInfo packetNode = nodeInfo.getParent();
                if (!"android.widget.RelativeLayout".equals(packetNode.getClassName()))
                    return false;
                String packetState = packetNode.getChild(1).getText().toString();
                if ("已拆开".equals(packetState)) return false;
                String packetContent = packetNode.getChild(0).getText().toString();

                AccessibilityNodeInfo messageNode = packetNode.getParent();

                Rect bounds = new Rect();
                messageNode.getBoundsInScreen(bounds);
                if (bounds.top < 0) return false;

                String[] packetInfo = getSenderContentDescriptionFromNode(messageNode, type);
                if (this.toString().equals(this.getSignature(packetInfo[0], packetState, packetInfo[1])))
                    return false;

                this.sender = packetInfo[0];
                this.time = packetInfo[1];
                this.content = packetContent;
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private String[] getSenderContentDescriptionFromNode(AccessibilityNodeInfo node, String type) {
        int count = node.getChildCount();
        String[] result = {"unknownSender", "unknownTime"};
        if ("wechat".equals(type)) {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo info = node.getChild(i);
                if ("android.widget.ImageView".equals(info.getClassName()) && "unknownSender".equals(result[0])) {
                    CharSequence contentDescription = info.getContentDescription();
                    if (contentDescription != null)
                        result[0] = contentDescription.toString().replaceAll("头像$", "");
                } else if ("android.widget.TextView".equals(info.getClassName()) && "unknownTime".equals(result[1])) {
                    CharSequence nodeText = info.getText();
                    if (nodeText != null)
                        result[1] = nodeText.toString();
                }
            }
        } else if ("qq".equals(type)) {
            if ("unknownTime".equals(result[1])) {
                result[1] = getCurrentTime();
            }
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo info = node.getChild(i);
                if ("android.widget.TextView".equals(info.getClassName()) && "unknownSender".equals(result[0])) {
                    CharSequence nodeText = info.getText();
                    if (nodeText != null)
                        result[0] = nodeText.toString();
                }
            }
        }
        return result;
    }

    private String getCurrentTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm");
        return sDateFormat.format(new java.util.Date());
    }
}
