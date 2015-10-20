package com.android.ted.sample;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ted.sample.model.Girl;
import com.android.ted.sample.viewpager.MainPagerAdapter;
import com.ted.coder.sdlayout.ScrollDownLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ScrollDownLayout mScrollDownLayout;
    private ArrayList<Girl> mAllGirlList;
    private TextView mGirlDesText;

    private MainPagerAdapter.OnClickItemListenerImpl mOnClickItemListener = new MainPagerAdapter.OnClickItemListenerImpl() {
        @Override
        public void onClickItem(View item, int position) {
            Toast.makeText(MainActivity.this, "You click at" + position, Toast.LENGTH_SHORT).show();

            if (mScrollDownLayout.getCurrentStatus() == ScrollDownLayout.Status.OPENED) {
                mScrollDownLayout.scrollToClose();
            }
        }
    };

    private ScrollDownLayout.OnScrollChangedListener mOnScrollChangedListener = new ScrollDownLayout.OnScrollChangedListener() {
        @Override
        public void onScrollProgressChanged(float currentProgress) {

        }

        @Override
        public void onScrollFinished(ScrollDownLayout.Status currentStatus) {
            if(currentStatus.equals(ScrollDownLayout.Status.EXIT)){
                finish();
            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mGirlDesText.setText(mAllGirlList.get(position).getDesContent());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_to_exit).setChecked(mScrollDownLayout.isSupportExit());
        return true;
    }

    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            mScrollDownLayout.setToClosed();
            return true;
        }else if(item.getItemId() == R.id.action_to_exit){
            mScrollDownLayout.setIsSupportExit(!mScrollDownLayout.isSupportExit());
            invalidateOptionsMenu();
            return true;
        }else if(item.getItemId() == R.id.action_open){
            mScrollDownLayout.setToOpen();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mGirlDesText = (TextView)findViewById(R.id.text_view);
        mScrollDownLayout = (ScrollDownLayout) findViewById(R.id.scroll_down_layout);

        mScrollDownLayout.setMinOffset(0);
        mScrollDownLayout.setMaxOffset(800);
        mScrollDownLayout.setExitOffset(1674);
        mScrollDownLayout.setToOpen();
        mScrollDownLayout.setIsSupportExit(true);
        mScrollDownLayout.setAllowHorizontalScroll(true);
        mScrollDownLayout.setOnScrollChangedListener(mOnScrollChangedListener);

        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(this);
        mainPagerAdapter.setOnClickItemListener(mOnClickItemListener);
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setOnPageChangeListener(mOnPageChangeListener);
        initGirlUrl();
        mainPagerAdapter.initViewUrl(mAllGirlList);
        mGirlDesText.setText(mAllGirlList.get(0).getDesContent());
    }


    private void initGirlUrl() {
        mAllGirlList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Girl girl = new Girl();
            girl.setImageUrl(Contstants.ImageUrl[i]);
            girl.setDesContent(Contstants.DesContent[i]);
            mAllGirlList.add(girl);
        }
    }

}
