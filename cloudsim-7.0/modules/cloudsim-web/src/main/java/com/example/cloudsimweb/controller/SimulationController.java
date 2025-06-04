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
        return simulationService.runSimulation(request.getVmCount(), request.getCloudletCount());
    }
}
