package com.allen.mianshiya.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.constant.CommonConstant;
import com.allen.mianshiya.constant.UserConstant;
import com.allen.mianshiya.exception.BusinessException;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.mapper.UserMapper;
import com.allen.mianshiya.model.dto.user.UserQueryRequest;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.enums.UserRoleEnum;
import com.allen.mianshiya.model.vo.LoginUserVO;
import com.allen.mianshiya.model.vo.UserVO;
import com.allen.mianshiya.service.UserService;
import com.allen.mianshiya.utils.SnowflakeUtils;
import com.allen.mianshiya.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.allen.mianshiya.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 添加用户
     *
     * @param user 用户信息
     * @return 新用户id
     */
    @Override
    public Long addUser(User user) {

        String userAccount = user.getUserAccount();
        String userRole = user.getUserRole();

        // 校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userRole),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(!UserConstant.ROLE_ARRAY.contains(userRole), ErrorCode.PARAMS_ERROR, "用户角色不存在");

        synchronized (userAccount.intern()) {
            // 插入默认密码
            String encryptPassword = DigestUtils
                    .md5DigestAsHex((CommonConstant.SALT + UserConstant.DEFAULT_USER_PASSWORD).getBytes());
            user.setUserPassword(encryptPassword);

            // 插入默认数据
            if (StringUtils.isBlank(user.getUserName())) {
                user.setUserName(UserConstant.DEFAULT_USER_NAME + SnowflakeUtils.getId());
            }
            if (StringUtils.isBlank(user.getUserAvatar())) {
                user.setUserAvatar(UserConstant.DEFAULT_USER_AVATAR);
            }
            if (StringUtils.isBlank(user.getUserProfile())) {
                user.setUserProfile(UserConstant.DEFAULT_USER_PROFILE);
            }

            // 调用方法
            boolean result = this.save(user);

            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return user.getId();
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户id
     * @return 删除结果
     */
    @Override
    public Boolean deleteUser(Long id) {
        // 查找用户是否存在
        User user = this.getById(id);
        ThrowUtils.throwIf(Objects.isNull(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return true;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新结果
     */
    @Override
    public Boolean updateUser(User user) {
        // 查找用户是否存在
        User oldUser = this.getById(user.getId());
        ThrowUtils.throwIf(Objects.isNull(oldUser), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(!UserConstant.ROLE_ARRAY.contains(user.getUserRole())
                , ErrorCode.PARAMS_ERROR, "用户角色不存在");

        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest 查询条件
     * @return 分页结果
     */
    @Override
    public Page<User> listUserByPage(UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(userQueryRequest));
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");

        synchronized (userAccount.intern()) {
            // 账户不能重复
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getUserAccount, userAccount);
            long count = this.baseMapper.selectCount(userLambdaQueryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");

            // 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((CommonConstant.SALT + userPassword).getBytes());

            // 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            // 默认数据
            user.setUserName(UserConstant.DEFAULT_USER_NAME + SnowflakeUtils.getId());
            user.setUserAvatar(UserConstant.DEFAULT_USER_AVATAR);
            user.setUserProfile(UserConstant.DEFAULT_USER_PROFILE);

            // 操作数据库
            boolean saveResult = this.save(user);
            ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败");

            return user.getId();
        }

    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");

        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((CommonConstant.SALT + userPassword).getBytes());

        // 查询用户是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(lambdaQueryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");

        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 用户登录（微信开放平台）
     *
     * @param wxOAuth2UserInfo 从微信获取的用户信息
     * @param request          http请求
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("union_id", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request http请求
     * @return 登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    /**
     * 用户注销
     *
     * @param request http请求
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request.getSession().getAttribute(USER_LOGIN_STATE) == null,
                ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVOByUserList(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param id 用户id
     * @return 脱敏用户信息
     */
    @Override
    public UserVO getUserVOById(Long id) {
        User user = this.getById(id);
        return this.getUserVOByUserList(user);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param ids 用户id列表
     * @return 脱敏用户信息Map
     */
    @Override
    public Map<Long, UserVO> getUserVOMapByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new HashMap<>();
        }

        List<User> userList = this.listByIds(ids);
        return userList.stream().map(this::getUserVOByUserList)
                .collect(Collectors.toMap(UserVO::getId, userVO -> userVO));
    }

    @Override
    public List<UserVO> getUserVOByUserList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVOByUserList).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        // 统一处理字符串参数
        String userName = Objects.toString(userQueryRequest.getUserName(), "");
        String userAccount = Objects.toString(userQueryRequest.getUserAccount(), "");
        String userProfile = Objects.toString(userQueryRequest.getUserProfile(), "");
        String userRole = Objects.toString(userQueryRequest.getUserRole(), "");
        String sortField = SqlUtils.toUnderlineCase(Objects.toString(userQueryRequest.getSortField(), ""));
        String sortOrder = Objects.toString(userQueryRequest.getSortOrder(), "");

        // 创建查询包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(!userAccount.isEmpty(), "user_account", userAccount);
        queryWrapper.eq(!userRole.isEmpty(), "user_role", userRole);
        queryWrapper.like(!userProfile.isEmpty(), "user_profile", userProfile);
        queryWrapper.like(!userName.isEmpty(), "user_name", userName);

        // 验证排序字段
        if (!sortField.isEmpty() && SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        }

        return queryWrapper;
    }
}
