package com.demo.calendar.project.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by varunsharma on 22/07/17.
 */

public class MonthViewPagerAdapter extends PagerAdapter implements MonthViewAdapter.OnDateClickListener {

    private int DAYS_IN_WEEK = 7;
    private final int MAX_AVAILABLE_VIEWS = 5;
    private ArrayList<Long> mMonths = new ArrayList<>(MAX_AVAILABLE_VIEWS);
    private MonthViewAdapter.OnDateClickListener mDateSelectedListener;

    public MonthViewPagerAdapter() {
        for (int i = 0; i < MAX_AVAILABLE_VIEWS; i++) {
            Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.MONTH, i - 2);
            mMonths.add(calendar.getTimeInMillis());
        }
    }

    @Override
    public int getCount() {
        return MAX_AVAILABLE_VIEWS;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        RecyclerView monthView = new RecyclerView(container.getContext());
        monthView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        monthView.setLayoutManager(new GridLayoutManager(container.getContext(), DAYS_IN_WEEK));
        MonthViewAdapter adapter = new MonthViewAdapter(mMonths.get(position));
        adapter.setOnDateClickListener(this);
        monthView.setAdapter(adapter);
        monthView.setTag(position);
        container.addView(monthView);
        return monthView;
    }

    public void addMonthsToRight() {
        for (int i = 0; i < MAX_AVAILABLE_VIEWS - 2; i++) {
            Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(mMonths.remove(0));
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, MAX_AVAILABLE_VIEWS);
            mMonths.add(calendar.getTimeInMillis());
        }

    }

    public void addMonthsToLeft() {
        for (int i = 0; i < MAX_AVAILABLE_VIEWS - 2; i++) {
            Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(mMonths.remove(MAX_AVAILABLE_VIEWS - 1));
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -MAX_AVAILABLE_VIEWS);
            mMonths.add(0, calendar.getTimeInMillis());
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public String getMonthYear(int position) {
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(mMonths.get(position));
        String[] months = DateFormatSymbols.getInstance().getMonths();
        return months[calendar.get(Calendar.MONTH)] +
                ", " +
                calendar.get(Calendar.YEAR);
    }

    public long getMonthInMillisecondsAtPosition(int position) {
        return mMonths.get(position);
    }

    public void setDateSelectedListener(MonthViewAdapter.OnDateClickListener listener) {
        mDateSelectedListener = listener;
    }

    @Override
    public void onDateSelected(int date) {
        if (mDateSelectedListener != null) {
            mDateSelectedListener.onDateSelected(date);
        }
    }
}
