package cn.joy.plus.tools.image.broswer;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


public abstract class HackPageAdapter<T> extends PagerAdapter {

	protected List<T> mImages;
	protected OnItemChangeListener mListener;
	protected PhotoViewAttacher.OnViewTapListener vtListener;
	protected ScaleType scaleType;
	protected Context context;

	public HackPageAdapter(List<T> mImages) {
		this.mImages = mImages;
	}

	public HackPageAdapter(Context context, List<T> mImages) {
		this.context = context;
		this.mImages = mImages;
	}

	@Override
	public int getCount() {
		return mImages.size();
	}

	public List<T> getList() {
		return mImages;
	}

	public void setScaleType(ScaleType sc) {
		this.scaleType = sc;
	}

	public ScaleType getScaleType() {
		return scaleType;
	}

	public void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener) {
		this.vtListener = listener;
	}

	@Override
	public View instantiateItem(ViewGroup container, final int position) {
		final PhotoView photoView = new PhotoView(container.getContext());
		if (scaleType != null)
			photoView.setScaleType(scaleType);
		photoView.setOnViewTapListener((view, x, y) -> {
			if (vtListener != null)
				vtListener.onViewTap(view, x, y);
		});
		// Now just add PhotoView to ViewPager and return it
		container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		photoView.post(() -> onLoadingImage(mImages.get(position), photoView, position));
		return photoView;
	}

	public abstract void onLoadingImage(T imageUri, PhotoView photoView, int position);

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		if (mListener != null)
			mListener.onItemChange(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void setOnItemChangeListener(OnItemChangeListener listener) {
		mListener = listener;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public interface OnItemChangeListener {
		void onItemChange(int currentPosition);
	}
}