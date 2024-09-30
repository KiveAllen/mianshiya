package com.allen.mianshiya.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 *
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField 排序字段
     * @return 合法返回 true
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    public R un(String sql) {
        return null;
    }
}
