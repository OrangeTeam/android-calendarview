package net.simonvt.calendarview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * This class should fix some ListView issues that make setSelectionFromTop() method not work.
 *
 * 1. If setAdapter() and setSelectionFromTop() are called outside touch mode and the layout pass (layoutChildren()) is
 * in touch mode, setting selection from top will not work when handleDataChanged() is called inside layoutChildren().
 *
 * This happens, because setSelectionFromTop() (outside touch mode) changes mNextSelectedPosition variable
 * (ListView L1934), but handleDataChanged() (called inside layoutChildren(), ListView L1545) will then change
 * mNextSelectedPosition to INVALID_POSITION (AbsListView L5558), since it is now in touch mode and
 * mResurrectToPosition == -1 (AbsListView L5547). The position passed to fillSpecific() (ListView L1620) will be -1,
 * making the selection from top fail.
 *
 * 2. If the setAdapter() is called outside touch mode and setSelectionFromTop() and layoutChildren() are called in
 * touch mode, setting the selection from top will also not work. Posting setSelectionFromTop() won't work in this case.
 *
 * This happens, because mNextSelectedPosition (indirectly) trumps mResurrectToPosition. When setting the adapter
 * outside touch mode, mNextSelectedPosition is set to 0 (ListView L495), because lookForSelectablePosition() won't
 * return INVALID_POSITION (it will probably return 0) since we are not in touch mode (ListView L1998). Then, inside
 * layoutChildren(), setSelectedPositionInt() is called with mNextSelectedPosition as argument (ListView L1563),
 * changing mSelectedPosition. Later, by calling reconcileSelectedPosition() (ListView L1620), mResurrectToPosition
 * won't be used as it should (AbsListView L5212). The position passed to fillSpecific() (ListView L1620) will be 0,
 * making the selection from top fail.
 *
 * This class overrides isInTouchMode() method, and forces it to return the touch mode value that was set when
 * setSelectionFromTop is called, until the end of the layoutChildren() method.
 * This will probably have some side-effects, though.
 *
 */
public class TopSelectionListView extends ListView {
	private Boolean mForceTouchMode;

	public TopSelectionListView(Context context) {
		super(context);
	}

	public TopSelectionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TopSelectionListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		super.setSelectionFromTop(position, y);
		mForceTouchMode = isInTouchMode();
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		mForceTouchMode = null;
	}

	@Override
	public boolean isInTouchMode() {
		return mForceTouchMode != null ?
				mForceTouchMode :
				super.isInTouchMode();
	}
}
