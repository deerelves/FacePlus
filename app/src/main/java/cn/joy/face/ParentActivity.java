package cn.joy.face;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public abstract class ParentActivity extends AppCompatActivity{

	private Toast mToast;

	protected void showMessage(String msg){
		if(mToast != null){
			mToast.cancel();
		}
		mToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
		mToast.show();
	}

	protected void showMessage(int resId){
		if(mToast != null){
			mToast.cancel();
		}
		mToast = Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT);
		mToast.show();
	}

	protected Context getContext(){
		return this;
	}

}
