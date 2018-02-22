package redis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.epayMall.pojo.User;
import com.epayMall.redis.ICacheService;
import com.epayMall.redis.impl.CacheServiceImpl;

public class JedisTest {
	
    private ApplicationContext context;
    private static final Logger logger = LoggerFactory.getLogger(JedisTest.class);
    
    private ICacheService cache;
    
    @Before
    public void before(){
    	context = new ClassPathXmlApplicationContext(new String[]
    			{"spring/spring-redis.xml"});
    	cache = (CacheServiceImpl) context.getBean("cacheService", CacheServiceImpl.class);
    	logger.info("{}-[test]  {} {}",getCurrentTime(),"in","start");
//    	for(int i=0; i<5; i++){
//    		cache.in(String.valueOf(i), "test"+String.valueOf(i));
//    		System.out.println("right push key:"+i);
//    	}

    }
    
    @org.junit.After
    public void After(){
    	logger.info("{}-[test]  {} {}",getCurrentTime(),"out","start");
//    	for(int i=0; i<5 ;i++){
//    		cache.out(String.valueOf(i));
//    		System.out.println("left pop key:"+i);
//    	}
    }
    
    /*-------------------String 类型--------------------*/
    
    /**
     * 查看某个key对应的value值是否存在
     */
    @Test
    public void testExists(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"exists","start");
    	boolean e = cache.exists("1");
    	System.out.println("exixsts 1?"+e);
    	
    }
    
    /**
     * 设置值
     */
    @Test
    public void TestSet(){
    	logger.info("{} -[test]- {} {}",now(),"set","start");
    	if (cache.exists("1")) {
			cache.set("1", "change 1");
		}
    	cache.set("0", "0000");
    	cache.set("2", "0002");

    }
    
    /**
     * 获取某一个key对应的value值
     */
    @Test
    public void testGet(){
    	logger.info("{} -[test]- {} {}",now(),"get","start");
    	if (cache.exists("1")) {
			String val = cache.get("1");
			System.out.println("value of 1 is "+val);
		} else {
			System.out.println("value of 1 not exists");
		}
    }
    
    /**
     * 删除某一个key对应的value值
     */
    @Test
    public void testDel(){
    	logger.info("{} -[test]- {} {}",now(),"delete","start");
    	if (cache.exists("1")) {
			System.out.println("exists 1");
			cache.delete("1");
		} else {
			System.out.println("not exists 1");
		}
    }
    
    /**
     * 删除多个key值
     */
    @Test
    public void testMDel(){
    	logger.info("{} -[test]- {} {}",now(),"mutil delete","start");
    	List<String> list = new ArrayList<>();
    	list.add("mset1");
    	list.add("mset2");
    	list.add("mset3");
    	cache.delete(list);
    	System.out.println("-----have deleted all list of keys----");
    	List<String> vals = cache.mget(list);
    	for (String string : vals) {
			System.out.println("mget values: " + string);
		}
    }
    
    /**
     * 设置多个key-value值
     */
    @Test
    public void testMSet(){
    	logger.info("{} -[test]- {} {}",now(),"delete","start");
    	Map<String, String> map = new HashMap<>();
    	map.put("mset1", "1");
    	map.put("mset2", "2");
    	map.put("mset3", "3");
    	cache.mset(map);
    	System.out.println("----multi set ----");
    	List<String> list = new ArrayList<>();
    	list.add("mset1");
    	list.add("mset2");
    	list.add("mset3");
    	List<String> vals = cache.mget(list);
    	for (String string : vals) {
			System.out.println("mget values: " + string);
		}
    }
    
    /**
     * 测试获取多个key值的集合，
     * 若key对应的值不存在则返回null
     */
    @Test
    public void testMGet(){
    	logger.info("{} -[test]- {} {}",getCurrentTime(),"get","start");
    	List<String> list = new ArrayList<>();
    	list.add("1");
    	list.add("0");
    	list.add("5");
    	List<String> vals = cache.mget(list);
    	for (String string : vals) {
			System.out.println("mget " + string);
		}
    }
    
    /**
     * keys 的返回集合类型只能是LinkedHashSet
     */
    @Test
    public void  testKeys(){
    	logger.info("{} -[test]- {} {}",now(),"keys","start");
    	Set<String> set = new LinkedHashSet<>();
    	set = (Set<String>) cache.keys("1");
    	for (String string : set) {
			System.out.println("keys " + string);
		}
    }
    
    /**
     * 同时设置多个值
     */
    @Test
    public void testMset(){
    	logger.info("{} -[test]- {} {}",now(),"mset","start");
    	Map<String, String> map = new HashMap<>();
    	map.put("0", "0");
    	map.put("1", "1");
    	cache.mset(map);
    }
    
    /*-------------------obj 类型-->String--------------------*/
    /**
     * 存储一个对象的json字符串一定时间，
     * 超出这个时间则从redis中删除该对象
     */
    @Test
    public void testSetObj(){
    	User u1 = new User();
    	u1.setUsername("genius");
    	u1.setId(88908);
    	u1.setPhone("122321312321");
    	u1.setEmail("ronhdkf@133.com");
    	logger.info("{}-[test] {} {}",getCurrentTime(),"testSetObj","start");
    	cache.set("user2", u1, 60L, TimeUnit.SECONDS);
    }
    
    @Test
    public void testGetObjJson(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"testGetObjJson","start");
    	String value = cache.get("user2");
    	System.out.println("testGetObjJson user2 via jsonType:"+value);
    }
    
    @Test
    public void testSaveUUID(){
    	String forgetToken = UUID.randomUUID().toString();
    	cache.set("token_admin", forgetToken, 12L, TimeUnit.HOURS);
    	System.out.println("testSaveUUID for token_admin ,the value is:"+forgetToken);
    }
    
    @Test
    public void testGetUUID(){
    	String value = cache.getNONValue("token_admin");
    	System.out.println("testGetUUID for token_admin ,the value is:"+value);
    }
    
    /*-------------------List 类型--------------------*/
    /**
     * 注意：以下api大多数只适用于对redis list类型的操作
     */
    @Test
    public void testPush() {
    	logger.info("{}-[test] {} {}",getCurrentTime(),"push","start");
    	cache.push("test1", "001");
    	cache.push("test2", "002");
    	cache.push("test3", "003");
    	cache.push("test4", "004");
    	cache.push("test1", "005");
	}
    
    @Test
    public void testSize(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"size","start");
    	System.out.println("size:"+cache.size("test1"));
    }
    
    @Test
    public void testRange(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"range","start");
    	List<String> list = cache.range("test1", 0, 10);
    	for(String s: list){
    		System.out.println(s);
    	}
    }
    
    @Test
    public void testLength(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"length","start");
    	Long length = cache.length("test1");
    	System.out.println(length);
    }
    
    @Test
    public void testIndex(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"index","start");
    	String value = cache.index("test1", 1);
    	System.out.println("index:"+value);
    }
    
    @Test
    public void testPop(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"pop","start");
    	String val = cache.pop("test1");
    	System.out.println("pop:"+val);
    }
    
    @Test
    public void testRemove(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"remove","start");
    	cache.remove("test2", 2L, null);
    }
    
    
    /*----------------sorted set------------------*/
    @Test
    public void testZadd(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"zadd","start");
    	String peter = "peter";
    	double score1 = 89.00;
    	String anne  = "anne";
    	double score2 = 90.01;
    	cache.zadd("scoreboard", anne, score2);
    	System.out.println("---------have zadd one val--------");
    }
    
    @Test
    public void testZRange(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"zrange","start");
    	List<String> scores = cache.zrange("scoreboard", 0L, 2L);
    	for (String string : scores) {
			System.out.println("member:"+string);
		}
    	
    }
    
    @Test
    public void testZscore(){
    	logger.info("{}-[test] {} {}",getCurrentTime(),"zscore","start");
    	String peter = "peter";
    	Double score = cache.zscore("scoreboard", peter);
    	System.out.println("---------"+peter+"'s score:");
    }
    
    public static String getCurrentTime(){
    	StringBuilder sb = new StringBuilder();
    	Calendar c = Calendar.getInstance();//可以对每个时间域单独修改

    	int year = c.get(Calendar.YEAR); 
    	int month = c.get(Calendar.MONTH); 
    	int date = c.get(Calendar.DATE); 
    	int hour = c.get(Calendar.HOUR_OF_DAY); 
    	int minute = c.get(Calendar.MINUTE); 
    	int second = c.get(Calendar.SECOND);
    	return sb.append(year).append("/").append(month).append("/").append(date)
    			.append(" ").append(hour).append(":").append(minute).append(":").append(second)
    			.toString();
    }
    
    public static String now(){
    	Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(now);
    }

	  
}
