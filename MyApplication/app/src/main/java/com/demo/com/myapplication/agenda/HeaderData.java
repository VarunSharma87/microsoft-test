package com.demo.calendar.project.agenda;

/**
 * Created by varunsharma on 20/08/17.
 */

public class HeaderData implements Data {

    private String mData;
    private long mTime;

    public HeaderData(String data, long date) {
        mData =  data;
        mTime = date;
    }

    public void setData(String data) {
        mData = data;
    }

    public String getData() {
        return mData;
    }

    public long getTime() {
        return mTime;
    }

    @Override
    public int getDataType() {
        return DATA_TYPE_HEADER;
    }
}
