package cn.joy.face.utils.image;

import android.content.Context;
import android.content.Intent;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.util.Arrays;
import java.util.List;

import cn.joy.plus.tools.image.selector.ImageSelectorActivity;


/**
 * User: Joy
 * Date: 2016/11/8
 * Time: 17:34
 * 图片选择器
 */

public class ImagePicker {

	private static final int MODE_CAMERA = ImagePickerActivity.IMAGE_PICKER_MODE_CAMERA;
	private static final int MODE_CAMERA_CROP = ImagePickerActivity.IMAGE_PICKER_MODE_CAMERA_CROP;
	private static final int MODE_SINGLE = ImagePickerActivity.IMAGE_PICKER_MODE_GALLERY_SINGLE;
	private static final int MODE_SINGLE_CROP = ImagePickerActivity.IMAGE_PICKER_MODE_GALLERY_SINGLE_CROP;
	private static final int MODE_MULTI = ImagePickerActivity.IMAGE_PICKER_MODE_GALLERY_MULTI;

	private Context context;
	private int count = 0;
	private int mode;
	private int cropMode = ImageSelectorActivity.IMAGE_SELECTOR_CROP_SHAPE_SQUARE;
	private float cropScale = 0;

	private ImagePickCallback mCallback;

	public static ImagePicker from(Context context) {
		return new ImagePicker(context);
	}

	private ImagePicker(Context context) {
		this.context = context;
		RxBus.get().register(this);
	}

	/**
	 * 单选
	 * @param crop 是否裁剪
	 */
	public ImagePicker single(boolean crop) {
		mode = crop ? MODE_SINGLE_CROP : MODE_SINGLE;
		return this;
	}

	/**
	 * 多选
	 */
	public ImagePicker multi(int count) {
		this.count = count;
		mode = MODE_MULTI;
		return this;
	}

	// 图片裁剪宽高比
	public ImagePicker cropScale(float scale){
		this.cropScale = scale;
		return this;
	}

	/**
	 * 调用照相机
	 * @param crop 是否裁剪
	 */
	public ImagePicker camera(boolean crop) {
		mode = crop ? MODE_CAMERA_CROP : MODE_CAMERA;
		return this;
	}

	/**
	 * 调用圆形裁剪
	 */
	public ImagePicker cropCircle() {
		cropMode = ImageSelectorActivity.IMAGE_SELECTOR_CROP_SHAPE_CIRCLE;
		return this;
	}

	public ImagePicker cropSquare(){
		cropMode = ImageSelectorActivity.IMAGE_SELECTOR_CROP_SHAPE_SQUARE;
		return this;
	}

	public ImagePicker listener(ImagePickCallback callback) {
		this.mCallback = callback;
		return this;
	}

	public void picker() {
		Intent intent = new Intent(context, ImagePickerActivity.class);
		intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_COUNT, count);
		intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_PICKER_MODE, mode);
		intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_CROP_SHAPE, cropMode);
		if(cropScale > 0f){
			intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_CROP_WIDTH_HEIGHT_SCALE, cropScale);
		}
		context.startActivity(intent);
	}

	@Subscribe(tags = {@Tag(ImagePickerActivity.RX_EVENT_IMAGE)},thread = EventThread.MAIN_THREAD)
	public void onPicker(String[] images) {
		if (images == null) {
			if (mCallback != null)
				mCallback.onError();
		} else if (images.length == 0) {
			if (mCallback != null)
				mCallback.onCancel();
		} else {
			if (mCallback != null) {
				mCallback.onPicked(Arrays.asList(images));
			}
		}
		RxBus.get().unregister(this);
	}

	public interface ImagePickCallback {
		void onPicked(List<String> images);

		void onCancel();

		void onError();
	}

	public static class SimpleImagePickCallback implements ImagePickCallback{

		@Override
		public void onPicked(List<String> images) {

		}

		@Override
		public void onCancel() {

		}

		@Override
		public void onError() {

		}
	}
}
