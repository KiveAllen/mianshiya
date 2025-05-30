package com.allen.monkeyQuestion.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.allen.monkeyQuestion.common.ErrorCode;
import com.allen.monkeyQuestion.constant.CommonConstant;
import com.allen.monkeyQuestion.constant.RedisConstant;
import com.allen.monkeyQuestion.constant.UserConstant;
import com.allen.monkeyQuestion.exception.BusinessException;
import com.allen.monkeyQuestion.exception.ThrowUtils;
import com.allen.monkeyQuestion.mapper.UserMapper;
import com.allen.monkeyQuestion.model.dto.user.UserQueryRequest;
import com.allen.monkeyQuestion.model.entity.User;
import com.allen.monkeyQuestion.model.vo.LoginUserVO;
import com.allen.monkeyQuestion.model.vo.UserVO;
import com.allen.monkeyQuestion.satoken.DeviceUtils;
import com.allen.monkeyQuestion.service.UserService;
import com.allen.monkeyQuestion.utils.SnowflakeUtils;
import com.allen.monkeyQuestion.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.allen.monkeyQuestion.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    RedissonClient redissonClient;

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

        // 记录用户的登录态 （旧方法）
//        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // Sa-Token 登录，并指定设备，同端登录互斥
        StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
        StpUtil.getSession().set(USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);
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
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (Objects.isNull(loginId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从当前登录用户信息中获取角色
        Object userObj = StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (userObj == null) return null;

        // 转为JSON对象
        JSONObject userObjJson = JSONUtil.parseObj(userObj);
        return JSONUtil.toBean(userObjJson, User.class);
    }


    /**
     * 用户注销
     *
     * @param request http请求
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        StpUtil.checkLogin();
        // 移除登录态
        StpUtil.logout();
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

    /**
     * 用户签到
     *
     * @param userId 用户id
     * @return 是否签到成功
     */
    @Override
    public boolean addUserSingIn(long userId) {
        LocalDate date = LocalDate.now();
        String key = RedisConstant.getUserSignInRedisKey(date.getYear(), userId);
        // 获取 Redis 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 获取当前日期的索引, 读取当前日期是一年中的第几天, 从 1 开始
        int offset = date.getDayOfYear();
        // 查询当天有没有签到
        if (!signInBitSet.get(offset)) {
            signInBitSet.set(offset, true);
            return true;
        }
        return true;
    }

    @Override
    public List<Integer> getUserSignInRecord(long userId, Integer year) {
        if (year == null) {
            LocalDate date = LocalDate.now();
            year = date.getYear();
        }
        String key = RedisConstant.getUserSignInRedisKey(year, userId);
        // 获取 Redis 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // ** 加载 BitSet 到内存中，避免 Redis 频繁的访问**
        BitSet bitSet = signInBitSet.asBitSet();
        // 改为签到的日期列表 List
        List<Integer> result = new LinkedList<>();
        // ** 通过索引 0 开始查找下一个被设置为 1 的位 （使用的方法是位运算的方式，比普通的for循环效率大大提升了）
        int index = bitSet.nextSetBit(0);
        while (index > 0) {
            result.add(index);
            index = bitSet.nextSetBit(index + 1);
        }
        return result;
    }

}
