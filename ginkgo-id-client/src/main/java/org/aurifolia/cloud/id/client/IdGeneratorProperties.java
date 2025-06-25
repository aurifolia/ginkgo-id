package org.aurifolia.cloud.id.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ginkgo.id.generator")
public class IdGeneratorProperties {
    private String type = "snowflake";
    private Long machineId = 1L;
    private String bizTag = "default";
    private Integer bufferSize = 4096;
    private Integer fillBatchSize = 512;
    private Long maxIdleTime = 100L;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getBizTag() { return bizTag; }
    public void setBizTag(String bizTag) { this.bizTag = bizTag; }
    public Integer getBufferSize() { return bufferSize; }
    public void setBufferSize(Integer bufferSize) { this.bufferSize = bufferSize; }
    public Integer getFillBatchSize() { return fillBatchSize; }
    public void setFillBatchSize(Integer fillBatchSize) { this.fillBatchSize = fillBatchSize; }
    public Long getMaxIdleTime() { return maxIdleTime; }
    public void setMaxIdleTime(Long maxIdleTime) { this.maxIdleTime = maxIdleTime; }
} 