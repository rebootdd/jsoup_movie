package com.douban.movie.jsoup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.douban.movie.ip.Address;
import com.douban.movie.ip.IpPool;
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
	
	private IpPool ipPool = new IpPool();
	//获取jedis对象
	private volatile JedisUtil ju = new JedisUtil("192.168.56.3", 6379);
	
	private volatile String movieUrl = "";
	
	//获取文档操作对象
	private volatile Document doc = null;
	/**
	 * 获取连接对象
	 * @param url
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public Document getDocument(String url) throws JSONException {
		//获取连接地址
		Address address =  ipPool.getIpAndPort();
		try {
			doc = Jsoup.connect(url).proxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress(address.getIp(), address.getPort())))
					.timeout(3000).ignoreContentType(true).get();
			return doc;
		} catch(Exception e) {
//			e.printStackTrace();
			LOG.debug("错误所在行：" + e.getStackTrace()[0].getLineNumber()
					+ "; 错误信息：" + e.getMessage());
			//如果连接有异常，则移除ip集合和jedis中的ip和端口
			ipPool.removeIpAndPort(address);
		}
		
		if (doc == null) {
			getDocument(url);
		} else {
			LinkQueue.removeVisitedUrl(url);
		}
		
//		if (jedisUrl != null) {
//			jedisUrl.close();
//		}
		return doc;
	}
	
	/**
	 * 线程需要执行的逻辑代码
	 */
	@Override
	public void run() {
		String url = "";
		Elements eles = null;
		
		//获取jedis对象
		Jedis jedisUrl = ju.getJedis();
		//循环获取地址
		while (!LinkQueue.unVisitedUrlEmpty()) {
			url = LinkQueue.unVisitedUrlDeQueue();
			try {
				Thread.sleep(4000);
				//输出集合的信息和抓取的网址
				System.out.println("开始集合的大小：" + LinkQueue.getUnVisitedUrlNum() + ", 抓取的网址：" + url);

				eles = getDocument(url).select("body");
				//System.out.println(eles.toString());
				String movieData = eles.get(0).text();
				JSONObject obj = JSONObject.parseObject(movieData);
				JSONArray jsonArr = JSONArray.fromObject(obj.get("data"));
				
				if (!jsonArr.isEmpty()) {
					for (Object json : jsonArr) {
						obj = JSONObject.parseObject(json.toString());
						movieUrl = obj.get("url").toString();
						jedisUrl.select(1);
						jedisUrl.set(movieUrl, movieUrl);
						//输出使用当前线程名+电影url
						System.out.println(Thread.currentThread().getName() + ":" + movieUrl);
					}
				}
				//移除使用过的url
				System.out.println("结束集合的大小："+ LinkQueue.getUnVisitedUrlNum());
			}catch(Exception e) {
//				e.printStackTrace();
				LOG.debug("错误所在行：" + e.getStackTrace()[0].getLineNumber()
						+ "; 错误信息：" + e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		MovieUrlJsoup movie = new MovieUrlJsoup();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(movie);
		executor.submit(movie);
//		executor.submit(movie);
		executor.shutdown();
	}
}
