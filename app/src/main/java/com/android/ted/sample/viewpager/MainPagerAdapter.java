/*
 *
 *  * Copyright (C) 2015 Ted xiong-wei@hotmail.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.android.ted.sample.viewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.ted.sample.model.Girl;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ted on 2015/6/19.
 */
public class MainPagerAdapter extends PagerAdapter implements View.OnClickListener {
    private ArrayList<Girl> mAllGirlList;
    private Map<Integer, View> mAllImageMap;
    private Context mContext;
    private OnClickItemListenerImpl mOnClickItemListener;

    @Override
    public void onClick(View v) {
        if (null != v && v instanceof ImageView) {
            if (mOnClickItemListener != null) {
                int position = -1;
                Girl girl = (Girl) v.getTag();
                if (mAllGirlList != null && girl != null) {
                    position = mAllGirlList.indexOf(girl);
                }
                mOnClickItemListener.onClickItem(v, position);
            }
        }
    }

    public MainPagerAdapter(Context context) {
        mContext = context;
        mAllImageMap = new HashMap<>();
        mAllGirlList = new ArrayList<>();
    }

    public void initViewUrl(ArrayList<Girl> girls) {
        if (null == girls) return;
        mAllGirlList.clear();
        mAllGirlList.addAll(girls);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAllGirlList.size();
    }

    @Override
    public float getPageWidth(int position) {
        return super.getPageWidth(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        if (mAllImageMap.containsKey(position)) {
            container.removeView(mAllImageMap.get(position));
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView;
        Girl girl = mAllGirlList.get(position);
        if (mAllImageMap.containsKey(position)) {
            View oldView = mAllImageMap.get(position);
            Object tag = oldView.getTag();
            if (null != tag && tag instanceof Girl) {
                if (tag.equals(girl)) {
                    itemView = oldView;
                    container.addView(itemView);
                    return itemView;
                }
            }
            container.removeView(oldView);
            mAllImageMap.remove(position);
        }

        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(mContext).load(girl.getImageUrl()).into(imageView);
        imageView.setTag(girl);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        mAllImageMap.put(position, imageView);
        itemView = imageView;
        itemView.setOnClickListener(this);
        container.addView(itemView);
        return itemView;
    }

    public void setOnClickItemListener(OnClickItemListenerImpl onClickItemListener) {
        mOnClickItemListener = onClickItemListener;
    }

    public interface OnClickItemListenerImpl {
        void onClickItem(View item, int position);
    }

}
