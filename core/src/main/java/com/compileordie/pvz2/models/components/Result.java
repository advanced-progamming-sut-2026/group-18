package com.compileordie.pvz2.models.components;

public class Result<T> {
    public final boolean isSuccess;
    public final String errorMessage;
    public final T data;

    private Result(boolean isSuccess, String errorMessage, T data) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    // Standard success with a payload
    public static <T> Result<T> success(T data) {
        return new Result<>(true, null, data);
    }

    // Parameter-less success for Result<Void>
    public static Result<Void> success() {
        return new Result<>(true, null, null);
    }

    // Standard failure
    public static <T> Result<T> failure(String error) {
        return new Result<>(false, error, null);
    }
}
