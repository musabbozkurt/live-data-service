package com.mb.livedataservice.service;

public interface ShelfService {

    void saveOrUpdate(Long productId, boolean active);

    void insertOrUpdateOnConflictAsAtomic(Long productId, boolean active);
}
