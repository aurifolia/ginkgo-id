package org.aurifolia.cloud.id.common.constant;

/**
 * ID生成服务常量
 *
 * @author Peng Dan
 * @since 2.0
 */
public class IdConstants {
    
    /**
     * 默认步长
     */
    public static final long DEFAULT_STEP = 1000L;
    
    /**
     * 机器ID最大值（10位）
     */
    public static final long MAX_MACHINE_ID = 1023L;
    
    /**
     * Snowflake epoch时间戳（2020-01-01 00:00:00）
     */
    public static final long SNOWFLAKE_EPOCH = 1577836800000L;
    
    /**
     * 默认缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    /**
     * 默认填充批次大小
     */
    public static final int DEFAULT_FILL_BATCH_SIZE = 512;
    
    /**
     * 默认最大空闲时间（毫秒）
     */
    public static final long DEFAULT_MAX_IDLE_TIME = 100L;
}
