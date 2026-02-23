package org.nikanikoo.flux.ui.custom;

import org.nikanikoo.flux.utils.Logger;

/**
 * Универсальный помощник для управления пагинацией
 * Решает проблему неправильного offset при фильтрации дубликатов
 */
public class PaginationHelper {
    private static final String TAG = "PaginationHelper";
    
    private int currentOffset = 0;
    private int itemsPerPage;
    private boolean hasMoreData = true;
    private boolean isLoading = false;
    private int totalItemsLoaded = 0;
    
    public PaginationHelper(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        Logger.d(TAG, "PaginationHelper created with itemsPerPage: " + itemsPerPage);
    }
    
    /**
     * Получить текущий offset для следующего запроса
     */
    public int getCurrentOffset() {
        return currentOffset;
    }
    
    /**
     * Уведомить о загрузке новых данных
     * @param itemsReceived количество элементов, полученных от API
     */
    public void onDataLoaded(int itemsReceived) {
        Logger.d(TAG, "onDataLoaded called: itemsReceived=" + itemsReceived + ", currentOffset=" + currentOffset + ", isLoading=" + isLoading + ", hasMoreData=" + hasMoreData);
        
        // Увеличиваем offset на количество реально полученных элементов
        currentOffset += itemsReceived;
        totalItemsLoaded += itemsReceived;
        isLoading = false;
        
        // Если получили 0 элементов - больше данных нет
        // НЕ проверяем itemsReceived < itemsPerPage, так как API может вернуть меньше по разным причинам
        if (itemsReceived == 0) {
            hasMoreData = false;
            Logger.d(TAG, "No more data available (received 0 items)");
        }
        
        Logger.d(TAG, "After onDataLoaded: offset=" + currentOffset + ", totalLoaded=" + totalItemsLoaded + ", isLoading=" + isLoading + ", hasMoreData=" + hasMoreData + ", canLoadMore=" + canLoadMore());
    }
    
    /**
     * Сбросить состояние пагинации (при обновлении списка)
     */
    public void reset() {
        Logger.d(TAG, "Resetting pagination state");
        currentOffset = 0;
        hasMoreData = true;
        isLoading = false;
        totalItemsLoaded = 0;
    }
    
    /**
     * Начать загрузку
     */
    public void startLoading() {
        isLoading = true;
        Logger.d(TAG, "startLoading called: offset=" + currentOffset + ", isLoading=" + isLoading + ", hasMoreData=" + hasMoreData + ", canLoadMore=" + canLoadMore());
    }
    
    /**
     * Остановить загрузку (при ошибке)
     */
    public void stopLoading() {
        isLoading = false;
        Logger.d(TAG, "Loading stopped");
    }
    
    /**
     * Установить, что данных больше нет
     */
    public void setNoMoreData() {
        hasMoreData = false;
        isLoading = false;
        Logger.d(TAG, "No more data flag set");
    }
    
    /**
     * Проверить, есть ли еще данные для загрузки
     */
    public boolean hasMoreData() {
        return hasMoreData;
    }
    
    /**
     * Проверить, идет ли загрузка
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Получить общее количество загруженных элементов
     */
    public int getTotalItemsLoaded() {
        return totalItemsLoaded;
    }
    
    /**
     * Можно ли загружать следующую страницу
     */
    public boolean canLoadMore() {
        return hasMoreData && !isLoading;
    }
}
