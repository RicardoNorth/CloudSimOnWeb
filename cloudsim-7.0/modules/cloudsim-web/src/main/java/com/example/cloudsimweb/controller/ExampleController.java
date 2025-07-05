package com.example.cloudsimweb.controller;

import com.example.cloudsimweb.model.CloudletResult;
import com.example.cloudsimweb.examples.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examples")
public class ExampleController {

    @PostMapping("/run")
    public List<CloudletResult> runExample(@RequestParam(defaultValue = "1") int exampleId) {
        return switch (exampleId) {
            case 1 -> CloudSimExample1Wrapper.run();
            case 2 -> CloudSimExample2Wrapper.run();
            case 3 -> CloudSimExample3Wrapper.run();
            default -> throw new IllegalArgumentException("不支持的示例 ID: " + exampleId);
        };
    }
}
