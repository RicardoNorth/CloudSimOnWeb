package com.example.cloudsimweb.service;

import com.example.cloudsimweb.core.MyDatacenterBroker;
import com.example.cloudsimweb.model.CloudletResult;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    public List<CloudletResult> runSimulation(int vmCount, int cloudletCount) {
        return runSimulation(vmCount, cloudletCount, "timeshared", null);
    }

    public List<CloudletResult> runSimulation(int vmCount, int cloudletCount, String algorithm) {
        return runSimulation(vmCount, cloudletCount, algorithm, null);
    }

    public List<CloudletResult> runSimulation(int vmCount, int cloudletCount, String algorithm, List<Integer> vmMipsList) {
        CloudSim.init(1, Calendar.getInstance(), false);
        Datacenter datacenter = createDatacenter("Datacenter_0");

        MyDatacenterBroker broker;
        try {
            broker = new MyDatacenterBroker("Broker");
        } catch (Exception e) {
            throw new RuntimeException("创建 Broker 失败", e);
        }

        List<Vm> vmList = createVms(vmCount, broker.getId(), algorithm, vmMipsList);
        List<Cloudlet> cloudletList = createCloudlets(cloudletCount, broker.getId());

        broker.submitMyVmList(vmList);
        broker.submitMyCloudletList(cloudletList);

        // 调度绑定策略
        switch (algorithm.toLowerCase()) {
            case "spaceshared" -> applySpaceShared(cloudletList, vmList, broker);
            case "mct" -> applyMCT(cloudletList, vmList, broker);
            default -> { /* timeshared 无需绑定 */ }
        }

        CloudSim.startSimulation();
        List<Cloudlet> resultList = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        return resultList.stream()
                .map(c -> new CloudletResult(c.getCloudletId(), c.getVmId(), c.getExecStartTime(), c.getFinishTime()))
                .collect(Collectors.toList());
    }

    private List<Vm> createVms(int count, int brokerId, String algorithm, List<Integer> vmMipsList) {
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int mips = 1000;
            if (vmMipsList != null && i < vmMipsList.size()) {
                mips = vmMipsList.get(i);
            }

            CloudletScheduler scheduler = switch (algorithm.toLowerCase()) {
                case "spaceshared", "mct" -> new CloudletSchedulerSpaceShared();
                default -> new CloudletSchedulerTimeShared();
            };

            Vm vm = new Vm(i, brokerId, mips, 1, 512, 1000, 10000, "Xen", scheduler);
            vms.add(vm);
        }
        return vms;
    }


    private List<Cloudlet> createCloudlets(int count, int brokerId) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UtilizationModel model = new UtilizationModelFull();
            Cloudlet cl = new Cloudlet(i, 10000, 1, 300, 300, model, model, model);
            cl.setUserId(brokerId);
            cloudlets.add(cl);
        }
        return cloudlets;
    }

    private void applySpaceShared(List<Cloudlet> cloudlets, List<Vm> vms, DatacenterBroker broker) {
        int index = 0;
        for (Cloudlet cl : cloudlets) {
            broker.bindCloudletToVm(cl.getCloudletId(), vms.get(index).getId());
            index = (index + 1) % vms.size();
        }
    }

    private void applyMCT(List<Cloudlet> cloudlets, List<Vm> vms, DatacenterBroker broker) {
        Map<Integer, Double> vmFinishTime = new HashMap<>();
        for (Vm vm : vms) vmFinishTime.put(vm.getId(), 0.0);

        for (Cloudlet cl : cloudlets) {
            double minTime = Double.MAX_VALUE;
            int bestVmId = -1;

            for (Vm vm : vms) {
                double estimated = vmFinishTime.get(vm.getId()) + cl.getCloudletLength() / vm.getMips();
                if (estimated < minTime) {
                    minTime = estimated;
                    bestVmId = vm.getId();
                }
            }

            broker.bindCloudletToVm(cl.getCloudletId(), bestVmId);
            double updated = vmFinishTime.get(bestVmId) + cl.getCloudletLength() / getVmById(vms, bestVmId).getMips();
            vmFinishTime.put(bestVmId, updated);
        }
    }

    private Vm getVmById(List<Vm> vmList, int id) {
        return vmList.stream().filter(vm -> vm.getId() == id).findFirst().orElseThrow();
    }

    private Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<Pe> peList = List.of(new Pe(0, new PeProvisionerSimple(20000)));
            Host host = new Host(i,
                    new RamProvisionerSimple(2048),
                    new BwProvisionerSimple(10000),
                    1000000,
                    peList,
                    new VmSchedulerTimeShared(peList));
            hostList.add(host);
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen",
                hostList,
                10.0, 3.0, 0.05, 0.001, 0.0);

        try {
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new ArrayList<>(), 0);
        } catch (Exception e) {
            throw new RuntimeException("创建数据中心失败", e);
        }
    }
}
