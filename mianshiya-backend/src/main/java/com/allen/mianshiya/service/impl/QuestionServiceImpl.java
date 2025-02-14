package com.allen.mianshiya.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.allen.mianshiya.common.ErrorCode;
import com.allen.mianshiya.constant.CommonConstant;
import com.allen.mianshiya.exception.BusinessException;
import com.allen.mianshiya.exception.ThrowUtils;
import com.allen.mianshiya.mapper.QuestionMapper;
import com.allen.mianshiya.model.dto.question.QuestionEsDTO;
import com.allen.mianshiya.model.dto.question.QuestionQueryRequest;
import com.allen.mianshiya.model.entity.Question;
import com.allen.mianshiya.model.entity.QuestionBankQuestion;
import com.allen.mianshiya.model.vo.QuestionVO;
import com.allen.mianshiya.model.vo.UserVO;
import com.allen.mianshiya.service.QuestionBankQuestionService;
import com.allen.mianshiya.service.QuestionService;
import com.allen.mianshiya.service.UserService;
import com.allen.mianshiya.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库题目服务实现
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 校验数据
     *
     * @param question 要校验的题目对象
     * @param add      对创建的数据进行校验
     */
    private void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        String title = question.getTitle();
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);

            // 重复题目
            long count = this.count(new LambdaQueryWrapper<Question>().eq(Question::getTitle, title));
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "题目已存在");
        }
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest 请求
     * @return 查询条件
     */
    private QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = SqlUtils.toUnderlineCase(questionQueryRequest.getSortField());
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目封装
     *
     * @param id 问题id
     * @return 问题VO
     */
    @Override
    public QuestionVO getQuestionVO(Long id) {
        // 查询数据库
        Question question = this.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);

        QuestionVO questionVO = QuestionVO.objToVo(question);
        UserVO userVO = userService.getUserVOById(question.getUserId());
        if (userVO != null) {
            questionVO.setUser(userVO);
        }

        // 对象转封装类
        return questionVO;
    }

    /**
     * 分页查询题目
     *
     * @param questionQueryRequest 查询条件
     * @return 分页结果
     */
    @Override
    public Page<Question> getQuestionPage(QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);

        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (questionBankId != null) {
            List<Question> questionList = questionBankQuestionService.getQuestionListByQuestionBankId(questionBankId);
            if (CollUtil.isNotEmpty(questionList)) {
                // 取出题目 id 集合
                Set<Long> questionIdSet = questionList.stream().map(Question::getId).collect(Collectors.toSet());
                // 复用原有题目表的查询条件
                queryWrapper.in("id", questionIdSet);
            } else {
                return new Page<>(current, size);
            }
        }

        // 查询数据库
        return this.page(new Page<>(current, size), queryWrapper);
    }

    /**
     * 分页获取题库题目封装
     *
     * @param questionQueryRequest 查询条件
     * @return VO分页
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(QuestionQueryRequest questionQueryRequest) {

        // 限制爬虫
        ThrowUtils.throwIf(questionQueryRequest.getPageSize() > 200, ErrorCode.PARAMS_ERROR);

        // 查询数据库
        Page<Question> questionPage = this.getQuestionPage(questionQueryRequest);

        // 转为 VO 的 page
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(
                questionPage.getCurrent(),
                questionPage.getSize(),
                questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }

        // 批量查询用户信息
        List<Long> userIds = questionList.stream().map(Question::getUserId).collect(Collectors.toList());
        Map<Long, UserVO> userVOByIds = userService.getUserVOMapByIds(userIds);

        // 对象列表 => 封装对象列表（在内存中进行分组）
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            UserVO userVO = userVOByIds.get(question.getUserId());
            if (userVO != null) {
                questionVO.setUser(userVO);
            }
            return questionVO;
        }).collect(Collectors.toList());

        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    /**
     * 删除题目
     *
     * @param id id
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Boolean deleteQuestion(Long id) {
        // 检验问题对象是否存在数据库内
        Question oldQuestion = this.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据库
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 删除题库题目联系
        Boolean deleteQuestionBankQuestion =
                questionBankQuestionService.deleteQuestionBankQuestion(null, id, false);
        ThrowUtils.throwIf(!deleteQuestionBankQuestion, ErrorCode.OPERATION_ERROR);

        return true;
    }

    /**
     * 更新题目
     *
     * @param question 问题类
     * @return 更新结果
     */
    @Override
    public Boolean updateQuestion(Question question) {
        // 数据校验
        this.validQuestion(question, false);
        // 判断是否存在
        Question oldQuestion = this.getById(question.getId());
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据库
        boolean result = this.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 添加题目
     *
     * @param question 问题类
     * @return 添加结果
     */
    @Override
    public Long addQuestion(Question question) {
        // 数据校验
        this.validQuestion(question, true);

        boolean result = this.save(question);

        // 数据库操作
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return question.getId();
    }

    /**
     * 从 ES 查询题目
     *
     * @param questionQueryRequest 查询参数
     * @return 查询
     */
    @Override
    public Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest) {
        // 获取参数
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String searchText = questionQueryRequest.getSearchText();
        List<String> tags = questionQueryRequest.getTags();
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        Long userId = questionQueryRequest.getUserId();
        // 注意，ES 的起始页为 0
        int current = questionQueryRequest.getCurrent() - 1;
        int pageSize = questionQueryRequest.getPageSize();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 构造查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤 (term 类似于 mysql 的等于 =)
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        if (questionBankId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("questionBankId", questionBankId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            // title = '' or content = '' or answer = ''
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("answer", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of(current, pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSorts(sortBuilder)
                .build();
        SearchHits<QuestionEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, QuestionEsDTO.class);

        // 复用 MySQL / MyBatis Plus 的分页对象，封装返回结果
        Page<Question> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Question> resourceList = new ArrayList<>();
        if (searchHits.hasSearchHits()) {
            List<SearchHit<QuestionEsDTO>> searchHitList = searchHits.getSearchHits();
            for (SearchHit<QuestionEsDTO> questionEsDTOSearchHit : searchHitList) {
                resourceList.add(QuestionEsDTO.dtoToObj(questionEsDTOSearchHit.getContent()));
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    /**
     * 批量删除题目
     *
     * @param questionIdList 題目的id列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteQuestions(List<Long> questionIdList) {
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "要删除的题目列表不能为空");
        for (Long questionId : questionIdList) {
            boolean result = this.removeById(questionId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除题目失败");
            // 移除题目题库关系
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId);
            List<QuestionBankQuestion> list = questionBankQuestionService.list(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(list)) {
                result = questionBankQuestionService.remove(lambdaQueryWrapper);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除题目题库关联失败");
            }
        }
        return true;
    }
}
