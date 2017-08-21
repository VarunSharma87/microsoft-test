package com.demo.calendar.project.agenda;

/**
 * Created by varunsharma on 03/08/17.
 */

public class EventObject {

    private long mStartTime;
    private long mEndTIme;
    private String mTitle;
    private int mColor;

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public long getEndTIme() {
        return mEndTIme;
    }

    public void setEndTIme(long mEndTIme) {
        this.mEndTIme = mEndTIme;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }
}
