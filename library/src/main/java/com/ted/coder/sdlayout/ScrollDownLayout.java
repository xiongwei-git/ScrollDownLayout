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

package com.ted.coder.sdlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.ted.coder.sdlayout.content.ContentScrollView;


/**
 * Layout that can scroll down to a max offset and can tell the scroll progress by
 * OnScrollProgressListener.
 */
public class ScrollDownLayout extends FrameLayout {
    private static final int MAX_SCROLL_DURATION = 400;
    private static final int MIN_SCROLL_DURATION = 100;
    private static final int FLING_VELOCITY_SLOP = 80;
    private static final float DRAG_SPEED_MULTIPLIER = 1f;
    private static final int DRAG_SPEED_SLOP = 30;
    private static final int MOTION_DISTANCE_SLOP = 10;
    private static final float SCROLL_TO_CLOSE_OFFSET_FACTOR = 0.2f;
    private static final float SCROLL_TO_EXIT_OFFSET_FACTOR = 0.1f;
    private final GestureDetector.OnGestureListener gestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (velocityY > FLING_VELOCITY_SLOP) {
                        if(lastFlingStatus.equals(Status.OPENED)){
                            lastFlingStatus = Status.EXIT;
                            scrollToExit();
                        }else {
                            scrollToOpen();
                            lastFlingStatus = Status.OPENED;
                        }
                        return true;
                    } else if (velocityY < FLING_VELOCITY_SLOP) {
                        scrollToClose();
                        lastFlingStatus = Status.CLOSED;
                        return true;
                    }
                    return false;
                }
            };

    private final AbsListView.OnScrollListener associatedListViewListener =
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    updateListViewScrollState(view);
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                     int totalItemCount) {
                    updateListViewScrollState(view);
                }
            };
    private float lastX;
    private float lastY;
    private float lastDownX;
    private float lastDownY;
    private Status lastFlingStatus = Status.CLOSED;
    private Scroller scroller;
    private GestureDetector gestureDetector;
    private boolean isEnable = true;
    private boolean isSupportExit = false;
    private boolean isAllowHorizontalScroll = true;
    private boolean isDraggable = true;
    private boolean isAllowPointerIntercepted = true;
    private boolean isCurrentPointerIntercepted = false;
    private InnerStatus currentInnerStatus = InnerStatus.OPENED;
    private int maxOffset = 0;
    private int minOffset = 0;
    private int exitOffset = 0;
    private OnScrollChangedListener onScrollChangedListener;
    private ContentScrollView mScrollView;

    public ScrollDownLayout(Context context) {
        super(context);
    }

    public ScrollDownLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
    }

    public ScrollDownLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFromAttributes(context, attrs);
    }

    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scroller = new Scroller(getContext(), null, true);
        } else {
            scroller = new Scroller(getContext());
        }
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScrollDownLayout, 0, 0);

        maxOffset = a.getDimensionPixelOffset(R.styleable.ScrollDownLayout_max_offset, maxOffset);

        a.recycle();
    }


    private ContentScrollView.OnScrollChangedListener mOnScrollChangedListener = new ContentScrollView.OnScrollChangedListener() {
        @Override
        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            if (null == mScrollView) return;
            if (mScrollView.getScrollY() == 0) {
                setDraggable(true);
            } else {
                setDraggable(false);
            }
        }
    };

    /**
     * Set the scrolled position of your view. This will cause a call to
     * {@link #onScrollChanged(int, int, int, int)} and the view will be
     * invalidated.
     *
     * @param x the x position to scroll to
     * @param y the y position to scroll to
     */
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (maxOffset == minOffset) {
            return;
        }
        //only from min to max or from max to min,send progress out. not exit
        if (-y <= maxOffset) {
            float progress = (float) (-y - minOffset) / (maxOffset - minOffset);
            onScrollProgressChanged(progress);
        }
        if (y == -minOffset) {
            // closed
            if (currentInnerStatus != InnerStatus.CLOSED) {
                currentInnerStatus = InnerStatus.CLOSED;
                onScrollFinished(Status.CLOSED);
            }
        } else if (y == -maxOffset) {
            // opened
            if (currentInnerStatus != InnerStatus.OPENED) {
                currentInnerStatus = InnerStatus.OPENED;
                onScrollFinished(Status.OPENED);
            }
        } else if (isSupportExit && y == -exitOffset) {
            // exited
            if (currentInnerStatus != InnerStatus.EXIT) {
                currentInnerStatus = InnerStatus.EXIT;
                onScrollFinished(Status.EXIT);
            }
        }
    }

    private void onScrollFinished(Status status) {
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollFinished(status);
        }
    }

    private void onScrollProgressChanged(float progress) {
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollProgressChanged(progress);
        }
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int currY = scroller.getCurrY();
            scrollTo(0, currY);
            if (currY == -minOffset || currY == -maxOffset || (isSupportExit && currY == -exitOffset)) {
                scroller.abortAnimation();
            } else {
                invalidate();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnable) {
            return false;
        }
        if (!isDraggable && currentInnerStatus == InnerStatus.CLOSED) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                lastDownX = lastX;
                lastDownY = lastY;
                isAllowPointerIntercepted = true;
                isCurrentPointerIntercepted = false;
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                    currentInnerStatus = InnerStatus.MOVING;
                    isCurrentPointerIntercepted = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isAllowPointerIntercepted = true;
                isCurrentPointerIntercepted = false;
                if (currentInnerStatus == InnerStatus.MOVING) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isAllowPointerIntercepted) {
                    return false;
                }
                if (isCurrentPointerIntercepted) {
                    return true;
                }
                int deltaY = (int) (ev.getY() - lastDownY);
                int deltaX = (int) (ev.getX() - lastDownX);
                if (Math.abs(deltaY) < MOTION_DISTANCE_SLOP) {
                    return false;
                }
                if (Math.abs(deltaY) < Math.abs(deltaX)) {
                    // horizontal event
                    if (isAllowHorizontalScroll) {
                        isAllowPointerIntercepted = false;
                        isCurrentPointerIntercepted = false;
                        return false;
                    }
                }
                if (currentInnerStatus == InnerStatus.CLOSED) {
                    // when closed, only handle downwards motion event
                    if (deltaY < 0) {
                        // upwards
                        return false;
                    }
                } else if (currentInnerStatus == InnerStatus.OPENED && !isSupportExit) {
                    // when opened, only handle upwards motion event
                    if (deltaY > 0) {
                        // downwards
                        return false;
                    }
                }
                isCurrentPointerIntercepted = true;
                return true;
            default:
                return false;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCurrentPointerIntercepted) {
            return false;
        }
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                int deltaY = (int) ((event.getY() - lastY) * DRAG_SPEED_MULTIPLIER);
                deltaY = (int) (Math.signum(deltaY)) * Math.min(Math.abs(deltaY), DRAG_SPEED_SLOP);
                if (disposeEdgeValue(deltaY)) {
                    return true;
                }
                currentInnerStatus = InnerStatus.MOVING;
                int toScrollY = getScrollY() - deltaY;
                if (toScrollY >= -minOffset) {
                    scrollTo(0, -minOffset);
                } else if (toScrollY <= -maxOffset && !isSupportExit) {
                    scrollTo(0, -maxOffset);
                } else {
                    scrollTo(0, toScrollY);
                }
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (currentInnerStatus == InnerStatus.MOVING) {
                    completeMove();
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private boolean disposeEdgeValue(int deltaY) {
        if (isSupportExit) {
            if (deltaY <= 0 && getScrollY() >= -minOffset) {
                return true;
            } else if (deltaY >= 0 && getScrollY() <= -exitOffset) {
                return true;
            }
        } else {
            if (deltaY <= 0 && getScrollY() >= -minOffset) {
                return true;
            } else if (deltaY >= 0 && getScrollY() <= -maxOffset) {
                return true;
            }
        }
        return false;
    }

    private void completeMove() {
        float closeValue = -((maxOffset - minOffset) * SCROLL_TO_CLOSE_OFFSET_FACTOR);
        if (getScrollY() > closeValue) {
            scrollToClose();
        } else {
            if (isSupportExit) {
                float exitValue = -((exitOffset - maxOffset) * SCROLL_TO_EXIT_OFFSET_FACTOR + maxOffset);
                if (getScrollY() <= closeValue && getScrollY() > exitValue) {
                    scrollToOpen();
                } else {
                    scrollToExit();
                }
            } else {
                scrollToOpen();
            }
        }
    }

    /**
     * Scroll the layout to open if it's closed, scroll to close otherwise.
     */
    public void showOrHide() {
        if (currentInnerStatus == InnerStatus.OPENED) {
            scrollToClose();
        } else if (currentInnerStatus == InnerStatus.CLOSED) {
            scrollToOpen();
        }
    }

    /**
     * Scroll the layout to open, meaning scroll down to maxOffset.
     */
    public void scrollToOpen() {
        if (currentInnerStatus == InnerStatus.OPENED) {
            return;
        }
        if (maxOffset == minOffset) {
            return;
        }
        int dy = -getScrollY() - maxOffset;
        if (dy == 0) {
            return;
        }
        currentInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (maxOffset - minOffset));
        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        invalidate();
    }

    /**
     * Scroll the layout to closed, meaning scroll to minOffset.
     */
    public void scrollToClose() {
        if (currentInnerStatus == InnerStatus.CLOSED) {
            return;
        }
        if (maxOffset == minOffset) {
            return;
        }
        int dy = -getScrollY() - minOffset;
        if (dy == 0) {
            return;
        }
        currentInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (maxOffset - minOffset));
        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        invalidate();
    }

    /**
     * Scroll the layout to exit, meaning scroll to exitOffset.
     */
    public void scrollToExit() {
        if (!isSupportExit) return;
        if (currentInnerStatus == InnerStatus.EXIT) {
            return;
        }
        if (exitOffset == maxOffset) {
            return;
        }
        int dy = -getScrollY() - exitOffset;
        if (dy == 0) {
            return;
        }
        currentInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (exitOffset - maxOffset));
        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        invalidate();
    }

    /**
     * Init the layout to open, with no animation.
     */
    public void setToOpen() {
        scrollTo(0, -maxOffset);
        currentInnerStatus = InnerStatus.OPENED;
        lastFlingStatus = Status.OPENED;
    }

    /**
     * Init the layout to closed, with no animation.
     */
    public void setToClosed() {
        scrollTo(0, -minOffset);
        currentInnerStatus = InnerStatus.CLOSED;
        lastFlingStatus = Status.CLOSED;
    }

    /**
     * Init the layout to exited, with no animation.
     */
//    public void setToExit() {
//        if (!isSupportExit) return;
//        scrollTo(0, -exitOffset);
//        currentInnerStatus = InnerStatus.EXIT;
//    }

    public void setMinOffset(int minOffset) {
        this.minOffset = minOffset;
    }

    public void setMaxOffset(int maxOffset) {
        this.maxOffset = maxOffset;
    }

    public void setExitOffset(int exitOffset) {
        this.exitOffset = exitOffset;
    }

    public void setEnable(boolean enable) {
        this.isEnable = enable;
    }

    public void setIsSupportExit(boolean isSupportExit) {
        this.isSupportExit = isSupportExit;
    }

    public boolean isSupportExit() {
        return isSupportExit;
    }

    public boolean isAllowHorizontalScroll() {
        return isAllowHorizontalScroll;
    }

    public void setAllowHorizontalScroll(boolean isAllowed) {
        isAllowHorizontalScroll = isAllowed;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public void setDraggable(boolean draggable) {
        this.isDraggable = draggable;
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        this.onScrollChangedListener = listener;
    }

    public Status getCurrentStatus() {
        switch (currentInnerStatus) {
            case CLOSED:
                return Status.CLOSED;
            case OPENED:
                return Status.OPENED;
            case EXIT:
                return Status.EXIT;
            default:
                return Status.OPENED;
        }
    }

    /**
     * Set associated list view, then this layout will only be able to drag down when the list
     * view is scrolled to top.
     *
     * @param listView
     */
    public void setAssociatedListView(AbsListView listView) {
        listView.setOnScrollListener(associatedListViewListener);
        updateListViewScrollState(listView);
    }

    private void updateListViewScrollState(AbsListView listView) {
        if (listView.getChildCount() == 0) {
            setDraggable(true);
        } else {
            if (listView.getFirstVisiblePosition() == 0) {
                View firstChild = listView.getChildAt(0);
                if (firstChild.getTop() == listView.getPaddingTop()) {
                    setDraggable(true);
                    return;
                }
            }
            setDraggable(false);
        }
    }

    public void setAssociatedScrollView(ContentScrollView scrollView) {
        this.mScrollView = scrollView;
        this.mScrollView.setScrollbarFadingEnabled(false);
        this.mScrollView.setOnScrollChangeListener(mOnScrollChangedListener);
    }

    private enum InnerStatus {
        EXIT, OPENED, CLOSED, MOVING, SCROLLING
    }

    /**
     * Indicate the status of ScrollDownLayout, only can be OPENED or CLOSED.
     */
    public enum Status {
        EXIT, OPENED, CLOSED
    }

    /**
     * Register this to ScrollDownLayout can monitor its scroll progress.
     */
    public interface OnScrollChangedListener {
        /**
         * Called every time when the scroll offset changed.
         *
         * @param currentProgress 0 to 1, 0 means close, 1 means open.
         */
        void onScrollProgressChanged(float currentProgress);

        /**
         * Called when the scroll state is changed.
         *
         * @param currentStatus the current status after change
         */
        void onScrollFinished(Status currentStatus);
    }

}
