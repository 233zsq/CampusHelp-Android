package com.campus.help.core.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel 基类：统一 loading / toast 通用 UI 状态。
 */
public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toast = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getToast() {
        return toast;
    }

    protected void setLoading(boolean loading) {
        this.loading.setValue(loading);
    }

    protected void toast(String message) {
        this.toast.setValue(message);
    }
}
