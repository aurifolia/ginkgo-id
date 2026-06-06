package org.aurifolia.cloud.id.sdk.fetcher;

/**
 * 号段获取器接口
 * <p>
 * 从ID服务获取下一个号段编号，由HTTP和RPC两种实现
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface SegmentFetcher {

    /**
     * 从ID服务获取下一个号段编号
     *
     * @return 号段编号，失败时返回null
     */
    Long fetchSegment();
}
