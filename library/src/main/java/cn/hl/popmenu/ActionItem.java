package cn.hl.popmenu;

import android.support.annotation.DrawableRes;

/**
 * 选项实体
 */
public class ActionItem {

    /** 标识，用于判断 */
    private int tag;
    /** 显示文字 */
    private String text;
    /** 图片资源id */
    @DrawableRes
    private int resId;

    public ActionItem(int tag, String text) {
        this.tag = tag;
        this.text = text;
    }

    public ActionItem(int tag, String text, int resId) {
        this.tag = tag;
        this.text = text;
        this.resId = resId;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(@DrawableRes int resId) {
        this.resId = resId;
    }

    @Override
    public String toString() {
        return "ActionItem{" +
                "text='" + text + '\'' +
                ", resId=" + resId +
                '}';
    }

}
