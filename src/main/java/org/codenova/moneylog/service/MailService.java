package org.codenova.moneylog.service;


import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.entity.Verification;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {
    private JavaMailSender mailSender;

    public boolean sendWelcomeMessage(User user) {
        SimpleMailMessage message = new SimpleMailMessage();


        message.setTo(user.getEmail());
        message.setSubject("[코드노바] 머니로그 회원가입을 환영합니다.");
        message.setText(" 안녕하세요, "+ user.getNickname() + "님! \n 머니로그에 가입해주셔서 감사합니다.\n\n 앞으로 다양한 컨텐츠와 서비스를 제공해드리겠습니다. \n\n 팀 코드노바 드림");

        boolean result =true;
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;
        }
        return result;
    }

    public boolean sendWelcomeHtmlMessage(User user) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("[코드노바] 머니로그 회원가입을 환영합니다.");

            String html = "<h2>안녕하세요, 머니로그입니다.</h2>";
            html += "<p><a href='http://192.168.10.61:8080'>머니로그</a>에 가입하신 것을 진심으로 환영합니다.</p>";
            html += "<p>앞으로 다양한 컨텐츠와 서비스를 제공해드리겠습니다.</p>";
            html += "<p><span style='color : gray'>팀 코드노바 올림</span></p>";
            messageHelper.setText(html , true);

            mailSender.send(message);

        }catch(Exception e){
            log.error("error = {}", e);
            return false;
        }
        return true;
    }


    public boolean sendTemporalPasswordMessage(String email, String temporalPassword) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("[코드노바] 임시비밀번호가 발급되었습니다");
        message.setText("문의하신 이메일 : "+ email +" 의 임시비밀번호는 \n\n"+ temporalPassword+"입니다.");

        boolean result =true;
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("error = {}", e);
            result = false;
        }
        return result;
    }

    public boolean sendVerificationMessage(User user, Verification verification) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "utf-8");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("[코드노바] 인증확인 메일입니다.");

            String html = "<h2>안녕하세요, 머니로그입니다.</h2>";
            html += "<p><a href='http://192.168.10.61:8080/auth/verification?token="+ verification.getToken() +"'>머니로그</a>인증확인 메일입니다.</p>";
            html += "<p>앞으로 다양한 컨텐츠와 서비스를 제공해드리겠습니다.</p>";
            html += "<p><span style='color : gray'>팀 코드노바 올림</span></p>";
            messageHelper.setText(html , true);

            mailSender.send(message);

        }catch(Exception e){
            log.error("error = {}", e.getMessage());
            return false;
        }
        return true;
    }

}
