package com.mit.money.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dsc.core.DscCoreAgent;
import com.android.dsc.core.DscCoreListener;
import com.android.dsc.core.DscRequestBase;
import com.android.dsc.location.DscLocationManager;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mit.fuli.ui.views.PagerSlidingTabStrip;
import com.mit.mitanalytics.UmengAgentFragment;
import com.mit.money.card.FuliCardInfo;
import com.mit.money.card.FuliCardProto;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mit.money.R;
import com.mit.money.card.parser.CardParser;

/**
 * Created by langduan on 16/3/30.
 */
public class HomeFragment extends UmengAgentFragment {
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final Gson sGson = new Gson();
    private boolean mShutdown;

    private List<FuliCardInfo> mCardInfos;      //从网上下载的全部数据，经过分类后保存到下面
    private List<String> mGroupNames;           //分类的名称，以此顺序显示
    private Map<String, List<FuliCardInfo>> mGroupInfos;  //分类内容列表

    private SectionsPagerAdapter mPagerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PagerSlidingTabStrip tabLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    // TODO: Customize parameter initialization
    public static HomeFragment newInstance(int columnCount) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mCardInfos = new ArrayList<FuliCardInfo>(50);
        mGroupNames = new ArrayList<>(10);
        mGroupInfos = new HashMap<>(10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_pager, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshLayout.setProgressViewOffset(true, -20, 100);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        mPagerAdapter = new SectionsPagerAdapter();
         tabLayout = (PagerSlidingTabStrip)view.findViewById(R.id.tabs);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(mPagerAdapter);
        tabLayout.setViewPager(viewPager);
        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        mShutdown = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mShutdown = true;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadData() {
        if ("none".equals(DscCoreAgent.getNetworkType(getActivity()))) {
            Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }

        DscCoreAgent.cancelAll(this.getClass().getSimpleName());

        long currMills = System.currentTimeMillis();

        String location = sGson.toJson(DscLocationManager.getInstance().getLocation(getActivity()));
        Request request = new DscRequestBase(Request.Method.POST,
                "http://www.fuli365.net/app_interface/app_main_interface.php",
                new FuliCardProto(getActivity(), currMills, currMills, location),
                null,
                new OnlineCardListener())
                .setTag(this.getClass().getSimpleName());
        DscCoreAgent.add(getActivity(), request);
    }

    class OnlineCardListener extends DscCoreListener<String> {
        @Override
        public void onDscResponse(String response) {
            if (mShutdown) {
                return;
            }
            List<FuliCardInfo> cardInfos = new ArrayList<>();
            try {
                JSONObject json = new JSONObject(response).getJSONObject("fuli_card_info");
                int success = json.getInt("success");
                if (1 == success) {
                    String data = json.getString("data");
                    data = data.replace("fl_card_id", "id");
                    data = data.replace("fl_card_datetime", "millis");
                    data = data.replace("fl_card_icon_url", "iconUrl");
                    data = data.replace("fl_card_content_title", "title");
                    data = data.replace("fl_card_content_data", "desc");
                    data = data.replace("fl_card_content_url", "action");
                    data = data.replace("fl_card_content_type", "type");
                    data = data.replace("fl_card_content_group", "group");
                    data = data.replace("fuli_card_parser", "parser");
                    cardInfos = sGson.fromJson(data,
                            new TypeToken<List<FuliCardInfo>>() {
                            }.getType());
                    Iterator<FuliCardInfo> it = cardInfos.iterator();
                    while (it.hasNext()) {
                        FuliCardInfo cardInfo = it.next();
                        if (null == cardInfo.getParserInstance()) {
                            it.remove();
                            cardInfos.remove(cardInfo);
                        }
                    }
                }
            } catch (Exception e) {

            }
            if (null != cardInfos && cardInfos.size() > 0) {
                mCardInfos.clear();
                mCardInfos.addAll(cardInfos);
            }
            mGroupNames.clear();
            for (String key : mGroupInfos.keySet()) {
                mGroupInfos.get(key).clear();
            }
            List<String> urls = new ArrayList<>(10);
            for (FuliCardInfo info : mCardInfos) {
                if (FuliCardInfo.CARD_TYPE_BANNER.equals(info.getType())) {
                    urls.add(info.getIconUrl());
                    if (mGroupInfos.containsKey(info.getType())) {
                        mGroupInfos.get(info.getType()).add(info);
                    } else {
                        List<FuliCardInfo> list = new ArrayList<>(50);
                        list.add(info);
                        mGroupInfos.put(info.getType(), list);
                    }
                } else if (FuliCardInfo.CARD_TYPE_RECOMMEND.equals(info.getType())) {
                    if (mGroupInfos.containsKey(info.getType())) {
                        mGroupInfos.get(info.getType()).add(info);
                    } else {
                        List<FuliCardInfo> list = new ArrayList<>(50);
                        list.add(info);
                        mGroupInfos.put(info.getType(), list);
                    }
                } else {
                    if (mGroupInfos.containsKey(info.getGroup())) {
                        List<FuliCardInfo> list = mGroupInfos.get(info.getGroup());
                        if (list.size() == 0) {
                            mGroupNames.add(info.getGroup());
                        }
                        mGroupInfos.get(info.getGroup()).add(info);
                    } else {
                        List<FuliCardInfo> list = new ArrayList<>(50);
                        list.add(info);
                        mGroupInfos.put(info.getGroup(), list);
                        mGroupNames.add(info.getGroup());
                    }
                }
            }

            mPagerAdapter.notifyDataSetChanged();
            tabLayout.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onDscErrorResponse(VolleyError volleyError) {
            if (mShutdown) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "数据读取错误，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    class SectionsPagerAdapter extends PagerAdapter {
        private static final String TAG = "homepage_adapter";
        private int mChildCount = 0;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home_list, null);
            ListView recyclerView = (ListView) view.findViewById(R.id.list);
            recyclerView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (0 == firstVisibleItem){
                        mSwipeRefreshLayout.setEnabled(true);
                    }else{
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                }
            });
            Context context = recyclerView.getContext();
            recyclerView.setAdapter(new CardViewAdapter(context, mGroupInfos.get(getPageTitle(position)), mListener));
            container.addView(view);
            Log.d("SectionsPagerAdapter", "instantiateItem(" + position + "," + view + ")");
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d("SectionsPagerAdapter", "destroyItem(" + position + "," + object + ")");
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return (null != mGroupNames) ? mGroupNames.size() : 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (null != mGroupNames) {
                return mGroupNames.get(position);
            } else {
                return "";
            }
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            Log.d("SectionsPagerAdapter", "notifyDataSetChanged(" + mChildCount + ")");
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            Log.d("SectionsPagerAdapter", "getItemPosition(" + mChildCount + "," + object + ")");
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FuliCardInfo item);
    }

    public class CardViewAdapter extends BaseAdapter {

        private Context mContext;
        private final List<FuliCardInfo> mValues;
        private final OnListFragmentInteractionListener mListener;

        public CardViewAdapter(Context context, List<FuliCardInfo> items, OnListFragmentInteractionListener listener) {
            mContext = context;
            mValues = items;
            mListener = listener;
        }

        @Override
        public int getCount() {
            return mValues.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (convertView == null) {
                view = View.inflate(mContext, R.layout.fragment_home_item, null);
                holder = new ViewHolder(view);
                holder.mCardView = (View) view.findViewById(R.id.item_card);
                holder.mCardContentView = (TextView) view.findViewById(R.id.item_card_content);
                holder.mCardIconView = (ImageView) view.findViewById(R.id.item_card_icon);

                holder.mListView = (View) view.findViewById(R.id.item_list);
                holder.mListTitleView = (TextView) view.findViewById(R.id.item_list_title);
                holder.mListContentView = (TextView) view.findViewById(R.id.item_list_content);
                holder.mListIconView = (ImageView) view.findViewById(R.id.item_list_icon);
                view.setTag(holder);
            } else {
                view = convertView;
            }
            final FuliCardInfo cardInfo = mValues.get(position);
            holder = (ViewHolder) view.getTag();
            holder.mItem = mValues.get(position);
            CardParser cardParser = cardInfo.getParserInstance();
            if (null != cardParser) {
                cardParser.bindViewHolder(mContext, holder);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(cardInfo);
                    }
                }
            });
            return view;
        }

        public class ViewHolder {
            public View mView;
            public View mCardView;
            public TextView mCardContentView;
            public ImageView mCardIconView;
            public View mListView;
            public TextView mListTitleView;
            public TextView mListContentView;
            public ImageView mListIconView;

            public FuliCardInfo mItem;

            public ViewHolder(View view) {
                mView = view;
                mCardView = view.findViewById(R.id.item_card);
                mCardContentView = (TextView) view.findViewById(R.id.item_card_content);
                mCardIconView = (ImageView) view.findViewById(R.id.item_card_icon);

                mListView = view.findViewById(R.id.item_list);
                mListTitleView = (TextView) view.findViewById(R.id.item_list_title);
                mListContentView = (TextView) view.findViewById(R.id.item_list_content);
                mListIconView = (ImageView) view.findViewById(R.id.item_list_icon);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mCardContentView.getText() + "'";
            }
        }
    }
}
