package com.allen.monkeyQuestion.constant;

import java.util.List;

/**
 * 用户常量
 *
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 项目权限数组
     */
    List<String> ROLE_ARRAY = List.of(ADMIN_ROLE, DEFAULT_ROLE, BAN_ROLE);

    // endregion

    // region 默认数据设置

    /**
     * 默认用户名
     */
    String DEFAULT_USER_NAME = "用户";

    /**
     * 默认密码
     */
    String DEFAULT_USER_PASSWORD = "12345678";

    /**
     * 默认头像
     */
    String DEFAULT_USER_AVATAR = "https://www.mianshiya.com/_next/image?url=%2Flogo.png&w=32&q=75";

    /**
     * 默认简介
     */
    String DEFAULT_USER_PROFILE = "用户未有什么想说的~";

    // endregion
}
