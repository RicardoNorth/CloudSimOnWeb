package com.example.cloudsimweb.core;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

public class MyDatacenterBroker extends DatacenterBroker {

    public MyDatacenterBroker(String name) throws Exception {
        super(name);
    }

    // ✅ 提交虚拟机列表
    public void submitMyVmList(List<Vm> list) {
        submitGuestList(list); // Vm 实现了 GuestEntity，因此可作为 GuestEntity 提交
    }

    // ✅ 提交任务列表，并绑定 VM
    public void submitMyCloudletList(List<Cloudlet> list) {
        List<Vm> vms = getGuestList(); // 调用父类提供的方法
        for (Cloudlet cloudlet : list) {
            int vmIndex = cloudlet.getCloudletId() % vms.size();
            cloudlet.setVmId(vms.get(vmIndex).getId());
        }
        submitCloudletList(list); // 调用父类方法
    }

    public List<Vm> getSubmittedVms() {
        return getGuestList(); // 使用父类提供的获取方法
    }

    public List<Cloudlet> getSubmittedCloudlets() {
        return getCloudletList(); // 使用父类提供的获取方法
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        super.processCloudletReturn(ev);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        super.processOtherEvent(ev);
    }
}
