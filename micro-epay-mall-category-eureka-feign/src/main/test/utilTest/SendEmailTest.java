package utilTest;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.epayMall.common.ServerResponse;
import com.epayMall.service.IMailService;
import com.epayMall.service.IUserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
@Component
public class SendEmailTest {

	@Autowired
	private IMailService iMailService;
	
	@Test
	public void testSendSimpleEmail() {
//		String recipientEmail = "1259188425@qq.com";
		String recipientEmail = "986924547@qq.com";
		String subject = "测试通知";
		String content = "123";
		
		iMailService.sendSimpleMail(recipientEmail, subject, content);
	}
	
	
	
}
