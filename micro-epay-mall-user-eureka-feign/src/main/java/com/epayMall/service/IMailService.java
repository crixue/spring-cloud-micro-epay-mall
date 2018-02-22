package com.epayMall.service;

public interface IMailService {

	boolean sendSimpleMail(String recipientEmail, String subject, String content);

}
