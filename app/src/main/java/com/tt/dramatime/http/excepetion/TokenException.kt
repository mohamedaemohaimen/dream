package com.tt.dramatime.http.excepetion

import com.hjq.http.exception.HttpException

/**
 * <pre>
 *   @author : wiggins
 *   Date:  2023/1/4
 *   desc : Token 失效异常
 * </pre>
 */
class TokenException : HttpException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}