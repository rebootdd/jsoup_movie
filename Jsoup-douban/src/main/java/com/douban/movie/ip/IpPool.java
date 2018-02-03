package com.douban.movie.ip;

import java.io.IOException;
import java.util.Set;

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

	private static Jedis jedis = new JedisUtil("192.168.56.5", 6379).getJedis();
		
	private static Set<String> ips = jedis.keys("*");


	/**
	 * 从redis中获取ip和端口
	 * 1.从redis中拿出所有的keys
	 * 2.判断集合是否为空，如果为空，就从网页上爬取代理ip，不为空则继续执行
	 * @throws IOException 
	 */
	public void initIps() throws IOException {
		if (ips.isEmpty()) {
			IpProxy.store();
			ips = jedis.keys("*");
		}
	}
	
	/**
	 * 获取ip和端口
	 * @return
	 * @throws IOException
	 */
	public Address getIpAndPort() {
		try {
			initIps();
		}catch(Exception e) {
			LOG.debug(e.getMessage());
		}
		Address address = null;
		for(String key : ips) {
			address = (Address)JSONArray.parse(key);
			return address;
		}
		return address;
	}
	
	/**
	 * 将redis中的数据删除
	 * @param address
	 */
	public void removeIpAndPort(Address address) {
		String key = JSONArray.toJSONString(address);
		System.out.println(key);
		jedis.del(key);
	}
	
	
	public static void main(String[] args) throws IOException {
		IpPool ip  = new IpPool();
		Address ad = new Address();
		ad.setIp("110.87.236.153");
		ad.setPort(8118);
		ip.removeIpAndPort(ad);
		System.out.println("完成");
	}
}
