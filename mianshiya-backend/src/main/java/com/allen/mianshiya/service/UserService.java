package com.allen.mianshiya.service;

import com.allen.mianshiya.model.dto.user.UserQueryRequest;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.vo.LoginUserVO;
import com.allen.mianshiya.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    // region 管理员增删改查

    /**
     * 添加用户
     *
     * @param user 用户信息
     * @return 新用户id
     */
    Long addUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户id
     * @return 删除结果
     */
    Boolean deleteUser(Long id);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    Boolean updateUser(User user);

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest 查询条件
     * @return 分页结果
     */
    Page<User> listUserByPage(UserQueryRequest userQueryRequest);

    // endregion


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      http请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request http请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request http请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    UserVO getUserVOByUserList(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param id 用户id
     * @return 脱敏用户信息
     */
    UserVO getUserVOById(Long id);

    /**
     * 获取脱敏的用户信息
     *
     * @param ids 用户id列表
     * @return 脱敏用户信息Map
     */
    Map<Long, UserVO> getUserVOMapByIds(List<Long> ids);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户列表
     * @return 脱敏用户信息列表
     */
    List<UserVO> getUserVOByUserList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户签到
     *
     * @param userId 用户id
     * @return 是否签到成功
     */
    boolean addUserSingIn(long userId);

    /**
     * 获取用户某个年份的签到记录
     *
     * @param userId 用户 id
     * @param year   年份（为空表示当前年份）
     * @return 签到记录映射
     */
    List<Integer> getUserSignInRecord(long userId, Integer year);


}
