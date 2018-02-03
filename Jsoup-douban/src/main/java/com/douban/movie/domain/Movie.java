package com.douban.movie.domain;

import java.io.Serializable;

/**
 * <p>Title: Movie</p>
 * <p>Description:电影的实体类</p>
 * @author wzhd
 * @date 2018年2月3日  上午10:35:05
 */
public class Movie implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	String movieName;	// 电影名字
	String year;		// 上映年份
	String director; 	// 导演
	String writers;		// 编剧
	String casts; 		// 主演
	String type;  		// 类型
	String rate; 		// 评分
	String peopleNum;	// 评价人数
	String image;		// 电影海报图片存到本地的路径
	String runTime;		// 电影时间长度
	public String getMovieName() {
		return movieName;
	}
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getWriters() {
		return writers;
	}
	public void setWriters(String writers) {
		this.writers = writers;
	}
	public String getCasts() {
		return casts;
	}
	public void setCasts(String casts) {
		this.casts = casts;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getPeopleNum() {
		return peopleNum;
	}
	public void setPeopleNum(String peopleNum) {
		this.peopleNum = peopleNum;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getRunTime() {
		return runTime;
	}
	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
