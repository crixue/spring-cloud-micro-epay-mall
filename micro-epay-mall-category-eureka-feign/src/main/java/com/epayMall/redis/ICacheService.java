package com.epayMall.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

public interface ICacheService {
	/*---------------值为String类型的列表类型---------------*/
	/** 
     * 压栈 
     *  
     * @param key 
     * @param value 
     * @return 
     */  
    public Long push(String key, String value);
  
    /** 
     * 出栈 
     *  
     * @param key 
     * @return 
     */  
    public String pop(String key);
  
    /** 
     * 入队 
     *  
     * @param key 
     * @param value 
     * @return 
     */  
    public Long in(String key, String value);
  
    /** 
     * 出队 
     *  
     * @param key 
     * @return 
     */  
    public String out(String key) ;
  
    /** 
     * 栈/队列长 
     *  
     * @param key 
     * @return 
     */  
    public Long length(String key) ;
  
    /** 
     * 范围检索 
     *  
     * @param key 
     * @param start 
     * @param end 
     * @return 
     */  
    public List<String> range(String key, int start, int end) ;
  
    /** 
     * 移除 
     *  
     * @param key 
     * @param i 
     * @param value 
     */  
    public void remove(String key, long i, String value);
  
    /** 
     * 检索 
     *  
     * @param key 
     * @param index 
     * @return 
     */  
    public String index(String key, long index);
  
    /** 
     * 置值，仅存储字符串类型的值 
     *  
     * @param key 
     * @param index 
     * @param value 
     */ 
    public void set(String key, String value);
    
  
    /** 
     * 裁剪 
     *  
     * @param key 
     * @param start 
     * @param end 
     */  
    public void trim(String key, long start, int end) ;
    
    /**
     * 获得某个值
     * @param key
     * @return
     */
    public String get(String key);
    
    /**
     * 获得某个值,如果该值并不存在则返回空字符串
     * @param key
     * @return
     */
    String getNONValue(String key);
    
    /**
     * 同时设置多个值
     * @param map
     */
    public void mset(Map<String, String> map);
    
    /**
     * 同时获得多个值
     * @param keys
     * @return
     */
    public List<String> mget(Collection<String> keys);
    
    /**
     * 同时获得多个对象
     * @param keys
     * @param clazz
     * @return
     */
    public <T> List<T> mget(Collection<String> keys, Class<T> clazz);
    
    /*---------------查询存储值为obj类型的列表类型----------------*/
    public int size(String key);
    
    public <T> List<T> rangeTranJson(String key, long start, long end,  Class<T> clazz);
    
    public <T> void leftPush(String key, T obj);
    
    public <T> void rightPush(String key, T obj);
    
    /**
     * 以一个json字符串的对象弹出元素
     * @param key
     * @param clazz
     * @return
     */
    public <T> T leftPop(String key,  Class<T> clazz);
    
    public <T> T rightPop(String key, Class<T> clazz);
    
    /**
     * 将集合中的每个值转换成json再将其右插入队列中
     * @param key
     * @param values
     * @param timeout
     * @param unit
     */
    public void rightPushAll(String key, Collection<?> values, Long timeout,
            TimeUnit unit);
    
    public <T> T getForList(Collection<String> keys, Class<T> clazz);
    
    public void remove(String key, int count, Object obj);
    /*---------------common---------------*/
    public boolean exists(String key);
    /**
     * 获取keys的值
     * 
     * @param pattern
     * @return
     */
    public Collection<String> keys(String pattern);
    
    /**
     * 删除某个key的值
     * @param key
     */
    public void delete(String key) ;
    
    /**
     * 删除keys集合的值
     * @param key
     */
    public void delete(Collection<String> key);
    
    /* ----------- 字符串类型 --------- */
    /**
     * 存储一个对象，该对象作为值最终会被专程json字符串
     * @param key
     * @param obj
     * @param timeout 可以为空
     * @param unit 可以为空
     */
    public <T> void set(String key, T obj, Long timeout, TimeUnit unit);
    
    public void set(String key, long index, String value) ;
    
    public <T> T getAndSet(String key, T obj);
    
    /**
     * 减少指定的整数
     * @param key
     * @param delta
     * @return
     */
    public int decrement(String key, int delta);
    
    public int increment(String key, int delta);
    
    /* ----------- 有序集合类型 --------- */
    /**
     * 查询有序集合的元素的数量
     * @param key
     * @return
     */
    public int zcard(String key);
    
    public List<String> zrange(String key, long start, long end);
    
    public void zadd(String key, Object obj, double score);
    
    public Double zscore(String key, Object obj);
    
    public void zaddAll(String key, List<TypedTuple<?>> tupleList, Long timeout, TimeUnit unit);
    
    public void zrem(String key, Object obj);
    
    public void unionStore(String destKey, Collection<String> keys, Long timeout, TimeUnit unit);

    /*----------- Hash类型 --------- */
	void hdelete(String key, String field);

	<T> List<T> hmgetValue(String key, Class<T> clazz);

	<T> T hget(String key, String field, Class<T> clazz);

	void hmadd(String key, Map<? extends Object, ? extends Object> map);

	void hadd(String key, String field, Object value); 
    
    
}
