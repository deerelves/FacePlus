package cn.joy.face.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class FaceDetectModel {

	@JSONField(name = "faces")
	private List<FaceModel> faceList;

	public List<FaceModel> getFaceList() {
		return faceList;
	}

	public void setFaceList(List<FaceModel> faceList) {
		this.faceList = faceList;
	}

	public class FaceModel {

		@JSONField(name = "face_token")
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}
}
