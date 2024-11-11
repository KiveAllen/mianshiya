package com.allen.mianshiya.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 群体删除题目请求
 */
@Data
public class QuestionBatchDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id列表
     */
    private List<Long> idList;
}