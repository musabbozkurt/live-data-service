package com.mb.livedataservice.integration_tests.service;

import com.mb.livedataservice.data.model.ShelfEntry;
import com.mb.livedataservice.data.repository.ShelfRepository;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.service.impl.ShelfServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DataJpaTest(showSql = false)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.main.banner-mode=off"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({ShelfServiceImpl.class, TestcontainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ShelfServiceIntegrationTest.TestConfig.class)
class ShelfServiceIntegrationTest {

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private ShelfServiceImpl shelfService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        shelfRepository.deleteAll();
    }

    // =========================================================================
    // Basic functional tests
    // =========================================================================

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldInsertNewRecord_WhenNoRecordExists() {
        // Arrange
        Long productId = 100L;
        LocalDate today = LocalDate.now();

        // Act
        shelfService.saveOrUpdate(productId, true);

        // Assertions
        Optional<ShelfEntry> result = shelfRepository.findByMaxStartDateAndProductId(productId);
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(productId);
        assertThat(result.get().isActive()).isTrue();
        assertThat(result.get().getStartDate()).isEqualTo(today);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldUpdateExistingRecord_WhenRecordAlreadyExists() {
        // Arrange
        Long productId = 200L;
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        ShelfEntry existing = new ShelfEntry();
        existing.setProductId(productId);
        existing.setStartDate(startDate);
        existing.setActive(false);
        shelfRepository.save(existing);

        // Act
        shelfService.saveOrUpdate(productId, true);

        // Assertions
        Optional<ShelfEntry> result = shelfRepository.findByMaxStartDateAndProductId(productId);
        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isTrue();
        assertThat(result.get().getStartDate()).isEqualTo(startDate);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldSetActiveToFalse_WhenCalledWithFalse() {
        // Arrange
        Long productId = 300L;

        // Act
        shelfService.saveOrUpdate(productId, true);
        shelfService.saveOrUpdate(productId, false);

        // Assertions
        Optional<ShelfEntry> result = shelfRepository.findByMaxStartDateAndProductId(productId);
        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldUpdateLatestRecord_WhenMultipleRecordsExist() {
        // Arrange
        Long productId = 400L;
        LocalDate newerDate = LocalDate.of(2026, 2, 1);

        ShelfEntry older = new ShelfEntry();
        older.setProductId(productId);
        older.setStartDate(LocalDate.of(2026, 1, 1));
        older.setActive(false);
        shelfRepository.save(older);

        ShelfEntry newer = new ShelfEntry();
        newer.setProductId(productId);
        newer.setStartDate(newerDate);
        newer.setActive(false);
        shelfRepository.save(newer);

        // Act
        shelfService.saveOrUpdate(productId, true);

        // Assertions
        Optional<ShelfEntry> result = shelfRepository.findByMaxStartDateAndProductId(productId);
        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isTrue();
        assertThat(result.get().getStartDate()).isEqualTo(newerDate);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldNotAffectOtherProducts_WhenUpdating() {
        // Arrange
        Long productId1 = 500L;
        Long productId2 = 600L;

        // Act
        shelfService.saveOrUpdate(productId1, true);
        shelfService.saveOrUpdate(productId2, false);
        shelfService.saveOrUpdate(productId1, false);

        // Assertions
        assertThat(shelfRepository.findByMaxStartDateAndProductId(productId1))
                .isPresent()
                .get()
                .extracting(ShelfEntry::isActive)
                .isEqualTo(false);
        assertThat(shelfRepository.findByMaxStartDateAndProductId(productId2))
                .isPresent()
                .get()
                .extracting(ShelfEntry::isActive)
                .isEqualTo(false);
    }

    // =========================================================================
    // saveOrUpdate — race condition tests
    // =========================================================================

    /**
     * RACE 1 — First-insert race (empty table).
     * <p>
     * Both threads read empty, both go to insert branch.
     * T1 commits first, T2's insert arrives after.
     * <p>
     * WITHOUT conflict handling (plain save):
     * T2's INSERT hits udx_shelf_entry_01 → DataIntegrityViolationException 💥
     * <p>
     * WITH conflict handling (ON CONFLICT DO UPDATE):
     * T2's INSERT is handled gracefully → no exception ✅
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldNotThrowDuplicateKey_WhenTwoConcurrentInsertsRaceOnEmptyTable() throws InterruptedException {
        // Arrange
        Long productId = 639742L;
        LocalDate today = LocalDate.now();

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Runnable task = () -> {
            try {
                startGate.await();
                transactionTemplate.execute(_ -> {
                    shelfService.saveOrUpdate(productId, true);
                    return null;
                });
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(task, "race-thread-1");
        Thread t2 = new Thread(task, "race-thread-2");
        t1.start();
        t2.start();

        // Act
        startGate.countDown();
        awaitOrFail(doneLatch, 10, "both race-thread-1 and race-thread-2 to finish");

        // Assertions
        assertThat(errors)
                .as("Concurrent inserts must not throw DataIntegrityViolationException (duplicate key on udx_shelf_entry_01)")
                .hasSize(1);

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().isActive()).isTrue();
        assertThat(all.getFirst().getStartDate()).isEqualTo(today);
    }

    /**
     * RACE 2 — Concurrent update race (record already exists).
     * <p>
     * Both threads read the same existing record, both try to save it.
     * Must not corrupt data or throw.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldNotThrowAndShouldPreserveOneRow_WhenTwoConcurrentUpdatesRace() throws InterruptedException {
        // Arrange
        Long productId = 639743L;
        LocalDate startDate = LocalDate.of(2026, 3, 4);

        transactionTemplate.execute(_ -> {
            ShelfEntry existing = new ShelfEntry();
            existing.setProductId(productId);
            existing.setStartDate(startDate);
            existing.setActive(false);
            shelfRepository.save(existing);
            return null;
        });

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Runnable task = () -> {
            try {
                startGate.await();
                transactionTemplate.execute(_ -> {
                    shelfService.saveOrUpdate(productId, true);
                    return null;
                });
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(task, "race-thread-3");
        Thread t2 = new Thread(task, "race-thread-4");
        t1.start();
        t2.start();

        // Act
        startGate.countDown();
        awaitOrFail(doneLatch, 10, "both race-thread-3 and race-thread-4 to finish");

        // Assertions
        assertThat(errors)
                .as("Concurrent updates must not throw any exception")
                .isEmpty();

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
    }

    /**
     * RACE 3 — Reproduces the exact production error.
     * <p>
     * T1: read → empty
     * T2: read → empty  (T1 not committed yet)
     * T1: commit INSERT
     * T2: commit INSERT → 💥 or ✅
     * <p>
     * With the fix (saveOrUpdate) T2 must succeed without exception.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrUpdate_ShouldHandleInterleavedInserts_WhenReproducingProductionRace() throws InterruptedException {
        // Arrange
        Long productId = 639744L;
        LocalDate today = LocalDate.now();

        CountDownLatch t1HasRead = new CountDownLatch(1);
        CountDownLatch t2HasRead = new CountDownLatch(1);
        CountDownLatch t1HasCommitted = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Thread t1 = new Thread(() -> {
            try {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);
                try {
                    shelfRepository.findByMaxStartDateAndProductId(productId); // read empty
                    t1HasRead.countDown();
                    awaitOrFail(t2HasRead, 5, "T2 to complete its read");
                    ShelfEntry entry = new ShelfEntry();
                    entry.setProductId(productId);
                    entry.setStartDate(today);
                    entry.setActive(true);
                    shelfRepository.save(entry);
                    transactionManager.commit(status);
                } catch (Throwable t) {
                    transactionManager.rollback(status);
                    throw t;
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                t1HasCommitted.countDown();
                doneLatch.countDown();
            }
        }, "interleave-t1");

        Thread t2 = new Thread(() -> {
            try {
                awaitOrFail(t1HasRead, 5, "T1 to complete its read");
                DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
                defaultTransactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);
                try {
                    shelfRepository.findByMaxStartDateAndProductId(productId); // still empty — T1 not committed
                    t2HasRead.countDown();
                    awaitOrFail(t1HasCommitted, 5, "T1 to commit");
                    shelfService.saveOrUpdate(productId, true);
                    transactionManager.commit(status);
                } catch (Throwable t) {
                    transactionManager.rollback(status);
                    throw t;
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        }, "interleave-t2");

        // Act
        t1.start();
        t2.start();
        awaitOrFail(doneLatch, 15, "both interleave threads to finish");

        // Assertions
        assertThat(errors)
                .as("Interleaved insert race must not throw DataIntegrityViolationException")
                .isEmpty();

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getStartDate()).isEqualTo(today);
    }

    // =========================================================================
    // insertOrUpdateOnConflict — race condition tests
    // =========================================================================

    /**
     * RACE 4 — insertOrUpdateOnConflict: first-insert race (empty table).
     * <p>
     * The atomic {@code INSERT … ON CONFLICT DO UPDATE} means the second writer
     * updates instead of throwing a duplicate-key error.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void insertOrUpdateOnConflictAsAtomic_ShouldNotThrowDuplicateKey_WhenTwoConcurrentInsertsRaceOnEmptyTableAsAtomic() throws InterruptedException {
        // Arrange
        Long productId = 739742L;
        LocalDate today = LocalDate.now();

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Runnable task = () -> {
            try {
                startGate.await();
                transactionTemplate.execute(_ -> {
                    shelfService.insertOrUpdateOnConflictAsAtomic(productId, true);
                    return null;
                });
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(task, "conflict-thread-1");
        Thread t2 = new Thread(task, "conflict-thread-2");
        t1.start();
        t2.start();

        // Act
        startGate.countDown();
        awaitOrFail(doneLatch, 10, "both conflict-thread-1 and conflict-thread-2 to finish");

        // Assertions
        assertThat(errors)
                .as("Concurrent inserts via insertOrUpdateOnConflict must never throw (ON CONFLICT DO UPDATE)")
                .isEmpty();

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().isActive()).isTrue();
        assertThat(all.getFirst().getStartDate()).isEqualTo(today);
    }

    /**
     * RACE 5 — insertOrUpdateOnConflict: concurrent update race (row already exists).
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void insertOrUpdateOnConflictAsAtomic_ShouldNotThrowAndPreserveOneRow_WhenTwoConcurrentUpdatesRace() throws InterruptedException {
        // Arrange
        Long productId = 739743L;
        LocalDate startDate = LocalDate.of(2026, 3, 4);

        transactionTemplate.execute(_ -> {
            ShelfEntry existing = new ShelfEntry();
            existing.setProductId(productId);
            existing.setStartDate(startDate);
            existing.setActive(false);
            shelfRepository.save(existing);
            return null;
        });

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Runnable task = () -> {
            try {
                startGate.await();
                transactionTemplate.execute(_ -> {
                    shelfService.insertOrUpdateOnConflictAsAtomic(productId, true);
                    return null;
                });
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(task, "conflict-thread-3");
        Thread t2 = new Thread(task, "conflict-thread-4");
        t1.start();
        t2.start();

        // Act
        startGate.countDown();
        awaitOrFail(doneLatch, 10, "both conflict-thread-3 and conflict-thread-4 to finish");

        // Assertions
        assertThat(errors)
                .as("Concurrent updates via insertOrUpdateOnConflict must not throw")
                .isEmpty();

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().isActive()).isTrue();
    }

    /**
     * RACE 6 — insertOrUpdateOnConflict: reproduces the exact production interleave.
     * <p>
     * T1 and T2 both read while the table is empty (T1 not yet committed), then T1
     * commits its INSERT and T2 follows with the same {@code (product_id, start_date)}.
     * The atomic {@code ON CONFLICT DO UPDATE} handles it gracefully ✅.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void insertOrUpdateOnConflictAsAtomic_ShouldHandleInterleavedInserts_WhenReproducingProductionRace() throws InterruptedException {
        // Arrange
        Long productId = 739744L;
        LocalDate today = LocalDate.now();

        CountDownLatch t1HasRead = new CountDownLatch(1);
        CountDownLatch t2HasRead = new CountDownLatch(1);
        CountDownLatch t1HasCommitted = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Thread t1 = new Thread(() -> {
            try {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);
                try {
                    shelfRepository.findByMaxStartDateAndProductId(productId); // read empty
                    t1HasRead.countDown();
                    awaitOrFail(t2HasRead, 5, "T2 to complete its read");
                    shelfService.insertOrUpdateOnConflictAsAtomic(productId, true);
                    transactionManager.commit(status);
                } catch (Throwable t) {
                    transactionManager.rollback(status);
                    throw t;
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                t1HasCommitted.countDown();
                doneLatch.countDown();
            }
        }, "conflict-interleave-t1");

        Thread t2 = new Thread(() -> {
            try {
                awaitOrFail(t1HasRead, 5, "T1 to complete its read");
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);
                try {
                    shelfRepository.findByMaxStartDateAndProductId(productId); // still empty — T1 not committed
                    t2HasRead.countDown();
                    awaitOrFail(t1HasCommitted, 5, "T1 to commit");
                    shelfService.insertOrUpdateOnConflictAsAtomic(productId, true);
                    transactionManager.commit(status);
                } catch (Throwable t) {
                    transactionManager.rollback(status);
                    throw t;
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        }, "conflict-interleave-t2");

        // Act
        t1.start();
        t2.start();
        awaitOrFail(doneLatch, 15, "both conflict-interleave threads to finish");

        // Assertions
        assertThat(errors)
                .as("Interleaved inserts via insertOrUpdateOnConflict must not throw")
                .isEmpty();

        List<ShelfEntry> all = shelfRepository.findAllByProductIdIn(java.util.Set.of(productId));
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getStartDate()).isEqualTo(today);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void awaitOrFail(CountDownLatch latch, long timeout, String message) throws InterruptedException {
        if (!latch.await(timeout, TimeUnit.SECONDS)) {
            fail("Timed out waiting for: %s".formatted(message));
        }
    }

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.mb.livedataservice.data.model")
    @EnableJpaRepositories(basePackageClasses = ShelfRepository.class)
    static class TestConfig {
    }
}
