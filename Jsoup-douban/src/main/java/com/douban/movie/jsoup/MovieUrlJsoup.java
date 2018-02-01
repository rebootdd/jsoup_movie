package com.douban.movie.jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.douban.movie.ip.IpProxy;
import com.douban.movie.queue.LinkQueue;
import com.douban.movie.util.JedisUtil;

import net.sf.json.JSONArray;
import redis.clients.jedis.Jedis;

/**
 * 
 * <p>Title: AllMovieUrl</p>
 * <p>
 * 		Description: 使用来抓取豆瓣电影具体网址的
 * 		如：https://movie.douban.com/subject/1291581/
 * </p>
 * @author wzhd
 * @date 2018年1月30日
 */
public class MovieUrlJsoup implements Runnable{
	
	private static final Logger LOG = LoggerFactory.getLogger(MovieUrlJsoup.class);
	
	//获取jedis对象
	private volatile Jedis jedisIp = new JedisUtil("192.168.56.5", 6379).getJedis();
	
	private volatile Jedis jedisUrl = new JedisUtil("192.168.56.3", 6379).getJedis();
	
	private volatile String movieUrl = "";
	
	Map<String, String> ipMap = new HashMap<>(16);
	
	public void getIp() {
		Set<String> ips = jedisIp.keys("*");
		for (String ip : ips) {
			ipMap.put(ip, jedisIp.get(ip));
		}
		if (jedisIp != null) {
			jedisIp.close();
		}
	}
	
	/**
	 * 获取连接对象
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	public Document getDocument(String url) {
		//端口
		int port;
		//ip地址
		String ip;
		
		Document doc = null;
		if (ipMap.isEmpty()) {
			try {
				Thread.sleep(4000);
				IpProxy.store();
				getIp();
			} catch (IOException e) {
				LOG.debug(e.getMessage(), new Exception());
			} catch (InterruptedException e) {
				LOG.debug(e.getMessage(), new Exception());
			}
			
			//遍历含有ip和端口的map集合,如果ip集合没有数据，不使用代理ip
			for(Map.Entry<String, String> entry : ipMap.entrySet()) {
				ip = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				
				try {
					doc = Jsoup.connect(url).proxy(ip, port).timeout(3000).ignoreContentType(true).get();
					return doc;
				} catch(Exception e) {
					LOG.debug(e.getMessage(), new Exception());
					//如果连接有异常，则移除ip集合和jedis中的ip和端口
					ipMap.remove(ip, port);
					jedisIp.del(ip);
				}
			}
			
		} else {
			//遍历含有ip和端口的map集合,如果ip集合没有数据，不使用代理ip
			for(Map.Entry<String, String> entry : ipMap.entrySet()) {
				ip = entry.getKey();
				port = Integer.parseInt(entry.getValue());
				try {
					doc = Jsoup.connect(url).proxy(ip, port).timeout(3000).ignoreContentType(true).get();
					return doc;
				} catch(Exception e) {
					LOG.debug(e.getMessage(), new Exception());
					//如果连接有异常，则移除ip集合和jedis中的ip和端口
					ipMap.remove(ip, port);
					jedisIp.del(ip);
				}
			}
		}
		if (jedisIp != null) {
			jedisIp.close();
		}
		if (doc == null) {
			getDocument(url);
		}
		return doc;
	}
	
	@Override
	public void run() {
		
		String url = "";
		
		Elements eles = null;
		
		while (!LinkQueue.unVisitedUrlEmpty()) {
			url = LinkQueue.unVisitedUrlDeQueue();
			try {
				Thread.sleep(4000);
				eles = getDocument(url).select("body");
				//			System.out.println(eles.toString());
				String movieData = eles.get(0).text();
				JSONObject obj = JSONObject.parseObject(movieData);
				
				JSONArray jsonArr = JSONArray.fromObject(obj.get("data"));
				
				if (!jsonArr.isEmpty()) {
					System.out.println("开始集合的大小：" + LinkQueue.getUnVisitedUrlNum() + ", 抓取的网址：" + url);
					for (Object json : jsonArr) {
						obj = JSONObject.parseObject(json.toString());
						movieUrl = obj.get("url").toString();
						//输出使用当前线程名+电影url
						System.out.println(Thread.currentThread().getName() + ":" + movieUrl);
						if (movieUrl.startsWith("http")) {
							jedisUrl.set(movieUrl, movieUrl);
						}
					}
				}
				//移除使用过的url
				if (jedisUrl.exists(movieUrl)) {
					LinkQueue.removeVisitedUrl(url);
				}
				System.out.println("结束集合的大小："+ LinkQueue.getUnVisitedUrlNum());
			}catch(Exception e) {
				LOG.debug(e.getMessage(), new Exception());
			}
		}
	}
	
	public static void main(String[] args) {
		MovieUrlJsoup movie = new MovieUrlJsoup();
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.submit(movie);
		executor.submit(movie);
		executor.submit(movie);
		executor.shutdown();
	}
}
