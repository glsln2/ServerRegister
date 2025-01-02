package com.lslnlk.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lslnlk.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取html
     * @param url
     * @return
     */
    public Document getHtml(String url) {
        try {
            return Jsoup.connect("https://www.serv00.com/offer/create_new_account").get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 识别验证码
     * @param url
     * @return
     */
    public String recognize(String url) {
        try {
            byte[] bytes = Jsoup.connect(url).ignoreContentType(true).execute().bodyAsBytes();
            String imageBase64 = Base64.getEncoder().encodeToString(bytes);

            HashMap<String, String> param = new HashMap<>();
            param.put("image_base64", imageBase64);
            String paramJson = objectMapper.writeValueAsString(param);
            String body = HttpUtil.createPost("http://localhost:5000/recognize")
                    .header("Content-Type", "application/json")
                    .body(paramJson)
                    .execute().body();
            HashMap<String,String> hashMap = objectMapper.readValue(body, new TypeReference<HashMap<String, String>>() {
            });
            return hashMap.get("result");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建账号
     * @param email
     * @return
     */
    public boolean createAccount(String email, String captcha0, String captcha1, String csrfmiddlewaretoken) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.put("referer", "https://www.serv00.com/offer/create_new_account");
        headers.put("origin", "https://www.serv00.com");
        HttpCookie httpCookie = new HttpCookie("csrftoken", csrfmiddlewaretoken);

        String first_name = UUID.randomUUID().toString().substring(0, 5);
        String last_name = UUID.randomUUID().toString().substring(0, 4);
        String username = UUID.randomUUID().toString().substring(0, 8);

        try (HttpResponse httpResponse = HttpUtil.createPost("https://www.serv00.com/offer/create_new_account.json")
                .headerMap(headers, true)
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .form("csrfmiddlewaretoken", csrfmiddlewaretoken)
                .form("first_name", first_name)
                .form("last_name", last_name)
                .form("username", username)
                .form("email", email)
                .form("captcha_0", captcha0)
                .form("captcha_1", captcha1)
                .form("question", 0)
                .form("tos", "on")
                .cookie(httpCookie)
                .execute()) {
            if (httpResponse.isOk()) {
                log.info("注册成功:{}", httpResponse.body());
                return true;
            } else {
                if (httpResponse.body().contains("Invalid CAPTCHA")) {
                    log.info("注册失败：验证码错误：{}",captcha1);
                } else if (httpResponse.body().contains("Maintenance time. Try again later.")) {
                    log.info("注册失败：{}","Maintenance time. Try again later.");
                } else {
                    log.info("注册失败：{}",httpResponse.body());
                }
                return false;
            }
        }
    }

    @Override
    public Boolean registerOne(String email) {
        Document doc = getHtml("https://www.serv00.com/offer/create_new_account");
        //获取captcha_0
        String captcha_0 = Objects.requireNonNull(doc.getElementById("id_captcha_0")).val();
        //获取csrfmiddlewaretoken
        String csrfmiddlewaretoken = doc.getElementsByAttributeValue("name", "csrfmiddlewaretoken").val();
        //获取验证码图片
        Elements elementsByClass = doc.getElementsByClass("captcha is-");
        String captcha_1_url = elementsByClass.get(0).attr("src");
        //识别验证码
        String captcha_1 = recognize("https://www.serv00.com" + captcha_1_url);
        return createAccount(email, captcha_0, captcha_1, csrfmiddlewaretoken);
    }
}
