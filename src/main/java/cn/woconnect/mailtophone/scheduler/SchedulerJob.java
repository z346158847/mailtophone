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
import javax.mail.search.FlagTerm;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 轮询接收邮件
 *
 * @author wjzhang
 * @date 2019/11/4  15:56
 */
@Component
public class SchedulerJob {
    /**
     * 告警消息
     */
    @Value("${woconnect.warning}")
    private String warning;
    /**
     * 通知电话
     */
    @Value("${woconnect.phone}")
    private String phone;
    /**
     * 邮箱名
     */
    @Value("${woconnect.username}")
    private String username;
    /**
     * 邮箱授权码  ali企业邮箱为密码
     */
    @Value("${woconnect.password}")
    private String password;

    @Scheduled(cron = "0/10 * * * * ? ")
    public void mailToPhone() throws Exception {
        // host  和  端口
        String host = "imap.mxhichina.com";
        String port = "993";

        // 配置连接邮箱所需参数
        Properties props = System.getProperties();
        // imap
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port);

        //ssl连接  linux中一般需要ssl
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.setProperty("mail.imap.socketFactory.port", port);
        props.setProperty("mail.imap.auth.login.disable", "true");
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.imap.socketFactory.fallback", "false");

        Session session = Session.getInstance(props);
        // 使用imap会话机制，连接服务器
        IMAPStore store = (IMAPStore) session.getStore("imap");
        // 建立连接
        store.connect(username, password);
        // 收件箱
        IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
        // 设置为读和写
        folder.open(Folder.READ_WRITE);
        // 获取总邮件数
        folder.getMessageCount();

        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        /**
         * Flag 类型列举如下
         * Flags.Flag.ANSWERED 邮件回复标记，标识邮件是否已回复。
         * Flags.Flag.DELETED 邮件删除标记，标识邮件是否需要删除。
         * Flags.Flag.DRAFT 草稿邮件标记，标识邮件是否为草稿。
         * Flags.Flag.FLAGGED 表示邮件是否为回收站中的邮件。
         * Flags.Flag.RECENT 新邮件标记，表示邮件是否为新邮件。
         * Flags.Flag.SEEN 邮件阅读标记，标识邮件是否已被阅读。
         * Flags.Flag.USER 底层系统是否支持用户自定义标记，只读。
         */
        // 得到收件箱文件夹信息，获取邮件列表
//        System.out.println("未读邮件数：" + folder.getUnreadMessageCount());

        //全部邮件
//        Message[] messages = folder.getMessages();

        // 根据设置好的条件获取message
        Message[] messages = folder.search(ft);
        for (Message message : messages) {
            // 过滤所需预警标题
            if (warning.equals(message.getSubject())) {
                System.out.println("###################发现预警消息##################");
                System.out.println("###################开始拨打电话##################");
                yunpianPhone();
                message.setFlag(Flags.Flag.SEEN, true);
                System.out.println("#################将邮件修改为已读#################");
                System.out.println("###################拨打电话完成##################");
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
    public void yunpianPhone() {

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
