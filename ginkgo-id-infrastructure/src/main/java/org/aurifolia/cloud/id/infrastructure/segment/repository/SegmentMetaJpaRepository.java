package org.aurifolia.cloud.id.infrastructure.segment.repository;

import jakarta.persistence.LockModeType;
import org.aurifolia.cloud.id.infrastructure.segment.po.SegmentMetaPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * 号段元数据JPA仓储
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SegmentMetaJpaRepository extends JpaRepository<SegmentMetaPO, Long> {

    Optional<SegmentMetaPO> findByBizTag(String bizTag);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SegmentMetaPO> findWithLockByBizTag(String bizTag);
}
