package cn.joy.plus.tools.image.selector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import java.util.ArrayList;

import cn.joy.plus.R;
import cn.joy.plus.tools.image.ImageTools;
import cn.joy.plus.tools.image.selector.crop.CropOverlayView;
import cn.joy.plus.tools.image.selector.crop.ImageCropperActivity;

/**
 * **********************
 * Author: yu
 * Date:   2015/7/22
 * Time:   12:11
 * **********************
 */
public class ImageSelectorActivity extends FragmentActivity implements MultiImageSelectorFragment.ImageSelectorCallBack {

	private static final String TAG = "ImageSelectorActivity";
	private static final int REQUEST_CROPPER_IMAGE = 0x2;

	/** 图片选择广播 */
	public static final String ACTION_IMAGE_SELECTOR = "ACTION_IMAGE_SELECTOR";

	public static final String EXTRA_IMAGE_SELECTOR_MODE = ImageSelectorConstants.EXTRA_IMAGE_SELECT_MODE;
	/** 单选 */
	public static final int IMAGE_SELECTOR_MODE_SINGLE_CROP = 0x3;
	/** 单选 + 切图 */
	public static final int IMAGE_SELECTOR_MODE_SINGLE = ImageSelectorConstants.IMAGE_SELECT_MODE_SINGLE;
	/** 多选 */
	public static final int IMAGE_SELECTOR_MODE_MULTI = ImageSelectorConstants.IMAGE_SELECT_MODE_MULTI;

	/** 单次最多选择数量 */
	public static final String EXTRA_IMAGE_SELECTOR_COUNT = ImageSelectorConstants.EXTRA_IMAGE_SELECT_COUNT;
	/** 单次最多选择数量:9 */
	private static final int IMAGE_SELECTOR_COUNT_DEFAULT = ImageSelectorConstants.IMAGE_SELECT_COUNT_DEFAULT;

	/** 是否显示拍照 */
	public static final String EXTRA_IMAGE_SELECTOR_SHOW_CAMERA = ImageSelectorConstants.EXTRA_IMAGE_SELECT_SHOW_CAMERA;
	/** 拍照后图片保存路径 */
	public static final String EXTRA_IMAGE_SELECTOR_CAMERA_SAVE_PATH = ImageSelectorConstants.EXTRA_IMAGE_SELECT_CAMERA_SAVE_PATH;
	/** 需要选择的图片目录 */
	public static final String EXTRA_IMAGE_SELECTOR_PATH = ImageSelectorConstants.EXTRA_IMAGE_SELECT_PATH;
	/** 选择后的图片的路径 */
	public static final String EXTRA_RESULT_IMAGE_SELECTED_PATH = ImageSelectorConstants.RESULT_IMAGE_PATH;
	public static final String EXTRA_RESULT_IMAGE_SELECTED_MODE = EXTRA_IMAGE_SELECTOR_MODE;
	public static final String EXTRA_RESULT_IMAGE_SELECTED_SOURCE = "EXTRA_RESULT_IMAGE_SELECTED_SOURCE";
	/** 图片模式 */
	public static final int IMAGE_SELECTED_SOURCE_CAMERA = MultiImageSelectorFragment.ImageSelectSource.Camera.getTypeId();
	public static final int IMAGE_SELECTED_SOURCE_ALBUM = MultiImageSelectorFragment.ImageSelectSource.Album.getTypeId();

	public static final String EXTRA_IMAGE_SELECTOR_CROP_SHAPE = "EXTRA_IMAGE_SELECTOR";
	public static final String EXTRA_IMAGE_SELECTOR_CROP_WIDTH_HEIGHT_SCALE = "_scale";

	public static final int IMAGE_SELECTOR_CROP_SHAPE_SQUARE = CropOverlayView.OVERLAY_SHAPE_SQUARE;
	public static final int IMAGE_SELECTOR_CROP_SHAPE_CIRCLE = CropOverlayView.OVERLAY_SHAPE_CIRCLE;

	/** 图片裁剪广播接收器 */
	ImageCropReceiver imgCropReceiver;

	private int selectorMode;
	private int selectorCount;
	private String selectorPath;
	private boolean isShowCamera;
	private String cameraSavePath;
	private int cropShape;
	private float cropWHScale;

	/** 当前的图片来源,{@link MultiImageSelectorFragment.ImageSelectSource} */
	private int currentSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_selector_activity);
		initIntent();
		initImageSelectorFragment();
	}

	void initIntent() {
		selectorMode = getIntent().getIntExtra(EXTRA_IMAGE_SELECTOR_MODE, IMAGE_SELECTOR_MODE_MULTI);
		selectorCount = getIntent().getIntExtra(EXTRA_IMAGE_SELECTOR_COUNT, IMAGE_SELECTOR_COUNT_DEFAULT);
		selectorPath = getIntent().getStringExtra(EXTRA_IMAGE_SELECTOR_PATH);
		isShowCamera = getIntent().getBooleanExtra(EXTRA_IMAGE_SELECTOR_SHOW_CAMERA, true);
		cameraSavePath = getIntent().getStringExtra(EXTRA_IMAGE_SELECTOR_CAMERA_SAVE_PATH);
		cropShape = getIntent().getIntExtra(EXTRA_IMAGE_SELECTOR_CROP_SHAPE, IMAGE_SELECTOR_CROP_SHAPE_SQUARE);
		cropWHScale = getIntent().getFloatExtra(EXTRA_IMAGE_SELECTOR_CROP_WIDTH_HEIGHT_SCALE, 1.0f);
	}

	void initImageSelectorFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(EXTRA_IMAGE_SELECTOR_MODE, selectorMode == IMAGE_SELECTOR_MODE_SINGLE_CROP ? IMAGE_SELECTOR_MODE_SINGLE : selectorMode);
		bundle.putInt(EXTRA_IMAGE_SELECTOR_COUNT, selectorCount);
		bundle.putBoolean(EXTRA_IMAGE_SELECTOR_SHOW_CAMERA, isShowCamera);
		bundle.putString(EXTRA_IMAGE_SELECTOR_CAMERA_SAVE_PATH, cameraSavePath);
		if (!TextUtils.isEmpty(selectorPath))
			bundle.putString(EXTRA_IMAGE_SELECTOR_PATH, selectorPath);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.frame, Fragment.instantiate(ImageSelectorActivity.this, MultiImageSelectorFragment.class.getName(), bundle))
				.commit();
	}

	@Override
	public void onImagesSelected(MultiImageSelectorFragment.ImageSelectSource source, final ArrayList<String> images) {
		final Intent resIntent = new Intent();
		resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_MODE, selectorMode);
		resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_SOURCE, currentSource = source.getTypeId());
		switch (selectorMode) {
			case IMAGE_SELECTOR_MODE_MULTI:
				new ImageRotateCheckTask(new ImageRotateCheckTask.CheckHandler() {
					@Override
					public void onCheckFinish() {
						resIntent.putStringArrayListExtra(EXTRA_RESULT_IMAGE_SELECTED_PATH, images);
						setResult(RESULT_OK, resIntent);
						resIntent.setAction(ACTION_IMAGE_SELECTOR);
						sendBroadcast(resIntent);
						finish();
					}
				}).execute(images.toArray(new String[images.size()]));
				break;
			case IMAGE_SELECTOR_MODE_SINGLE:
				new ImageRotateCheckTask(new ImageRotateCheckTask.CheckHandler() {
					@Override
					public void onCheckFinish() {
						resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_PATH, images.get(0));
						setResult(RESULT_OK, resIntent);
						resIntent.setAction(ACTION_IMAGE_SELECTOR);
						sendBroadcast(resIntent);
						finish();
					}
				}).execute(images.toArray(new String[images.size()]));
				break;
			/** 获取到图片并且需要切割 */
			case IMAGE_SELECTOR_MODE_SINGLE_CROP:
				Log.e(TAG, "start crop image, image path is " + images.get(0));
				Intent intent = new Intent(ImageSelectorActivity.this, ImageCropperActivity.class);
				intent.putExtra(ImageCropperActivity.EXTRA_IMAGE_PATH, images.get(0));
				intent.putExtra(ImageCropperActivity.EXTRA_IMAGE_CROP_SHAPE, cropShape);
				intent.putExtra(ImageCropperActivity.EXTRA_IMAGE_CROP_WIDTH_HEIGHT_SCALE, cropWHScale);
				startActivityForResult(intent, REQUEST_CROPPER_IMAGE);
				registerImageCropReceiver();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CROPPER_IMAGE) {
			String path = data.getStringExtra(ImageCropperActivity.EXTRA_IMAGE_PATH);
			Intent resIntent = new Intent();
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_PATH, path);
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_MODE, selectorMode);
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_SOURCE, currentSource);
			setResult(RESULT_OK, resIntent);
			resIntent.setAction(ACTION_IMAGE_SELECTOR);
			sendBroadcast(resIntent);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	void registerImageCropReceiver() {
		if (imgCropReceiver == null)
			imgCropReceiver = new ImageCropReceiver();
		imgCropReceiver.register();
	}

	class ImageCropReceiver extends BroadcastReceiver {

		public void register() {
			IntentFilter filter = new IntentFilter(ImageCropperActivity.ACTION_IMAGE_CROPPER);
			ImageSelectorActivity.this.registerReceiver(this, filter);
		}

		public void unregister() {
			ImageSelectorActivity.this.unregisterReceiver(this);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String path = intent.getStringExtra(ImageCropperActivity.EXTRA_IMAGE_PATH);
			Intent resIntent = new Intent();
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_PATH, path);
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_MODE, selectorMode);
			resIntent.putExtra(EXTRA_RESULT_IMAGE_SELECTED_SOURCE, currentSource);
			ImageSelectorActivity.this.setResult(Activity.RESULT_OK, resIntent);
			resIntent.setAction(ACTION_IMAGE_SELECTOR);
			sendBroadcast(resIntent);
			unregister();
			finish();
		}
	}

	/**
	 * 图片角度调整工具
	 */
	static class ImageRotateCheckTask extends AsyncTask<String, Void, Void> {

		interface CheckHandler {
			void onCheckFinish();
		}

		CheckHandler handler;

		ImageRotateCheckTask(CheckHandler handler) {
			this.handler = handler;
		}

		@Override
		protected Void doInBackground(String... params) {
			for (String param : params) {
				if (ImageTools.getExifOrientation(param) != 0) {
					ImageTools.rotateImage(param);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (handler != null) {
				handler.onCheckFinish();
			}
		}
	}
}
