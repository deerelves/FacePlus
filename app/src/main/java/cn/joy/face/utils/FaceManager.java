package cn.joy.face.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hwangjr.rxbus.RxBus;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.joy.face.AppKeeper;
import cn.joy.face.Constants;
import cn.joy.face.bean.FaceDetectModel;
import cn.joy.face.bean.FaceSearchModel;
import cn.joy.face.bean.FaceSetModel;
import cn.joy.plus.tools.image.ImageTools;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class FaceManager {

	private static final String TAG = "FaceManager";

	// 最大可搜索失败检测次数
	private static final int SEARCH_MAX_ERROR_TYPE = 5;

	private static FaceManager manager;

	public static FaceManager getInstance() {
		return manager == null ? manager = new FaceManager() : manager;
	}

	private static final String OUTID = "test";

	private SharedUtil mShared;

	private Subscriber<? super Mat> mSearchSubscription;
	private Subscription mSearchManager;
	private Mat mTempMat;

	private int mCurrentSearchTime = 0;

	private FaceManager() {
		mShared = new SharedUtil(AppKeeper.getInstance().getContext());
	}

	/**
	 * 创建人脸集合
	 */
	public void createFaceSet() {
		if (mShared.isFaceSetCreate()) {
			Log.e(TAG, "人脸库已存在");
			return;
		}
		Log.e(TAG, "开始创建人脸库");
		Map<String, String> map = new HashMap<>();
		map.put("outer_id", OUTID);
		post("https://api-cn.faceplusplus.com/facepp/v3/faceset/create", map, FaceSetModel.class).subscribe(faceSetModel -> {
			if (!TextUtils.isEmpty(faceSetModel.getToken())) {
				mShared.setFaceSetCreate();
				Log.e(TAG, "创建人脸库成功");
			}
		}, throwable -> {
			if (throwable.getMessage().contains("FACESET_EXIST")) {
				// 已经创建
				mShared.setFaceSetCreate();
				Log.e(TAG, "人脸库已存在！不需要重新创建！");
			} else {
				Log.e(TAG, "创建人脸库失败" + throwable.getMessage());
			}
		});
	}

	/**
	 * 添加人脸
	 */
	public Observable<FaceSetModel> addFace(String path, String name) {
		return Observable.just(path).subscribeOn(Schedulers.newThread()).map(p -> {
			String savePath = AppKeeper.getInstance().getImageCachePath() + "/" + System.currentTimeMillis() + ".jpg";
			return ImageTools.compressImageAsFile(path, savePath);
		}).flatMap(s -> {
			Log.d(TAG, "压缩后图片地址 \n" + s);
			Map<String, String> map = new HashMap<>();
			map.put("image_file", s);
			return Observable.<FaceSetModel>create(sb ->
					// 上传图片获取token
					post("https://api-cn.faceplusplus.com/facepp/v3/detect", map, FaceDetectModel.class) //
							.retry(5).subscribe(model -> {
						Map<String, String> map2 = new HashMap<>();
						map2.put("outer_id", OUTID);
						map2.put("face_tokens", model.getFaceList().get(0).getToken());
						// 添加token到人脸库
						post("https://api-cn.faceplusplus.com/facepp/v3/faceset/addface", map2, FaceSetModel.class) //
								.retry(5) //
								.subscribe(sb::onNext, sb::onError);
					}, sb::onError)).map(fm -> {
				try {
					String savePath = getFaceSavePath();
					File file = new File(savePath);
					if (!file.exists()) {
						file.mkdir();
					}
					copyFile(s, savePath + new File(s).getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return fm;
			}).observeOn(AndroidSchedulers.mainThread());
		});
	}

	/**
	 * 开始搜索face
	 */
	public void searchFace(Mat mat) {
		Log.d(TAG, "检测到人脸，开始识别---");
		if (mSearchSubscription == null) {
			Observable.<Mat>create(sb -> mSearchSubscription = sb).subscribeOn(Schedulers.newThread())
					.map(m -> {
						// 旋转mat并转换为bitmap
						Bitmap bitmap = Bitmap.createBitmap(m.height(), m.width(), Bitmap.Config.RGB_565);
						if (mTempMat == null) {
							mTempMat = new Mat(m.height(), m.width(), m.type());
						}
						Core.rotate(m, mTempMat, Core.ROTATE_90_CLOCKWISE);
						Utils.matToBitmap(mTempMat, bitmap);
						return bitmap;
					})
					.map(bitmap -> ImageTools.compressBitmapAsFile(bitmap, AppKeeper.getInstance()
							.getImageCachePath() + "/" + System.currentTimeMillis() + ".jpg"))
					.subscribe(path -> {
						Map<String, String> map = new HashMap<>();
						map.put("outer_id", OUTID);
						map.put("image_file", path);
						// 开始识别
						post("https://api-cn.faceplusplus.com/facepp/v3/search", map, FaceSearchModel.class) //
								//	.retry(5) //
								.subscribe(faceSearch -> {
									Log.d(TAG, JSON.toJSONString(faceSearch));
									// 匹配成功
									if (faceSearch.isFaceSearched()) {
										Log.d(TAG, "检测到人脸，匹配成功!!!");
										RxBus.get().post(Constants.RX_TAG_FACE_SEARCH_RESULT, Boolean.TRUE);
										mCurrentSearchTime = 0;
										finishSearchFace();
									} else {
										Log.d(TAG, "检测到人脸，但未匹配!!!" + (mCurrentSearchTime + 1));
										if (++mCurrentSearchTime > SEARCH_MAX_ERROR_TYPE) {
											RxBus.get().post(Constants.RX_TAG_FACE_SEARCH_RESULT, Boolean.FALSE);
											mCurrentSearchTime = 0;
										}
									}
									// 删除文件
									try {
										new File(path).delete();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}, throwable -> {
									Log.d(TAG, "检测到人脸，但服务器失败!!!");
									try {
										new File(path).delete();
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
					});
		}
		if (mSearchManager != null) {
			mSearchManager.unsubscribe();
		}
		mSearchManager = Observable.interval(100, TimeUnit.MILLISECONDS).subscribe(aLong -> {
			if (mSearchSubscription != null) {
				mSearchSubscription.onNext(mat);
				mSearchManager.unsubscribe();
			}
		});
	}

	/**
	 * 结束搜索
	 */
	public void finishSearchFace() {
		if (mSearchSubscription != null) {
			mSearchSubscription.unsubscribe();
			mSearchSubscription = null;
		}
	}

	public String getFaceSavePath() {
		return AppKeeper.getInstance().getFilePath() + "/faces/";
	}

	/**
	 * 复制文件
	 */
	private void copyFile(String fromFile, String toFile) throws IOException {
		FileInputStream ins = new FileInputStream(fromFile);
		FileOutputStream out = new FileOutputStream(toFile);
		byte[] b = new byte[1024];
		int n = 0;
		while ((n = ins.read(b)) != -1) {
			out.write(b, 0, n);
		}

		ins.close();
		out.close();
	}

	private <T> Observable<T> post(String url, Map<String, String> body, Class<T> clz) {
		return Observable.create(sb -> {
			OkHttpClient client = new OkHttpClient();
			RequestBody requestBody = null;
			if (body != null && body.containsKey("image_file")) {
				File file = new File(body.remove("image_file"));
				Log.d("上传文件", file.getName());
				MultipartBody.Builder builder = new MultipartBody.Builder() //
						.setType(MultipartBody.FORM) //
						.addFormDataPart("image_file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
				builder.addFormDataPart("api_key", Constants.API_KEY);
				builder.addFormDataPart("api_secret", Constants.API_SECRET);
				if (body.size() > 0) {
					for (String s : body.keySet()) {
						builder.addFormDataPart(s, body.get(s));
					}
				}
				requestBody = builder.build();
			} else {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("api_key", Constants.API_KEY);
				builder.add("api_secret", Constants.API_SECRET);
				if (body != null && body.size() > 0) {
					for (String s : body.keySet()) {
						builder.add(s, body.get(s));
					}
				}
				requestBody = builder.build();
			}

			Request.Builder builder = new Request.Builder();
			builder.url(url);
			builder.post(requestBody);
			client.newCall(builder.build()).enqueue(new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					sb.onError(e);
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
					try {
						String resp = response.body().string();
						Log.e(TAG, resp);
						JSONObject json = JSON.parseObject(resp);
						if (response.code() != 200) {
							sb.onError(new NullPointerException(response.message()));
							return;
						}
						if (json.containsKey("error_message") && !TextUtils.isEmpty(json.getString("error_message"))) {
							sb.onError(new NullPointerException(json.getString("error_message")));
							return;
						}
						sb.onNext(JSON.parseObject(resp, clz));
					} catch (Exception e) {
						e.printStackTrace();
						sb.onError(e);
					}
				}
			});
		});
	}
}
