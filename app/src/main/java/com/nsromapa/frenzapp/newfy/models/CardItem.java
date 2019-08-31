package com.nsromapa.frenzapp.newfy.models;

/**
 * Created by amsavarthan on 1/3/18.
 */

public class CardItem {

    private int mTextResource;
    private int mTitleResource;
    private int mImageResource;
    private String mColorResource;
    private String mButtonText1,mButtonText2;

    public CardItem(int mTextResource, int mTitleResource, int mImageResource, String mColorResource, String mButtonText1, String mButtonText2) {
        this.mTextResource = mTextResource;
        this.mTitleResource = mTitleResource;
        this.mImageResource = mImageResource;
        this.mColorResource = mColorResource;
        this.mButtonText1 = mButtonText1;
        this.mButtonText2 = mButtonText2;
    }

    public int getmTextResource() {
        return mTextResource;
    }

    public void setmTextResource(int mTextResource) {
        this.mTextResource = mTextResource;
    }

    public int getmTitleResource() {
        return mTitleResource;
    }

    public void setmTitleResource(int mTitleResource) {
        this.mTitleResource = mTitleResource;
    }

    public int getmImageResource() {
        return mImageResource;
    }

    public void setmImageResource(int mImageResource) {
        this.mImageResource = mImageResource;
    }

    public String getmColorResource() {
        return mColorResource;
    }

    public void setmColorResource(String mColorResource) {
        this.mColorResource = mColorResource;
    }

    public String getmButtonText1() {
        return mButtonText1;
    }

    public void setmButtonText1(String mButtonText1) {
        this.mButtonText1 = mButtonText1;
    }

    public String getmButtonText2() {
        return mButtonText2;
    }

    public void setmButtonText2(String mButtonText2) {
        this.mButtonText2 = mButtonText2;
    }
}
