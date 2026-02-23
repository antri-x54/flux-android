package org.nikanikoo.flux.data.managers;

import android.content.Context;

import org.nikanikoo.flux.data.managers.api.OpenVKApi;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseManager<T extends BaseManager<T>> {
    protected final OpenVKApi api;
    protected final Context context;

    private static final ConcurrentHashMap<Class<?>, BaseManager<?>> instances = new ConcurrentHashMap<>();

    protected BaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.api = OpenVKApi.getInstance(this.context);
    }

    protected static <T extends BaseManager<T>> T getInstance(Class<T> clazz, Context context) {
        BaseManager<?> manager = instances.get(clazz);
        if (manager == null) {
            synchronized (instances) {
                manager = instances.get(clazz);
                if (manager == null) {
                    try {
                        java.lang.reflect.Constructor<T> constructor = clazz.getDeclaredConstructor(Context.class);
                        constructor.setAccessible(true);
                        manager = constructor.newInstance(context.getApplicationContext());
                        instances.put(clazz, manager);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
                    }
                }
            }
        }
        return (T) manager;
    }

    protected OpenVKApi getApi() {
        return api;
    }

    protected Context getContext() {
        return context;
    }
}
