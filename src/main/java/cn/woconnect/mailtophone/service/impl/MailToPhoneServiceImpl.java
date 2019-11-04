package cn.woconnect.mailtophone.service.impl;

import cn.woconnect.mailtophone.service.MailToPhoneService;
import cn.woconnect.mailtophone.utils.HttpUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.*;

/**
 * 接收邮件电话通知
 * @author wjzhang
 * @date 2019/11/1  18:30
 */
@Service("mailtophoneservice")
public class MailToPhoneServiceImpl implements MailToPhoneService {

    private String subject;

    @Override
    public void resceiveMailToPhoneVoice() {
        boolean isOpenidle = true;
        String freqs = "5000";
        String host = "imap.qq.com";
        int port = 993;
        String username = "517317384@qq.com";
        String password = "rfufgihobsumcafj";
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
/* Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                            这里有一个错我是这么解决的（Windows -> Preferences，Java/Compiler/Errors/Warnings->
            Deprecated and restricted API， Forbidden reference (access rules)，原始设定为Error修改为Warning）*/
        Properties props = System.getProperties();
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.imap.socketFactory.port","993");
        props.setProperty("mail.store.protocol","imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.auth.login.disable", "true");
        Session session = Session.getDefaultInstance(props,null);
        session.setDebug(false);
        IMAPFolder folder= null;
        IMAPStore store=null;

        try {
            //得到邮件仓库并连接
            store=(IMAPStore)session.getStore("imap");  // 使用imap会话机制，连接服务器
            store.connect(host,port,username,password);
            folder=(IMAPFolder)store.getFolder("INBOX"); //收件箱

            Folder defaultFolder = store.getDefaultFolder();
            Folder[] allFolder = defaultFolder.list();

            System.out.println("################连接邮箱服务器成功#################");
            for (int i = 0; i < allFolder.length; i++) {
                System.out.println("这个是服务器中的文件夹="+allFolder[i].getFullName());
            }
            // 使用只读方式打开收件箱
            folder.open(Folder.READ_WRITE);

            System.out.println("##################开始监听新邮件###################");
            folder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent ev) {
                    Message[] msgs = ev.getMessages();
                    System.out.println("Get " + msgs.length + " new messages");

                    // Just dump out the new messages
                    for (int i = 0; i < msgs.length; i++) {
                        try {
                            System.out.println("-----");
                            System.out.println("总邮件数 " + msgs[i].getMessageNumber() + ":");
                            System.out.println("发现新邮件主题 " + ":");
                            System.out.println(msgs[i].getSubject());
//                            System.out.println("内容" + ((MimeMessage)msgs[i]).getContent().toString());

                            String from = InternetAddress.toString(msgs[i].getFrom());
                            System.out.println("表单" + from);
                            String replyTo = InternetAddress.toString(msgs[i].getReplyTo());
                            System.out.println("发件人"  + replyTo);
                            String to = InternetAddress.toString(msgs[i].getRecipients(Message.RecipientType.TO));
                            System.out.println("地址" + to);

                            Date sentDate = msgs[i].getSentDate();
                            System.out.println("发送日期" + sentDate);

                            Date receDate = msgs[i].getReceivedDate();
                            System.out.println("接收日期" + receDate) ;
                            Enumeration headers = msgs[i].getAllHeaders();
                            Date date = new Date();
                            System.out.println("响应时间" + date.getTime());
                            System.out.println("延迟时间" + (date.getTime() - receDate.getTime()));
                            if ("测试主题".equals(msgs[i].getSubject())){
//                                int code = yunpianPhone();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            // 检查是否支持imap idle,尝试使用idle
            int freq = Integer.parseInt(freqs);
            boolean supportsIdle = false;
            try {
                if (isOpenidle && folder instanceof IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) folder;
                    f.idle();
                    supportsIdle = true;
                }
            } catch (FolderClosedException fex) {
                throw fex;
            } catch (MessagingException mex) {
                supportsIdle = false;
            }
            for (; ; ) {
                if (supportsIdle && folder instanceof IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) folder;
                    f.idle();
                    System.out.println("##################持续监听新邮件###################");
                } else {
                    System.out.println("idle不支持进入轮询");
                    Thread.sleep(freq); // sleep for freq milliseconds

                    // 注意。getMessageCount时会触发监听器
                    folder.getMessageCount();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }







    public int voicePhone(){

        String host = "http://yuyin2.market.alicloudapi.com";
        String path = "/dx/voice_notice";
        String method = "POST";
        String appcode = "2dd201c029fb425aa6d322d8dc640bfa";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("param", "param");
        querys.put("phone", "17634966141");
        querys.put("tpl_id", "TP18031516");
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
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public int yunpianPhone(){

        String host = "https://voice.yunpian.com";
        String path = "/v2/voice/send.json";
        String method = "POST";
        String apikey = "4c84252c6be90ed0e28e17f27d8a962f";
        String code = "123456";
        String phone = "17634966141";

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);


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
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;

    }







}
