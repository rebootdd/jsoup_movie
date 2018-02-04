package com.douban.movie.jsoup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.douban.movie.web.domain.MovieCritic;

/**
 * <p>Title: FileReviewJsoup</p>
 * <p>Description:抓取某部电影的长影评</p>
 * @author wzhd
 * @date 2018年2月3日 下午3:44:21
 */
public class LongFilmReviewJsoup extends FilmReviewJsoup{
	
	/**
	 * 获取某一部电影的影评人信息
	 * @param url
	 * @throws JSONException
	 */
	public List<MovieCritic> getMovieCritic(String url) {

		Document doc = null;
		try {
			doc = getDocument(url);
		} catch (JSONException e) {
			LOG.debug(e.getMessage());
		}
		
		List<Long> idList = new CopyOnWriteArrayList<>();
		List<String> nickNameList = new CopyOnWriteArrayList<>();
		List<String> reviewTimeList = new CopyOnWriteArrayList<>();
		
		List<MovieCritic> mcList = new CopyOnWriteArrayList<>();
		
		Elements eles = doc.select("a[property=v:reviewer]");
//		System.out.println("========影评人昵称=======");
		for (Element element : eles) {
			nickNameList.add(element.text());
		}
//		System.out.println("=======影评时间=========");
		eles = doc.select("span[property=v:dtreviewed]");
		for (Element element : eles) {
			reviewTimeList.add(element.text());
		}
//		System.out.println("=======影评人id=======");
		eles = doc.select(".main.review-item");
		for (Element element : eles) {
			idList.add(Long.parseLong(element.attr("id")));
		}
		
		for (int i = 0; i < nickNameList.size(); i++) {
			mcList.add(new MovieCritic(idList.get(i), nickNameList.get(i), reviewTimeList.get(i)));
		}
		return mcList;
	}
	
	public static void main(String[] args) throws JSONException {
		String url = "https://movie.douban.com/subject/1292052/reviews?start=0";
		
		LongFilmReviewJsoup frj = new LongFilmReviewJsoup();
		frj.getMovieCritic(url);
	}
}
