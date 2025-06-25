package org.aurifolia.cloud.id.metaserver.client.dto;

public class SegmentMetaDTO {
    private Long id;
    private String bizTag;
    private Long nextId;
    private Long step;
    private String createTime;
    private String updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizTag() { return bizTag; }
    public void setBizTag(String bizTag) { this.bizTag = bizTag; }
    public Long getNextId() { return nextId; }
    public void setNextId(Long nextId) { this.nextId = nextId; }
    public Long getStep() { return step; }
    public void setStep(Long step) { this.step = step; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
} 