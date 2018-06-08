package cn.joy.face.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Author: Joy
 * Date:   2018/6/8
 */

public class FaceSearchModel {

	@JSONField(name = "results")
	private List<FaceResultModel> faceResultList;
	@JSONField(name = "thresholds")
	private FaceThresholds faceThresholds;

	public void setFaceThresholds(FaceThresholds faceThresholds) {
		this.faceThresholds = faceThresholds;
	}

	public FaceThresholds setFaceThresholds() {
		return faceThresholds;
	}

	/**
	 * 是否匹配到该人脸
	 * @return 如果 faceThresholds 的十万分之一阈值>50则表示成功
	 */
	public boolean isFaceSearched() {
		return faceResultList != null && faceResultList.size() > 0 && (faceResultList.get(0).confidence > faceThresholds.getLevel3());
	}

	public List<FaceResultModel> getFaceResultList() {
		return faceResultList;
	}

	public void setFaceResultList(List<FaceResultModel> faceResultList) {
		this.faceResultList = faceResultList;
	}

	public class FaceThresholds {
		// 误识率为千分之一的置信度阈值；
		@JSONField(name = "1e-3")
		private float level1;
		// 误识率为万分之一的置信度阈值；
		@JSONField(name = "1e-4")
		private float level2;
		// 误识率为十万分之一的置信度阈值；
		@JSONField(name = "1e-5")
		private float level3;

		public float getLevel1() {
			return level1;
		}

		public void setLevel1(float level1) {
			this.level1 = level1;
		}

		public float getLevel2() {
			return level2;
		}

		public void setLevel2(float level2) {
			this.level2 = level2;
		}

		public float getLevel3() {
			return level3;
		}

		public void setLevel3(float level3) {
			this.level3 = level3;
		}
	}


	public class FaceResultModel {

		@JSONField(name = "face_token")
		private String token;
		@JSONField(name = "user_id")
		private String userId;
		private float confidence;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public float getConfidence() {
			return confidence;
		}

		public void setConfidence(float confidence) {
			this.confidence = confidence;
		}
	}
}
