package com.douban.movie.jsoup;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.douban.movie.web.domain.MovieCritic;

/**
 * <p>Title: LittleFilmReviewJsoup</p>
 * <p>Description:爬取短评</p>
 * @author wzhd
 * @date 2018年2月4日 下午4:30:42
 */
public class LittleFilmReviewJsoup {
	
	private static final Logger LOG = LoggerFactory.getLogger(MovieDescriptionJsoup.class);
	
	private static Map<String, String> cookieMap = new HashMap<>(16);
	
	static {
		cookieMap.put("__utma", "223695111.422971750.1517747722.1517747722.1517747722.1");
		cookieMap.put("__utmb", "223695111.0.10.1517747722");
		cookieMap.put("__utmc", "223695111");
		cookieMap.put("__utmz", "223695111.1517747722.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
		cookieMap.put("_pk_id.100001.4cf6", "5f94050b922c1138.1517747722.1.1517748549.1517747722.");
		cookieMap.put("_pk_ses.100001.4cf6", "*");
//		cookieMap.put("as", "https://movie.douban.com/subject/26611804/comments?start=400&limit=20&sort=new_score&status=P&percent_type=");
		cookieMap.put("bid", "8UtlX5rKY3w");
		cookieMap.put("ck", "sfr9");
		cookieMap.put("dbcl2", "\"173524835:xpRnuvXtJY8\"");
		cookieMap.put("ps", "y");
	}
	
	protected Document getDocument(String url) {
		Document doc = null;
		Connection conn = null;
		try {
			Thread.sleep(5500);
//			System.out.println(cookieMap.toString());
			conn = Jsoup.connect(url).timeout(3000).cookies(cookieMap);
			conn.header("Accept", "text/html,application/xhtml+xm…plication/xml;q=0.9,*/*;q=0.8");
			conn.header("Accept-Encoding", "gzip, deflate, br");
			conn.header("Accept-Language", "en-US,en;q=0.5");
			conn.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/59.0");
			doc = conn.get();
		} catch (IOException e) {
			System.out.println(url);
			LOG.debug(e.getMessage(), e);
		} catch (InterruptedException e) {
			System.out.println(url);
			LOG.debug(e.getMessage(), e);
		}
		if(doc == null) {
			doc = getDocument(url);
		}
		return doc;
	}
	
	/**
	 * 获取某一部电影的影评信息
	 * @param url
	 * @throws JSONException
	 */
	public void getMovieCritic(String url) {

		Document doc = getDocument(url);

		List<Long> idList = new ArrayList<>();
		List<String> nickNameList = new ArrayList<>();
		List<String> reviewTimeList = new ArrayList<>();
		List<String> reviewsList = new ArrayList<>();
		List<MovieCritic> mcList = new ArrayList<>();
		
		Elements eles = doc.select("span.comment-info a");
//		System.out.println("========影评人昵称=======");
		for (Element element : eles) {
			nickNameList.add(element.text());
//			System.out.println(element.text());
		}
//		System.out.println("=======影评时间=========");
		eles = doc.select("span.comment-time");
		for (Element element : eles) {
			reviewTimeList.add(element.attr("title"));
//			System.out.println(element.attr("title"));
		}
//		System.out.println("=======影评人id=======");
		eles = doc.select(".comment-item");
		for (Element element : eles) {
			idList.add(Long.parseLong(element.attr("data-cid")));
//			System.out.println(element.attr("data-cid"));
		}
		
		eles = doc.select(".comment p");
		for (Element element : eles) {
			reviewsList.add(element.text());
//			System.out.println(element.text());
		}
		
		for (int i = 0; i < nickNameList.size(); i++) {
			mcList.add(new MovieCritic(idList.get(i), nickNameList.get(i), 
					reviewsList.get(i), reviewTimeList.get(i)));
		}
		conDataBase(mcList);
		
		 Element ele = doc.selectFirst("#paginator a.next");
		 if(ele == null) {
			 System.exit(1);
		 }
		 String href = ele.attr("href");
		 int start = href.indexOf("=");
		 int end = href.indexOf("&");
		 String numb = href.substring(start+1, end);
		 if (numb.matches("[4,8]{1}[0]{2,}")) {
			 System.out.println(numb);
			 try {
				Thread.sleep(4*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		 }
		 getMovieCritic("https://movie.douban.com/subject/1292052/comments" + href);
	}
	
	public void conDataBase(List<MovieCritic> mcList) {
		String url = "jdbc:mysql://localhost:3366/douban?characterEncoding=utf-8";
		String username = "root";
		String password = "123";
		//执行SQL语句
		String sql = "insert into movie_critic(id, nick_name, reviews, review_time) values";
		Statement state = null;
		java.sql.Connection conn = null;
		try {
			//注册驱动
			Class.forName("com.mysql.jdbc.Driver");
			//获取连接
			conn = DriverManager.getConnection(url, username, password);
			conn.setAutoCommit(true);
			state = conn.createStatement();
		}catch(Exception e) {
			LOG.debug(e.getMessage(), e);
		}
		String nowSql = "";
		for (MovieCritic mc : mcList) {
			nowSql = sql + "(" + mc.getId() + ",\"" + mc.getNickName() + "\",\"" + mc.getReviews() + "\",\"" + mc.getReviewTime() + "\")";
			System.out.println(nowSql);
			try {
				state.execute(nowSql);
			} catch (SQLException e) {
				LOG.debug(e.getMessage(), e);
			}
			nowSql = "";
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.debug(e.getMessage(), e);
			}finally {
				conn = null;
			}
		}
	}
	
	public static void main(String[] args) {
		LittleFilmReviewJsoup lfj = new LittleFilmReviewJsoup();
		lfj.getMovieCritic("https://movie.douban.com/subject/1292052/comments?status=F");
		
//		System.out.println("600".matches("[1,2,4,6,8]{1}[0]{2,}"));
//		MovieCritic mc = new MovieCritic(1000l, "wzng", "nihao看", "102710");
//		List<MovieCritic> mcList= new ArrayList<>();
//		mcList.add(mc);
//		lfj.conDataBase(mcList);
	}
	
}
