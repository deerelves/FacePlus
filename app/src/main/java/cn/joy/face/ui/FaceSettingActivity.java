package cn.joy.face.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.joy.face.ParentActivity;
import cn.joy.face.R;
import cn.joy.face.utils.FaceManager;
import cn.joy.face.utils.image.ImagePicker;
import cn.joy.face.widget.dialog.ImageSelectorDialog;

/**
 * Author: Joy
 * Date:   2018/6/8
 */

public class FaceSettingActivity extends ParentActivity {

	private static final String TAG = "FaceSetting";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 全屏|保持屏幕常亮
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_face_setting);
		ButterKnife.bind(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	@OnClick({R.id.btnSwitchCamera, R.id.btnInsert, R.id.btnBrowser, R.id.btnCancel})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.btnSwitchCamera:
				break;
			case R.id.btnInsert:
				new ImageSelectorDialog(getContext()).noCrop().listener(new ImagePicker.SimpleImagePickCallback() {
					@Override
					public void onPicked(List<String> images) {
						super.onPicked(images);
						String path = images.get(0);
						FaceManager.getInstance().addFace(path, "") //
								.subscribe(faceSetModel -> showMessage("添加成功"), throwable -> {
									String err = "添加出错:" + throwable.getMessage();
									showMessage(err);
									Log.d(TAG, err);
								});
					}
				}).show();
				break;
			case R.id.btnBrowser:
				startActivity(new Intent(getContext(), FaceImageActivity.class));
				break;
			case R.id.btnCancel:
				finish();
				break;
		}
	}
}
