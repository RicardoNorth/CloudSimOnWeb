// ✅ SimulationService.java
package com.example.cloudsimweb.service;

import com.example.cloudsimweb.core.MyDatacenterBroker;
import com.example.cloudsimweb.model.CloudletResult;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class SimulationService {

    public List<CloudletResult> runSimulation(int vmCount, int cloudletCount) {
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter datacenter = createDatacenter("Datacenter_0");

        MyDatacenterBroker broker = null;
        try {
            broker = new MyDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < vmCount; i++) {
            Vm vm = new Vm(i, broker.getId(), 1000, 1, 512, 1000, 10000,
                    "Xen", new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }

        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < cloudletCount; i++) {
            UtilizationModel utilizationModel = new UtilizationModelFull();
            Cloudlet cloudlet = new Cloudlet(i, 10000, 1, 300, 300,
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(broker.getId());
            cloudletList.add(cloudlet);
        }

        // ✅ 使用自定义方法名避免冲突
        broker.submitMyVmList(vmList);
        broker.submitMyCloudletList(cloudletList);

        CloudSim.startSimulation();
        List<Cloudlet> resultList = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        List<CloudletResult> results = new ArrayList<>();
        for (Cloudlet c : resultList) {
            results.add(new CloudletResult(
                    c.getCloudletId(),
                    c.getVmId(),
                    c.getExecStartTime(),
                    c.getFinishTime()
            ));
        }

        return results;
    }

    private Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < 2; i++) { // 创建两个 Host
            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(1000))); // 每个 host 一个核心

            Host host = new Host(
                    i,
                    new RamProvisionerSimple(2048),       // 2GB RAM
                    new BwProvisionerSimple(10000),       // 10Gbps 带宽
                    1000000,                               // 1,000,000 MIPS
                    peList,
                    new VmSchedulerTimeShared(peList)
            );
            hostList.add(host);
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), new ArrayList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

}
