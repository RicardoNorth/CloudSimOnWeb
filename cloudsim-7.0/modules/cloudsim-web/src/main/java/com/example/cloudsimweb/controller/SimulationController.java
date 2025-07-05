package com.example.cloudsimweb.controller;

import com.example.cloudsimweb.model.SimulationRequest;
import com.example.cloudsimweb.model.CloudletResult;
import com.example.cloudsimweb.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @PostMapping("/simulate")
    public List<CloudletResult> simulate(@RequestBody SimulationRequest request) {
        // 获取前端传来的算法字段，如果为空则默认使用 "timeshared"
        System.out.println("收到的 VM MIPS 列表: " + request.getVmMipsList());
        String algorithm = request.getAlgorithm();
        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "timeshared";
        }

        // 调试信息：输出前端传来的 VM MIPS 列表
        System.out.println("收到的 VM MIPS 列表: " + request.getVmMipsList());

        // 调用支持 MIPS 参数的方法
        return simulationService.runSimulation(
                request.getVmCount(),
                request.getCloudletCount(),
                algorithm.toLowerCase(),
                request.getVmMipsList()
        );
    }
}
