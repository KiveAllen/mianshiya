package com.allen.mianshiya.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除题库题目关系
 */
@Data
public class QuestionBankQuestionDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 题库 id
     */
    private Long questionBankId;
    /**
     * 题目 id
     */
    private Long questionId;
}
