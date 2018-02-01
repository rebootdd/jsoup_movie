package com.douban.movie.queue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.douban.movie.util.JedisUtil;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: LinkQueue</p>
 * <p>Description:将数据添加到队列中</p>
 * @author cyj
 * @date 2018年1月27日
 */
public class LinkQueue {

	private static volatile Set<String> visitedUrl = new HashSet<>();
	
	private static volatile Queue unVisitedUrl = new Queue();
	
	static {
		//初始化数据,将每页20条数据的源网址添加到队列中
//		for(int i = 1500; i < 10000; i+=20) {
//			unVisitedUrl.addQueue("https://movie.douban.com/j/new_search_subjects?sort=T&range=0,10&tags=电影&start=" + i);
//		}
		
		//将具体电影页面详情页网址添加到数据中
		JedisUtil jedisUrl = new JedisUtil("192.168.56.3", 6379);
		//获取jedis对象
		Jedis jedis = jedisUrl.getJedis();
	}
	
	
	//得到未访问的URL
	public static Queue getUnVisitedUrl() {
		return unVisitedUrl;
	}
	
	//得到未访问的地址
	public static Set<String> getVisitedUrl() {
		Collections.synchronizedSet(visitedUrl);
		return visitedUrl;
	}
	
	//增加到已访问列表中去
	public static void addVisitedUrl(String url) {
		visitedUrl.add(url);
	}
	
	 // 移除访问过的 URL
    public static void removeVisitedUrl(String url){
        visitedUrl.remove(url);
    }
    // 未访问过的 URL 出列
    public static String unVisitedUrlDeQueue(){
        return unVisitedUrl.delQueue();
    }
    
    // 在unVisitedUrl 加入之前判断其中是否有重复的 ， 当无重复时才做添加
    public static void addUnvisitedUrl(String url){
        if((!unVisitedUrl.contains(url)) && (url!=null) && (!visitedUrl.contains(url))){
            unVisitedUrl.addQueue(url);
        }
        
    } 
    
    // 已访问的数目
    public static int getVisitedUrlNum(){
        return visitedUrl.size();
    }
    
    // 待访问的数目
    public static int getUnVisitedUrlNum(){
        return unVisitedUrl.size();
    }
    
    // 判断 待访问队列 是否为空
    public static boolean unVisitedUrlEmpty(){
        return unVisitedUrl.isQueueEmpty();
    }
	
	
}
