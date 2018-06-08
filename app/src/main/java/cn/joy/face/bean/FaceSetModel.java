package cn.joy.face.bean;

/**
 * Author: Joy
 * Date:   2018/6/7
 */

public class FaceSetModel extends ResponseModel{

	// 用户自定义的 FaceSet 标识，如果未定义则返回值为空
	private String outerId;
	// FaceSet 的标识
	private String token;
	// 本次操作成功加入 FaceSet的face_token 数量
	private int addCount;
	// 操作结束后 FaceSet 中的 face_token 总数量
	private int total;

	public String getOuterId() {
		return outerId;
	}

	public void setOuterId(String outerId) {
		this.outerId = outerId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getAddCount() {
		return addCount;
	}

	public void setAddCount(int addCount) {
		this.addCount = addCount;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
