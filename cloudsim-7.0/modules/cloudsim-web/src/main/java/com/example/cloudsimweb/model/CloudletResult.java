package com.example.cloudsimweb.model;

public class CloudletResult {
    private long cloudletId;
    private long vmId;
    private double startTime;
    private double finishTime;

    public CloudletResult(long cloudletId, long vmId, double startTime, double finishTime) {
        this.cloudletId = cloudletId;
        this.vmId = vmId;
        this.startTime = startTime;
        this.finishTime = finishTime;
    }

    public long getCloudletId() { return cloudletId; }
    public long getVmId() { return vmId; }
    public double getStartTime() { return startTime; }
    public double getFinishTime() { return finishTime; }
}
