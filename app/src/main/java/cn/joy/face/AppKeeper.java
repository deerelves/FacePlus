package cn.joy.face;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * User: Joy
 * Date: 2017/1/3
 * Time: 10:23
 */

public class AppKeeper {

	private static AppKeeper ak;

	/**
	 * 直接判断 不加锁
	 */
	public static void initQuick(Context context) {
		if (ak == null) {
			ak = new AppKeeper(context);
		}
	}

	public static void init(Context context) {
		synchronized (AppKeeper.class) {
			if (ak == null) {
				ak = new AppKeeper(context);
			}
		}
	}

	public static AppKeeper newInstance(Context context) {
		return new AppKeeper(context);
	}

	public static AppKeeper getInstance() {
		if (ak == null) {
			throw new NullPointerException("please init first!!!");
		}
		return ak;
	}

	private Context context;
	private String channelId;

	private AppKeeper(Context context) {
		this.context = context.getApplicationContext();
		//  初始化Log
	}

	/**
	 * 设置是否打印log
	 * @param debug debug
	 */
	public AppKeeper debug(boolean debug) {
		return this;
	}

	/**
	 * 获取缓存总目录
	 * 可以用来存放图片、网络等缓存文件
	 * @return 缓存总目录
	 */
	public String getCachePath() {
		String cachePath;
		if (context.getExternalCacheDir() != null && !TextUtils.isEmpty(context.getExternalCacheDir().getAbsolutePath())) {
			cachePath = context.getExternalCacheDir().getAbsolutePath();
		} else {
			cachePath = context.getCacheDir().getAbsolutePath();
		}
		return cachePath;
	}

	/**
	 * 获取图片缓存路径
	 * @return 图片缓存路径
	 */
	public String getImageCachePath() {
		String imagePath = getCachePath() + "/img";
		File f = new File(imagePath);
		if (!f.exists()) {
			f.mkdirs();
		}
		return imagePath;
	}

	/**
	 * 获取文件存储总目录
	 * 可以用来存放下载的文件等
	 * @return 文件存储总目录
	 */
	public String getFilePath() {
		String filePath;
		if (context.getExternalFilesDir(null) != null && !TextUtils.isEmpty(context.getExternalCacheDir().getAbsolutePath())) {
			filePath = context.getExternalFilesDir(null).getAbsolutePath();
		} else {
			filePath = context.getCacheDir().getAbsolutePath();
		}
		return filePath;
	}

	@SuppressWarnings("ConstantConditions")
	public String getDownloadPath() {
		try {
			return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
		} catch (NullPointerException e) {
			e.printStackTrace();
			return getFilePath() + "/download/";
		}
	}

	public Context getContext() {
		return context;
	}

	/**
	 * 获取渠道ID
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * 设置渠道ID
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
}
