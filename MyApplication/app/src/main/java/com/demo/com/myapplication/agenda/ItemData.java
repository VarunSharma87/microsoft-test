package com.demo.calendar.project.agenda;

/**
 * Created by varunsharma on 20/08/17.
 */

public class ItemData implements Data {

    private String mData;
    private long mTime;

    public ItemData(String data, long time) {
        mData = data;
        mTime = time;
    }

    public void setData(String data) {
        mData = data;
    }

    public String getData() {
        return mData;
    }

    @Override
    public int getDataType() {
        return DATA_TYPE_ITEM;
    }

    @Override
    public long getTime() {
        return mTime;
    }
}
