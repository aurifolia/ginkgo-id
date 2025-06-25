package org.aurifolia.cloud.id.metaserver.client.dto;

public class MachineDTO {
    private Long id;
    private String bizTag;
    private Long machineId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizTag() { return bizTag; }
    public void setBizTag(String bizTag) { this.bizTag = bizTag; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
} 