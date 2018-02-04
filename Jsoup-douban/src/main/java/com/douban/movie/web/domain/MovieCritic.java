package com.douban.movie.web.domain;

/**
 * <p>Title: MovieCritic</p>
 * <p>Description:影评人信息实体类</p>
 * @author wzhd
 * @date 2018年2月3日 下午4:12:23
 */
public class MovieCritic {
	
	private Long id;
	private String nickName;
	private String reviews;
	private String reviewTime;
	
	public MovieCritic() {}
	
	
	public MovieCritic(Long id, String nickName, String reviews, String reviewTime) {
		super();
		this.id = id;
		this.nickName = nickName;
		this.reviews = reviews;
		this.reviewTime = reviewTime;
	}
	
	public MovieCritic(Long id, String nickName, String reviewTime) {
		super();
		this.id = id;
		this.nickName = nickName;
		this.reviewTime = reviewTime;
	}


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getReviewTime() {
		return reviewTime;
	}
	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

	public String getReviews() {
		return reviews;
	}
	public void setReviews(String reviews) {
		this.reviews = reviews;
	}
	
	
}
