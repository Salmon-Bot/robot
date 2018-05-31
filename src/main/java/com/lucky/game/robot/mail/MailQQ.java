package com.lucky.game.robot.mail;

import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
public class MailQQ {

    public static void sendEmail(String subject, String content,String toUser) {
        try {
            //0.1 确定连接位置
            Properties props = new Properties();
//            //获取163邮箱smtp服务器的地址，
//            props.setProperty("mail.host", "smtp.qq.com");
//            //是否进行权限验证。
//            props.setProperty("mail.smtp.auth", "true");

            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", "smtp.qq.com");
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.socketFactory.port", "465");

            //0.2确定权限（账号和密码）
            Authenticator authenticator = new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    //填写自己的163邮箱的登录帐号和授权密码，授权密码的获取，在后面会进行讲解。
                    return new PasswordAuthentication("824968443@qq.com", "wlpjwdwpwsqybdib");
                }
            };

            //1 获得连接
            /**
             * props：包含配置信息的对象，Properties类型
             *         配置邮箱服务器地址、配置是否进行权限验证(帐号密码验证)等
             *
             * authenticator：确定权限(帐号和密码)
             *
             * 所以就要在上面构建这两个对象。
             */

            Session session = Session.getDefaultInstance(props, authenticator);


            //2 创建消息
            Message message = new MimeMessage(session);
            // 2.1 发件人        xxx@163.com 我们自己的邮箱地址，就是名称
            message.setFrom(new InternetAddress("824968443@qq.com"));
            /**
             * 2.2 收件人
             *         第一个参数：
             *             RecipientType.TO    代表收件人
             *             RecipientType.CC    抄送
             *             RecipientType.BCC    暗送
             *         比如A要给B发邮件，但是A觉得有必要给要让C也看看其内容，就在给B发邮件时，
             *         将邮件内容抄送给C，那么C也能看到其内容了，但是B也能知道A给C抄送过该封邮件
             *         而如果是暗送(密送)给C的话，那么B就不知道A给C发送过该封邮件。
             *     第二个参数
             *         收件人的地址，或者是一个Address[]，用来装抄送或者暗送人的名单。或者用来群发。可以是相同邮箱服务器的，也可以是不同的
             *         这里我们发送给我们的qq邮箱
             */
            InternetAddress[] internetAddressTo = InternetAddress.parse(toUser);

            message.addRecipients(RecipientType.TO,internetAddressTo);
            // 2.3 主题（标题）
            message.setSubject(subject);
            // 2.4 正文
            //设置编码，防止发送的内容中文乱码。
            message.setContent(content, "text/html;charset=UTF-8");


            //3发送消息
            Transport.send(message);
            log.info("send email success");
        } catch (MessagingException e) {
            log.error("send email fail.. mag=" + e.getMessage());
            ;
        }
    }
}