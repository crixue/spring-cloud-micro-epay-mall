package com.epayMall.redis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.epayMall.pojo.User;
import com.epayMall.redis.ICacheService;
import com.epayMall.util.JsonUtil;

@Service("cacheService")
public class CacheServiceImpl implements ICacheService {
	@Autowired
	@Qualifier("redisTemplate")
	private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	@Qualifier("stringRedisTemplate")
	private StringRedisTemplate stringRedisTemplate;
	

	/*---------------值为String类型的列表类型---------------*/
	@Override
	public Long push(String key, String value) {
		return redisTemplate.opsForList().leftPush(key, value);
	}

	@Override
	public String pop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	@Override
	public Long in(String key, String value) {
		return redisTemplate.opsForList().rightPush(key, value);
	}

	@Override
	public String out(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	@Override
	public Long length(String key) {
		return redisTemplate.opsForList().size(key);
	}

	@Override
	public List<String> range(String key, int start, int end) {
		return redisTemplate.opsForList().range(key, start, end);
	}

	@Override
	public void remove(String key, long i, String value) {
		redisTemplate.opsForList().remove(key, i, value);
	}

	@Override
	public String index(String key, long index) {
		return redisTemplate.opsForList().index(key, index);
	}

	@Override
	public void set(String key, long index, String value) {
		redisTemplate.opsForList().set(key, index, value);

	}

	@Override
	public void trim(String key, long start, int end) {
		redisTemplate.opsForList().trim(key, start, end);

	}

	@Override
	public void set(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Override
	public <T> void set(String key, T obj, Long timeout, TimeUnit unit) {
		if (obj == null) {
			return;
		}
		
		String value ;
		if (obj instanceof String) {
			value = obj.toString();
		} else {
			value = toJson(obj);
		}

		
		if (timeout != null) {
			redisTemplate.opsForValue().set(key, value, timeout, unit);
		} else {
			set(key, value);
		}

	}
	
	@Override
	public boolean setIfNotExists(String key, String value) {
		return redisTemplate.opsForValue().setIfAbsent(key, value);
	}
	
	@Override
	public <T> T get(String key, Class<T> clazz){
		return parseJson(get(key), clazz);
		
	}
	
	
	/*---------------查询存储值为obj类型的列表类型----------------*/
	@Override
	public int size(String key) {
		return redisTemplate.opsForList().size(key).intValue();
	}

	@Override
	public <T> List<T> rangeTranJson(String key, long start, long end, Class<T> clazz) {
		List<String> list = redisTemplate.opsForList().range(key, start, end);
		return parseJsonList(list, clazz);
	}

	/*-------------------List 类型--------------------*/
	@Override
	public <T> void leftPush(String key, T obj) {
		if (obj == null) {
			return;
		}

		redisTemplate.opsForList().leftPush(key, toJson(obj));
	}

	@Override
	public <T> void rightPush(String key, T obj) {
		if (obj == null) {
			return;
		}

		redisTemplate.opsForList().rightPush(key, toJson(obj));
	}

	@Override
	public <T> T leftPop(String key, Class<T> clazz) {
		String value = redisTemplate.opsForList().leftPop(key);
		return parseJson(value, clazz);
	}

	@Override
	public <T> T rightPop(String key, Class<T> clazz) {
		String value = redisTemplate.opsForList().rightPop(key);
		return parseJson(value, clazz);
	}

	@Override
	public void rightPushAll(String key, Collection<?> values, Long timeout, TimeUnit unit) {
		if (values == null || values.isEmpty()) {
			return;
		}

		redisTemplate.opsForList().rightPushAll(key, toJsonList(values));
		if (timeout != null) {
			redisTemplate.expire(key, timeout, unit);
		}
	}

	@Override
	public <T> T getForList(Collection<String> keys, Class<T> clazz) {
		String value = redisTemplate.opsForValue().get(keys);
        return parseJson(value, clazz);
	}

	@Override
	public void remove(String key, int count, Object obj) {
        if (obj == null) {
            return;
        }
         
        redisTemplate.opsForList().remove(key, count, toJson(obj));
	}


	/*---------------common---------------*/
	@Override
	public boolean expire(String key, long timeout, TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}
	
	@Override
	public boolean exists(String key){
		return redisTemplate.hasKey(key);
	}
	
	@Override
	public Collection<String> keys(String pattern) {
		return redisTemplate.keys(pattern);
	}

	@Override
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	@Override
	public void delete(Collection<String> keys) {
		redisTemplate.delete(keys);
	}

	/* ----------- string --------- */
	@Override
	public int decrement(String key, int delta) {
		Long value = redisTemplate.opsForValue().increment(key, -delta);
		return value.intValue();
	}

	@Override
	public int increment(String key, int delta) {
		Long value = redisTemplate.opsForValue().increment(key, delta);
		return value.intValue();
	}
	
	@Override
	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	@Override
	public String getNONValue(String key){
		if (null == key || !exists(key)) {
			return StringUtils.EMPTY;
		}
		return get(key);
	}
	
	@Override
    public void mset(java.util.Map<String,String> map) {
		redisTemplate.opsForValue().multiSet(map);
	};
	
	
	@Override
	public <T> List<T> mget(Collection<String> keys, Class<T> clazz) {
		List<String> values = redisTemplate.opsForValue().multiGet(keys);
        return parseJsonList(values, clazz);
	}
	

	@Override
	public List<String> mget(Collection<String> keys) {
		return redisTemplate.opsForValue().multiGet(keys);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAndSet(String key, T obj) {
		if (obj == null) {
			return (T) get(key);
		}
		return (T) redisTemplate.opsForValue().getAndSet(key, toJson(obj));
	}

	/*----------- Hash类型 --------- */
	@Override
	public void hadd(String key, String field, Object value) {
		String valueJson = toJson(value);
		redisTemplate.opsForHash().put(key, field, valueJson);
	}
	
	@Override
	public void hmadd(String key, Map<? extends Object, ? extends Object> map) {
		redisTemplate.opsForHash().putAll(key, map);
	}
	
	@Override
	public <T> T hget(String key, String field, Class<T> clazz) {
		Object valueObj = redisTemplate.opsForHash().get(key, field); 
		String json = StringUtils.EMPTY;
		if(valueObj instanceof String) {
			json = (String) valueObj;
		}
		return parseJson(json, clazz);
	}
	
	@Override
	public <T> List<T> hmgetValue(String key, Class<T> clazz){
		List<Object> list = redisTemplate.opsForHash().values(key);
		List<T> values = new ArrayList<>();
		for(Object obj: list) {
			if(obj instanceof String) {
				String json = (String) obj;
				T t = parseJson(json, clazz);
				values.add(t);
			}
		}
		
		return values;
	}
	
	@Override
	public void hdelete(String key, String field) {
		redisTemplate.opsForHash().delete(key, field);
	}
	

	/* ----------- 有序集合类型 --------- */
	@Override
	public int zcard(String key) {
		return redisTemplate.opsForZSet().zCard(key).intValue();
	}

	@Override
	public List<String> zrange(String key, long start, long end) {
		Set<String> set = redisTemplate.opsForZSet().range(key, start, end);
		return setToList(set);
	}

	private List<String> setToList(Set<String> set) {
		if (set == null) {
			return null;
		}
		return new ArrayList<String>(set);
	}

	@Override
	public void zadd(String key, Object obj, double score) {
		if (obj == null) {
			return;
		}
		redisTemplate.opsForZSet().add(key, toJson(obj), score);
	}
	
	@Override
	public Double zscore(String key, Object obj) {
		Double score = redisTemplate.opsForZSet().score(key, toJson(obj));
		return score;
		
	};

	@Override
	public void zaddAll(String key, List<TypedTuple<?>> tupleList, Long timeout, TimeUnit unit) {
		if (tupleList == null || tupleList.isEmpty()) {
			return;
		}

		Set<TypedTuple<String>> tupleSet = toTupleSet(tupleList);
		redisTemplate.opsForZSet().add(key, tupleSet);
		if (timeout != null) {
			redisTemplate.expire(key, timeout, unit);
		}

	}

	private Set<TypedTuple<String>> toTupleSet(List<TypedTuple<?>> tupleList) {
		Set<TypedTuple<String>> tupleSet = new LinkedHashSet<TypedTuple<String>>();
		for (TypedTuple<?> t : tupleList) {
			tupleSet.add(new DefaultTypedTuple<String>(toJson(t.getValue()), t.getScore()));
		}
		return tupleSet;
	}

	@Override
	public void zrem(String key, Object obj) {
		if (obj == null) {
			return;
		}
		redisTemplate.opsForZSet().remove(key, toJson(obj));

	}

	@Override
	public void unionStore(String destKey, Collection<String> keys, Long timeout, TimeUnit unit) {
		if (keys == null || keys.isEmpty()) {
			return;
		}

		Object[] keyArr = keys.toArray();
		String key = (String) keyArr[0];

		Collection<String> otherKeys = new ArrayList<String>(keys.size() - 1);
		for (int i = 1; i < keyArr.length; i++) {
			otherKeys.add((String) keyArr[i]);
		}

		redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
		if (timeout != null) {
			redisTemplate.expire(destKey, timeout, unit);
		}

	}

	private static <T> String toJson(T obj) {
		String value = JsonUtil.obj2String(obj);
		return value;

	}

	private static <T> T parseJson(String json, Class<T> clazz) {
		return JsonUtil.string2Obj(json, clazz);
	}

	private static <T> List<T> parseJsonList(List<String> list, Class<T> clazz) {
		if (list == null) {
			return null;
		}

		List<T> result = new ArrayList<T>();
		for (String s : list) {
			result.add(parseJson(s, clazz));
		}
		return result;
	}

	private static List<String> toJsonList(Collection<?> values) {
		if (values == null) {
			return null;
		}

		List<String> result = new ArrayList<String>();
		for (Object obj : values) {
			result.add(toJson(obj));
		}
		return result;
	}

	public static void main(String[] args) {
		Map<User, User> map = new HashMap<>();
		List<User> users = new ArrayList<>();
		User u1 = new User();
		u1.setId(123);
		u1.setPhone("1264565");
		users.add(u1);
		map.put(u1, u1);
		String val  = JSON.toJSONString(map);
		System.out.println(val);
	}

}
