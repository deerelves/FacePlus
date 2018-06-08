package cn.joy.face.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;

import cn.joy.face.R;
import cn.joy.plus.tools.image.broswer.ImagePhotoViewBrowser;
import uk.co.senab.photoview.PhotoView;

/**
 * **********************
 * Author: Joy
 * Date:   2018-03-01
 * Time:   19:54
 * **********************
 */

public class ImageBrowserView extends ImagePhotoViewBrowser<String> {

	public ImageBrowserView(Context context) {
		super(context);
	}

	public ImageBrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onLoadingImage(String image, PhotoView view, int position) {
		Glide.with(getContext()).load(image).placeholder(R.drawable.transparent).error(R.drawable.transparent).into(view);
//		ImageLoader.display(getContext(), view, image, R.drawable.transparent, R.drawable.transparent);
	}
}
