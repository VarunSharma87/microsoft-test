package com.demo.calendar.project.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.calendar.project.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by varunsharma on 22/07/17.
 */

public class MonthViewAdapter extends RecyclerView.Adapter {


    private OnDateClickListener mListener;

    public interface OnDateClickListener {
        void onDateSelected(int date);
    }

    private int mDays;
    private final int WEEK_HEADER_COUNT = 7;
    private final int VIEW_TYPE_DAY = 0;
    private final int VIEW_TYPE_DATE = 1;
    private int mDayStartOffset;
    private String[] mWeekdays;
    private int mSelectedDate = -1;

    public MonthViewAdapter(long currentMonthInMillis) {
        Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(currentMonthInMillis);
        mDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + getDateStartOffset(calendar);
        mWeekdays = DateFormatSymbols.getInstance().getShortWeekdays();
        mSelectedDate = mDayStartOffset;
    }

    private int getDateStartOffset(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = calendar.get(Calendar.DAY_OF_WEEK);
        mDayStartOffset = startDay - Calendar.MONDAY;
        if (mDayStartOffset < 0) {
            mDayStartOffset += 7;
        }

        return mDayStartOffset = mDayStartOffset + WEEK_HEADER_COUNT;
    }

    @Override
    public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MonthViewHolder viewHolder = null;
        View view = inflater.inflate(R.layout.month_day_cell, parent, false);
        viewHolder = new MonthViewHolder(view);
        if (viewType == VIEW_TYPE_DATE) {
            view.setOnClickListener(viewHolder);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MonthViewHolder viewHolder = (MonthViewHolder) holder;
        viewHolder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mDays;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < WEEK_HEADER_COUNT) {
            return VIEW_TYPE_DAY;
        }
        return VIEW_TYPE_DATE;
    }

    public void resetSelectedItem() {
        int oldPosition = mSelectedDate;
        mSelectedDate = mDayStartOffset;
        notifyItemChanged(oldPosition);
        notifyItemChanged(mSelectedDate);

    }
    private class MonthViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTextView;

        MonthViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(this);

        }

        void bindView(int position) {

            if (position < WEEK_HEADER_COUNT) {
                mTextView.setText(getDay(position));
            } else if (position < mDayStartOffset) {
                mTextView.setText(null);
            } else {
                mTextView.setText(String.valueOf(position - mDayStartOffset + 1));
            }
            if (mSelectedDate == position) {
                mTextView.setBackgroundColor(mTextView.getContext().getResources().getColor(android.R.color.holo_green_light));
            } else {
                mTextView.setBackgroundColor(mTextView.getContext().getResources().getColor(android.R.color.transparent));
            }
        }

        public String getDay(int position) {
            position = Calendar.MONDAY + position;
            if (position > WEEK_HEADER_COUNT) {
                position = position - WEEK_HEADER_COUNT;
            }

            return mWeekdays[position];
        }

        @Override
        public void onClick(View view) {
            int oldPosition = mSelectedDate;
            mSelectedDate = getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(mSelectedDate);
            notifyDateClickListeners();
        }
    }

    private void notifyDateClickListeners() {
        Calendar calendar = GregorianCalendar.getInstance();
        int date = mSelectedDate - mDayStartOffset;
        calendar.set(Calendar.DAY_OF_MONTH, date);
        if (mListener != null) {
            mListener.onDateSelected(date);
        }
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        mListener = listener;
    }
}
