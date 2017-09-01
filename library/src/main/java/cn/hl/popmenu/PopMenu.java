package cn.hl.popmenu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * PopMenu
 */
public final class PopMenu {

    private final int mScreenWidthPixels;
    private int mGravity;

    private final PopupWindow mPopupWindow;
    private final int mScreenHeightPixels;

    private LinearLayout mContentView;
    private ImageView mArrowView;

    private View mAnchorView;

    private int mBackgroundColor;
    private RecyclerView mRecyclerView;

    private PopMenu.OnItemClickListener mItemClickListener;
    private CommonAdapter<ActionItem> mAdapter;
    private List<ActionItem> mData = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClickListener(ActionItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mItemClickListener = onItemClickListener;
    }

    public void addData(List<ActionItem> data) {
        mData.addAll(data);
    }

    public void addData(ActionItem item) {
        mData.add(item);
    }

    public void clearData() {
        mData.clear();
    }

    private PopMenu(Builder builder) {
        DisplayMetrics dm = builder.mContext.getResources().getDisplayMetrics();
        mScreenHeightPixels = dm.heightPixels;
        mScreenWidthPixels = dm.widthPixels;
        mPopupWindow = new PopupWindow(builder.mContext);
        mPopupWindow.setBackgroundDrawable(null);
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setContentView(getContentView(builder));
        mPopupWindow.setFocusable(true);//让popupwindow获取焦点
        mPopupWindow.setOutsideTouchable(builder.isCancelable);
    }

    private View getContentView(final Builder builder) {
        GradientDrawable drawable = new GradientDrawable();
        mBackgroundColor = builder.mBackgroundColor;
        drawable.setColor(mBackgroundColor);
        drawable.setCornerRadius(builder.mCornerRadius);

        mRecyclerView = new RecyclerView(builder.mContext);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(builder.mContext));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(builder.mContext)
                .size(builder.mLineSize)
                .color(builder.mLineColor)
                .build());
        mData.addAll(builder.mData);
        mAdapter = new CommonAdapter<ActionItem>(builder.mContext,
                R.layout.list_item_popmenu, mData) {

            @Override
            protected void convert(final ViewHolder holder, final ActionItem item, final int position) {
                holder.setText(R.id.tv_pop_menu, item.getText());
                holder.setTextColor(R.id.tv_pop_menu, builder.mTextColor);
                ImageView iv = holder.getView(R.id.iv_pop_menu);
                if (builder.mIsShowImage) {
                    if (item.getResId() != 0) {
                        iv.setImageResource(item.getResId());
                        iv.setVisibility(View.VISIBLE);
                    } else {
                        iv.setVisibility(View.INVISIBLE);
                    }
                } else {
                    iv.setVisibility(View.GONE);
                }
                if (mItemClickListener == null)
                    return;
                holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemClickListener.onItemClickListener(item, position);
                        dismiss();
                    }
                });
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRecyclerView.setBackground(drawable);
        } else {
            //noinspection deprecation
            mRecyclerView.setBackgroundDrawable(drawable);
        }

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        textViewParams.gravity = Gravity.CENTER;
        int cornerRadius = (int) builder.mCornerRadius;
        mRecyclerView.setPadding(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
        mRecyclerView.setLayoutParams(textViewParams);

        mArrowView = new ImageView(builder.mContext);

        LinearLayout.LayoutParams arrowLayoutParams = new LinearLayout.LayoutParams((int) builder.mArrowWidth, (int) builder.mArrowHeight, 0);

        arrowLayoutParams.gravity = Gravity.CENTER;
        mArrowView.setLayoutParams(arrowLayoutParams);

        mContentView = new LinearLayout(builder.mContext);
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mContentView.setOrientation(LinearLayout.VERTICAL);

        mContentView.addView(mArrowView);
        mContentView.addView(mRecyclerView);//为了计算高度
        return mContentView;
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void show(final View anchorView) {
        if (!isShowing()) {
            mAnchorView = anchorView;
            mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mLocationLayoutListener);

            anchorView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
            anchorView.post(new Runnable() {
                @Override
                public void run() {
                    mPopupWindow.showAsDropDown(anchorView);
                }
            });
        }
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    private PointF calculateLocation() {
        PointF location = new PointF();

        final RectF anchorRect = Util.calculateRectInWindow(mAnchorView);
        final PointF anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());
        switch (mGravity) {
            case Gravity.TOP:
                location.x = anchorCenter.x - mContentView.getWidth() / 2f;
                location.y = anchorRect.top - mContentView.getHeight();
                break;
            case Gravity.BOTTOM:
                location.x = anchorCenter.x - mContentView.getWidth() / 2f;
                location.y = anchorRect.bottom;
                break;
        }
        float marginX = Util.dpToPx(2);
        if (mScreenWidthPixels - location.x - mContentView.getWidth() < marginX) {
            location.x = mScreenWidthPixels - mContentView.getWidth() - marginX;
        } else if (location.x < marginX) {
            location.x = marginX;
        }
        return location;
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mLocationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Util.removeOnGlobalLayoutListener(mContentView, this);

            RectF anchorRect = Util.calculateRectOnScreen(mAnchorView);
            if (anchorRect.centerY() <= mScreenHeightPixels >> 1) {
                mGravity = Gravity.BOTTOM;
            } else {
                mGravity = Gravity.TOP;
            }
            mContentView.removeAllViews();
            if (mGravity == Gravity.TOP) {
                mContentView.addView(mRecyclerView);
                mContentView.addView(mArrowView);
            } else {
                mContentView.addView(mArrowView);
                mContentView.addView(mRecyclerView);
            }
            PointF location = calculateLocation();
            mArrowView.setImageDrawable(new ArrowDrawable(mBackgroundColor, mGravity));
            mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mArrowLayoutListener);
            mPopupWindow.setClippingEnabled(true);
            mPopupWindow.update((int) location.x, (int) location.y, mPopupWindow.getWidth(), mPopupWindow.getHeight());
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mArrowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Util.removeOnGlobalLayoutListener(mContentView, this);

            RectF anchorRect = Util.calculateRectOnScreen(mAnchorView);
            RectF contentViewRect = Util.calculateRectOnScreen(mContentView);
            float x, y;
            if (mGravity == Gravity.BOTTOM || mGravity == Gravity.TOP) {
                x = mContentView.getPaddingLeft() + Util.dpToPx(2);
                float centerX = (contentViewRect.width() / 2f) - (mArrowView.getWidth() / 2f);
                float newX = centerX - (contentViewRect.centerX() - anchorRect.centerX());
                if (newX > x) {
                    if (newX + mArrowView.getWidth() + x > contentViewRect.width()) {
                        x = contentViewRect.width() - mArrowView.getWidth() - x;
                    } else {
                        x = newX;
                    }
                }
                y = mArrowView.getTop();
                y = y + (mGravity == Gravity.TOP ? -1 : +1);
            } else {
                y = mContentView.getPaddingTop() + Util.dpToPx(2);
                float centerY = (contentViewRect.height() / 2f) - (mArrowView.getHeight() / 2f);
                float newY = centerY - (contentViewRect.centerY() - anchorRect.centerY());
                if (newY > y) {
                    if (newY + mArrowView.getHeight() + y > contentViewRect.height()) {
                        y = contentViewRect.height() - mArrowView.getHeight() - y;
                    } else {
                        y = newY;
                    }
                }
                x = mArrowView.getLeft();
                x = x + (mGravity == Gravity.START ? -1 : +1);
            }
            mArrowView.setX(x);
            mArrowView.setY(y);

            startAnim(x, y);
        }
    };

    private void startAnim(float x, float y) {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mContentView, "scaleX", 0.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mContentView, "scaleY", 0.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mContentView, "alpha", 0.5f, 1f);
        mContentView.setPivotX(x + mArrowView.getWidth() / 2);
        if (mGravity == Gravity.TOP) {
            mContentView.setPivotY(y + mArrowView.getHeight());
        } else {
            mContentView.setPivotY(y);
        }
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY).with(alpha);//两个动画同时开始
        animatorSet.start();
    }

    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {

        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            dismiss();
        }
    };

    public static final class Builder {

        private int mBackgroundColor = Color.BLACK;
        public int mTextColor = Color.LTGRAY;
        private boolean isCancelable = true;

        private float mCornerRadius;
        private float mArrowHeight;
        private float mArrowWidth;

        private Context mContext;
        private List<ActionItem> mData = new ArrayList<>();
        private int mLineColor = Color.GRAY;
        private int mLineSize = 1;
        private boolean mIsShowImage = true;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        public Builder setCancelable(boolean cancelable) {
            isCancelable = cancelable;
            return this;
        }

        public Builder setBackgroundColor(@ColorInt int color) {
            mBackgroundColor = color;
            return this;
        }

        public Builder setTextColor(@ColorInt int color) {
            mTextColor = color;
            return this;
        }

        public Builder setLineColor(@ColorInt int color) {
            mLineColor = color;
            return this;
        }

        public Builder setLineSize(@Size int size) {
            mLineSize = size;
            return this;
        }

        public Builder setShowImage(boolean isShowImage) {
            mIsShowImage = isShowImage;
            return this;
        }

        public Builder setCornerRadius(@DimenRes int resId) {
            return setCornerRadius(mContext.getResources().getDimension(resId));
        }

        public Builder setCornerRadius(float radius) {
            mCornerRadius = radius;
            return this;
        }

        public Builder setArrowHeight(@DimenRes int resId) {
            return setArrowHeight(mContext.getResources().getDimension(resId));
        }

        public Builder setArrowHeight(float height) {
            mArrowHeight = height;
            return this;
        }

        public Builder setArrowWidth(@DimenRes int resId) {
            return setArrowWidth(mContext.getResources().getDimension(resId));
        }

        public Builder setArrowWidth(float width) {
            mArrowWidth = width;
            return this;
        }

        public Builder addData(List<ActionItem> items) {
            mData.clear();
            mData.addAll(items);
            return this;
        }

        public Builder addData(ActionItem item) {
            mData.add(item);
            return this;
        }

        public PopMenu build() {

            if (mArrowHeight == 0) {
                mArrowHeight = Util.dpToPx(5);
            }
            if (mArrowWidth == 0) {
                mArrowWidth = Util.dpToPx(12);
            }
            return new PopMenu(this);
        }

    }
}
