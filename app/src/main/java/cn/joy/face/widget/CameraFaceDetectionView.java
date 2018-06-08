package cn.joy.face.widget;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.joy.face.R;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class CameraFaceDetectionView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2 {

	private static final String TAG = "RobotCameraView";
	private OnFaceDetectorListener mOnFaceDetectorListener;
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	private CascadeClassifier mJavaDetector;
	// 记录切换摄像头点击次数
	private int mCameraSwitchCount = 0;

	private Mat mRgba;
	private Mat mGray;

	private int mAbsoluteFaceSize = 0;
	// 脸部占屏幕多大面积的时候开始识别
	private static final float RELATIVE_FACE_SIZE = 0.2f;

	public CameraFaceDetectionView(Context context, int cameraId) {
		super(context, cameraId);
	}

	public CameraFaceDetectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			return;
		}
		setCvCameraViewListener(this);
		try {
			InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
			File cascadeDir = getContext().getApplicationContext().getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}

			is.close();
			os.close();

			mJavaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
			if (mJavaDetector.empty()) {
				Log.e(TAG, "级联分类器加载失败");
				mJavaDetector = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "没有找到级联分类器");
		}
	}


	/**
	 * 切换摄像头
	 * @return 切换摄像头是否成功
	 */
	public boolean switchCamera() {
		// 摄像头总数
		int numberOfCameras = Camera.getNumberOfCameras();
		// 2个及以上摄像头
		if (1 < numberOfCameras) {
			// 设备没有摄像头
			int index = ++mCameraSwitchCount % numberOfCameras;
			disableView();
			setCameraIndex(index);
			enableView();
			return true;
		}
		return false;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// 子线程（非UI线程）
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		//		// todo 旋转处理
		//		Core.transpose(mScaleImage, mTempImage);
		//		Imgproc.resize(mTempImage, mScaleImage, mScaleImage.size(), 0.0D, 0.0D, 0); //将转置后的图像缩放为mRgbaF的大小
		//		Core.rotate(mScaleImage, mScaleImage, 0);

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * RELATIVE_FACE_SIZE) > 0) {
				mAbsoluteFaceSize = Math.round(height * RELATIVE_FACE_SIZE);
			}
		}

		if (mJavaDetector != null) {
			//			MatOfRect faces = new MatOfRect();
			//			mJavaDetector.detectMultiScale(mGray, // 要检查的灰度图像
			//					faces, // 检测到的人脸
			//					1.1, // 表示在前后两次相继的扫描中，搜索窗口的比例系数。默认为1.1即每次搜索窗口依次扩大10%;
			//					10, // 默认是3 控制误检测，表示默认几次重叠检测到人脸，才认为人脸存在
			//					Objdetect.CASCADE_DO_CANNY_PRUNING// 返回一张最大的人脸（无效？）
			//							| Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_DO_ROUGH_SEARCH, //CV_HAAR_DO_CANNY_PRUNING ,// CV_HAAR_SCALE_IMAGE, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
			//					new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size(mGray.width(), mGray.height()));
			//
			//			// 检测到人脸
			//			Rect[] facesArray = faces.toArray();
			//			for (Rect aFacesArray : facesArray) {
			//				Imgproc.rectangle(mRgba, aFacesArray.tl(), aFacesArray.br(), FACE_RECT_COLOR, 3);
			//				if (null != mOnFaceDetectorListener) {
			//					mOnFaceDetectorListener.onFace(mRgba, aFacesArray);
			//				}
			//			}
		}

		// todo tag2
		MatOfRect faces = new MatOfRect();

		// 使用classifier来识别人脸
		if (mJavaDetector != null) {
			mJavaDetector.detectMultiScale(mRgba, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
		}
		// 如果找到人脸，画个绿框框来显示人脸
		Rect[] facesArray = faces.toArray();
		if (facesArray != null && facesArray.length > 0) {
			for (Rect aFacesArray : facesArray) {
				Imgproc.rectangle(mRgba, aFacesArray.tl(), aFacesArray.br(), new Scalar(0, 255, 0, 255), 3);
			}
			if (null != mOnFaceDetectorListener) {
				mOnFaceDetectorListener.onFace(mRgba, facesArray[0]);
			}
		} else {
			if (null != mOnFaceDetectorListener) {
				mOnFaceDetectorListener.onFaceFinish();
			}
		}
		return mRgba;
	}

	public interface OnFaceDetectorListener {
		// 检测到一个人脸的回调
		void onFace(Mat mat, Rect rect);

		void onFaceFinish();
	}

	/**
	 * 添加人脸识别额监听
	 * @param listener 回调接口
	 */
	public void setOnFaceDetectorListener(OnFaceDetectorListener listener) {
		mOnFaceDetectorListener = listener;
	}
}
