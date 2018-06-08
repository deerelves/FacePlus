package cn.joy.face.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import cn.joy.face.AppKeeper;

/**
 * **********************
 * Author: yu
 * Date:   2015/8/5
 * Time:   17:42
 * **********************
 */
public class DeviceHelper {

	private static int STATUS_BAR_HEIGHT;

	private static Context getContext() {
		return AppKeeper.getInstance().getContext();
	}

	/**
	 * 获取设备id
	 */
	@SuppressLint({"MissingPermission", "HardwareIds"})
	@Deprecated
	public static String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static DisplayMetrics getDisplayMetrics() {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm;
	}

	/**
	 * dp转换px
	 */
	public static int dp2Px(float dp) {
		return (int) (dp * getDisplayMetrics().density + 0.5f);
	}

	/**
	 * 根据屏幕密度以及最短边的比例, 将dp转换等比例px
	 * @param dp dp
	 */
	public static int dp2bestPx(float dp) {
		return (int) (dp * (float) Math.min(getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels) / 480f);
	}

	public static int getStatusHeight(Context context) {
		if (STATUS_BAR_HEIGHT > 0)
			return STATUS_BAR_HEIGHT;
		int statusBarHeight1 = -1;
		//获取status_bar_height资源的ID
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			//根据资源ID获取响应的尺寸值
			statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
		}
		return STATUS_BAR_HEIGHT = statusBarHeight1;
	}

	public static int getStatusHeight() {
		return getStatusHeight(AppKeeper.getInstance().getContext());
	}

	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	public static int getScreenWidth() {
		return getScreenWidth(getContext());
	}

	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static int getScreenHeight() {
		return getScreenHeight(getContext());
	}

	public static float getScreenDensity() {
		return getScreenDensity(AppKeeper.getInstance().getContext());
	}

	public static float getScreenDensity(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm.density;
	}

	@SuppressLint("RestrictedApi")
	public static void hideSupportActionBar(Context context, boolean actionBar, boolean statusBar) {
		if (actionBar) {
			AppCompatActivity appCompatActivity = getAppCompActivity(context);
			if (appCompatActivity != null) {
				ActionBar ab = appCompatActivity.getSupportActionBar();
				if (ab != null) {
					ab.setShowHideAnimationEnabled(false);
					ab.hide();
				}
			}
		}
		if (statusBar) {
			if (context instanceof FragmentActivity) {
				FragmentActivity fragmentActivity = (FragmentActivity) context;
				fragmentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getAppCompActivity(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
	}

	@SuppressLint("RestrictedApi")
	public static void showSupportActionBar(Context context, boolean actionBar, boolean statusBar) {
		if (actionBar) {
			AppCompatActivity appCompatActivity = getAppCompActivity(context);
			if (appCompatActivity != null) {
				ActionBar ab = appCompatActivity.getSupportActionBar();
				if (ab != null) {
					ab.setShowHideAnimationEnabled(false);
					ab.show();
				}
			}
		}

		if (statusBar) {
			if (context instanceof FragmentActivity) {
				FragmentActivity fragmentActivity = (FragmentActivity) context;
				fragmentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getAppCompActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
	}

	/**
	 * Get AppCompatActivity from context
	 * @param context context
	 * @return AppCompatActivity if it's not null
	 */
	public static AppCompatActivity getAppCompActivity(Context context) {
		if (context == null)
			return null;
		if (context instanceof AppCompatActivity) {
			return (AppCompatActivity) context;
		} else if (context instanceof ContextThemeWrapper) {
			return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
		}
		return null;
	}

	/**
	 * 获取进程的名称
	 */
	public static String getCurrentProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		if (activityManager == null) {
			return null;
		}

		List<ActivityManager.RunningAppProcessInfo> runningProcessList = activityManager.getRunningAppProcesses();
		if (runningProcessList == null || runningProcessList.isEmpty()) {
			return null;
		}

		for (ActivityManager.RunningAppProcessInfo appProcess : runningProcessList) {
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}

	/**
	 * 判断是否是远程进程信息
	 * @return true 代表远程   false 代表不是远程
	 */
	public static boolean isRemoteProcess(Context context) {
		final String processName = getCurrentProcessName(context);
		return !TextUtils.isEmpty(processName) && processName.contains(":");
	}

	public static PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
		// 获取packageManager的实例
		PackageManager packageManager = getContext().getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		return packageManager.getPackageInfo(getContext().getPackageName(), 0);
	}

	/**
	 * 获取版本名称
	 */
	public static String getVersionName() {
		String version = "";
		try {
			version = getPackageInfo().versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 获取版本号
	 */
	public static int getVersionCode() {
		int versionCode = 1;
		try {
			versionCode = getPackageInfo().versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 键盘隐藏和弹出
	 * @param v      v
	 * @param isShow isShow
	 */
	@Deprecated
	public static void toggelInput(View v, boolean isShow) {
		toggleInput(v, isShow);
	}

	/**
	 * 键盘隐藏和弹出
	 * @param v      v
	 * @param isShow isShow
	 */
	public static void toggleInput(View v, boolean isShow) {
		if (v == null)
			return;
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (isShow) {
			imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
		} else {
			if (imm.isActive())
				imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}

	public static boolean isInputDisplaying(Context context) {
		return ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).isActive();
	}

	public static boolean isAppForeground() {
		return isAppForeground(getContext());
	}

	/**
	 * 判断当前应用是否在前台运行
	 * @param context context
	 */
	public static boolean isAppForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 返回当前设备是否已经ROOT
	 */
	public static boolean isDeviceRooted() {
		if (checkRootMethod1()) {
			return true;
		}
		if (checkRootMethod2()) {
			return true;
		}
		if (checkRootMethod3()) {
			return true;
		}
		return false;
	}

	//	/**
	//	 * 获取设备的IP地址
	//	 */
	//	public static String getDeviceHost() {
	//		String host;
	//		WiFiManager wifiUtils = new WiFiManager(Joy.getContext());
	//		if (wifiUtils.isWIfiConnected()) {
	//			host = wifiUtils.getLocalHost();
	//		} else {
	//			host = getDeviceIPAddress();
	//		}
	//		return host;
	//	}


	public enum NetworkType {
		None, Wifi, Mobile, UnKnow
	}

	/**
	 * 当前网络是否可用
	 */
	public static boolean isNetworkAvailable() {
		return getContext() != null && getNetworkType(getContext()) != NetworkType.None;
	}

	public static boolean isNetworkAvailable(Context context) {
		return getNetworkType(context) != NetworkType.None;
	}

	/**
	 * 获取当前网络状态
	 * @param context context
	 * @return None:无网络连接;Mobile:手机网络;Wifi:Wifi网络;UnKnow:未知网络连接
	 */
	public static NetworkType getNetworkType(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return NetworkType.None;
		}

		switch (networkInfo.getType()) {
			case ConnectivityManager.TYPE_MOBILE:
				return NetworkType.Mobile;
			case ConnectivityManager.TYPE_WIFI:
				return NetworkType.Wifi;
			default:
				return NetworkType.UnKnow;
		}
	}


	private static String getDeviceIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取本机号码
	 * @return
	 */
	public static String getLocalPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	private static boolean checkRootMethod1() {
		String buildTags = Build.TAGS;
		if (buildTags != null && buildTags.contains("test-keys")) {
			return true;
		}
		return false;
	}

	private static boolean checkRootMethod2() {
		try {
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private static boolean checkRootMethod3() {
		if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取系统版本号
	 */
	public static int getAndroidVersionCode() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * @author Kevin Kowalewski
	 */
	public static class ExecShell {
		public enum SHELL_CMD {
			check_su_binary(new String[]{"/system/xbin/which", "su"});
			String[] command;

			SHELL_CMD(String[] command) {
				this.command = command;
			}
		}

		public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
			String line = null;
			ArrayList<String> fullResponse = new ArrayList<String>();
			Process localProcess = null;
			try {
				localProcess = Runtime.getRuntime().exec(shellCmd.command);
			} catch (Exception e) {
				return null;
			}
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(localProcess.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
			try {
				while ((line = in.readLine()) != null) {
					fullResponse.add(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return fullResponse;
		}
	}

	/**
	 * 打电话
	 * @param context context
	 * @param number  number
	 */
	public static void call(Context context, String number) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + number));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 浏览器
	 * @param context context
	 * @param uri     uri
	 */
	public static void browser(Context context, String uri) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://" + uri));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 发短信
	 * @param context context
	 * @param number  number
	 * @param content content
	 */
	public static void msg(Context context, String number, String content) {
		Uri uri = Uri.parse("smsto://" + number);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", content);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 获取缓存目录
	 * @param context context
	 * @return 如果内存卡存在，则获取内存卡中缓存目录，否则获取设备内存缓存目录
	 */
	public static String getCacheDir(Context context) {
		File file = context.getExternalCacheDir();
		if (file != null && !TextUtils.isEmpty(file.getAbsolutePath())) {
			return file.getAbsolutePath();
		} else {
			return context.getCacheDir().getAbsolutePath();
		}
	}

	public static String getCacheDir() {
		return getCacheDir(getContext());
	}

	/**
	 * 获取文件目录
	 * @param context context
	 * @return 如果内存卡存在，则获取内存卡中文件目录，否则获取设备内存文件目录
	 */
	public static String getFileDir(Context context) {
		File file = context.getExternalFilesDir(null);
		if (file != null && !TextUtils.isEmpty(file.getAbsolutePath())) {
			return file.getAbsolutePath();
		} else {
			return context.getFilesDir().getAbsolutePath();
		}
	}

	public static String getFileDir() {
		return getFileDir(getContext());
	}

	/**
	 * checkPermissions
	 * @param context
	 * @param permission
	 * @return true or false
	 */
	public static boolean checkPermissions(Context context, String permission) {
		PackageManager localPackageManager = context.getPackageManager();
		return localPackageManager.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	public static String getIPAddress() {
		return getIPAddress(AppKeeper.getInstance().getContext());
	}

	public static String getIPAddress(Context context) {
		@SuppressLint("MissingPermission") NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
				try {
					//Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
					for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
						NetworkInterface intf = en.nextElement();
						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
							InetAddress inetAddress = enumIpAddr.nextElement();
							if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
								return inetAddress.getHostAddress();
							}
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				}

			} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				@SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
				return ipAddress;
			}
		} else {
			//当前无网络连接,请在设置中打开网络
		}
		return "";
	}

	//	public static boolean isAppInstalled(Context context, Uri uri) {
	//		PackageManager pm = context.getPackageManager();
	//		boolean installed = false;
	//		try {
	//			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
	//			installed = true;
	//		} catch (PackageManager.NameNotFoundException e) {
	//			installed = false;
	//		}
	//		return installed;
	//	}

	public static boolean isAppInstalled(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		List<String> pName = new ArrayList<>();
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		return pName.contains(packageName);
	}

	/**
	 * 将得到的int类型的IP转换为String类型
	 * @param ip
	 * @return
	 */
	public static String intIP2StringIP(int ip) {
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
	}

	public static boolean isMainProcess(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
			if (appProcess.pid == pid) {
				return context.getApplicationInfo().packageName.equals(appProcess.processName);
			}
		}
		return false;
	}
}
