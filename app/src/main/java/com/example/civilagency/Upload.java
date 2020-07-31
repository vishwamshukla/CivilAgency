package com.example.civilagency;


import com.google.firebase.database.Exclude;

public class Upload {

    private String mImageUrl, mPotholeType, mAddress,mLandmark,mDimension, mComment, mDate, mDateFull, mTime, videourl, mPhone, mTimeKey;
    private String mKey;
    private String VideoDescription;
    private String VideoUri;


    public Upload(String ImageUrl, String mPotholeType, String mAddress, String mLandmark, String mDimension, String mComment, String mDate, String mDateFull, String mTime, String mPhone, String mTimeKey) {
        this.mImageUrl = ImageUrl;
        this.mPotholeType = mPotholeType;
        this.mAddress = mAddress;
        this.mLandmark = mLandmark;
        this.mDimension = mDimension;
        this.mComment = mComment;
        this.mKey = mKey;
        this.mDate = mDate;
        this.mDateFull = mDateFull;
        this.mTime = mTime;
        this.mPhone = mPhone;
        this.mTimeKey = mTimeKey;



    }

    public Upload() {

    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public String getmPotholeType() {
        return mPotholeType;
    }

    public void setmPotholeType(String mPotholeType) {
        this.mPotholeType = mPotholeType;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getmLandmark() {
        return mLandmark;
    }

    public void setmLandmark(String mLandmark) {
        this.mLandmark = mLandmark;
    }

    public String getmDimension() {
        return mDimension;
    }

    public void setmDimension(String mDimension) {
        this.mDimension = mDimension;
    }

    public String getmComment() {
        return mComment;
    }

    public void setmComment(String mComment) {
        this.mComment = mComment;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmDateFull() {
        return mDateFull;
    }

    public void setmDateFull(String mDateFull) {
        this.mDateFull = mDateFull;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmTimeKey() {
        return mTimeKey;
    }

    public void setmTimeKey(String mTimeKey) {
        this.mTimeKey = mTimeKey;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    @Exclude

    public String getKey(){
        return mKey;
    }
    @Exclude
    public void setKey(String key) {
        mKey = key;
    }






    //  public String getVideoUrl(){
    //     return mVideoUrl;
    // }

    //  public void setVideoUrl(String videoUrl){
    //    mVideoUrl = videoUrl;
    //  }

}