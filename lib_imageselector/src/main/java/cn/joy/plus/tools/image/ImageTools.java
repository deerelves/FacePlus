package cn.joy.plus.tools.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageTools {

	private static final String TAG = "ImageTools";
	private static final int MAX_SIZE = 300 * 1024;
	private static final int MAX_WIDTH = 1080;
	private static final int MAX_HEIGHT = 1920;

	public static void compressBitmapAsFileAsync(Bitmap bitmap, String savePath, ImageCompressHandler<String> handler) {
		compressBitmapAsFileAsync(bitmap, savePath, MAX_WIDTH, MAX_HEIGHT, handler);
	}

	public static void compressBitmapAsFileAsync(final Bitmap bitmap, final String savePath, final int targetWidth, final int targetHeight,
			final ImageCompressHandler<String> handler) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return ImageTools.compressBitmapAsFile(bitmap, savePath, targetWidth, targetHeight);
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);
				if (handler != null) {
					if (TextUtils.isEmpty(s))
						handler.onFailure();
					else
						handler.onCompressed(s);
				}
			}
		}.execute();
	}

	public static void compressImageAsFileAsync(final String filePath, final String savePath, final ImageCompressHandler<String> handler) {
		compressImageAsFileAsync(filePath, savePath, MAX_SIZE, MAX_WIDTH, MAX_HEIGHT, handler);
	}

	public static void compressImageAsFileAsync(final String filePath, final String savePath, final int maxSize, final int targetWidth, final int targetHeight,
			final ImageCompressHandler<String> handler) {

		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... params) {
				return ImageTools.compressImageAsFile(filePath, savePath, maxSize, targetWidth, targetHeight);
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);
				if (handler != null) {
					if (TextUtils.isEmpty(s))
						handler.onFailure();
					else
						handler.onCompressed(s);
				}
			}
		}.execute();
	}

	/**
	 * 将图片压缩并保存到指定目录
	 * @param filePath 图片文件目录
	 * @param savePath 图片保存目录
	 * @return 图片保存目录
	 */
	public static String compressImageAsFile(String filePath, String savePath, int maxSize, int targetWidth, int targetHeight) {
		// 图片角度不对
		if (getExifOrientation(filePath) != 0) {
			Log.d("tag", "图片角度不对");
			rotateImage(filePath);
		}
		byte[] image = getImageThumbnail(filePath, maxSize, targetWidth, targetHeight);
		return saveBitmapCompress(savePath, image);
	}

	public static String compressImageAsFile(String filePath, String savePath) {
		return compressImageAsFile(filePath, savePath, MAX_SIZE, MAX_WIDTH, MAX_HEIGHT);
	}

	/**
	 * 将图片压缩
	 * @param filePath     图片地址
	 * @param targetWidth  需要压缩的目标宽度
	 * @param targetHeight 需要压缩的目标高度
	 * @return 压缩后的图片bitmap
	 */
	public static Bitmap compressImage(String filePath, int targetWidth, int targetHeight) {
		byte[] image = getImageThumbnail(filePath, targetWidth, targetHeight);
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static Bitmap compressImage(String filePath) {
		return compressImage(filePath, MAX_WIDTH, MAX_HEIGHT);
	}

	/**
	 * 将图片压缩
	 * @param bmp          目标图片
	 * @param targetWidth  需要压缩的目标宽度
	 * @param targetHeight 需要压缩的目标高度
	 * @return 压缩后的图片bitmap
	 */
	public static Bitmap compressBitmap(Bitmap bmp, int targetWidth, int targetHeight) {
		byte[] image = getImageThumbnail(bmp, targetWidth, targetHeight);
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static Bitmap compressBitmap(Bitmap bmp) {
		return compressBitmap(bmp, MAX_WIDTH, MAX_HEIGHT);
	}

	public static String compressBitmapAsFile(Bitmap bmp, String savePath, int targetWidth, int targetHeight) {
		return saveBitmapCompress(savePath, getImageThumbnail(bmp, targetWidth, targetHeight));
	}

	public static String compressBitmapAsFile(Bitmap bmp, String savePath) {
		return compressBitmapAsFile(bmp, savePath, MAX_WIDTH, MAX_HEIGHT);
	}

	private static byte[] getImageThumbnail(String filePath) {
		return getImageThumbnail(filePath, MAX_WIDTH, MAX_HEIGHT);
	}

	private static byte[] getImageThumbnail(String filePath, int targetWidth, int targetHeight) {
		return getImageThumbnail(filePath, MAX_SIZE, targetWidth, targetHeight);
	}

	/**
	 * 将图片压缩到固定尺寸
	 * @param filePath     图片地址
	 * @param targetWidth  目标宽度
	 * @param targetHeight 目标高度
	 * @return 图片int数据
	 */
	private static byte[] getImageThumbnail(String filePath, int maxSize, int targetWidth, int targetHeight) {
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		Bitmap b = BitmapFactory.decodeFile(filePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		if (b != null) {
			b.recycle();
		}

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetWidth, photoH / targetHeight);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap image = BitmapFactory.decodeFile(filePath, bmOptions);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int q = 100;
		//		Logs.i("TAG", "图片大小 kb---->" + baos.toByteArray().length / 1024);
		while (baos.toByteArray().length > maxSize && q >= 0) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, q, baos);//这里压缩options%，把压缩后的数据存放到baos中
			q -= 5;//每次都减少10
		}
		image.recycle();
		Log.i("TAG", "压缩后图片大小 kb---->" + baos.toByteArray().length / 1024);
		return (baos.toByteArray());
	}

	/**
	 * 将图片压缩到固定尺寸
	 * @param bmp          图片
	 * @param targetWidth  目标宽度
	 * @param targetHeight 目标高度
	 * @return 图片int数据
	 */
	private static byte[] getImageThumbnail(Bitmap bmp, int targetWidth, int targetHeight) {
		// Determine how much to scale down the image
		int newWidth = targetWidth > bmp.getWidth() ? bmp.getWidth() : targetWidth;
		int newHeight = targetHeight > bmp.getHeight() ? bmp.getHeight() : targetHeight;
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / bmp.getWidth();
		float scaleHeight = ((float) newHeight) / bmp.getHeight();
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Log.e(TAG, "getImageThumbnail old width:height-->" + bmp.getWidth() + ":" + bmp.getHeight() + "  new width:height-->" + newWidth + ":" + newHeight);
		Log.e(TAG, "getImageThumbnail scaleX:scaleY  " + scaleWidth + ":" + scaleHeight);
		// 得到新的图片
		Bitmap image = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		bmp.recycle();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int q = 100;
		Log.i("TAG", "图片大小 kb---->" + baos.toByteArray().length / 1024);
		while (baos.toByteArray().length > MAX_SIZE) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, q, baos);//这里压缩options%，把压缩后的数据存放到baos中
			q -= 10;//每次都减少10
		}
		image.recycle();
		Log.i("TAG", "压缩后图片大小 kb---->" + baos.toByteArray().length / 1024);
		return (baos.toByteArray());
	}

	/**
	 * 保存图片byte数据到本地
	 * @param savePath 需要保存的地址
	 * @param image    需要保存的图片数据
	 */
	private static String saveBitmapCompress(String savePath, byte[] image) {
		try {
			FileOutputStream fos = new FileOutputStream(savePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(image);
			bos.flush();
			fos.getFD().sync();
			bos.close();
			Log.i(TAG, "saveBitmap 成功");
			return savePath;

		} catch (IOException e) {
			Log.i(TAG, "saveBitmap:失败" + e.getMessage());
		}
		return null;
	}

	/**
	 * 将bitmap保存到指定目录
	 * @param bmp      bmp
	 * @param savePath savePath
	 */
	public static boolean saveBitmap(Bitmap bmp, String savePath) {
		try {
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(savePath));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param filePath 文件地址
	 * @return 文件byte
	 */
	private static byte[] getVideoThumbnail(String filePath) {
		// Get the dimensions of the bitmap
		Bitmap image = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.FULL_SCREEN_KIND);
		Matrix matrix = new Matrix();
		matrix.postScale(0.5f, 0.5f); //长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		resizeBmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
		return (baos.toByteArray());
	}

	/**
	 * 获取图片的旋转角度
	 * @param filepath filepath
	 */
	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.d(TAG, "cannot read exif" + ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
			}
		}
		return degree;
	}

	/**
	 * 旋转图片，使图片保持正确的方向。
	 * @param bitmap  原始图片
	 * @param degrees 原始图片的角度
	 * @return Bitmap 旋转后的图片
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
		if (degrees == 0 || null == bitmap) {
			return bitmap;
		}
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		return bmp;
	}

	/**
	 * 根据图片地址,获取图片当前旋转角度,纠正并保存
	 * @param filePath 图片地址
	 */
	public static void rotateImage(String filePath) {
		int degrees = getExifOrientation(filePath);
		if (degrees == 0)
			return;
		Log.e(TAG, "rotateImage ---->  " + filePath);
		compressBitmapAsFile(rotateBitmap(BitmapFactory.decodeFile(filePath), degrees), filePath);
	}

	public interface ImageCompressHandler<T> {
		void onCompressed(T t);

		void onFailure();
	}
}
