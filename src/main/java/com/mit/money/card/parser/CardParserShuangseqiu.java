package com.mit.money.card.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.android.dsc.core.DscCoreAgent;
import com.mit.money.activity.HomeFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by hxd on 16-1-21.
 */
public class CardParserShuangseqiu extends CardParser {
    public CardParserShuangseqiu(Object tag) {
        super(tag);
    }

    @Override
    public void bindViewHolder(Context context, HomeFragment.CardViewAdapter.ViewHolder holder) {
        if (holder instanceof HomeFragment.CardViewAdapter.ViewHolder) {
            HomeFragment.CardViewAdapter.ViewHolder viewHolder = (HomeFragment.CardViewAdapter.ViewHolder) holder;
            viewHolder.mCardView.setVisibility(View.GONE);
            viewHolder.mListView.setVisibility(View.VISIBLE);

            DscCoreAgent.initImageLoader(context.getApplicationContext());
            ImageLoader.getInstance().displayImage(viewHolder.mItem.getIconUrl(),viewHolder.mListIconView,sNormalOptions);

            String title = viewHolder.mItem.getTitle();

            SpannableString sps = new SpannableString(title);

            int start = title.indexOf("双色球");
            int end = start + 3;
            if (start >= 0 && end < title.length()) {
                sps.setSpan(new ForegroundColorSpan(Color.RED),
                        start, start + 3,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            start = title.indexOf("第");
            end = title.indexOf("期");
            if (start >= 0 && end >= 0 && start < title.length() - 1) {
                sps.setSpan(new ForegroundColorSpan(Color.RED),
                        start + 1, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            viewHolder.mListTitleView.setText(sps);

            BallInfo[] ballInfos = BallInfo.getBallInfo(viewHolder.mItem.getDesc(), " ");
            if (null != ballInfos) {
                final int radius = 30;
                sps = new SpannableString(BallInfo.sBallDesc);
                for (int i = 0; i < ballInfos.length; i++) {
                    BallInfo ball = ballInfos[i];
                    Bitmap bmp = createBallBitmap(2 * radius, 2 * radius, ball.number, ball.color);
                    ImageSpan span = new ImageSpan(context, bmp, ImageSpan.ALIGN_BOTTOM);
                    sps.setSpan(span, ball.startIndex, ball.endIndex, Spanned.SPAN_COMPOSING);
                }
            }
            viewHolder.mListContentView.setText(sps);
        }
    }

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
//    public void setItemView(final Context context, FuliCardInfo cardInfo, CardRecyclerViewAdapter.ViewHolder holder) {
//        DscCoreAgent.loadImage(context,
//                cardInfo.getIconUrl(),
//                new DscCoreAgent.DscCoreImageListener(holder.mCardIconView,
//                        R.drawable.ic_load_default,
//                        R.drawable.ic_load_default));
//
//        String title = cardInfo.getTitle();
//        SpannableString sps = new SpannableString(title);
//        int start = title.indexOf("双色球");
//        int end = start + 3;
//        if (start >= 0 && end < title.length()) {
//            sps.setSpan(new ForegroundColorSpan(Color.RED),
//                    start, start + 3,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        start = title.indexOf("第");
//        end = title.indexOf("期");
//        if (start >= 0 && end >= 0 && start < title.length() - 1) {
//            sps.setSpan(new ForegroundColorSpan(Color.RED),
//                    start + 1, end,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//        holder.mCardContentView.setText(sps);
//        holder.getEdit().setVisibility(View.GONE);
//
//
//        BallInfo[] ballInfos = BallInfo.getBallInfo(cardInfo.getDesc(), " ");
//        if (null == ballInfos) {
//            holder.getDesc().setText("");
//        } else {
//            final int radius = 30;
//            sps = new SpannableString(BallInfo.sBallDesc);
//            for (int i = 0; i < ballInfos.length; i++) {
//                BallInfo ball = ballInfos[i];
//                Bitmap bmp = createBallBitmap(2 * radius, 2 * radius, ball.number, ball.color);
//                ImageSpan span = new ImageSpan(context, bmp, ImageSpan.ALIGN_BOTTOM);
//                sps.setSpan(span, ball.startIndex, ball.endIndex, Spanned.SPAN_COMPOSING);
//            }
//            holder.getDesc().setText(sps);
//        }
//    }

    static class BallInfo {
        static String sBallDesc;
        String number;
        int startIndex;  //号码开始位置
        int endIndex;    //号码结束位置
        int color;  //号码颜色

        static BallInfo[] getBallInfo(String desc, String joinStr) {
            if (TextUtils.isEmpty(desc)) {
                return null;
            }
            desc = desc.trim();
            desc = desc.replace(",", " ");
            desc = desc.replace("+", " ");
            String[] splitNums = desc.split(" ");
            if (null == splitNums || splitNums.length != 7) {
                return null;
            }
            if (null == joinStr) {
                joinStr = " ";
            }
            StringBuffer strBuf = new StringBuffer();

            BallInfo[] ballArray = new BallInfo[splitNums.length];
            for (int i = 0; i < splitNums.length; i++) {
                BallInfo ball = new BallInfo();
                ball.number = splitNums[i];
                ball.startIndex = strBuf.length();
                strBuf.append(splitNums[i]);
                ball.endIndex = strBuf.length();
                if ((i + 1) == splitNums.length) {
                    ball.color = Color.BLUE;
                } else {
                    strBuf.append(joinStr);
                    ball.color = Color.RED;
                }
                ballArray[i] = ball;
            }
            sBallDesc = strBuf.toString();
            return ballArray;
        }
    }

    private static Bitmap createBallBitmap(int width, int height, String number, int color) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        Bitmap newbmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newbmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawCircle(width / 2, height / 2, Math.min(width, height) / 2, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(width * 2 / 3);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float baseY = (height - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(number, width / 2, baseY, paint);
        return newbmp;
    }
}
