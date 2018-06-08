package cn.joy.face.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.joy.face.Constants;
import cn.joy.face.ParentActivity;
import cn.joy.face.R;
import cn.joy.face.utils.FaceManager;
import cn.joy.face.widget.CameraFaceDetectionView2;
import cn.joy.face.widget.ClockView;
import cn.joy.face.widget.OnFaceDetectorListener;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class FaceCheckActivity extends ParentActivity implements OnFaceDetectorListener {

	private static final String TAG = "FC";

	private static SimpleDateFormat mFormat = new SimpleDateFormat("MM月dd日  EEE", Locale.CHINA);

	@BindView(R.id.layoutLoading)
	View mLayoutLoading;
	@BindView(R.id.cameraView)
	CameraFaceDetectionView2 mCameraView;
	@BindView(R.id.clockView)
	ClockView mClockView;
	@BindView(R.id.textDate)
	TextView mTextDate;
	@BindView(R.id.textMsg)
	TextView mTextMsg;

	private Subscription mSubscription;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		// 全屏|保持屏幕常亮
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_check);
		ButterKnife.bind(this);

		mCameraView.setOnFaceDetectorListener(this);

		FaceManager.getInstance().createFaceSet();
		RxBus.get().register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		RxBus.get().unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCameraView.init();
		mCameraView.enableView();
		mClockView.performAnimation();
		mTextDate.setText(mFormat.format(new Date(System.currentTimeMillis())));
	}

	@Override
	protected void onPause() {
		super.onPause();
		mClockView.cancelAnimation();
	}

	@Override
	public void onFace(Mat mat, Rect rect) {
		if (mSubscription != null) {
			mSubscription.unsubscribe();
		}
		// 隐藏Loading页面
		runOnUiThread(() -> {
			if (mLayoutLoading.getVisibility() != View.GONE) {
				mLayoutLoading.setVisibility(View.GONE);
			}
		});
		// 三秒后重新显示Loading页面
		mSubscription = Observable.timer(3, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
			mLayoutLoading.setVisibility(View.VISIBLE);
		});
		FaceManager.getInstance().searchFace(mat);
	}

	@Override
	public void onFaceFinish() {
		FaceManager.getInstance().finishSearchFace();
	}

	@Subscribe(tags = {@Tag(Constants.RX_TAG_FACE_SEARCH_RESULT)}, thread = EventThread.MAIN_THREAD)
	public void onSearchFaceSuccess(Boolean success) {
		mCameraView.disableView();
		Observable.timer(5, TimeUnit.SECONDS) //
				.subscribeOn(Schedulers.newThread()) //
				.observeOn(AndroidSchedulers.mainThread()) //
				.subscribe(aLong -> {
				}, Throwable::printStackTrace, () -> {
					// 显示Loading页面
					if (mLayoutLoading.getVisibility() != View.VISIBLE) {
						mLayoutLoading.setVisibility(View.VISIBLE);
					}
					mTextMsg.setVisibility(View.GONE);
					mCameraView.enableView();
					Log.d(TAG, "重新开始工作！");
				});
		// UI改变
		mTextMsg.setVisibility(View.VISIBLE);
		MediaPlayer mediaPlayer;
		if (success) {
			mTextMsg.setText("欢迎回家\n已为您开门");
			mediaPlayer = MediaPlayer.create(getContext(), R.raw.media_welcome);
		} else {
			mTextMsg.setText("非常抱歉\n未能查到您的信息");
			mediaPlayer = MediaPlayer.create(getContext(), R.raw.media_error);
		}
		mediaPlayer.setOnCompletionListener(mp -> {
			if(mp != null){
				mp.stop();
				mp.release();
			}
		});
		mediaPlayer.start();
	}

	@OnClick(R.id.btnSetting)
	public void onViewClicked() {
		startActivity(new Intent(getContext(), FaceSettingActivity.class));
	}
}
