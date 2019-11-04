package cn.woconnect.mailtophone.scheduler;

import cn.woconnect.mailtophone.utils.HttpUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.*;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wjzhang
 * @date 2019/11/4  15:56
 */
@Component
public class SchedulerJob {

    @Value("${woconnect.warning}")
    private String warning;
    @Value("${woconnect.phone}")
    private String phone;
    @Value("${woconnect.username}")
    private String username;
    @Value("${woconnect.password}")
    private String password;

    @Scheduled(cron = "0/10 * * * * ? ")
    public void mailToPhone() throws Exception {
        // host  和  端口
        String host = "imap.mxhichina.com";
        String port = "993";

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Properties props = System.getProperties();
        // imap
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);

        //ssl
        props.setProperty("mail.imap.socketFactory.port", port);
        props.setProperty("mail.imap.auth.login.disable", "true");
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.imap.socketFactory.fallback", "false");

        Session session = Session.getInstance(props);

        IMAPStore store = (IMAPStore) session.getStore("imap"); // 使用imap会话机制，连接服务器
        store.connect(username, password);
        IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
        folder.open(Folder.READ_WRITE);
        // 获取总邮件数
//        total = folder.getMessageCount();

        // 得到收件箱文件夹信息，获取邮件列表
//        System.out.println("未读邮件数：" + folder.getUnreadMessageCount());
        Message[] messages = folder.getMessages();
        int messageNumber = 0;
        for (Message message : messages) {
            Flags flags = message.getFlags();
            if (flags.contains(Flags.Flag.SEEN)) {
//                    System.out.println("这是一封已读邮件");
            } else {

                if (warning.equals(message.getSubject())) {
                    System.out.println("###################发现预警消息##################");
                    System.out.println("###################开始拨打电话##################");
                    yunpianPhone();
                    message.setFlag(Flags.Flag.SEEN, true);
                    System.out.println("#################将邮件修改为已读#################");
                    System.out.println("###################拨打电话完成##################");
                }
            }

        }
        // 释放资源
        if (folder != null)
            folder.close(true);
        if (store != null)
            store.close();


    }

    /**
     * 云片拨打电话接口
     */
    public void yunpianPhone(){

        String host = "https://voice.yunpian.com";
        String path = "/v2/voice/send.json";
        String method = "POST";
        String apikey = "4c84252c6be90ed0e28e17f27d8a962f";
        String code = "888888";
        // 请求头
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);

        // 参数
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("apikey", apikey);
        querys.put("code", code);
        querys.put("mobile", phone);

        Map<String, String> bodys = new HashMap<String, String>();

        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            // 响应
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
