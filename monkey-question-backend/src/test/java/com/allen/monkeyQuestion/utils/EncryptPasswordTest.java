package com.allen.monkeyQuestion.utils;

import com.allen.monkeyQuestion.constant.CommonConstant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

/**
 * EasyExcel 测试
 *
 */
@SpringBootTest
public class EncryptPasswordTest {

    @Test
    public void getEncryptPassword() {
        // 加密
        String password = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((CommonConstant.SALT + password).getBytes());
        System.out.println(encryptPassword);
    }


}