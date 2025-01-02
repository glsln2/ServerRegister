package com.lslnlk.controller;

import com.lslnlk.common.Result;
import com.lslnlk.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
@RequestMapping
public class RegisterController {

    @Resource
    private RegisterService registerService;

    @RequestMapping
    public String index(Model model) {
        return "index";
    }

    @RequestMapping("/register")
    @ResponseBody
    public Result<Boolean> register(@RequestParam String email) {
        Boolean b = registerService.registerOne(email);
        if (b) {
            return Result.success(b);
        }else{
            return Result.error(b);
        }
    }

    @RequestMapping("/registerBatch")
    public void registerBatch(String email,Integer threadCount,Integer registerCount, HttpServletResponse response) {
        //从response中获取输出流
        response.setContentType("text/html;charset=utf-8");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            AtomicInteger atomicCount = new AtomicInteger(1);
            AtomicBoolean atomicFlag = new AtomicBoolean(false);
            //创建三个线程
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    while (true) {
                        if (atomicFlag.get()) {
                            break;
                        }
                        if (atomicCount.get() > registerCount) {
                            break;
                        }
                        Boolean newValue = registerService.registerOne(email);
                        atomicFlag.set(newValue);
                        log.info("第{}次,结果:{}", atomicCount.get(),newValue);
                        try {
                            outputStream.write(("第" + atomicCount.get()+"次,结果:"+newValue + "<br>").getBytes());
                            outputStream.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        atomicCount.getAndIncrement();
                    }
                });
                thread.start();
            }
            //等待所有线程结束
            while (Thread.activeCount() > 1) {
                Thread.yield();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
