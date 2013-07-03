package at.sti2.sparkwave.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="error")
public class ErrorBean {
	
	private String errMsg;
	private int errorCode;
	
	public ErrorBean() {
		
	}

	public ErrorBean(String errMsg, int errorCode) {
		this.errMsg = errMsg;
		this.errorCode = errorCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	
}