package com.douban.movie.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * <p>Title: JedisUtil</p>
 * <p>Description:连接redis的工具</p>
 * @author wenquan
 * @date 2018年1月27日
 */
public class JedisUtil {
	
	private JedisPool pool = null;
	private String host;
	private int port;
	
	public JedisUtil() { }
	
	public JedisUtil(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/*** <p>Description: 获取jedispool 连接池 </p>
	* @param 
	*/
	private JedisPool getPool() {  
	    if (pool == null) {  
	        JedisPoolConfig config = new JedisPoolConfig();  
	        
	        //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；  
	        //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。  
	        config.setMaxTotal(30); 
	        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。  
	        config.setMaxIdle(10);  
	        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；  
	        config.setMaxWaitMillis(5*1000);
	        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；  
	        config.setTestOnBorrow(true);  

	        pool = new JedisPool(config, host, port);  

	    }  
	    return pool;  
	}  

	/*** <p>Description: 返回资源 </p>
	* @param 
	*/
	public void returnResource(JedisPool pool, Jedis jedis) {  
	    if (jedis != null) {  
	         jedis.close();
	    }  
	}  

	/**
	 *  <p>Description: 获取jedis 实例</p>
	* @param 
	*/
	public Jedis getJedis() {  
	    Jedis jedis = null; 
	    pool = getPool();  
	    jedis = pool.getResource(); 
	    return jedis;
	}  

	/*** <p>Description: 得到值</p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key
	*/
	public String get(String key){  
	    String value = null;  
	    Jedis jedis = null;
	    try {
	        jedis= getJedis();
	        value = jedis.get(key); 
	    } catch(Exception e){
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);  
	    }
	    return value;  
	}  

	/*** <p>Description: 设置键值</p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key value
	*/
	public String set(String key,String value){
	     Jedis jedis = null;
	     String ans = null;
	     try {  
	         jedis = getJedis();  
	         ans = jedis.set(key,value);  
	     } catch (Exception e) {  
	         //释放redis对象  
	         if(jedis != null){
	             jedis.close();
	         }
	         e.printStackTrace();  
	     } finally {  
	         //返还到连接池  
	         returnResource(pool, jedis);  
	     }  
	     return ans;
	}


	/*** <p>Description: 设置键值 并同时设置有效期</p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key seconds秒数 value
	*/
	public String setex(String key,int seconds,String value){
	     Jedis jedis = null;
	     String ans = null;
	     try {  

	         String.valueOf(100);
	         jedis = getJedis();  
	         ans = jedis.setex(key,seconds,value);  
	     } catch (Exception e) {  
	         if(jedis != null){
	            jedis.close();
	         }
	        e.printStackTrace();  
	     } finally {  
	         //返还到连接池  
	         returnResource(pool, jedis);  
	     }  
	     return ans;
	}

	/*** <p>Description: </p>
	 * <p>通过key 和offset 从指定的位置开始将原先value替换</p>
	 * <p>下标从0开始,offset表示从offset下标开始替换</p>
	 * <p>如果替换的字符串长度过小则会这样</p>
	 * <p>example:</p>
	 * <p>value : bigsea@zto.cn</p>
	 * <p>str : abc </p>
	 * <P>从下标7开始替换  则结果为</p>
	 * <p>RES : bigsea.abc.cn</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return 返回替换后  value 的长度
	*/
	public Long setrange(String key,String str,int offset){
	     Jedis jedis = null;  

	     try {  
	         jedis = getJedis();  
	         return jedis.setrange(key, offset, str);  
	     } catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	        return 0L;
	     } finally {  
	         //返还到连接池  
	         returnResource(pool, jedis);  
	     }
	}

	/*** <p>Description: 通过批量的key获取批量的value</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return 成功返回value的集合, 失败返回null的集合 ,异常返回空
	*/
	public List<String> mget(String...keys){
	    Jedis jedis = null;  
	    List<String> values = null;
	    try {  
	        jedis = getJedis();  
	        values = jedis.mget(keys);  
	    } catch (Exception e) {  
	    if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	    } finally {  
	        //返还到连接池  
	        returnResource(pool, jedis);  
	    }
	    return values;
	}

	/*** <p>Description: 批量的设置key:value,可以一个</p>
	 * <p>obj.mset(new String[]{"key2","value1","key2","value2"})</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return
	*/
	public String mset(String...keysvalues){
	    Jedis jedis = null;
	    String ans = null;
	    try{
	        jedis = getJedis();
	        ans = jedis.mset(keysvalues);
	    }catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	    } finally {  
	        //返还到连接池  
	        returnResource(pool, jedis);  
	    }
	    return ans;
	}

	/*** <p>Description: 通过key向指定的value值追加值</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param key str 追加字符串
	*/
	public Long append(String key,String str){
	     Jedis jedis = null;  

	     try {  
	         jedis = getJedis();  
	         return jedis.append(key, str);  
	     } catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	        return 0L;
	     } finally {  
	         //返还到连接池  
	         returnResource(pool, jedis);  
	     }  
	}

	/*** <p>Description: 判断key是否存在</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	*/
	public Boolean exists(String key){
	     Jedis jedis = null;  
	     try {  
	         jedis = getJedis();  
	         return jedis.exists(key);  
	     } catch (Exception e) {  
	         if(jedis != null){
	             jedis.close();
	         }
	         e.printStackTrace();  
	         return false;
	     } finally {  
	        //返还到连接池  
	         returnResource(pool, jedis);  
	     }  
	}

	/*** <p>设置key value,如果key已经存在则返回0,nx==> not exist</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return 成功返回1 如果存在 和 发生异常 返回 0
	*/
	public Long setnx(String key,String value){
	 Jedis jedis = null;  
	    try {  
	        jedis = getJedis();  
	        return jedis.setnx(key,value);  
	    } catch (Exception e) {  
	        if(jedis != null){
	             jedis.close();
	        }
	        e.printStackTrace();  
	        return 0l;
	    } finally {  
	        //返还到连接池  
	        returnResource(pool, jedis);  
	    }  
	}

	/*** <p>Description: 查看该键还有多少秒过期</p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key seconds秒数
	*/
	public Long expire(String key,int seconds){

	     Jedis jedis = null;  
	     Long res = 0l;
	     try {  
	         jedis = getJedis();  
	         res = jedis.expire(key, seconds);  
	     } catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	     } finally {  
	         //返还到连接池  
	         returnResource(pool, jedis);  
	     }  
	     return res;
	}


	/*** <p>Description: 删除某个键</p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key
	*/
	public Long del(String...keys){

	     Jedis jedis = null;  
	     Long res = 0l;
	     try {  
	         jedis = getJedis();  
	         res = jedis.del(keys);  
	     } catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	     } finally {  
	        //返还到连接池  
	        returnResource(pool, jedis);  
	     }  
	     return res;
	}


	/*** <p>Description: 该key还能存活多久 </p>
	* @author wenquan
	* @date  2017年1月5日
	* @param key
	*/
	public Long timetolive(String key){
	     Jedis jedis = null;  
	     Long res = 0l;
	     try {  
	         jedis = getJedis();  
	         res = jedis.ttl(key);  
	     } catch (Exception e) {  
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();  
	     } finally {  
	        //返还到连接池  
	        returnResource(pool, jedis);  
	     }  
	     return res;
	}

	/*** <p>Description: 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return 成功返回1 失败返回0 
	*/
	public Long msetnx(String...keysvalues){
	    Jedis jedis = null;
	    Long res = 0L;
	    try {
	        jedis = getJedis();
	        res =jedis.msetnx(keysvalues);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/*** <p>Description: 设置key的值,并返回一个旧值</p>
	* @author wenquan
	* @date  2017年1月6日
	* @param 
	* @return 旧值 如果key不存在 则返回null
	*/
	public String getset(String key,String value){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.getSet(key, value);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过下标 和key 获取指定下标位置的 value</p>
	 * @param key
	 * @param startOffset 开始位置 从0 开始 负数表示从右边开始截取
	 * @param endOffset 
	 * @return 如果没有返回null 
	 */
	public String getrange(String key, int startOffset ,int endOffset){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.getrange(key, startOffset, endOffset);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1</p>
	 * @param key
	 * @return 加值后的结果
	 */
	public Long incr(String key){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.incr(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key给指定的value加值,如果key不存在,则这是value为该值</p>
	 * @param key
	 * @param integer
	 * @return
	 */
	public Long incrBy(String key,Long integer){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.incrBy(key, integer);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>对key的值做减减操作,如果key不存在,则设置key为-1</p>
	 * @param key
	 * @return
	 */
	public Long decr(String key) {
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.decr(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}


	/**
	 * <p>减去指定的值</p>
	 * @param key
	 * @param integer
	 * @return
	 */
	public Long decrBy(String key,Long integer){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.decrBy(key, integer);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取value值的长度</p>
	 * @param key
	 * @return 失败返回null 
	 */
	public Long serlen(String key){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.strlen(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key给field设置指定的值,如果key不存在,则先创建</p>
	 * @param key
	 * @param field 字段
	 * @param value
	 * @return 如果存在返回0 异常返回null
	 */
	public Long hset(String key,String field,String value) {
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hset(key, field, value);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0</p>
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Long hsetnx(String key,String field,String value){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hsetnx(key, field, value);
	    } catch (Exception e) {
	        if(jedis != null){
	         jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key同时设置 hash的多个field</p>
	 * @param key
	 * @param hash
	 * @return 返回OK 异常返回null
	 */
	public String hmset(String key,Map<String, String> hash){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hmset(key, hash);
	    } catch (Exception e) {
	        if(jedis != null){
	         jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key 和 field 获取指定的 value</p>
	 * @param key
	 * @param field
	 * @return 没有返回null
	 */
	public String hget(String key, String field){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hget(key, field);
	    } catch (Exception e) {
	        if(jedis != null){
	         jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key 和 fields 获取指定的value 如果没有对应的value则返回null</p>
	 * @param key
	 * @param fields 可以使 一个String 也可以是 String数组
	 * @return 
	 */
	public List<String> hmget(String key,String...fields){
	    Jedis jedis = null;
	    List<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hmget(key, fields);
	    } catch (Exception e) {
	        if(jedis != null){
	         jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key给指定的field的value加上给定的值</p>
	 * @param key
	 * @param field
	 * @param value 
	 * @return
	 */
	public Long hincrby(String key ,String field ,Long value){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hincrBy(key, field, value);
	    } catch (Exception e) {
	        if(jedis != null){
	         jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key和field判断是否有指定的value存在</p>
	 * @param key
	 * @param field
	 * @return
	 */
	public Boolean hexists(String key , String field){
	    Jedis jedis = null;
	    Boolean res = false;
	    try {
	        jedis = getJedis();
	        res = jedis.hexists(key, field);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回field的数量</p>
	 * @param key
	 * @return
	 */
	public Long hlen(String key){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hlen(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;

	}

	/**
	 * <p>通过key 删除指定的 field </p>
	 * @param key
	 * @param fields 可以是 一个 field 也可以是 一个数组
	 * @return
	 */
	public Long hdel(String key ,String...fields){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hdel(key, fields);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回所有的field</p>
	 * @param key
	 * @return
	 */
	public Set<String> hkeys(String key){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hkeys(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回所有和key有关的value</p>
	 * @param key
	 * @return
	 */
	public List<String> hvals(String key){
	    Jedis jedis = null;
	    List<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hvals(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取所有的field和value</p>
	 * @param key
	 * @return
	 */
	public Map<String, String> hgetall(String key){
	    Jedis jedis = null;
	    Map<String, String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.hgetAll(key);
	    } catch (Exception e) {
	        if(jedis != null){
	             jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key向list头部添加字符串</p>
	 * @param key
	 * @param strs 可以使一个string 也可以使string数组
	 * @return 返回list的value个数
	 */
	public Long lpush(String key ,String...strs){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lpush(key, strs);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key向list尾部添加字符串</p>
	 * @param key
	 * @param strs 可以使一个string 也可以使string数组
	 * @return 返回list的value个数
	 */
	public Long rpush(String key ,String...strs){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.rpush(key, strs);
	    } catch (Exception e) {
	        if(jedis != null){
	             jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key在list指定的位置之前或者之后 添加字符串元素</p>
	 * @param key 
	 * @param where LIST_POSITION枚举类型
	 * @param pivot list里面的value
	 * @param value 添加的value
	 * @return
	 */
	public Long linsert(String key, LIST_POSITION where,
	        String pivot, String value){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.linsert(key, where, pivot, value);
	    } catch (Exception e) {
	        if(jedis != null){
	             jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key设置list指定下标位置的value</p>
	 * <p>如果下标超过list里面value的个数则报错</p>
	 * @param key 
	 * @param index 从0开始
	 * @param value
	 * @return 成功返回OK
	 */
	public String lset(String key ,Long index, String value){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lset(key, index, value);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key从对应的list中删除指定的count个 和 value相同的元素</p>
	 * @param key 
	 * @param count 当count为0时删除全部
	 * @param value 
	 * @return 返回被删除的个数
	 */
	public Long lrem(String key,long count,String value){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lrem(key, count, value);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key保留list中从strat下标开始到end下标结束的value值</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return 成功返回OK
	 */
	public String ltrim(String key ,long start ,long end){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.ltrim(key, start, end);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key从list的头部删除一个value,并返回该value</p>
	 * @param key
	 * @return 
	 */
	public String lpop(String key){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lpop(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key从list尾部删除一个value,并返回该元素</p>
	 * @param key
	 * @return
	 */
	public String rpop(String key){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.rpop(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value</p>
	 * <p>如果第一个list为空或者不存在则返回null</p>
	 * @param srckey
	 * @param dstkey
	 * @return
	 */
	public String rpoplpush(String srckey, String dstkey){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.rpoplpush(srckey, dstkey);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取list中指定下标位置的value</p>
	 * @param key
	 * @param index
	 * @return 如果没有返回null
	 */
	public String lindex(String key,long index){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lindex(key, index);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回list的长度</p>
	 * @param key
	 * @return
	 */
	public Long llen(String key){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.llen(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取list指定下标位置的value</p>
	 * <p>如果start 为 0 end 为 -1 则返回全部的list中的value</p>
	 * @param key 
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> lrange(String key,long start,long end){
	    Jedis jedis = null;
	    List<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.lrange(key, start, end);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key向指定的set中添加value</p>
	 * @param key
	 * @param members 可以是一个String 也可以是一个String数组
	 * @return 添加成功的个数
	 */
	public Long sadd(String key,String...members){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sadd(key, members);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key删除set中对应的value值</p>
	 * @param key
	 * @param members 可以是一个String 也可以是一个String数组
	 * @return 删除的个数
	 */
	public Long srem(String key,String...members){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.srem(key, members);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key随机删除一个set中的value并返回该值</p>
	 * @param key
	 * @return
	 */
	public String spop(String key){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.spop(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取set中的差集</p>
	 * <p>以第一个set为标准</p>
	 * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
	 * @return 
	 */
	public Set<String> sdiff(String...keys){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sdiff(keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取set中的差集并存入到另一个key中</p>
	 * <p>以第一个set为标准</p>
	 * @param dstkey 差集存入的key
	 * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
	 * @return 
	 */
	public Long sdiffstore(String dstkey,String... keys){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sdiffstore(dstkey, keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取指定set中的交集</p>
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public Set<String> sinter(String...keys){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sinter(keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取指定set中的交集 并将结果存入新的set中</p>
	 * @param dstkey
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public Long sinterstore(String dstkey,String...keys){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sinterstore(dstkey, keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回所有set的并集</p>
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public Set<String> sunion(String... keys){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sunion(keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key返回所有set的并集,并存入到新的set中</p>
	 * @param dstkey 
	 * @param keys 可以使一个string 也可以是一个string数组
	 * @return
	 */
	public Long sunionstore(String dstkey,String...keys){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sunionstore(dstkey, keys);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key将set中的value移除并添加到第二个set中</p>
	 * @param srckey 需要移除的
	 * @param dstkey 添加的
	 * @param member set中的value
	 * @return
	 */
	public Long smove(String srckey, String dstkey, String member){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.smove(srckey, dstkey, member);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取set中value的个数</p>
	 * @param key
	 * @return
	 */
	public Long scard(String key){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.scard(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key判断value是否是set中的元素</p>
	 * @param key
	 * @param member
	 * @return
	 */
	public Boolean sismember(String key,String member){
	    Jedis jedis = null;
	    Boolean res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.sismember(key, member);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取set中随机的value,不删除元素</p>
	 * @param key
	 * @return
	 */
	public String srandmember(String key){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.srandmember(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key获取set中所有的value</p>
	 * @param key
	 * @return
	 */
	public Set<String> smembers(String key){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.smembers(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key删除给定区间内的元素</p>
	 * @param key 
	 * @param start 
	 * @param end
	 * @return
	 */
	public Long zremrangeByRank(String key ,long start, long end){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.zremrangeByRank(key, start, end);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key删除指定score内的元素</p>
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Long zremrangeByScore(String key,double start,double end){
	    Jedis jedis = null;
	    Long res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.zremrangeByScore(key, start, end);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}
	/**
	 * <p>返回满足pattern表达式的所有key</p>
	 * <p>keys(*)</p>
	 * <p>返回所有的key</p>
	 * @param pattern
	 * @return
	 */
	public Set<String> keys(String pattern){
	    Jedis jedis = null;
	    Set<String> res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.keys(pattern);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}

	/**
	 * <p>通过key判断值得类型</p>
	 * @param key
	 * @return
	 */
	public String type(String key){
	    Jedis jedis = null;
	    String res = null;
	    try {
	        jedis = getJedis();
	        res = jedis.type(key);
	    } catch (Exception e) {
	        if(jedis != null){
	            jedis.close();
	        }
	        e.printStackTrace();
	    } finally {
	        returnResource(pool, jedis);
	    }
	    return res;
	}
}
