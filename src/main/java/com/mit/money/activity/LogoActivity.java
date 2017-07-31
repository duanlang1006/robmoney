package com.mit.money.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.android.dsc.core.DscCoreAgent;
import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.utils.SpUtil;
import com.mit.money.view.ImageCycleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by android on 3/2/16.
 */
public class LogoActivity extends UmengAgentActivity {
    private ImageCycleView mCycleView;
    private ImageView imageView;

    private ArrayList<String> mImageUrl = null;

    private String imageUrl1 = "";
    private String imageUrl2 = "";
    private String imageUrl3 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_logo);
        initViews();
        if (!getFirstFlag()) {
            SpUtil.setValue(this, SpUtil.KEY_FIRST_ENTER_IN, true);
            mCycleView.setVisibility(View.VISIBLE);
//            imageView.setVisibility(View.GONE);
            initImageResource();
            mCycleView.setImageResources(mImageUrl, mCycleViewListener);
        } else {
            mCycleView.setVisibility(View.GONE);
//            imageView.setVisibility(View.VISIBLE);
//            imageView.setImageResource(R.drawable.logo);
        }
        DscCoreAgent.initImageLoader(this.getApplicationContext());
    }

    private void initViews() {
        mCycleView = (ImageCycleView) this.findViewById(R.id.imageCycleView);
        imageView = (ImageView) this.findViewById(R.id.imageView);
    }

    private boolean getFirstFlag() {
        return (boolean) SpUtil.getValue(this, SpUtil.KEY_FIRST_ENTER_IN, false);
    }

    private void initImageResource() {
        mImageUrl = new ArrayList<>();
        mImageUrl.add(imageUrl1);
        mImageUrl.add(imageUrl2);
        mImageUrl.add(imageUrl3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCycleView.startImageCycle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCycleView.stopImageCycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ImageCycleView.ImageCycleViewListener mCycleViewListener = new ImageCycleView.ImageCycleViewListener() {
        @Override
        public void displayImage(String imageURL, ImageView imageView) {
            ImageLoader.getInstance().displayImage(imageURL,imageView);
//            DscCoreAgent.loadImage(getApplicationContext(),
//                    imageURL,
//                    new DscCoreAgent.DscCoreImageListener(imageView,
//                            0,
//                            0));
        }

        @Override
        public void onImageClick(int position, View imageView) {
        }
    };
}
