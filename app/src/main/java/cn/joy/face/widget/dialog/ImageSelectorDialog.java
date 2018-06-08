package cn.joy.face.widget.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.joy.face.R;
import cn.joy.face.utils.image.ImagePicker;

/**
 * **********************
 * Author: Joy
 * Date:   2017-12-21
 * Time:   18:32
 * **********************
 */

public class ImageSelectorDialog extends BottomSheetDialog implements ImagePicker.ImagePickCallback {

	private boolean crop = true;
	private ImagePicker.ImagePickCallback mCallback;

	public ImageSelectorDialog(@NonNull Context context) {
		super(context);
		setContentView(R.layout.dialog_select_image);
		ButterKnife.bind(this);
	}

	public ImageSelectorDialog noCrop() {
		crop = false;
		return this;
	}

	public ImageSelectorDialog listener(ImagePicker.ImagePickCallback mCallback) {
		this.mCallback = mCallback;
		return this;
	}

	@OnClick({R.id.btnCamera, R.id.btnAlbum})
	public void onViewClicked(View view) {
		ImagePicker picker = ImagePicker.from(getContext()).listener(this);
		if (crop)
			picker.cropSquare();
		switch (view.getId()) {
			case R.id.btnCamera:
				picker.camera(crop);
				break;
			case R.id.btnAlbum:
				picker.single(crop);
				break;
		}
		picker.picker();
		dismiss();
	}

	@OnClick(R.id.btnCancel)
	public void onCloseClick() {
		dismiss();
	}

	@Override
	public void onPicked(List<String> images) {
		if (mCallback != null) {
			mCallback.onPicked(images);
		}
		dismiss();
	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onError() {

	}

}
