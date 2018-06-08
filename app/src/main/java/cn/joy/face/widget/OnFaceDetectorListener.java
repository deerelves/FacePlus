package cn.joy.face.widget;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Author: Joy
 * Date:   2018/6/8
 */
public interface OnFaceDetectorListener {
	// 检测到一个人脸的回调
	void onFace(Mat mat, Rect rect);

	void onFaceFinish();
}
