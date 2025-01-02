package com.lslnlk.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;	// 约定好的，业务状态响应码。

    private String msg; // 业务状态提示信息。

    private T data; //接口处理完成后，返回的数据结果。

    public Result(ResultCode code, T data) {
        this.code = code.getCode();
        this.msg = code.getMsg();
        this.data = data;
    }
    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultCode.SUCCESS,data);
    }
    public static <T> Result<T> error(T data) {
        return new Result<T>(ResultCode.ERROR, data);
    }
    public static <T> Result<T> error(ResultCode code,T data) {
        return new Result<T>(code, data);
    }
}


