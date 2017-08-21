package com.demo.calendar.project.month;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.demo.calendar.project.adapters.AgendaViewAdapter;
import com.demo.calendar.project.adapters.MonthViewAdapter;
import com.demo.calendar.project.adapters.MonthViewPagerAdapter;
import com.demo.calendar.project.agenda.Data;
import com.demo.calendar.project.agenda.EventObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by varunsharma on 29/07/17.
 */

public class CalendarPresenter implements ViewPager.OnPageChangeListener, MonthViewAdapter.OnDateClickListener {

    private CustomViewPager mMonthView;
    private RecyclerView mAgendaView;
    private final int VIEW_PAGER_LAST_POSITION = 4;
    private final int VIEW_PAGER_FIRST_POSITION = 0;
    private ViewModelEventListener mListener;
    private int mCurrentVisibleMonth;
    private int mCurrentVisibleYear;
    private boolean mAgendaScrolled = false;
    private boolean mMonthScrolled = false;

    public CalendarPresenter(CustomViewPager monthView, RecyclerView agendaView, ViewModelEventListener listener) {
        mMonthView = monthView;
        MonthViewPagerAdapter adapter = (MonthViewPagerAdapter) monthView.getAdapter();
        adapter.setDateSelectedListener(this);
        mAgendaView = agendaView;
        mListener = listener;
        mCurrentVisibleMonth = getMonth(System.currentTimeMillis());
        mCurrentVisibleYear = getYear(System.currentTimeMillis());
    }

    public void updateEventsData() {
        AgendaViewAdapter agendaAdapter = (AgendaViewAdapter) mAgendaView.getAdapter();
        long startTime = agendaAdapter.getStartDay();
        long endTime = startTime + agendaAdapter.getHeaderCount()*agendaAdapter.MILLISECONDS_IN_DAY;
        HashMap<Long, ArrayList<EventObject>> eventMap = fetchNativeCalendarEvents(startTime, endTime);
        if (eventMap != null) {
            agendaAdapter.updateData(eventMap);
        }
        agendaAdapter.notifyDataSetChanged();
        int scrollPosition = agendaAdapter.getCurrentDatePosition();
        if (scrollPosition >= 0) {
            mAgendaView.scrollToPosition(scrollPosition);
        }
    }

    private RecyclerView.OnScrollListener mAgendaScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            AgendaViewAdapter adapter = (AgendaViewAdapter) recyclerView.getAdapter();
            adapter.onScrolled(recyclerView);

            int firstPosition = adapter.getCurrentAdapterPosition();
            Data data = adapter.getDataAtPosition(firstPosition);
            if (data.getDataType() == Data.DATA_TYPE_ITEM) {
                return;
            }
            long dayVisible = data.getTime();
            int month = getMonth(dayVisible);
            int year = getYear(dayVisible);
            if (mMonthScrolled) {
                mMonthScrolled = false;
                return;
            }
            if (month != mCurrentVisibleMonth) {
                mAgendaScrolled = true;
                int currentItem = mMonthView.getCurrentItem();
                if (month > mCurrentVisibleMonth) {
                    if (year < mCurrentVisibleYear) {
                        mMonthView.setCurrentItem(currentItem - 1);
                    } else {
                        mMonthView.setCurrentItem(currentItem + 1);
                    }
                } else {
                    if (year > mCurrentVisibleYear) {
                        mMonthView.setCurrentItem(currentItem + 1);
                    } else {
                        mMonthView.setCurrentItem(currentItem - 1);
                    }
                }
                mCurrentVisibleMonth = month;
                mCurrentVisibleYear = year;
            }
        }
    };

    private String getMonthName(long timeInMilliseconds) {
        String[] months = DateFormatSymbols.getInstance().getMonths();
        return months[getMonth(timeInMilliseconds)];
    }

    private int getMonth(long timeInMilliseconds) {
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeInMilliseconds);
        return calendar.get(Calendar.MONTH);
    }

    private int getYear(long timeInMilliseconds) {
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeInMilliseconds);
        return calendar.get(Calendar.YEAR);
    }

    public RecyclerView.OnScrollListener getAgendaScrollListener() {
        return mAgendaScrollListener;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        RecyclerView recyclerView = (RecyclerView) mMonthView.findViewWithTag(position);
        if (recyclerView != null) {
            MonthViewAdapter monthViewAdapter = (MonthViewAdapter) recyclerView.getAdapter();
            monthViewAdapter.resetSelectedItem();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        int position = mMonthView.getCurrentItem();
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mMonthScrolled = true;
            if (position == VIEW_PAGER_LAST_POSITION) {
                MonthViewPagerAdapter pagerAdapter = (MonthViewPagerAdapter) mMonthView.getAdapter();
                pagerAdapter.addMonthsToRight();
                mMonthView.setAdapter(null);
                mMonthView.setAdapter(pagerAdapter);
                mMonthView.setCurrentItem(VIEW_PAGER_FIRST_POSITION + 1, false);
            } else if (position == VIEW_PAGER_FIRST_POSITION) {
                MonthViewPagerAdapter pagerAdapter = (MonthViewPagerAdapter) mMonthView.getAdapter();
                pagerAdapter.addMonthsToLeft();
                mMonthView.setAdapter(null);
                mMonthView.setAdapter(pagerAdapter);
                mMonthView.setCurrentItem(VIEW_PAGER_LAST_POSITION - 1, false);
            }

            mListener.onMonthChanged(getCurrentMonthYear());
            if (!mAgendaScrolled) {
                updateAgendaViewVisibleMonth();
            } else {
                mAgendaScrolled = false;
            }

        }
    }

    private void updateAgendaViewVisibleMonth() {
        MonthViewPagerAdapter pagerAdapter = (MonthViewPagerAdapter) mMonthView.getAdapter();
        long milliseconds = pagerAdapter.getMonthInMillisecondsAtPosition(mMonthView.getCurrentItem());
        AgendaViewAdapter agendaAdapter = (AgendaViewAdapter) mAgendaView.getAdapter();
        long startTime = agendaAdapter.getStartDay();

        if (milliseconds < startTime) {
            agendaAdapter.addItemsInTheBeginning();
            updateEventsData();
        }

        updateAgendaViewScrollPosition(milliseconds, agendaAdapter);
    }

    private void updateAgendaViewScrollPosition(long milliseconds, AgendaViewAdapter agendaAdapter) {
        int position = getAgendaViewPosition(milliseconds);
        if (position == -1) {
            agendaAdapter.addItemsAtTheEnd(agendaAdapter.getItemCount() + 1);
            updateEventsData();
        }

        position = getAgendaViewPosition(milliseconds);

        LinearLayoutManager layoutManager = (LinearLayoutManager) mAgendaView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(position, 0);
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(milliseconds);
        mCurrentVisibleMonth = calendar.get(Calendar.MONTH);
        mCurrentVisibleYear = calendar.get(Calendar.YEAR);
    }

    private int getAgendaViewPosition(long time) {
        AgendaViewAdapter adapter = (AgendaViewAdapter) mAgendaView.getAdapter();
        ArrayList<Data> dataList = adapter.getData();
        return doBinarySearch(0, dataList.size()-1, dataList, time);
    }

    private int doBinarySearch(int start, int end, ArrayList<Data> dataList, long time) {
        if (start >= end) {
            return -1;
        }
        int middle = (start + end) / 2;

        Data data = dataList.get(middle);
        int adapterPosition = -1;
        if (data.getTime() == time) {
            int currentPosition = middle;
            while (data.getTime() == time) {
                adapterPosition = currentPosition;
                currentPosition--;
                data = dataList.get(currentPosition);
            }
        } else {
            if (data.getTime() > time) {
                adapterPosition = doBinarySearch(start, middle-1, dataList, time);
            } else {
                adapterPosition = doBinarySearch(middle+1, end, dataList, time);
            }
        }
        return adapterPosition;
    }

    public String getCurrentMonthYear() {
        MonthViewPagerAdapter adapter = (MonthViewPagerAdapter) mMonthView.getAdapter();
        String monthYear = adapter.getMonthYear(mMonthView.getCurrentItem());
        return monthYear;
    }

    private HashMap<Long, ArrayList<EventObject>> fetchNativeCalendarEvents(long startTime, long endTime) {
        String[] projection = {CalendarContract.Events.TITLE, CalendarContract.Events.CALENDAR_COLOR, CalendarContract.Events.ALL_DAY, CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND};
        Uri contentUri = CalendarContract.Events.CONTENT_URI;
        Context context = mMonthView.getContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            mListener.requestPermission(new String[]{Manifest.permission.READ_CALENDAR});
            return null;
        }

        String selection = CalendarContract.Events.DTSTART + " >= ?" + " AND " + CalendarContract.Events.DTSTART + " <= ?";
        String selectionArgs[] = {String.valueOf(startTime), String.valueOf(endTime)};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
        return parseEventsData(cursor);
    }

    //
    private HashMap<Long, ArrayList<EventObject>> parseEventsData(Cursor cursor) {
        HashMap<Long, ArrayList<EventObject>> eventMap = new HashMap<>();
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            calendar.clear();
            EventObject event = new EventObject();
            event.setStartTime(cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
            event.setEndTIme(cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND)));
            event.setTitle(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));

            calendar.setTimeInMillis(event.getStartTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);


            long dayInMillis = calendar.getTimeInMillis();
            ArrayList<EventObject> eventList = eventMap.get(dayInMillis);
            if (eventList == null) {
                eventList = new ArrayList<>();
            }
            eventList.add(event);
            eventMap.put(dayInMillis, eventList);
            cursor.moveToNext();
        }
        return eventMap;
    }

    public void onPermissionGranted() {
        updateEventsData();
    }

    @Override
    public void onDateSelected(int date) {
        MonthViewPagerAdapter adapter = (MonthViewPagerAdapter) mMonthView.getAdapter();
        long currentMonthInMillis = adapter.getMonthInMillisecondsAtPosition(mMonthView.getCurrentItem());
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(currentMonthInMillis);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        AgendaViewAdapter agendaAdapter = (AgendaViewAdapter) mAgendaView.getAdapter();
        updateAgendaViewScrollPosition(calendar.getTimeInMillis(), agendaAdapter);
    }

    public interface ViewModelEventListener {
        void onMonthChanged(String monthYear);
        void requestPermission(String[] permissions);
    }
}
