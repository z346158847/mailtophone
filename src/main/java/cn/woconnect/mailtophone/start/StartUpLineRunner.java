package cn.woconnect.mailtophone.start;

import cn.woconnect.mailtophone.service.MailToPhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author wjzhang
 * @date 2019/11/1  18:35
 */
@Component
@Order(value = 1)
public class StartUpLineRunner implements CommandLineRunner {
    @Autowired
    private MailToPhoneService mailToPhoneService;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("################开始连接邮箱服务器#################");
        mailToPhoneService.resceiveMailToPhoneVoice();

    }
}
