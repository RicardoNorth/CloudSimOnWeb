package com.example.cloudsimweb.examples;

import com.example.cloudsimweb.model.CloudletResult;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.examples.CloudSimExample2;

import java.util.ArrayList;
import java.util.List;

public class CloudSimExample2Wrapper {
    public static List<CloudletResult> run() {
        // 清除日志输出（可选）
        Log.setDisabled(true);

        // 运行原始示例
        CloudSimExample2.main(null);

        // 获取结果
        List<Cloudlet> finishedCloudlets = CloudSimExample2.broker.getCloudletReceivedList();
        List<CloudletResult> results = new ArrayList<>();

        for (Cloudlet c : finishedCloudlets) {
            if (c.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                results.add(new CloudletResult(
                        c.getCloudletId(),
                        c.getGuestId(),
                        c.getExecStartTime(),
                        c.getExecFinishTime()
                ));
            }
        }
        return results;
    }
}
