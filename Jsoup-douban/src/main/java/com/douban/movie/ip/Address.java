package com.douban.movie.ip;

import java.io.Serializable;

/**
 * <p>Title: IP</p>
 * <p>Description:ip对象，存放收集的代理ip</p>
 * @author wzhd
 * @date 2018年2月2日
 */
public class Address implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String ip;
	private int port;
	
	public Address() {}
	
	public Address(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
