package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.ShelfEntry;
import com.mb.livedataservice.data.repository.ShelfRepository;
import com.mb.livedataservice.service.ShelfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShelfServiceImpl implements ShelfService {

    private final ShelfRepository shelfRepository;

    @Override
    @Transactional
    public void saveOrUpdate(Long productId, boolean active) {
        LocalDate today = LocalDate.now();
        log.debug("saveOrUpdate: productId: {}, active: {}, today: {}", productId, active, today);

        shelfRepository.findByMaxStartDateAndProductId(productId)
                .ifPresentOrElse(
                        existing -> {
                            existing.setActive(active);
                            shelfRepository.save(existing);
                            log.debug("Updated existing ShelfEntry id: {} for productId: {}", existing.getId(), productId);
                        },
                        () -> {
                            ShelfEntry entry = new ShelfEntry();
                            entry.setProductId(productId);
                            entry.setStartDate(today);
                            entry.setActive(active);
                            shelfRepository.saveAndFlush(entry);
                            log.debug("Inserted new ShelfEntry for productId: {}, startDate: {}", productId, today);
                        });
    }

    @Override
    @Transactional
    public void insertOrUpdateOnConflictAsAtomic(Long productId, boolean active) {
        LocalDate startDate = shelfRepository.findByMaxStartDateAndProductId(productId)
                .map(ShelfEntry::getStartDate)
                .orElseGet(LocalDate::now);
        shelfRepository.insertOrUpdateOnConflict(productId, startDate, active);
    }
}
