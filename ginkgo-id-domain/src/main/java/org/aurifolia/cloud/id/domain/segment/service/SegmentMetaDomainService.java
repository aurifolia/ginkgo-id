package org.aurifolia.cloud.id.domain.segment.service;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;

/**
 * 号段元数据领域服务
 * <p>
 * 核心业务规则：分配号段
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
public class SegmentMetaDomainService {
    
    /**
     * 默认号段步长
     */
    private static final Long DEFAULT_STEP = 1000L;
    
    private final SegmentMetaRepository repository;
    
    public SegmentMetaDomainService(SegmentMetaRepository repository) {
        this.repository = repository;
    }

}
