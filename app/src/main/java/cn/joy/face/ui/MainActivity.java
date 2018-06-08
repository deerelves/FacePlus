package cn.joy.face.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import cn.joy.face.Constants;
import cn.joy.face.ParentActivity;
import cn.joy.face.R;
import cn.joy.face.utils.ConUtil;

public class MainActivity extends ParentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//		checkAuthorization();
		authSuccess();
	}

	/**
	 * 鉴权
	 */
	private void checkAuthorization() {
		int type = Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw.megviifacepp_0_5_2_model));
		if (type == 2) { // 非联网授权
			return;
		}
		final LicenseManager licenseManager = new LicenseManager(getContext());
		licenseManager.setAuthTimeBufferMillis(0);
		licenseManager.takeLicenseFromNetwork(Constants.CN_LICENSE_URL, ConUtil.getUUIDString(getContext()), Constants.API_KEY, Constants.API_SECRET, Facepp.getApiName(), "1", new LicenseManager.TakeLicenseCallback() {
			@Override
			public void onSuccess() {
				// 鉴权成功
				authSuccess();
			}

			@Override
			public void onFailed(int i, byte[] bytes) {
				if (TextUtils.isEmpty(Constants.API_KEY) || TextUtils.isEmpty(Constants.API_SECRET)) {
					if (!ConUtil.isReadKey(getContext())) {
						authFail(1001, "");
					} else {
						authFail(1001, "");
					}
				} else {
					String msg = "";
					if (bytes != null && bytes.length > 0) {
						msg = new String(bytes);
					}
					authFail(i, msg);
				}
			}
		});
	}

	/**
	 * 鉴权成功
	 */
	private void authSuccess() {
		startActivity(new Intent(getContext(), FaceCheckActivity.class));
		finish();
	}

	/**
	 * 鉴权失败
	 */
	private void authFail(int errCode, String msg) {
		showMessage(getString(R.string.auth_fail_format, msg));
	}
}
