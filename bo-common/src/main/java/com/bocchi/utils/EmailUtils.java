package com.bocchi.utils;

import com.bocchi.constant.OrderConstant;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

@Slf4j
public class EmailUtils {

    public static String getRandomCode() {
        // 获得6位数随机验证码
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static boolean sendEmail(String toAddress, String subject, String message) {
        try {
            // 邮件发送者的邮箱和授权码
            String userName = "........@qq.com";
            String password = "";
            // SMTP服务器属性设置
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true"); // 需要请求认证
            properties.put("mail.smtp.host", "smtp.qq.com"); // SMTP服务器地址
            properties.put("mail.smtp.port", "465"); // SMTP端口号
            properties.setProperty("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");

            // 创建验证器
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 创建邮件会话
            Session session = Session.getInstance(properties, auth);
            // 创建Email消息
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(userName));
            InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
            msg.setRecipients(Message.RecipientType.TO, toAddresses);
            msg.setSubject(subject);
            msg.setSentDate(new java.util.Date());
            msg.setText(message);
            // 发送邮件
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public static boolean sendVerificationEmail(String toAddress, String code) {
        String subject = "Bocchi的家";
        String message = "您的随机验证码是 : " + code + ",该验证码在5分钟内有效,请尽快使用!";
        return sendEmail(toAddress, subject, message);
    }

    public static boolean sendOrderStatusEmail(String toAddress, int orderStatus) {
        String subject = "Bocchi的家";
        String message = "";

        if (orderStatus == OrderConstant.HAD_SEND) {
            message = "您的订单正在派送中，骑手将尽快为您送达，请您耐心等待！";
        } else if (orderStatus == OrderConstant.COMPILE) {
            message = "您的订单已经为您送达，请尽快领取！";
        } else {
            return false;
        }

        return sendEmail(toAddress, subject, message);
    }

    public static boolean sendOrderStatusEmailToUser(String toAddress, int orderStatus, String originalEmail) {
        String subject = "Bocchi的家";
        String message = "由于订单收货人（" + originalEmail + "）无法通知到位，这里对您本人进行提示 ： ";

        if (orderStatus == OrderConstant.HAD_SEND) {
            message += "您的订单正在派送中，骑手将尽快为您送达，请您耐心等待！";
        } else if (orderStatus == OrderConstant.COMPILE) {
            message += "您的订单已经为您送达，请尽快领取！";
        } else {
            return false;
        }

        return sendEmail(toAddress, subject, message);
    }
}
