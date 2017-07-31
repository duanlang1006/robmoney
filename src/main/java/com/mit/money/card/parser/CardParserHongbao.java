package com.mit.money.card.parser;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.android.dsc.core.DscCoreAgent;
import com.mit.money.activity.HomeFragment;
import com.mit.money.card.FuliCardInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by hxd on 16-1-21.
 */
public class CardParserHongbao extends CardParser {
    private static int screenWidth,screenHeight;

    public CardParserHongbao(Object tag) {
        super(tag);
    }

    @Override
    public void bindViewHolder(Context context, HomeFragment.CardViewAdapter.ViewHolder holder) {
        if (screenHeight == 0 || screenWidth == 0) {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }
        DscCoreAgent.initImageLoader(context.getApplicationContext());

        if (holder instanceof HomeFragment.CardViewAdapter.ViewHolder) {
            HomeFragment.CardViewAdapter.ViewHolder viewHolder = (HomeFragment.CardViewAdapter.ViewHolder) holder;
            if (FuliCardInfo.CARD_TYPE_CARD.equals(viewHolder.mItem.getType())) {
                viewHolder.mCardView.setVisibility(View.VISIBLE);
                viewHolder.mListView.setVisibility(View.GONE);

                ImageLoader.getInstance().displayImage(viewHolder.mItem.getIconUrl(),viewHolder.mCardIconView,sNormalOptions);
                viewHolder.mCardContentView.setText(viewHolder.mItem.getDesc());
            } else if (FuliCardInfo.CARD_TYPE_LIST.equals(viewHolder.mItem.getType())) {
                viewHolder.mCardView.setVisibility(View.GONE);
                viewHolder.mListView.setVisibility(View.VISIBLE);

                ImageLoader.getInstance().displayImage(viewHolder.mItem.getIconUrl(),viewHolder.mListIconView,sRoundOptions);
                viewHolder.mListTitleView.setText(viewHolder.mItem.getTitle());
                viewHolder.mListContentView.setText(viewHolder.mItem.getDesc());
            }
        }
    }

//    public static class CardImageListener implements ImageLoader.ImageListener {
//        private int defaultImageResId;
//        private int errorImageResId;
//        private ImageView imageView;
//
//        public CardImageListener(ImageView view, int defaultImageResId, int errorImageResId) {
//            this.imageView = view;
//            this.defaultImageResId = defaultImageResId;
//            this.errorImageResId = errorImageResId;
//            imageView.setImageResource(defaultImageResId);
//        }
//
//        @Override
//        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
//            Bitmap bmp = response.getBitmap();
//            if (bmp != null) {
//
////                ViewGroup.LayoutParams lp = imageView.getLayoutParams();
////                lp.width =ViewGroup.LayoutParams.WRAP_CONTENT;
////                lp.height = ;
////                imageView.setLayoutParams(lp);
//
//                imageView.setMaxWidth(screenWidth);
//                imageView.setMaxHeight(screenWidth );
////                if (bmp.getWidth() > 0 && bmp.getHeight() > 0 && imageView.getWidth() > 0) {
////                    int height = bmp.getHeight() / bmp.getWidth() * imageView.getWidth();
////                    if (height > 0) {
////                        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
////                    }
////                }
//                imageView.setImageBitmap(bmp);
//            } else if (defaultImageResId != 0) {
//                imageView.setImageResource(defaultImageResId);
//            }
//        }
//
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            if (errorImageResId != 0) {
//                imageView.setImageResource(errorImageResId);
//            }
//        }
//    }

//    @Override
//    public void setHeaderView(Context context, FuliCardInfo cardInfo, CardRecyclerViewAdapter.ViewHolder holder) {
//        if (TextUtils.isEmpty(cardInfo.getGroup())) {
//            holder.getText().setText("");
//            holder.getText().setVisibility(View.GONE);
//        } else {
//            holder.getText().setText(cardInfo.getGroup());
//            holder.getText().setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public void setItemView(Context context, FuliCardInfo cardInfo, CardRecyclerViewAdapter.ViewHolder holder) {
//        DscCoreAgent.loadImage(context,
//                cardInfo.getIconUrl(),
//                new DscCoreAgent.DscCoreImageListener(holder.mCardIconView,
//                        R.drawable.ic_load_default,
//                        R.drawable.ic_load_default));
//        holder.mCardContentView.setText(cardInfo.getTitle());
//        holder.getDesc().setText(cardInfo.getDesc());
//        holder.getEdit().setVisibility(View.GONE);
//    }
}
