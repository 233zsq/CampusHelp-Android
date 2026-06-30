package com.campus.help.core.base;

/**
 * 通用回调（避免与 retrofit2.Callback 混淆，按需 import）。
 */
public interface Callback<T> {
    void onResult(T result);
}
