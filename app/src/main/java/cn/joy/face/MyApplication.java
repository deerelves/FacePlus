package cn.joy.face;

import android.app.Application;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class MyApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		AppKeeper.init(this);
	}
}
