package com.allen.mianshiya.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量创建题库题目请求
 */
@Data
public class QuestionBankQuestionBatchAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 题库id
     */
    private Long questionBankId;
    /**
     * 题目id
     */
    private List<Long> questionIdList;
}