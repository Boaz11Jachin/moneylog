package org.codenova.moneylog.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.entity.Verification;
import org.codenova.moneylog.repository.UserRepository;
import org.codenova.moneylog.repository.VerificationRepository;
import org.codenova.moneylog.request.FindPasswordRequest;
import org.codenova.moneylog.request.LoginRequest;
import org.codenova.moneylog.service.KakaoAPIService;
import org.codenova.moneylog.service.MailService;
import org.codenova.moneylog.service.NaverAPIService;
import org.codenova.moneylog.vo.KakaoTokenResponse;
import org.codenova.moneylog.vo.NaverProfileResponse;
import org.codenova.moneylog.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalTime.now;


@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

    private KakaoAPIService kakaoAPIService;
    private NaverAPIService naverAPIService;
    private MailService mailService;

    private UserRepository userRepository;
    private VerificationRepository verificationRepository;

    @GetMapping("/login")
    public String loginHandel(Model model){
        //log.info("loginHandle.... executed");

        model.addAttribute("kakaoClientId", "a7f28adcc2f89a36ae98e52e9717cfd9");
        model.addAttribute("kakaoRedirectUri", "http://192.168.10.61:8080/auth/kakao/callback");
        model.addAttribute("naverClientId", "K7tySdVXAoP1xMmOuL8g");
        model.addAttribute("naverRedirectUri", "http://192.168.10.61:8080/auth/naver/callback");

        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPostHandel(@ModelAttribute LoginRequest loginRequest,
                                  HttpSession session,
                                  Model model){

        User user =
                userRepository.findByEmail(loginRequest.getEmail());
        if(user != null && user.getPassword().equals(loginRequest.getPassword())){
            session.setAttribute("user", user);
            return "redirect:/index";
        }

        return "redirect:/auth/login";
    }


    @GetMapping("/signup")
    public String signupGetHandle(Model model){

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user){

        User found = userRepository.findByEmail(user.getEmail());
        if(found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
            mailService.sendWelcomeHtmlMessage(user);
        }
        return "redirect:/index";
    }

    @GetMapping("/find-password")
    public String findPasswordHandle(Model model){

        return "auth/find-password";
    }

    @PostMapping("/find-password")
    public String findPasswordPostHandle(@ModelAttribute @Valid FindPasswordRequest req,
                                         BindingResult result,
                                         Model model){
        if(result.hasErrors()){
            model.addAttribute("error", "이메일 형식이 아닙니다.");
            return "auth/find-password-error";
        }

        User found = userRepository.findByEmail(req.getEmail());
        if(found == null){
            model.addAttribute("error", "등록되지 않은 이메일 입니다.");
            return "auth/find-password-error";
        }

        String temporalPassword = UUID.randomUUID().toString().substring(0, 8);
        userRepository.updatePasswordByEmail(req.getEmail(), temporalPassword);
        mailService.sendTemporalPasswordMessage(req.getEmail(), temporalPassword);



        return "auth/find-password-success";
    }



    @GetMapping("/send-token")
    public String sendTokenHandle(@SessionAttribute("user") User user,
                                  Model model) {
        String token = UUID.randomUUID().toString().replace("-","");
        Verification one = Verification.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .userEmail(user.getEmail())
                .build();
        verificationRepository.create(one);
        mailService.sendVerificationMessage(user, one);
        // 어디다가 보내야하는지와 생성된 토큰번호를 넘겨줘야함.

        return "auth/send-token";

    }

    @GetMapping("/email-verify")
    public String emailVerifyHandle(@RequestParam("token") String token, Model model,
                                    @SessionAttribute("user") User user,
                                    HttpSession session) {
        Verification found = verificationRepository.findByToken(token);
        if (found == null) {
            model.addAttribute("error", "유효하지 않은 인증토큰 입니다.");
            return "auth/email-verify-error";
        }
        // found.getExpiresAt();   // 토큰이 가진 유효만료시점
        // LocalDateTime.now();    // 인증 시점
        if (LocalDateTime.now().isAfter(found.getExpiresAt())) {
            model.addAttribute("error", "유효기간이 만료된 인증토큰 입니다.");
            return "auth/email-verify-error";
        }

        String userEmail = found.getUserEmail();
        userRepository.updateVerifiedByEmail(userEmail);
        user.setVerified("T");
        session.setAttribute("user", user);


        return "auth/email-verify-success";
    }











    @GetMapping("/naver/callback")
    public String naverCallbackHandle(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpSession session) throws JsonProcessingException {
        ////log.info("code = {}", code);
        //log.info("code = {}, state = {}", code, state);
        NaverTokenResponse naverTokenResponse = naverAPIService.exchangeToken(code, state);
        //log.info("naver.accessToken = {}", naverTokenResponse.getAccessToken());
        //log.info("naver.refreshToken = {}", naverTokenResponse.getRefreshToken());

        naverAPIService.exchangeProfile(naverTokenResponse.getAccessToken());


        NaverProfileResponse profileResponse = naverAPIService.exchangeProfile(naverTokenResponse.getAccessToken());
        log.info("profileResponse id = {}", profileResponse.getId());
        log.info("profileResponse nickname = {}", profileResponse.getNickname());
        log.info("profileResponse profileImage = {}", profileResponse.getProfileImage());

        User found = userRepository.findByProviderAndProviderId("NAVER", profileResponse.getId());
        if(found != null){
            session.setAttribute("user", found);
        }else{
            User user = User.builder().provider("NAVER").providerId(profileResponse.getId())
                    .nickname(profileResponse.getNickname())
                    .picture(profileResponse.getProfileImage()).verified("T").build();
            userRepository.save(user);
            session.setAttribute("user", user);
        }


        return "redirect:/index";
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallbackHandle(@RequestParam("code") String code,
                                      HttpSession session) throws JsonProcessingException {
        //log.info("code = {}", code);
        KakaoTokenResponse response = kakaoAPIService.exchangeToken(code);
        log.info("response.idToken = {}", response.getIdToken());

        DecodedJWT decodedJWT = JWT.decode(response.getIdToken());
        String sub = decodedJWT.getClaim("sub").asString();
        String nickname = decodedJWT.getClaim("nickname").asString();
        String picture = decodedJWT.getClaim("picture").asString();

        User found = userRepository.findByProviderAndProviderId("KAKAO", sub);
        if(found != null){
            session.setAttribute("user", found);
        }else{
            User user = User.builder().provider("KAKAO").providerId(sub).nickname(nickname).picture(picture).verified("T").build();
            userRepository.save(user);
            session.setAttribute("user", user);
        }

        log.info("decodedJWT: sub={}, nickname={}, picture={}", sub, nickname, picture);

        return "redirect:/index";
    }


}
