package cn.woconnect.mailtophone.service;

/**
 * @author wjzhang
 * @date 2019/11/1  18:29
 */
public interface MailToPhoneService {

    /**
     * 利用javamail的监听器去监听是否收到新邮件
     * 但有bug 连续收到两封邮件（间隔大概小于20s）
     * 会延迟  最长可能10分钟
     * 最后还是采用轮询方式
     */
    void resceiveMailToPhoneVoice();

}
