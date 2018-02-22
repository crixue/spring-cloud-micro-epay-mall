package com.epayMall.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.pool.DruidDataSource;

	@Configuration    
	public class DruidConfig {    
	    private Logger logger = LoggerFactory.getLogger(DruidConfig.class);    
	        
	    @Value("${spring.datasource.url}")    
	    private String dbUrl;    
	        
	    @Value("${spring.datasource.username}")    
	    private String username;    
	        
	    @Value("${spring.datasource.password}")    
	    private String password;    
	        
	    @Value("${spring.datasource.driver-class-name}")    
	    private String driverClassName;    
	        
	    @Value("${spring.datasource.druid.initial-size}")    
	    private int initialSize;    
	        
	    @Value("${spring.datasource.druid.min-idle}")    
	    private int minIdle;    
	        
	    @Value("${spring.datasource.druid.max-active}")    
	    private int maxActive;    
	        
	    @Value("${spring.datasource.druid.max-wait}")    
	    private int maxWait;    
	        
	    @Value("${spring.datasource.druid.time-between-eviction-runs-millis}")    
	    private int timeBetweenEvictionRunsMillis;    
	        
	    @Value("${spring.datasource.druid.min-evictable-idle-time-millis}")    
	    private int minEvictableIdleTimeMillis;    
	        
	    @Value("${spring.datasource.druid.validation-query}")    
	    private String validationQuery;    
	        
	    @Value("${spring.datasource.druid.test-while-idle}")    
	    private boolean testWhileIdle;    
	        
	    @Value("${spring.datasource.druid.test-on-borrow}")    
	    private boolean testOnBorrow;    
	        
	    @Value("${spring.datasource.druid.test-on-return}")    
	    private boolean testOnReturn;    
	        
	    @Value("${spring.datasource.druid.pool-prepared-statements}")    
	    private boolean poolPreparedStatements;    
	        
	    @Value("${spring.datasource.druid.max-pool-prepared-statement-per-connection-size}")    
	    private int maxPoolPreparedStatementPerConnectionSize;    
	        
	    @Value("${spring.datasource.druid.filters}")    
	    private String filters;    
	        
	    @Bean     //声明其为Bean实例    
	    @Primary  //在同样的DataSource中，首先使用被标注的DataSource    
	    public DataSource dataSource(){    
	        DruidDataSource datasource = new DruidDataSource();    
	            
	        datasource.setUrl(this.dbUrl);    
	        datasource.setUsername(username);    
	        datasource.setPassword(password);    
	        datasource.setDriverClassName(driverClassName);    
	            
	        //configuration    
	        datasource.setInitialSize(initialSize);    
	        datasource.setMinIdle(minIdle);    
	        datasource.setMaxActive(maxActive);    
	        datasource.setMaxWait(maxWait);    
	        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);    
	        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);    
	        datasource.setValidationQuery(validationQuery);    
	        datasource.setTestWhileIdle(testWhileIdle);    
	        datasource.setTestOnBorrow(testOnBorrow);    
	        datasource.setTestOnReturn(testOnReturn);    
	        datasource.setPoolPreparedStatements(poolPreparedStatements);    
	        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);    
	        try {    
	            datasource.setFilters(filters);    
	        } catch (SQLException e) {    
	            logger.error("druid configuration initialization filter", e);    
	        }    
	            
	        return datasource;    
	    }    
	} 
