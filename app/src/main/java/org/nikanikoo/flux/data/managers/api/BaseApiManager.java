package org.nikanikoo.flux.data.managers.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nikanikoo.flux.data.managers.BaseManager;
import org.nikanikoo.flux.utils.AsyncTaskHelper;
import org.nikanikoo.flux.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// базовый класс. todo использовать для всех менеджеров.

public abstract class BaseApiManager<T> extends BaseManager<BaseApiManager<T>> {
    
    protected final OpenVKApi api;
    protected final String tag;

    protected BaseApiManager(Context context, String tag) {
        super(context);
        this.api = OpenVKApi.getInstance(context);
        this.tag = tag;
    }

    protected void executeListApiCall(String methodName, Map<String, String> params,
                                       ItemParser<T> parser, ApiCallback<List<T>> callback) {
        api.callMethod(methodName, params, new OpenVKApi.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                AsyncTaskHelper.executeAsync(() -> parseListResponse(response, parser),
                    new AsyncTaskHelper.AsyncCallback<List<T>>() {
                        @Override
                        public void onSuccess(List<T> items) {
                            callback.onSuccess(items);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                Logger.e(tag, "API error in " + methodName + ": " + error);
                callback.onError(error);
            }
        });
    }

    protected void executeSingleApiCall(String methodName, Map<String, String> params,
                                         ItemParser<T> parser, ApiCallback<T> callback) {
        api.callMethod(methodName, params, new OpenVKApi.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                AsyncTaskHelper.executeAsync(() -> {
                    try {
                        T item = parser.parse(response);
                        return item;
                    } catch (Exception e) {
                        Logger.e(tag, "Error parsing response", e);
                        throw e;
                    }
                }, new AsyncTaskHelper.AsyncCallback<T>() {
                    @Override
                    public void onSuccess(T item) {
                        callback.onSuccess(item);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Logger.e(tag, "API error in " + methodName + ": " + error);
                callback.onError(error);
            }
        });
    }

    protected void executeActionApiCall(String methodName, Map<String, String> params,
                                         ApiCallback<Boolean> callback) {
        api.callMethod(methodName, params, new OpenVKApi.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    boolean success = !response.has("error");
                    callback.onSuccess(success);
                } catch (Exception e) {
                    Logger.e(tag, "Error processing action response", e);
                    callback.onError(e.getMessage());
                }
            }
            
            @Override
            public void onError(String error) {
                Logger.e(tag, "API error in " + methodName + ": " + error);
                callback.onError(error);
            }
        });
    }

    protected List<T> parseListResponse(JSONObject response, ItemParser<T> parser) {
        List<T> items = new ArrayList<>();
        
        try {
            if (response.has("response")) {
                Object responseObj = response.get("response");
                
                if (responseObj instanceof JSONArray) {
                    // Direct array response
                    JSONArray array = (JSONArray) responseObj;
                    for (int i = 0; i < array.length(); i++) {
                        T item = parser.parse(array.getJSONObject(i));
                        if (item != null) {
                            items.add(item);
                        }
                    }
                } else if (responseObj instanceof JSONObject) {
                    // Object with items array
                    JSONObject obj = (JSONObject) responseObj;
                    if (obj.has("items")) {
                        JSONArray array = obj.getJSONArray("items");
                        for (int i = 0; i < array.length(); i++) {
                            T item = parser.parse(array.getJSONObject(i));
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(tag, "Error parsing list response", e);
        }
        
        return items;
    }

    protected Map<String, String> createPaginationParams(int count, int offset) {
        Map<String, String> params = new HashMap<>();
        params.put("count", String.valueOf(count));
        params.put("offset", String.valueOf(offset));
        return params;
    }

    protected Map<String, String> createOwnerParams(int ownerId) {
        Map<String, String> params = new HashMap<>();
        params.put("owner_id", String.valueOf(ownerId));
        return params;
    }

    public interface ItemParser<T> {
        T parse(JSONObject json) throws Exception;
    }
}
