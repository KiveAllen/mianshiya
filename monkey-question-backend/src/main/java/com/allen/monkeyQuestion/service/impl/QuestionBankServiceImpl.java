package com.allen.monkeyQuestion.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.allen.monkeyQuestion.common.ErrorCode;
import com.allen.monkeyQuestion.constant.CommonConstant;
import com.allen.monkeyQuestion.constant.QuestionConstant;
import com.allen.monkeyQuestion.exception.BusinessException;
import com.allen.monkeyQuestion.exception.ThrowUtils;
import com.allen.monkeyQuestion.mapper.QuestionBankMapper;
import com.allen.monkeyQuestion.model.dto.question.QuestionQueryRequest;
import com.allen.monkeyQuestion.model.dto.questionBank.QuestionBankQueryRequest;
import com.allen.monkeyQuestion.model.entity.QuestionBank;
import com.allen.monkeyQuestion.model.vo.QuestionBankVO;
import com.allen.monkeyQuestion.model.vo.UserVO;
import com.allen.monkeyQuestion.service.QuestionBankQuestionService;
import com.allen.monkeyQuestion.service.QuestionBankService;
import com.allen.monkeyQuestion.service.QuestionService;
import com.allen.monkeyQuestion.service.UserService;
import com.allen.monkeyQuestion.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题库服务实现
 */
@Service
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    /**
     * 添加题库
     *
     * @param questionBank 添加的题库
     * @return 添加的题库id
     */
    @Override
    public Long addQuestionBank(QuestionBank questionBank) {
        // 数据校验
        this.validQuestionBank(questionBank, true);

        if (StringUtils.isBlank(questionBank.getPicture())) {
            questionBank.setPicture(QuestionConstant.DEFAULT_PICTURE);
        }

        // 写入数据库
        boolean result = this.save(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 返回新写入的数据 id
        return questionBank.getId();
    }

    /**
     * 删除题库
     *
     * @param id 题库id
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Boolean deleteQuestionBank(long id) {
        // 判断是否存在
        QuestionBank oldQuestionBank = this.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR, "此题库不存在");

        // 操作数据库
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 删除题库题目联系
        Boolean deleteQuestionBankQuestion =
                questionBankQuestionService.deleteQuestionBankQuestion(id, null, false);
        ThrowUtils.throwIf(!deleteQuestionBankQuestion, ErrorCode.OPERATION_ERROR);

        return true;
    }

    /**
     * 更新题库
     *
     * @param questionBank 题库
     * @return 是否修改成功
     */
    @Override
    public Boolean updateQuestionBank(QuestionBank questionBank) {
        // 数据校验
        this.validQuestionBank(questionBank, false);
        // 判断是否存在
        long id = questionBank.getId();
        QuestionBank oldQuestionBank = this.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);

        if (questionBank.getPicture() != null && StringUtils.isBlank(questionBank.getPicture())) {
            questionBank.setPicture(QuestionConstant.DEFAULT_PICTURE);
        }

        // 操作数据库
        boolean result = this.updateById(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 分页查询题库
     *
     * @param questionBankQueryRequest 查询条件
     * @return 分页结果
     */
    @Override
    public Page<QuestionBank> listQuestionBankByPage(QuestionBankQueryRequest questionBankQueryRequest) {
        return this.page(new Page<>(questionBankQueryRequest.getCurrent(),
                        questionBankQueryRequest.getPageSize()),
                this.getQueryWrapper(questionBankQueryRequest));
    }

    /**
     * 获取题库
     *
     * @param id 题库id
     * @return 题库类
     */
    @Override
    public QuestionBank getQuestionBank(Long id) {
        // 查询数据库
        QuestionBank questionBank = this.getById(id);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取类
        return questionBank;
    }

    /**
     * 获取题库封装
     *
     * @param id 题库id
     * @return 封装类
     */
    @Override
    public QuestionBankVO getQuestionBankVO(Long id, Boolean needQueryQuestionList, int current, int pageSize) {
        QuestionBank questionBank = this.getQuestionBank(id);
        // 获取封装类
        QuestionBankVO questionBankVO = QuestionBankVO.objToVo(questionBank);

        UserVO userVO = userService.getUserVOById(questionBank.getUserId());
        if (userVO != null) {
            questionBankVO.setUser(userVO);
        }

        if (needQueryQuestionList) {
            QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
            questionQueryRequest.setQuestionBankId(id);
            questionQueryRequest.setCurrent(current);
            questionQueryRequest.setPageSize(pageSize);
            questionBankVO.setQuestionPage(questionService.getQuestionVOPage(questionQueryRequest));
        }

        return questionBankVO;
    }

    /**
     * 分页获取题库封装
     *
     * @param questionBankQueryRequest 查询条件
     * @return 封装分页类结果
     */
    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(QuestionBankQueryRequest questionBankQueryRequest) {
        Page<QuestionBank> questionBankPage = this.listQuestionBankByPage(questionBankQueryRequest);

        // 创建封装分页类
        List<QuestionBank> questionBankList = questionBankPage.getRecords();
        Page<QuestionBankVO> questionBankVOPage = new Page<>(
                questionBankPage.getCurrent(),
                questionBankPage.getSize(),
                questionBankPage.getTotal());
        if (CollUtil.isEmpty(questionBankList)) {
            return questionBankVOPage;
        }

        List<Long> userIds = questionBankList.stream().map(QuestionBank::getUserId).collect(Collectors.toList());
        Map<Long, UserVO> userVOMap = userService.getUserVOMapByIds(userIds);

        // 对象列表 => 封装对象列表
        List<QuestionBankVO> questionBankVOList =
                questionBankList.stream().map(
                        questionBank -> {
                            QuestionBankVO questionBankVO = QuestionBankVO.objToVo(questionBank);
                            UserVO userVO = userVOMap.get(questionBank.getUserId());
                            if (userVO != null) {
                                questionBankVO.setUser(userVO);
                            }
                            return questionBankVO;
                        }
                ).collect(Collectors.toList());
        questionBankVOPage.setRecords(questionBankVOList);
        return questionBankVOPage;
    }


    /**
     * 校验数据
     *
     * @param questionBank 题库
     * @param add          对创建的数据进行校验
     */
    private void validQuestionBank(QuestionBank questionBank, boolean add) {
        String title = questionBank.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
            // 不能创建相同标题的题库
            long count = this.count(Wrappers.lambdaQuery(QuestionBank.class).eq(QuestionBank::getTitle, title));
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "已存在相同标题的题库");
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取QueryWrapper
     *
     * @param questionBankQueryRequest 查询条件
     * @return QueryWrapper
     */
    private QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQueryRequest.getId();
        Long notId = questionBankQueryRequest.getNotId();
        String title = questionBankQueryRequest.getTitle();
        String description = questionBankQueryRequest.getDescription();
        String searchText = questionBankQueryRequest.getSearchText();
        String sortField = SqlUtils.toUnderlineCase(questionBankQueryRequest.getSortField());
        String sortOrder = questionBankQueryRequest.getSortOrder();
        Long userId = questionBankQueryRequest.getUserId();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

}
