package com.demo.calendar.project.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.calendar.project.R;
import com.demo.calendar.project.agenda.Data;
import com.demo.calendar.project.agenda.EventObject;
import com.demo.calendar.project.agenda.HeaderData;
import com.demo.calendar.project.agenda.ItemData;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;


/**
 * Created by varunsharma on 25/07/17.
 */

public class AgendaViewAdapter extends RecyclerView.Adapter {

    private long mStartDay;
    private long mCurrentTime;
    private int mHeaderCount = 121;
    public static final int BUFFER_DAYS = 70;
    public final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000L;
    private final long BUFFER_VALUE = ((long) BUFFER_DAYS) * MILLISECONDS_IN_DAY;
    private final String[] mWeekDays = DateFormatSymbols.getInstance().getShortWeekdays();
    private final String[] MONTHS = DateFormatSymbols.getInstance().getMonths();
    private int mCurrentDatePosition;
    private HashMap<Long, ArrayList<EventObject>> mEventMap;
    private Calendar mCalendarInstance;
    private final int VIEW_TYPE_HEADER = 1;
    private final int VIEW_TYPE_CHILD = 2;
    private ArrayList<Data> mAgendaData;

    public AgendaViewAdapter(long currentTime) {

        mCalendarInstance = GregorianCalendar.getInstance(Locale.getDefault());
        reInitializeCalendar(mCalendarInstance, currentTime);
        mCurrentTime = mCalendarInstance.getTimeInMillis();
        mStartDay = currentTime - BUFFER_VALUE;
        populateData();
    }

    private void reInitializeCalendar(Calendar calendar, long time) {
        calendar.clear();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void populateData() {
        mAgendaData = new ArrayList<>();
        mCurrentDatePosition = -1;
        for (int i = 0; i < mHeaderCount; i++) {
            long time = i * MILLISECONDS_IN_DAY + mStartDay;
            String date = getDateString(time);

            reInitializeCalendar(mCalendarInstance, time);
            mAgendaData.add(new HeaderData(date, mCalendarInstance.getTimeInMillis()));
            if (mCurrentDatePosition < 0 && mCurrentTime == mCalendarInstance.getTimeInMillis()) {
                mCurrentDatePosition = mAgendaData.size() - 1;
            }

            if (mEventMap != null) {
                ArrayList<EventObject> eventList = mEventMap.get(mCalendarInstance.getTimeInMillis());
                if (eventList != null) {
                    for (int j = 0; j < eventList.size(); j++) {
                        EventObject event = eventList.get(j);
                        mAgendaData.add(new ItemData(event.getTitle(), mCalendarInstance.getTimeInMillis()));
                    }
                } else {
                    mAgendaData.add(new ItemData("No event", mCalendarInstance.getTimeInMillis()));
                }
            } else {
                mAgendaData.add(new ItemData("No event", mCalendarInstance.getTimeInMillis()));
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mHeaderTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mHeaderTextView = (TextView) itemView.findViewById(R.id.header_view);
        }

        public void bindView(int position) {
            Data data = mAgendaData.get(position);
            mHeaderTextView.setText(data.getData());
        }

    }

    private String getDateString(long time) {
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(time);
        StringBuilder builder = new StringBuilder();
        builder.append(mWeekDays[calendar.get(Calendar.DAY_OF_WEEK)])
                .append(", ")
                .append(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))
                .append(" ")
                .append(MONTHS[calendar.get(Calendar.MONTH)]);
        return builder.toString();
    }

    private class ChildViewHolder extends RecyclerView.ViewHolder {

        private TextView mChildTextView;

        public ChildViewHolder(View itemView) {
            super(itemView);
            mChildTextView = (TextView) itemView.findViewById(R.id.row_item);
        }

        public void bindView(int position) {
            Data data = mAgendaData.get(position);
            mChildTextView.setText(data.getData());
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.child_layout;
        if (viewType == VIEW_TYPE_HEADER) {
            layout = R.layout.header_layout;
        }
        View contentView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_TYPE_HEADER) {
            viewHolder = new HeaderViewHolder(contentView);
        } else {
            viewHolder = new ChildViewHolder(contentView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                ((HeaderViewHolder) holder).bindView(position);
                break;

            case VIEW_TYPE_CHILD:
                ((ChildViewHolder) holder).bindView(position);
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        Data data = mAgendaData.get(position);
        if (data.getDataType() == Data.DATA_TYPE_HEADER) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_CHILD;
    }

    @Override
    public int getItemCount() {
        return mAgendaData.size();
    }

    public void onScrolled(RecyclerView recyclerView) {

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastPosition = layoutManager.findLastVisibleItemPosition();
        int firstPosition = layoutManager.findFirstVisibleItemPosition();

        mCurrentDatePosition = firstPosition;
        if (firstPosition == 0) {
            addItemsInTheBeginning();
            mCurrentDatePosition = (int) (BUFFER_VALUE / MILLISECONDS_IN_DAY);
        } else if (lastPosition == getItemCount() - 1) {
            addItemsAtTheEnd(lastPosition);
        }
    }

    public int getCurrentAdapterPosition() {
        return mCurrentDatePosition;
    }

    public void addItemsInTheBeginning() {
        mHeaderCount += (int) (BUFFER_VALUE / MILLISECONDS_IN_DAY);
        mStartDay -= BUFFER_VALUE;
        notifyItemRangeInserted(0, BUFFER_DAYS);
    }

    public void addItemsAtTheEnd(int position) {
        mHeaderCount += (int) (BUFFER_VALUE / MILLISECONDS_IN_DAY);
        notifyItemRangeInserted(position + 1, BUFFER_DAYS);
    }

    public long getStartDay() {
        return mStartDay;
    }

    public void updateData(HashMap<Long, ArrayList<EventObject>> eventMap) {
        mEventMap = eventMap;
        populateData();
    }

    public int getHeaderCount() {
        return mHeaderCount;
    }

    public Data getDataAtPosition(int position) {
        return mAgendaData.get(position);
    }

    public ArrayList<Data> getData() {
        return mAgendaData;
    }

    public int getCurrentDatePosition() {
        return mCurrentDatePosition;
    }
}
