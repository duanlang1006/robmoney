package com.mit.money.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mit.money.R;
import com.mit.money.activity.RobMainActivity;

import java.util.ArrayList;

/**
 * Created by android on 3/3/16.
 */
public class ImageCycleView extends LinearLayout {

    private Context mContext;
    private ViewPager mViewPager = null;
    private ViewGroup mGroup;
    private ImageView[] mImageViews = null;
    private int mImageIndex = 0;
    private float mScale;
    private RelativeLayout mViewGroup;

    private boolean interruptFlag = false;  //中断自动翻页

    private Handler mHandler = new Handler();

    public ImageCycleView(Context context) {
        super(context);
    }

    public ImageCycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.cycle_view, this);
        mViewGroup = (RelativeLayout) findViewById(R.id.cycleView);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 开始图片滚动
                        startImageTimerTask();
                        break;
                    default:
                        // 停止图片滚动
                        stopImageTimerTask();
                        break;
                }
                return false;
            }
        });
        // 滚动图片右下指示器视图
        mGroup = (ViewGroup) findViewById(R.id.viewGroup);
    }

    /**
     * 装填图片数据
     */
    public void setImageResources(ArrayList<String> imageUrlList, ImageCycleViewListener imageCycleViewListener) {
        // 清除所有子视图
        mGroup.removeAllViews();
        // 图片广告数量
        final int imageCount = imageUrlList.size();
        mImageViews = new ImageView[imageCount];
        for (int i = 0; i < imageCount; i++) {
            ImageView mImageView = new ImageView(mContext);
            int imageParams = (int) (mScale * 20 + 0.5f);// XP与DP转换，适应不同分辨率
            int imagePadding = (int) (mScale * 5 + 0.5f);
            mImageView.setLayoutParams(new LayoutParams(imageParams, imageParams));
            mImageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
            mImageViews[i] = mImageView;
            if (i == 0) {
                mImageViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
            } else {
                mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
            }
            mGroup.addView(mImageViews[i]);
        }
        ImageCycleAdapter mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList, imageCycleViewListener);
        mViewPager.setAdapter(mAdvAdapter);
        startImageTimerTask();
    }

    /**
     * 开始轮播(手动控制自动轮播与否，便于资源控制)
     */
    public void startImageCycle() {
        startImageTimerTask();
    }

    /**
     * 暂停轮播——用于节省资源
     */
    public void stopImageCycle() {
        stopImageTimerTask();
    }

    /**
     * 开始图片滚动任务
     */
    private void startImageTimerTask() {
        stopImageTimerTask();
        // 图片每5秒滚动一次
        mHandler.postDelayed(mImageTimerTask, 2000);
    }

    /**
     * 停止图片滚动任务
     */
    private void stopImageTimerTask() {
        mHandler.removeCallbacks(mImageTimerTask);
    }

    /**
     * 图片自动轮播Task
     */
    private Runnable mImageTimerTask = new Runnable() {
        @Override
        public void run() {
            if (mImageViews != null && !interruptFlag) {
                // 下标等于图片列表长度说明已滚动到最后一张图片,重置下标
                if (++mImageIndex > mImageViews.length) {
                    mImageIndex = mImageViews.length - 1;
                }
            }
            mViewPager.setCurrentItem(mImageIndex);
        }
    };

    /**
     * 轮播图片状态监听器
     */
    private final class PageChangeListener implements ViewPager.OnPageChangeListener {
        private Button leftBtn;
        private TimeCount time;
        private View btnView;

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING)
                interruptFlag = true;
            if (state == ViewPager.SCROLL_STATE_IDLE)
                startImageTimerTask(); // 开始下次计时
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //最后一张图，加载按键布局
            if (position == mImageViews.length - 1) {
                btnView = LayoutInflater.from(mContext).inflate(R.layout.logo_enter_btn, mViewGroup);
                leftBtn = (Button) mViewGroup.findViewById(R.id.left_btn);
                leftBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, RobMainActivity.class);
                        mContext.startActivity(intent);
                    }
                });
                Button rightBtn = (Button) mViewGroup.findViewById(R.id.right_btn);
                rightBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
                if (!interruptFlag) {
                    time = new TimeCount(3000, 1000);
                    time.start();
                }
            } else {
                if (btnView != null) {
                    mViewGroup.removeView(btnView);
                    btnView = null;
                    invalidate();
                }
                if (time != null)
                    time.cancel();
            }
        }

        @Override
        public void onPageSelected(int index) {
            // 设置当前显示的图片下标
            mImageIndex = index;
            // 设置图片滚动指示器背景
            mImageViews[index].setBackgroundResource(R.drawable.banner_dian_focus);
            for (int i = 0; i < mImageViews.length; i++) {
                if (index != i) {
                    mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
                }
            }
        }

        class TimeCount extends CountDownTimer {
            public TimeCount(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
            }

            @Override
            public void onFinish() {//计时完毕时触发
                Intent intent = new Intent(mContext, RobMainActivity.class);
                mContext.startActivity(intent);
            }

            @Override
            public void onTick(long millisUntilFinished) {//计时过程显示
                leftBtn.setText(millisUntilFinished / 1000 + "秒" + "| 跳过");
            }
        }

    }

    private class ImageCycleAdapter extends PagerAdapter {
        private Context mContext;
        //图片视图缓存列表
        private ArrayList<ImageView> mImageViewCacheList;
        //图片资源列表
        private ArrayList<String> mImageList = new ArrayList<>();
        //图片点击监听器
        private ImageCycleViewListener mImageCycleViewListener;

        public ImageCycleAdapter(Context context, ArrayList<String> list, ImageCycleViewListener imageCycleViewListener) {
            mContext = context;
            mImageList = list;
            mImageCycleViewListener = imageCycleViewListener;
            mImageViewCacheList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            String imageUrl = mImageList.get(position);
            ImageView imageView;
            if (mImageViewCacheList.isEmpty()) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            } else {
                imageView = mImageViewCacheList.remove(0);
            }
            // 设置图片点击监听
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImageCycleViewListener.onImageClick(position, v);
                }
            });
            imageView.setTag(imageUrl);
            container.addView(imageView);
            mImageCycleViewListener.displayImage(imageUrl, imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView view = (ImageView) object;
            container.removeView(view);
            mImageViewCacheList.add(view);
        }

    }

    /**
     * 轮播控件的监听事件
     */
    public interface ImageCycleViewListener {

        /**
         * 加载图片资源
         */
        void displayImage(String imageURL, ImageView imageView);

        /**
         * 单击图片事件
         */
        void onImageClick(int position, View imageView);
    }


}
