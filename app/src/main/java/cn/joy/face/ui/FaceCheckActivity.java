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

	private static final int STATUS_STOP = -1;
	private static final int STATUS_WAIT = 0;
	private static final int STATUS_SEARCHING = 1;
	private static final int STATUS_SUCCESS = 2;
	private static final int STATUS_ERROR = 3;

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

	private int mStatus = STATUS_STOP;
	private Subscription mSubscription;

	private boolean isActivityRunning = false;

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
		mCameraView.init();

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
		isActivityRunning = true;
		onStatusChanged(STATUS_WAIT);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActivityRunning = false;
		onStatusChanged(STATUS_STOP);
	}

	@Override
	public void onFace(Mat mat, Rect rect) {
		runOnUiThread(() -> onStatusChanged(STATUS_SEARCHING));
		// 状态改变
		if (mSubscription != null) {
			mSubscription.unsubscribe();
		}
		// 如果三秒内没有再次获取到人脸信息，改变状态为等待状态
		mSubscription = Observable.timer(2, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aLong -> onStatusChanged(STATUS_WAIT));
		FaceManager.getInstance().searchFace(mat);
	}

	@Override
	public void onFaceFinish() {
		// 状态改变
		if (mSubscription != null) {
			mSubscription.unsubscribe();
		}
		FaceManager.getInstance().finishSearchFace();
	}

	@Subscribe(tags = {@Tag(Constants.RX_TAG_FACE_SEARCH_RESULT)}, thread = EventThread.MAIN_THREAD)
	public void onSearchFaceSuccess(Boolean success) {
		// 如果当前状态为搜索状态，才能改变为结果状态
		if (mStatus == STATUS_SEARCHING)
			onStatusChanged(success ? STATUS_SUCCESS : STATUS_ERROR);
	}

	private void onStatusChanged(int status) {
		onStatusChanged(status, false);
	}

	/**
	 * 当前状态已经发生改变
	 */
	private void onStatusChanged(int status, boolean force) {
		// 如果当前状态与新状态相同或当前状态为搜索结果状态之后 UI不做改变
		if (!force && (mStatus == status || mStatus > STATUS_SEARCHING))
			return;
		Log.d(TAG, "状态改变 --" + status);
		mStatus = status;
		switch (mStatus) {
			case STATUS_STOP:
				mTextMsg.setVisibility(View.GONE);
				mLayoutLoading.setVisibility(View.VISIBLE);
				mClockView.cancelAnimation();
				mCameraView.disableView();
				break;
			case STATUS_WAIT:
				mTextMsg.setVisibility(View.GONE);
				mLayoutLoading.setVisibility(View.VISIBLE);
				mClockView.performAnimation();
				mTextDate.setText(mFormat.format(new Date(System.currentTimeMillis())));
				mCameraView.enableView();
				break;
			case STATUS_SEARCHING:
				mTextMsg.setVisibility(View.GONE);
				mLayoutLoading.setVisibility(View.GONE);
				mClockView.cancelAnimation();
				FaceManager.getInstance().startSearchFace();
				break;
			case STATUS_SUCCESS:
			case STATUS_ERROR:
				boolean success = mStatus == STATUS_SUCCESS;
				mTextMsg.setVisibility(View.VISIBLE);
				mLayoutLoading.setVisibility(View.GONE);
				mTextMsg.setText(success ? "欢迎回家\n已为您开门" : "非常抱歉\n未能查到您的信息");

				MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), success ? R.raw.media_welcome : R.raw.media_error);
				mediaPlayer.setOnCompletionListener(mp -> {
					if (mp != null) {
						mp.stop();
						mp.release();
					}
				});
				mediaPlayer.start();
				// 暂停10s后重新开始工作
				Observable.timer(10, TimeUnit.SECONDS) //
						.subscribeOn(Schedulers.newThread()) //
						.observeOn(AndroidSchedulers.mainThread()) //
						.subscribe(aLong -> {
						}, Throwable::printStackTrace, () -> {
							// 改变状态为等待状态
							if (isActivityRunning) {
								onStatusChanged(STATUS_WAIT, true);
								Log.d(TAG, "重新开始工作！");
							}
						});

				mCameraView.disableView();
				break;
		}
	}

	@OnClick(R.id.btnSetting)
	public void onViewClicked() {
		startActivity(new Intent(getContext(), FaceSettingActivity.class));
	}
}
