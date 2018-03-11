package com.epayMall.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPoolConfig;

//@Configuration
public class RedisConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

	@Value("${spring.redis.host}")
	private String host;
	
	@Value("${spring.redis.port}")
	private int port;
	
	/**
	 * @Bean 和 @ConfigurationProperties
	 * 该功能在官方文档是没有提到的，我们可以把@ConfigurationProperties和@Bean和在一起使用。 
     * 举个例子，我们需要用@Bean配置一个Config对象，Config对象有a，b，c成员变量需要配置， 
     * 那么我们只要在yml或properties中定义了a=1,b=2,c=3， 
     * 然后通过@ConfigurationProperties就能把值注入进Config对象中 
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = "spring.redis.pool")
	public JedisPoolConfig jedisPoolConfig(){
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		return jedisPoolConfig;
	}
	
	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(host);
		factory.setPort(port);
		factory.setUsePool(true);
		factory.setPoolConfig(jedisPoolConfig());
		logger.info("JedisConnectionFactory has been initlized");
		return factory;
	}
	
	/**
	 * 序列化方式 建议key/hashKey采用StringRedisSerializer
	 * @return
	 */
	@Bean(name="redisTemplateTemplate")
	public RedisTemplate<?, ?> redisTemplate() {
		RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(stringRedisSerializer());
		redisTemplate.setHashKeySerializer(stringRedisSerializer());
		redisTemplate.setValueSerializer(jdkSerializationRedisSerializer());
		redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer());
		logger.info("RedisTemplate has been initlized");
		return redisTemplate;
	}
	
	@Bean
	public StringRedisSerializer stringRedisSerializer() {
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		return stringRedisSerializer;
	}
	
	@Bean
	public JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
		JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		return jdkSerializationRedisSerializer;
	}
	
}
