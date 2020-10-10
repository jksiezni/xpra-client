/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.jksiezni.xpra.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ScaleGestureDetectorCompat;
import timber.log.Timber;

/**
 * Scrolling view.
 */
public class XYScrollView extends FrameLayout {

	private final GestureDetectorCompat gestureDetector;
	private final ScaleGestureDetector scaleGestureDetector;

	private boolean mIsBeingDragged;
	private VelocityTracker mVelocityTracker;
	private OverScroller mScroller;
	private int mActivePointerId;
	private int mLastMotionX;
	private int mLastMotionY;
	private int mTouchSlop;


	public XYScrollView(Context context) {
		super(context);
		gestureDetector = new GestureDetectorCompat(context, gestureListener);
		scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
		init();
	}

	public XYScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public XYScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		gestureDetector = new GestureDetectorCompat(context, gestureListener);
		scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
		init();
	}

	private void init() {
		ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleGestureDetector, true);
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * Shortcut the most recurring case: the user is in the dragging
		 * state and he is moving his finger.  We want to intercept this
		 * motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
			return true;
		}

		/*
		 * Don't try to intercept touch if we can't scroll anyway.
		 */
		if (getScrollY() == 0 && !canScrollVertically(1)
				&& getScrollX() == 0 && !canScrollHorizontally(1)) {
			return true;
		}

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_MOVE: {
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
				 * whether the user has moved far enough from his original down touch.
				 */

				/*
				 * Locally do absolute value. mLastMotionY is set to the y value
				 * of the down event.
				 */
				final int activePointerId = mActivePointerId;
				if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
					// If we don't have a valid id, the touch down wasn't on content.
					break;
				}

				final int pointerIndex = ev.findPointerIndex(activePointerId);
				if (pointerIndex == -1) {
                    Timber.e("Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
					break;
				}

				final int y = (int) ev.getY(pointerIndex);
				final int yDiff = Math.abs(y - mLastMotionY);
				if (yDiff > mTouchSlop) {
					mIsBeingDragged = true;
					mLastMotionY = y;
					initOrResetVelocityTracker();
					mVelocityTracker.addMovement(ev);
					final ViewParent parent = getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {
				final int x = (int) ev.getX();
				final int y = (int) ev.getY();
				if (!inChild(x, y)) {
					mIsBeingDragged = false;
					recycleVelocityTracker();
					break;
				}

				/*
				 * Remember location of down touch.
				 * ACTION_DOWN always refers to pointer index 0.
				 */
				mLastMotionX = x;
				mLastMotionY = y;
				mActivePointerId = ev.getPointerId(0);

				initOrResetVelocityTracker();
				mVelocityTracker.addMovement(ev);
				/*
				 * If being flinged and user touches the screen, initiate drag;
				 * otherwise don't.  mScroller.isFinished should be false when
				 * being flinged.
				 */
				mIsBeingDragged = !mScroller.isFinished();
				//startNestedScroll(SCROLL_AXIS_VERTICAL | SCROLL_AXIS_HORIZONTAL);
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				/* Release the drag */
				mIsBeingDragged = false;
				mActivePointerId = MotionEvent.INVALID_POINTER_ID ;
				recycleVelocityTracker();
//				if (mScroller.springBack(mScrollX, mScrollY, 0, 0, 0, getScrollRange())) {
//					postInvalidateOnAnimation();
//				}
				//stopNestedScroll();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp(ev);
				break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mIsBeingDragged;
	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
				MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionY = (int) ev.getY(newPointerIndex);
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event)
				|| scaleGestureDetector.onTouchEvent(event)
				|| super.onTouchEvent(event);
	}

	public View getChildView() {
		return getChildAt(0);
	}

	private boolean inChild(int x, int y) {
		if (getChildCount() > 0) {
			final View child = getChildAt(0);
			final int scrollY = child.getScrollY();
			return !(y < child.getTop() - scrollY
					|| y >= child.getBottom() - scrollY
					|| x < child.getLeft()
					|| x >= child.getRight());
		}
		return false;
	}

	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return super.onDown(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Timber.i("distx: " + distanceX + ", distY: " + distanceY);
			scrollBy((int)distanceX, (int)distanceY);
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

	};
	private final ScaleGestureDetector.SimpleOnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

		float originScaleX;
		float originScaleY;

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			if (getChildView() == null) {
				return false;
			}
			originScaleX = getChildView().getScaleX();
			originScaleY = getChildView().getScaleY();
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.i("ads", "onScale(): " + detector.getScaleFactor());
			float scaleFactor = detector.getScaleFactor();
			getChildView().setScaleX(originScaleX * scaleFactor);
			getChildView().setScaleY(originScaleY * scaleFactor);
			return false;
		}
	};
}
