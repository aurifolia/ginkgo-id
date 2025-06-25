package org.aurifolia.cloud.id.metaserver.service;

import org.aurifolia.cloud.id.metaserver.common.dto.SnowflakeNodeDTO;
import org.aurifolia.cloud.id.metaserver.common.dto.SegmentMetaDTO;

public interface MetaService {
    SnowflakeNodeDTO allocateMachineId(String bizTag);
    SegmentMetaDTO nextSegment(String bizTag, Long step);
} 