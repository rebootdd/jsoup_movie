package com.douban.movie.jsoup;

import java.util.Set;

import com.douban.movie.queue.Queue;
import com.douban.movie.util.JedisUtil;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: MovieDescriptionJsoup</p>
 * <p>Description:抓取豆瓣电影具体网页的详情</p>
 * @author wzhd
 * @date 2018年2月1日
 */
public class MovieDescriptionJsoup {
	
	private Queue queue = new Queue();
	
	private Jedis jedisUrl = new JedisUtil("192.168.56.3", 6379).getJedis();
	
	private Jedis jedisIp = new JedisUtil("192.168.56.5", 6379).getJedis();
	
	public void addUrlToQueue() {
		
		Set<String> keys = jedisUrl.keys("*");
		
		//每一次取100条数据，将数据添加到队列中
		if (keys.size() >= 100) {
			int i = 0;
			for(String key : keys) {
				
			}
		}else {
			
		}
	}
	
}
