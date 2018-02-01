package com.douban.movie.ip;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: IpProxy</p>
 * <p>Description:抓取西刺代理ip</p>
 * @author wzhd
 * @date 2018年1月29日
 */
public class IpProxy {
	
	private static Map<String, String> ipMap = new HashMap<>();
	
	public static void go() throws IOException {
		String url = "http://www.xicidaili.com/";
		Connection conn = Jsoup.connect(url).timeout(5000);

		Elements eles = conn.get().select("#ip_list tbody tr");
//		Element ele = eles.get(1);
		for (Element ele : eles) {
			if (ele.children().size() > 7 && "高匿".equals(ele.child(4).text())) {
				ipMap.put(ele.child(1).text(), ele.child(2).text());
			}
		}
	}
	
	public static void store() throws IOException {
		//获取网页的所有符合条件的ip和端口
		go();
		
		Jedis jedis = new Jedis("192.168.56.5", 6379);
		
		if (!ipMap.isEmpty()) {
			for(Map.Entry<String, String> entry : ipMap.entrySet()) {
				jedis.set(entry.getKey(), entry.getValue());
			}
		}
		
		jedis.close();
	}
	
}
