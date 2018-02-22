package com.epayMall.service.impl;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.epayMall.service.IMailService;
import com.epayMall.util.Constants;

@Service("iMailService")
public class MailServiceImpl implements IMailService {
	
	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    public static final String HOST = "smtp.163.com";  
    public static final String PROTOCOL = "smtp";     
    public static final int PORT = 25;  
    public static final String SENDER = Constants.getProperty("mail.sender");  //发件人的email  
    public static final String PWD = Constants.getProperty("mail.password");  //发件人客户端授权码  
    
    /** 
     * 获取Session(mail) 邮件会话对象
     * @return 
     */  
    private static Session getSession() {  
        Properties props = new Properties();  
        props.put("mail.smtp.host", HOST);//设置服务器地址  
        props.put("mail.store.protocol" , PROTOCOL);//设置协议  
        props.put("mail.smtp.port", PORT);//设置端口  
        props.put("mail.smtp.auth" , true);  
          
        Authenticator authenticator = new Authenticator() {  
  
            @Override  
            protected PasswordAuthentication getPasswordAuthentication() {  
                return new PasswordAuthentication(SENDER, PWD);  
            }  
              
        };  
        Session session = Session.getDefaultInstance(props , authenticator);  
          
        return session;  
    } 
    
    /** 
     * 发送简单的文本文件（不含附件）
     */
    @Override
    public boolean sendSimpleMail(String recipientEmail, String subject, String content){
    	Session session = getSession();
    	
    	// Instantiate a message  
        Message msg = new MimeMessage(session);  

        //Set message attributes  
        try {
			msg.setFrom(new InternetAddress(SENDER));
	        InternetAddress[] address = {new InternetAddress(recipientEmail)};  
	        msg.setRecipients(Message.RecipientType.TO, address);  
	        msg.setSubject(subject);  
	        msg.setSentDate(new Date());  
	        msg.setContent(content , "text/html;charset=utf-8");  

	        //Send the message  
	        Transport transport = session.getTransport();
	        transport.connect(SENDER, PWD);
	        transport.send(msg);
	        transport.close();
	        return true;
		} catch (MessagingException e) {
			logger.error("发送邮件发生异常，异常原因：{}", e);
			return false;
		}  
 
    }
	
}
