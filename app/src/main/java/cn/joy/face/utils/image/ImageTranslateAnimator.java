package cn.joy.face.utils.image;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

/**
 * **********************
 * Author: Joy
 * Date:   2017-12-21
 * Time:   14:49
 * **********************
 */

public class ImageTranslateAnimator {

	public static void transImageView(ImageView imageView) {
		final boolean[] TURN_RIGHT = {true};
		// 图片宽度
		int bw = imageView.getDrawable().getBounds().width();
		int iw = imageView.getWidth();
		Matrix matrix = new Matrix(imageView.getImageMatrix());
		// 将图片移动到最开端
		matrix.postTranslate((iw - bw) / 2, 0);
		imageView.setImageMatrix(matrix);

		ValueAnimator animator = ValueAnimator.ofInt(0, bw - iw);
		animator.setRepeatMode(ValueAnimator.RESTART);
		animator.setInterpolator(new AccelerateInterpolator());
		animator.setDuration(10000);
		animator.addUpdateListener(animation -> {
			int value = (int) animation.getAnimatedValue();
			matrix.postTranslate(TURN_RIGHT[0] ? value : -value, 0);
			imageView.setImageMatrix(matrix);
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationRepeat(Animator animation) {
				TURN_RIGHT[0] = !TURN_RIGHT[0];
			}
		});
		animator.start();
	}
}
