package cn.joy.face.bean;

import java.util.List;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class FaceSetEditor {

	private String name;
	private String intro;
	private String outerId;
//	private String tag;
	private List<String> faceToken;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getOuterId() {
		return outerId;
	}

	public void setOuterId(String outerId) {
		this.outerId = outerId;
	}

	public List<String> getFaceToken() {
		return faceToken;
	}

	public void setFaceToken(List<String> faceToken) {
		this.faceToken = faceToken;
	}
}
