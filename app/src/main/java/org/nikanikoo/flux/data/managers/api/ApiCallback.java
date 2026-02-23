package org.nikanikoo.flux.data.managers.api;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
