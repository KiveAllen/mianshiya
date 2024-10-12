package com.allen.mianshiya.controller;

import cn.hutool.json.JSONUtil;
import com.allen.mianshiya.annotation.AuthCheck;
import com.allen.mianshiya.common.BaseResponse;
import com.allen.mianshiya.common.DeleteRequest;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.common.ResultUtils;
import com.allen.mianshiya.constant.UserConstant;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.model.dto.question.QuestionAddRequest;
import com.allen.mianshiya.model.dto.question.QuestionQueryRequest;
import com.allen.mianshiya.model.dto.question.QuestionUpdateRequest;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.User;
import com.allen.mianshiya.model.vo.QuestionVO;
import com.allen.mianshiya.service.QuestionService;
import com.allen.mianshiya.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    // region 增删改查 管理员

    /**
     * 创建题目（仅管理员可用）
     *
     * @param questionAddRequest 问题创建参数
     * @param request            http请求
     * @return 新写入的数据 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        // 检验请求是否存在
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);

        // 拷贝属性
        Question question = new Question();
        question.setTags(JSONUtil.toJsonStr(questionAddRequest.getTags()));
        BeanUtils.copyProperties(questionAddRequest, question);

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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        // 检验请求是否存在
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionService.deleteQuestion(deleteRequest.getId()));
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest 更新条件
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(id));
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

    // endregion
}
