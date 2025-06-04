package com.example.cloudsimweb.model;

public class SimulationRequest {
    private int vmCount;
    private int cloudletCount;

    public int getVmCount() {
        return vmCount;
    }

    public void setVmCount(int vmCount) {
        this.vmCount = vmCount;
    }

    public int getCloudletCount() {
        return cloudletCount;
    }

    public void setCloudletCount(int cloudletCount) {
        this.cloudletCount = cloudletCount;
    }
}
