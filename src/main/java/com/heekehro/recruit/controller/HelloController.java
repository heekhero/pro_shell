package com.heekehro.recruit.controller;


import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.heekehro.recruit.service.PersonService;
import com.heekehro.recruit.utils.IpUtil;
import com.heekehro.recruit.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;

@Controller
public class HelloController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @Autowired
    private PersonService personService;

    private String secCode = "@Ag&1(+s007HF#~`A>Yst#^";

    private static final String KEY = "Lp?<1`99sdgt(DJ8a^*kj&^%";

    @GetMapping(value = {"/", ""})
    public String redictoMain(HttpServletRequest request){
        String ip = IpUtil.getIpAddr(request);
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        if((opsForValue.get(ip + secCode) != null) && (Integer.parseInt(opsForValue.get(ip + secCode)) >= 4)){
            return "html/Thanks";
        }
        return "html/Login";
    }

    @GetMapping(value = "/err")
    public String red2Que(HttpServletRequest request){
        String ip = IpUtil.getIpAddr(request);
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        if((opsForValue.get(ip + secCode) != null) && (Integer.parseInt(opsForValue.get(ip + secCode)) >= 4)){
            return "html/Thanks";
        }
        return "html/Main";
    }

    @PostMapping(value = "/info")
    public String get_info(@RequestParam("age") String age,
                           @RequestParam("gender") String gender,
                           HttpServletRequest request){
        String ip = IpUtil.getIpAddr(request);
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        if((opsForValue.get(ip + secCode) != null) && (Integer.parseInt(opsForValue.get(ip + secCode)) >= 4)){
            return "html/Thanks";
        }
        if(hasLength(age)) {
            opsForValue.set(ip + secCode + "age", age);
        }
        if(hasLength(gender)) {
            opsForValue.set(ip + secCode + "gender", gender);
        }
        return "html/Main";
    }

    @PostMapping(value = "/data_gain")
    public String get_data(Person person, HttpServletRequest request,
                           @RequestParam("varcode") String varcode,
                           @RequestParam("cla") String cla){
        String ip = IpUtil.getIpAddr(request);
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        if((opsForValue.get(ip + secCode) != null) && (Integer.parseInt(opsForValue.get(ip + secCode)) >= 4)){
            return "html/Thanks";
        }
        person.setAge(Integer.parseInt(opsForValue.get(ip + secCode + "age")));
        person.setGender(Integer.parseInt(opsForValue.get(ip + secCode + "gender")));
        person.setIp(ip);
        person.setHidden(Integer.parseInt(cla));
        String hash = MD5Util.getMD5Code(KEY + "@" + varcode.toLowerCase());
        if((opsForValue.get(ip + secCode + "Vail") == null) || !(opsForValue.get(ip + secCode + "Vail").equals(hash))){
            return "html/Error";
        }
        personService.createData(person);
        opsForValue.increment(ip + secCode, 1);
        if((opsForValue.get(ip + secCode) != null ) && (Integer.parseInt(opsForValue.get(ip + secCode)) >= 4)){
            return "html/Thanks";
        }
        return "html/Main";
    }

    @GetMapping("/defaultKaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        byte[] captchaChallengeAsJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String temp = "ACEFGHKMNPRSTUVWXYabcefghkmnprstuvwxy3456789";
            String[] arg_strs = temp.split("");
            Random random = new Random();
            StringBuffer a= new StringBuffer();
            for(int i = 0; i < 4; i++){
                a.append(arg_strs[random.nextInt(arg_strs.length)]);
            }
            String createText = a.toString();
            System.out.println(createText);
            String ip = IpUtil.getIpAddr(httpServletRequest);
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            String hash = MD5Util.getMD5Code(KEY + "@" + createText.toLowerCase());
            opsForValue.set(ip + secCode + "Vail", hash);

            httpServletRequest.getSession().setAttribute("vrifyCode", createText);
            //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = defaultKaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream =
                httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }

    private static boolean hasLength(String str) {
        if (str != null && !"".equals(str.trim())) {
            return true;
        }
        return false;
    }

}
