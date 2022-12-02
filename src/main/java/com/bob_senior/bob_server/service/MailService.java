package com.bob_senior.bob_server.service;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.email.EmailAuthRequestDTO;
import com.bob_senior.bob_server.domain.email.EmailAuthResDTO;
import com.bob_senior.bob_server.domain.email.entity.EmailAuth;
import com.bob_senior.bob_server.domain.user.entity.User;
import com.bob_senior.bob_server.repository.EmailAuthRepository;
import com.bob_senior.bob_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@EnableAsync
@Log4j2
public class MailService {

    @Autowired
    private MailSender mailSender;

    private final JavaMailSender javaMailSender;
    private final EmailAuthRepository emailAuthRepository;
    private final UserRepository userRepository;

    public void sendEmail(String toAddress, String fromAddress,
                          String subject, String msgBody) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setFrom(fromAddress);
        smm.setTo(toAddress);
        smm.setSubject(subject);
        smm.setText(msgBody);

        mailSender.send(smm);
    }


    //인증 이메일 발송
    @Async
    public void send(String email, String authToken) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(email);
        smm.setSubject("회원가입 이메일 인증");
        smm.setText("http://localhost:8080/confirm-mail?email="+email+"&authToken="+authToken);

        javaMailSender.send(smm);
    }


    public EmailAuthResDTO authMail(EmailAuthRequestDTO emailAuthRequestDTO) throws BaseException{

        EmailAuth emailAuth = emailAuthRepository.findByEmailAndAndAuthToken(emailAuthRequestDTO.getEmail(), emailAuthRequestDTO.getAuthToken());


        //만약 만료라면 (만료시간<현재시간) 즉 만료시간 지남
        //만료 시간 안내
        if(emailAuth.getExpireDate().isBefore(LocalDateTime.now())){

            log.info("emailAuth.getExpireDate() = {}",emailAuth.getExpireDate());
            log.info("LocalDateTime.now() = {}", LocalDateTime.now());

            throw new BaseException(BaseResponseStatus.EXPIRED_MAIL_LINK);
        }


        User user = userRepository.findTopByEmailOrderByCreatedAtDesc(emailAuthRequestDTO.getEmail());
        user.setAuthorizedStatus("A");
        userRepository.save(user);


        emailAuth.authMail();
        emailAuthRepository.save(emailAuth);

        return new EmailAuthResDTO("인증이 완료되었습니다. 로그인 해주세요.");
    }


}
