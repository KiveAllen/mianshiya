package com.allen.monkeyQuestion.exception;

import com.allen.monkeyQuestion.common.ErrorCode;

/**
 * 抛异常工具类
 *
 */
public class ThrowUtils {

    /**
     * 直接抛异常
     * @param errorCode 错误码
     *
     */
    public static void throwException(ErrorCode errorCode) {
        throw new BusinessException(errorCode);
    }

    /**
     * 直接抛异常
     * @param errorCode 错误码
     * @param message 错误信息
     *
     */
    public static void throwException(ErrorCode errorCode, String message) {
        throw new BusinessException(errorCode, message);
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 是否成立
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 是否成立
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 是否成立
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
