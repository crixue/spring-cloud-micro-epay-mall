package com.epayMall.component.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.epayMall.redis.ICacheService;

@Component
public class MySessionDao extends AbstractSessionDAO {

	private static Logger logger = LoggerFactory.getLogger(MySessionDao.class);
	
	@Autowired
	private ICacheService cacheService;
	
	private static final String SHIRO_REDIS_PREFIX_KEY = "shiro_session:";
	
	private synchronized String generateCustomKey(String sessionId) {
		return SHIRO_REDIS_PREFIX_KEY + sessionId;
	}
	
	@Override
	public void delete(Session session) {
		Serializable sessionId = session.getId();
		String sessionIdStr = sessionId.toString();
		
		logger.debug("delete session:{}", sessionIdStr);
		String key = generateCustomKey(sessionIdStr);
		cacheService.delete(key);
	}

	@Override
	public Collection<Session> getActiveSessions() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public void update(Session session) throws UnknownSessionException {
		doCreate(session);
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = session.getId();
		String sessionIdStr = sessionId.toString();
		
		logger.debug("do create new session:{}", sessionIdStr);
		String key = generateCustomKey(sessionIdStr);
		cacheService.set(key, session, 7L, TimeUnit.DAYS);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if(sessionId == null){
			return null;
		}
		String sessionIdStr = sessionId.toString();
		logger.debug("do read one session:{}", sessionIdStr);
		
		String key = generateCustomKey(sessionIdStr );
		String valueJson = cacheService.get(key);
		if(StringUtils.isNoneBlank(valueJson)) {
			Session session = JSON.parseObject(valueJson, Session.class);
			return session;
		} else {
			return null;
		}

	}

}
