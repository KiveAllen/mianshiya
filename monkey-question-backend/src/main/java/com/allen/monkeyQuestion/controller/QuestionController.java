package com.allen.monkeyQuestion.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.allen.monkeyQuestion.common.BaseResponse;
import com.allen.monkeyQuestion.common.DeleteRequest;
import com.allen.monkeyQuestion.common.ErrorCode;
import com.allen.monkeyQuestion.common.ResultUtils;
import com.allen.monkeyQuestion.constant.UserConstant;
import com.allen.monkeyQuestion.exception.BusinessException;
import com.allen.monkeyQuestion.exception.ThrowUtils;
import com.allen.monkeyQuestion.manager.CounterManager;
import com.allen.monkeyQuestion.model.dto.question.QuestionAddRequest;
import com.allen.monkeyQuestion.model.dto.question.QuestionBatchDeleteRequest;
import com.allen.monkeyQuestion.model.dto.question.QuestionQueryRequest;
import com.allen.monkeyQuestion.model.dto.question.QuestionUpdateRequest;
import com.allen.monkeyQuestion.model.entity.Question;
import com.allen.monkeyQuestion.model.entity.User;
import com.allen.monkeyQuestion.model.vo.QuestionVO;
import com.allen.monkeyQuestion.service.QuestionService;
import com.allen.monkeyQuestion.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private CounterManager counterManager;

    @NacosValue(value = "${warn.count:10}", autoRefreshed = true)
    private Integer warnCount;

    @NacosValue(value = "${ban.count:20}", autoRefreshed = true)
    private Integer banCount;


    // region 增删改查 管理员

    /**
     * 创建题目（仅管理员可用）
     *
     * @param questionAddRequest 问题创建参数
     * @param request            http请求
     * @return 新写入的数据 id
     */
    @PostMapping("/add")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        // 检验请求是否存在
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 拷贝属性
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        question.setTags(JSONUtil.toJsonStr(questionAddRequest.getTags()));

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());

        // 调用 service
        return ResultUtils.success(questionService.addQuestion(question));
    }

    /**
     * 删除题目（仅管理员可用）
     *
     * @param deleteRequest 数据删除参数
     * @return 删除是否成功
     */
    @DeleteMapping("/delete")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        // 检验请求是否存在
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionService.deleteQuestion(deleteRequest.getId()));
    }

    /**
     * 批量删除题目（仅管理员可用）
     *
     * @param batchDeleteRequest 批量删除参数
     * @return 是否删除成功
     */
    @PostMapping("/delete/batch")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteQuestions(@RequestBody QuestionBatchDeleteRequest batchDeleteRequest) {
        // 检验请求是否存在
        ThrowUtils.throwIf(batchDeleteRequest == null || batchDeleteRequest.getQuestionIdList() == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionService.batchDeleteQuestions(batchDeleteRequest.getQuestionIdList()));
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest 更新条件
     * @return 是否更新成功
     */
    @PutMapping("/update")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        ThrowUtils.throwIf(questionUpdateRequest == null || questionUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Question question = new Question();
        question.setTags(JSONUtil.toJsonStr(questionUpdateRequest.getTags()));
        BeanUtils.copyProperties(questionUpdateRequest, question);
        return ResultUtils.success(questionService.updateQuestion(question));
    }

    /**
     * 根据 id 获取题目（仅管理员可用）
     *
     * @param id 题目id
     * @return Question
     */
    @GetMapping("/get")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> getQuestionById(@RequestParam Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(question);
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest 查询条件
     * @return Page<Question>
     */
    @PostMapping("/list/page")
    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        return ResultUtils.success(questionService.getQuestionPage(questionQueryRequest));
    }

    // endregion

    // region 用户

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id 题目id
     * @return QuestionVO
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        crawlerDetect(loginUser.getId());

        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(id));
    }

    /**
     * 检测爬虫
     *
     * @param loginUserId 登录用户id
     */
    private void crawlerDetect(long loginUserId) {
        // 调用多少次时告警
        final int WARN_COUNT = warnCount;
        // 超过多少次封号
        final int BAN_COUNT = banCount;
        // 拼接访问 key
        String key = String.format("user:access:%s", loginUserId);
        // 一分钟内访问次数，180 秒过期
        long count = counterManager.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 180);
        // 是否封号
        if (count > BAN_COUNT) {
            // 踢下线
            StpUtil.kickout(loginUserId);
            // 封号
            User updateUser = new User();
            updateUser.setId(loginUserId);
            updateUser.setUserRole("ban");
            userService.updateById(updateUser);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "访问太频繁，已被封号");
        }
        // 是否告警
        if (count == WARN_COUNT) {
            // 可以改为向管理员发送邮件通知
            throw new BusinessException(110, "警告访问太频繁");
        }
    }


    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest 查询条件
     * @return Page<QuestionVO> (题目列表)
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {

        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(questionService.getQuestionVOPage(questionQueryRequest));
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest 查询条件
     * @return Page<QuestionVO> (题目列表)
     */
    @PostMapping("/list/page/vo/sentinel")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPageSentinel(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request) {

        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 基于IP限流
        String remoteAddress = request.getRemoteAddr();
        Entry entry = null;
        try {
            entry = SphU.entry("listQuestionVOByPage", EntryType.IN, 1, remoteAddress);
            return ResultUtils.success(questionService.getQuestionVOPage(questionQueryRequest));
        } catch (Throwable ex) {
            if (!BlockException.isBlockException(ex)) {
                Tracer.trace(ex);
            }
            if (ex instanceof DegradeException) {
                return handleFallback(questionQueryRequest, ex);
            }
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "访问过于频繁，请稍后重试！");
        } finally {
            if (entry != null) {
                entry.exit(1, remoteAddress);
            }
        }


    }

    /**
     * listQuestionBankVOByPage 降级操作：直接返回本地数据
     */
    public BaseResponse<Page<QuestionVO>> handleFallback(QuestionQueryRequest questionQueryRequest,
                                                         Throwable ex) {
        // 可以返回本地数据或空数据
        return ResultUtils.success(null);
    }


    /**
     * 搜索题目(ES)
     *
     * @param questionQueryRequest 查询条件
     * @return Page<QuestionVO> (题目列表)
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<QuestionVO>> searchQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.searchFromEs(questionQueryRequest);
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        questionVOPage.setRecords(questionPage.getRecords().stream().map(QuestionVO::objToVo).collect(Collectors.toList()));
        return ResultUtils.success(questionVOPage);
    }


    // endregion
}
