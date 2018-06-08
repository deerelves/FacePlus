package cn.joy.plus.tools.image.broswer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public abstract class ImagePhotoViewBrowser<T> extends FrameLayout {

	private HackyViewPager mViewPager;
	private HackPageAdapter<T> mAdapter;
	private OnBackPressedListener mListener;

	public ImagePhotoViewBrowser(Context context) {
		super(context);
		initView();
	}

	public ImagePhotoViewBrowser(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setBackgroundColor(getResources().getColor(android.R.color.black));
		setFocusable(true);
		setFocusableInTouchMode(true);
		mViewPager = new HackyViewPager(getContext());
		addView(mViewPager, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mViewPager.setAdapter(mAdapter = onCreateHackPageAdapter());
		setScaleType(ScaleType.CENTER_INSIDE);
	}

	protected HackPageAdapter<T> onCreateHackPageAdapter() {
		return new HackPageAdapter<T>(getContext(), new ArrayList<T>()) {
			@Override
			public void onLoadingImage(T imageUri, PhotoView photoView, int position) {
				ImagePhotoViewBrowser.this.onLoadingImage(imageUri, photoView, position);
			}
		};
	}

	/**
	 * 设置页面滑动事件
	 * @param listener 监听
	 */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mViewPager.addOnPageChangeListener(listener);
	}

	/**
	 * 设置图片加载的ScaleType
	 * @param mScaleType 图片显示方式
	 */
	public void setScaleType(ScaleType mScaleType) {
		mAdapter.setScaleType(mScaleType);
	}

	public ScaleType getScaleType() {
		return mAdapter.getScaleType();
	}

	/**
	 * 设置图片资源列表
	 */
	public void setImageResource(T[] res) {
		setImageResource(res, 0);
	}

	public void setImageResource(T[] res, int position) {
		setImageResource(new ArrayList<>(Arrays.asList(res)), position);
	}

	public void setImageResource(List<T> list) {
		setImageResource(list, 0);
	}

	public void setImageResource(List<T> list, int position) {
		mAdapter.getList().clear();
		mAdapter.getList().addAll(list);
		mAdapter.notifyDataSetChanged();
		mViewPager.setCurrentItem(position > mAdapter.getCount() - 1 ? mAdapter.getCount() - 1 : position, false);
	}

	public abstract void onLoadingImage(T t, PhotoView view, int position);

	/**
	 * 设置图片单击事件
	 * @param mListener mListener
	 */
	public void setOnViewTapClick(PhotoViewAttacher.OnViewTapListener mListener) {
		mAdapter.setOnViewTapListener(mListener);
	}

	public T getCurrentImageResource() {
		return mAdapter.getList().get(getCurrentPosition());
	}

	public int getCurrentPosition() {
		return mViewPager.getCurrentItem();
	}

	public int getImageCount() {
		return mAdapter.getCount();
	}

	/**
	 * 按键监听
	 */
	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_BACK && mListener != null && mListener.onBack(keyCode, event);
	}

	public void setOnBackPressedListener(OnBackPressedListener listener) {
		mListener = listener;
	}

	public interface OnBackPressedListener {
		boolean onBack(int keyCode, KeyEvent event);
	}
}

