package org.example.resumeai.common;

/**
 * 统一API响应结果封装类
 */
public class Result {
    private String code;
    private Object data;
    private String msg;

    public Result() {}

    public Result(String code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static Result success() {
        return new Result("200", null, "请求成功");
    }

    public static Result success(Object data) {
        return new Result("200", data, "请求成功");
    }

    public static Result success(Object data, String msg) {
        return new Result("200", data, msg);
    }

    public static Result error(String msg) {
        return new Result("500", null, msg);
    }

    public static Result error(String code, String msg) {
        return new Result(code, null, msg);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}
