package com.douban.movie.ip;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.douban.movie.util.JedisUtil;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: IpPool</p>
 * <p>Description:将所有的代理ip放到ip中</p>
 * @author wzhd
 * @date 2018年2月2日
 */
public class IpPool {
	
	private static final Logger LOG = LoggerFactory.getLogger(IpPool.class);

	private static JedisUtil ju = new JedisUtil("192.168.56.5", 6379);
		
	private static Set<String> ips = new HashSet<>();

	/**
	 * 从redis中获取ip和端口
	 * 1.从redis中拿出所有的keys
	 * 2.判断集合是否为空，如果为空，就从网页上爬取代理ip，不为空则继续执行
	 * @throws IOException 
	 */
	public void initIps() throws IOException {
		Jedis jedisIp = ju.getJedis();
		ips = jedisIp.keys("*");
		if (ips.isEmpty()) {
			IpProxy.store();
			ips = jedisIp.keys("*");
		}
		if (jedisIp != null) {
			jedisIp.close();
		}
	}
	
	/**
	 * 获取访问地址
	 * @return	访问地址
	 * @throws JSONException 
	 * @throws IOException
	 */
	public Address getIpAndPort() throws JSONException {
		try {
			initIps();
		}catch(Exception e) {
			LOG.debug("错误所在行：" + e.getStackTrace()[0].getLineNumber()
					+ "; 错误信息：" + e.getMessage());
		}
		Address address = new Address();
		String ip = "";
		int port = 0;
		for(String key : ips) {
			JSONObject obj = new JSONObject(key);
			ip = obj.get("ip").toString();
			port = Integer.parseInt(obj.get("port").toString());
			address.setIp(ip);address.setPort(port);
			return address;
		}
		return address;
	}
	
	/**
	 * 将redis中的数据删除
	 * @param address
	 */
	public void removeIpAndPort(Address address) {
		Jedis jedisIp = ju.getJedis();
		String key = JSONArray.toJSONString(address);
		System.out.println(key);
		jedisIp.del(key);
	}
	
//	public static void main(String[] args) throws IOException, JSONException {
//		IpPool ip  = new IpPool();
//		Address ad = new Address();
//		ad.setIp("110.87.236.153");
//		ad.setPort(8118);
//		ip.removeIpAndPort(ad);
//		ip.getIpAndPort();
//		System.out.println("完成");
//	}
}
