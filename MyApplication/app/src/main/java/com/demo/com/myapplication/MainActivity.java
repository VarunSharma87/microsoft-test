package com.demo.calendar.project;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;

import com.demo.calendar.project.adapters.AgendaViewAdapter;
import com.demo.calendar.project.adapters.MonthViewPagerAdapter;
import com.demo.calendar.project.month.CalendarPresenter;
import com.demo.calendar.project.month.CustomViewPager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CalendarPresenter.ViewModelEventListener {

    private CustomViewPager mViewPager;
    private CheckedTextView mMonthViewDropdown;
    private RecyclerView mAgendaView;
    private final int REQUEST_READ_CALENDAR = 1;
    CalendarPresenter mCalendarPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME);


        mMonthViewDropdown = (CheckedTextView) findViewById(R.id.month_dropdown);
        mMonthViewDropdown.setOnClickListener(this);


        mViewPager = (CustomViewPager) findViewById(R.id.viewPager);
        MonthViewPagerAdapter viewPagerAdapter = new MonthViewPagerAdapter();
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setCurrentItem(2);

        mAgendaView = (RecyclerView) findViewById(R.id.agendaView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAgendaView.setLayoutManager(layoutManager);
        AgendaViewAdapter agendaAdapter = new AgendaViewAdapter(System.currentTimeMillis());

        mCalendarPresenter = new CalendarPresenter(mViewPager, mAgendaView, this);
        mViewPager.addOnPageChangeListener(mCalendarPresenter);

        mAgendaView.addOnScrollListener(mCalendarPresenter.getAgendaScrollListener());
        mAgendaView.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mAgendaView.addItemDecoration(divider);
        mAgendaView.setAdapter(agendaAdapter);

        layoutManager.scrollToPositionWithOffset(AgendaViewAdapter.BUFFER_DAYS, 0);
        updateToolbar(mCalendarPresenter.getCurrentMonthYear());
        mCalendarPresenter.updateEventsData();
    }

    private void updateToolbar(String month) {
        mMonthViewDropdown.setText(month);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (mMonthViewDropdown.isChecked()) {
            mViewPager.setVisibility(View.VISIBLE);
        } else {
            mViewPager.setVisibility(View.GONE);
        }
        mMonthViewDropdown.toggle();
    }

    @Override
    public void onMonthChanged(String monthYear) {
        updateToolbar(monthYear);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestPermission(String[] permission) {
        requestPermissions(permission, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCalendarPresenter.onPermissionGranted();
                }
        }
    }
}
