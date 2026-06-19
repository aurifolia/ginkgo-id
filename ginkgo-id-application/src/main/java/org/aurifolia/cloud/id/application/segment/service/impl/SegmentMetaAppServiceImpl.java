package org.aurifolia.cloud.id.application.segment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.application.segment.command.AllocateSegmentCommand;
import org.aurifolia.cloud.id.application.segment.command.SegmentMetaRegisterCommand;
import org.aurifolia.cloud.id.application.segment.converter.SegmentMetaConverter;
import org.aurifolia.cloud.id.application.segment.dto.SegmentMetaDTO;
import org.aurifolia.cloud.id.application.segment.service.SegmentMetaAppService;
import org.aurifolia.cloud.id.domain.exception.DomainException;
import org.aurifolia.cloud.id.domain.exception.IdDomainErrorCode;
import org.aurifolia.cloud.id.domain.segment.entity.SegmentMeta;
import org.aurifolia.cloud.id.domain.segment.repository.SegmentMetaRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 号段元数据应用服务实现
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@Service
public class SegmentMetaAppServiceImpl implements SegmentMetaAppService {

    private final SegmentMetaRepository repository;

    public SegmentMetaAppServiceImpl(SegmentMetaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SegmentMetaDTO allocateSegment(AllocateSegmentCommand command) {
        SegmentMeta meta = repository.findByBizTagForUpdate(command.getBizTag())
                .orElseThrow(() -> new DomainException(IdDomainErrorCode.BIZ_TAG_NOT_FOUND, command.getBizTag()));

        meta.allocateNextSegment();
        repository.update(meta);

        return SegmentMetaConverter.INSTANCE.toDTO(meta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerBizTag(SegmentMetaRegisterCommand command) {
        try {
            SegmentMeta meta = SegmentMeta.create(command.getBizTag());
            repository.save(meta);
        } catch (DuplicateKeyException e) {
            throw new DomainException(IdDomainErrorCode.BIZ_TAG_ALREADY_EXISTS, command.getBizTag());
        }
    }
}
