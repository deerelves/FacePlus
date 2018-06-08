package cn.joy.face.utils.image;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.joy.face.ParentActivity;
import cn.joy.face.R;
import cn.joy.face.utils.DeviceHelper;
import cn.joy.face.widget.ImageBrowserView;

/**
 * **********************
 * Author: Joy
 * Date:   2017-12-20
 * Time:   16:53
 * **********************
 */

public class ImageLoader {


	static class SimpleRequestListener<T, R> implements RequestListener<T, R> {
		@Override
		public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
			return false;
		}

		@Override
		public boolean onResourceReady(R resource, T model, Target<R> target, boolean isFromMemoryCache, boolean isFirstResource) {
			return false;
		}
	}


	public static void display(Context context, ImageView view, String url, int loading, int error) {
		if (checkContext(context))
			Glide.with(context).load(url).asBitmap().placeholder(loading).error(error).transform(createCenterCrop(context)).into(view);
	}

	public static void display(Context context, ImageView view, File file) {
		if (checkContext(context))
			Glide.with(context)
					.load(file)
					.asBitmap()
					.transform(createCenterCrop(context))
					.into(view);
	}

	public static void display(Context context, ImageView view, int resId) {
		if (checkContext(context))
			Glide.with(context).load(resId).asBitmap().transform(createCenterCrop(context)).into(view);
	}


	private static Bitmap createVideoThumbnail(String filePath, int kind) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			if (filePath.startsWith("http://") || filePath.startsWith("https://") || filePath.startsWith("widevine://")) {
				retriever.setDataSource(filePath, new HashMap<>());
			} else {
				retriever.setDataSource(filePath);
			}
			bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //retriever.getFrameAtTime(-1);
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
			ex.printStackTrace();
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
			ex.printStackTrace();
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
				ex.printStackTrace();
			}
		}

		if (bitmap == null) {
			return null;
		}

		if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {//压缩图片 开始处
			// Scale down the bitmap if it's too large.
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int max = Math.max(width, height);
			if (max > 512) {
				float scale = 512f / max;
				int w = Math.round(scale * width);
				int h = Math.round(scale * height);
				bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
			}//压缩图片 结束处
		} else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, 96, 96, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	private static CenterCrop createCenterCrop(Context context) {
		return new CenterCrop(context);
	}


	private static boolean checkContext(Context context) {
		if (context == null)
			return false;
		if (context instanceof Activity) {
			Activity activity = (Activity) context;
			if (activity.isFinishing())
				return false;
		}
		return true;
	}

	private static BitmapPool getPool(Context context) {
		return Glide.get(context).getBitmapPool();
	}


	public static void browser(ParentActivity activity, String url) {
		List<String> list = new ArrayList<>();
		list.add(url);
		browser(activity, list);
	}

	public static void browser(ParentActivity activity, List<String> imageUrl) {
		View parent = LayoutInflater.from(activity).inflate(R.layout.dialog_image_browser, null);
		ImageBrowserView mImageBrowserView = parent.findViewById(R.id.image);
		mImageBrowserView.setImageResource(imageUrl);
		mImageBrowserView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		final Dialog dialog = new AlertDialog.Builder(activity, R.style.APP_Dialog_Loading).create();
		dialog.setOnShowListener(dialog1 -> {
			ViewGroup.LayoutParams params = mImageBrowserView.getLayoutParams();
			params.width = DeviceHelper.getScreenWidth();
			params.height = DeviceHelper.getScreenHeight();
			mImageBrowserView.setLayoutParams(params);
		});
		dialog.show();
		dialog.setContentView(parent);

		//noinspection ConstantConditions
		dialog.getWindow().setGravity(Gravity.FILL);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			dialog.getWindow().getDecorView().setSystemUiVisibility(option);
		}
		//		dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		//		dialog.getWindow().getAttributes().flags = dialog.getWindow()
		//				.getAttributes().flags | WindowManager.LayoutParams.FLAG_FULLSCREEN ;


		dialog.setCanceledOnTouchOutside(false);
		mImageBrowserView.setOnViewTapClick((view, x, y) -> dialog.dismiss());
		mImageBrowserView.setOnBackPressedListener((keyCode, event) -> {
			dialog.dismiss();
			return true;
		});
	}

	public static Bitmap getDrawableCache(View view) {
		if (view == null) {
			return null;
		}
		Bitmap screenshot;
		screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
		Canvas c = new Canvas(screenshot);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		return screenshot;
	}
}
