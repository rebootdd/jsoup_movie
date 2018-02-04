package com.douban.movie.ip;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.douban.movie.util.JedisUtil;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: IpProxy</p>
 * <p>Description:抓取西刺代理ip</p>
 * @author wzhd
 * @date 2018年1月29日
 */
public class IpProxy {
	
	private static Map<Address, Address> ipMap = new HashMap<>(16);
	
	private static JedisUtil ju = new JedisUtil("192.168.56.5", 6379);
	
	/**
	 * 从网页上爬取ip地址和端口
	 * @throws IOException
	 */
	public static void go() throws IOException {
		String url = "http://www.xicidaili.com/";
		Connection conn = Jsoup.connect(url).timeout(5000);

		Elements eles = conn.get().select("#ip_list tbody tr");
//		Element ele = eles.get(1);
		Address address = null;
		for (Element ele : eles) {
			if (ele.children().size() > 7 && "高匿".equals(ele.child(4).text())) {
				String ip = ele.child(1).text();
				int port = Integer.parseInt(ele.child(2).text());
				address = new Address(ip, port);
				ipMap.put(address, address);
			}
		}
	}
	
	/**
	 * 将网页中爬取的地址链接，存入到数据库
	 * @throws IOException
	 */
	public static void store() throws IOException {
		//获取网页的所有符合条件的ip和端口
		go();
		
		Jedis jedis = ju.getJedis();
		
		if (!ipMap.isEmpty()) {
			String addressJson = null;
			for(Address address : ipMap.keySet()) {
				addressJson = JSONArray.toJSONString(address);
				jedis.set(addressJson, addressJson);
			}
		}
		if (jedis != null) {
			jedis.close();
		}
	}
}
