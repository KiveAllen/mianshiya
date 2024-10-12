package com.allen.mianshiya.utils;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeUtils {


    /**
     * 获取下一个雪花ID
     * @return 雪花id
     */
    public static Long getId() {
        SnowflakeGenerator snowflakeGenerator = new SnowflakeGenerator();
        return snowflakeGenerator.next();
    }

}
