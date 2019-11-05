package cn.woconnect.mailtophone.service.impl;

import cn.woconnect.mailtophone.service.MailToPhoneService;
import cn.woconnect.mailtophone.utils.HttpUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.*;

/**
 * 接收邮件电话通知
 *
 * @author wjzhang
 * @date 2019/11/1  18:30
 */
@Service("mailtophoneservice")
public class MailToPhoneServiceImpl implements MailToPhoneService {

    private Date date = new Date();

    private long timeSpace = 30 * 1000L;

    /**
     * 利用javamail的监听器去监听是否收到新邮件
     * 但有bug 连续收到两封邮件（间隔大概小于20s）
     * 会延迟  最长可能10分钟
     * 最后还是采用轮询方式
     */
    @Override
    public void resceiveMailToPhoneVoice() {
        // 收件人邮箱  和  授权码
        String username = "517317384@qq.com";
        String password = "rfufgihobsumcafj";
        // host  和  端口
        String host = "imap.qq.com";
        Integer port = 993;

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        // 构建属性
        Properties props = System.getProperties();
        // imap
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.port", port.toString());

        //ssl
        props.setProperty("mail.imap.socketFactory.port", port.toString());
        props.setProperty("mail.imap.auth.login.disable", "true");
        props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);

        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);
        IMAPFolder folder = null;
        IMAPStore store = null;

        try {
            //得到邮件仓库并连接
            store = (IMAPStore) session.getStore("imap");  // 使用imap会话机制，连接服务器
            store.connect(host, port, username, password);
            folder = (IMAPFolder) store.getFolder("INBOX"); //收件箱
            // 全部文件夹
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] allFolder = defaultFolder.list();

            System.out.println("################连接邮箱服务器成功#################");
            for (int i = 0; i < allFolder.length; i++) {
                System.out.println("这个是服务器中的文件夹=" + allFolder[i].getFullName());
            }
            // 使用读写方式打开收件箱
            folder.open(Folder.READ_WRITE);
            System.out.println("##################开始监听新邮件###################");


            // 监听邮件新增和删除变化
            folder.addMessageCountListener(new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    Message[] msgs = e.getMessages();
                    System.out.println("Get " + msgs.length + " new messages");

                    // Just dump out the new messages
                    for (int i = 0; i < msgs.length; i++) {
                        try {
                            System.out.println("-------------------------");
                            System.out.println("总邮件数 " + msgs[i].getMessageNumber() + ":");
                            System.out.println("发现新邮件，主题:");
                            System.out.println(msgs[i].getSubject());

                            Date receDate = msgs[i].getReceivedDate();
                            System.out.println("接收日期" + receDate);

                            Date date1 = new Date();
                            System.out.println("响应时间" + date1.getTime());
                            System.out.println("延迟时间" + (date1.getTime() - receDate.getTime()));

                            // 过滤主题
                            if ("测试主题".equals(msgs[i].getSubject())) {
                                if (date1.getTime() - date.getTime() > timeSpace) {
                                    date = date1;
                                    System.out.println("时间更新" + date);
                                    System.out.println("拨打一次电话");
                                }
//                                int code = yunpianPhone();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void messagesRemoved(MessageCountEvent e) {
                }
            });


            // imap监听
            boolean isOpenidle = true;
            // 睡眠时间
            String freqs = "5000";

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
                    System.out.println("##################持续监听新邮件###################");
                    IMAPFolder f = (IMAPFolder) folder;
                    f.idle();
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
}
