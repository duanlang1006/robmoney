package com.mit.money.card;

import com.mit.money.card.parser.CardParser;
import com.mit.money.card.parser.CardParserFactory;

/**
 * Created by hxd on 16-1-19.
 */
public class FuliCardInfo {
//    数据格式如下：
//    {"card":{2,"主标题","动作(编辑，跳转)",[
//                {"url1","记事一","描述一","2016-1-10 10:00","跳转到url1"},
//                {"url2","记事2","描述2","2016-1-10 10:00","跳转到url2"},
//                {"url3","记事3","描述3","2016-1-10 10:00","跳转到url3"}]}}
    public static final String CARD_TYPE_BANNER = "banner";
    public static final String CARD_TYPE_LIST = "list";
    public static final String CARD_TYPE_CARD = "card";
    public static final String CARD_TYPE_RECOMMEND = "recommend";

    private long id;
    private String type;
    private long millis;      //时间
    private String iconUrl;   //网络url和本地资源都用此表示
    private String title;
    private String desc;      //描述
    private String action;    //点击后的动作,网络url或者intent
    private String group;     //用于显示卡片的title：一周日程，今日红包，...
    private String parser;    //指定谁来解析
    private CardParser parserInstance;       //本地处理用，CardParser实例

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    public CardParser getParserInstance() {
        if (null == parserInstance){
            parserInstance = CardParserFactory.createCardParser(parser, null);
        }
        return parserInstance;
    }

//    public FuliCardInfo(Parcel in) {
//        s_key = in.readString();
//        s_name = in.readString();
//        s_datatype = in.readString();
//        step = in.readInt();
//        specialtopic_data = in.readArrayList(SpecialTopicData.class.getClassLoader());
//        data = in.readArrayList(HomePageApkData.class.getClassLoader());
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }
//
//    public static final Parcelable.Creator<SubjectData> CREATOR = new Parcelable.Creator<SubjectData>() {
//        @Override
//        public SubjectData createFromParcel(Parcel in) {
//            return new SubjectData(in);
//        }
//
//        @Override
//        public SubjectData[] newArray(int size) {
//            return new SubjectData[size];
//        }
//    };
}