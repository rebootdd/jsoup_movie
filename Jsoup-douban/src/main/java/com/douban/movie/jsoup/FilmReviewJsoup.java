package com.douban.movie.jsoup;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.douban.movie.ip.Address;
import com.douban.movie.ip.IpPool;
import com.douban.movie.web.domain.MovieCritic;

/**
 * <p>Title: FilmReviewJsoup</p>
 * <p>Description:爬取影评</p>
 * @author wzhd
 * @date 2018年2月4日 下午4:26:17
 */
public abstract class FilmReviewJsoup {
	
	protected static final Logger LOG = LoggerFactory.getLogger(MovieDescriptionJsoup.class);
	
	private IpPool ipPool = new IpPool();
	
	/**
	 * 获取文档操作对象 
	 * @param url
	 * @return
	 * @throws JSONException
	 */
	protected synchronized Document getDocument(String url) throws JSONException {
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
	
}
