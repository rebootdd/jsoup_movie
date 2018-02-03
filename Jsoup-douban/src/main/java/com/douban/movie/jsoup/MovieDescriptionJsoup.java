package com.douban.movie.jsoup;

import java.io.IOException;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.douban.movie.domain.Movie;
import com.douban.movie.ip.Address;
import com.douban.movie.ip.IpPool;
import com.douban.movie.queue.Queue;
import com.douban.movie.util.FetchImgUtil;
import com.douban.movie.util.JedisUtil;

/**
 * <p>Title: MovieDescriptionJsoup</p>
 * <p>Description:抓取豆瓣电影具体网页的详情</p>
 * @author wzhd
 * @date 2018年2月1日
 */
public class MovieDescriptionJsoup {
	
	private static final Logger LOG = LoggerFactory.getLogger(MovieDescriptionJsoup.class);
	
	private Queue queue = new Queue();
	
	private JedisUtil ju = new JedisUtil("192.168.56.3", 6379);
	
	private IpPool ipPool = new IpPool();
	
	//将url添加到队列中
	public void addUrlToQueue() {
		
	}
	
	private Document getDocument(String url) throws JSONException {
		Address address = ipPool.getIpAndPort();
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).proxy(address.getIp(), address.getPort()).get();
			return doc;
		} catch (IOException e) {
			LOG.debug(e.getMessage());
			ipPool.removeIpAndPort(address);
		}
		
		if (doc == null) {
			doc = getDocument(url);
		}
		return doc;
	}
	
	private void run(String url) throws JSONException {
		
		String movieName;	// 电影名字
		String year;		// 上映年份
		String director; 	// 导演
		String writers;		// 编剧
		String casts; 		// 主演
		String type = "";  	// 类型
		String rate; 		// 评分
		String peopleNum;	// 评价人数
		String image;		// 电影海报图片存到本地的路径
		String runTime;		// 电影时间长度
		
		//创建电影对象
		Movie movie = new Movie();
		
		Document doc = getDocument(url);
		//下载图片
		String imgUrl = doc.select(".nbgnbg img").attr("src");
		image = FetchImgUtil.fetchImg(imgUrl);
//		System.out.println("本地图片地址：" + image);
		movie.setImage(image);
		//电影名和上映年份
		Elements eles = doc.select("#content h1 span");
		movieName = eles.get(0).text();
		year = eles.get(1).text().substring(1, 5);
//		System.out.println("电影名字:" + movieName + " , 上映年份:" + year);
		movie.setMovieName(movieName);
		movie.setYear(year);
		//电影导演，编剧，主演
		eles = doc.select(".attrs");
		director = eles.get(0).text();
		writers = eles.get(1).text().replace(" ", "");
		casts = eles.get(2).text().replace(" ", "");
//		System.out.println("导演：" + director + ", 编剧：" + writers + ", 主演：" + casts);
		movie.setDirector(director);
		movie.setWriters(writers);
		movie.setCasts(casts);
		//电影类型
		eles = doc.select("span[property=v:genre]");
		int i = 0;
		for (Element ele : eles) {
			i++;
			if (eles.size() > i) {
				type += ele.text() + "/";
			} else {
				type += ele.text();
			}
		}
//		System.out.println("类型：" + type);
		movie.setType(type);
		//电影时长
		Element ele = doc.select("span[property=v:runtime]").get(0);
		runTime = "<br>".equals(ele.nextSibling().toString())? ele.text() : ele.text()+ele.nextSibling();
//		System.out.println(runTime);
		movie.setRunTime(runTime);
		
		
		ele = doc.selectFirst("strong[property=v:average]");
		rate = ele.text();
//		System.out.println(rate);
		movie.setRate(rate);
		
		ele = doc.selectFirst("span[property=v:votes]");
		peopleNum = ele.text();
//		System.out.println(peopleNum);
		movie.setPeopleNum(peopleNum);
		
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		MovieDescriptionJsoup mdj = new MovieDescriptionJsoup();
		String url = "https://movie.douban.com/subject/1292052/";
		mdj.run(url);
	}
}
