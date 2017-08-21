package com.demo.calendar.project.agenda;

/**
 * Created by varunsharma on 20/08/17.
 */

public interface Data {

    int DATA_TYPE_HEADER = 1;
    int DATA_TYPE_ITEM = 2;

    String getData();
    void setData(String data);
    int getDataType();
    long getTime();
}
