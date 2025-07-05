package com.example.cloudsimweb.model;

import java.util.List;

public class SimulationRequest {
    private int vmCount;
    private int cloudletCount;
    private String algorithm;
    private List<Integer> vmMipsList;

    public int getVmCount() { return vmCount; }
    public void setVmCount(int vmCount) { this.vmCount = vmCount; }

    public int getCloudletCount() { return cloudletCount; }
    public void setCloudletCount(int cloudletCount) { this.cloudletCount = cloudletCount; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public List<Integer> getVmMipsList() { return vmMipsList; }
    public void setVmMipsList(List<Integer> vmMipsList) { this.vmMipsList = vmMipsList; }
}
