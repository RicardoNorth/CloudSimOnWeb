package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a data center with one host and run one cloudlet on it.
 */
public class CloudSimExample1 {
	// DatacenterBroker 可以创建并提交 VM, 创建并提交 Cloudlet, 匹配 VM 和 Cloudlet，接受任务结果
	public static DatacenterBroker broker;

	// 添加 VM 和 Cloudlet 列表
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		Log.println("Starting CloudSimExample1...");

		try {
			// 第一步：初始化 CloudSim 包。在创建任何实体之前都必须调用它。
			int num_user = 1; // 云用户的数量
			Calendar calendar = Calendar.getInstance(); // 获取当前的时间
 			boolean trace_flag = false; // 是否记录详细事件轨迹

			/* 注释开始 - Dinesh Bhagwat
			 * 初始化 CloudSim 库。
			 * init() 会调用 initCommonVariable()，而 initCommonVariable() 又会调用 initialize()（这三个方法都定义在 CloudSim.java 中）。
			 * initialize() 会创建两个集合 —— 一个 SimEntity 对象的 ArrayList（名为 entities，用于表示所有的模拟实体）；
			 * 另一个是 LinkedHashMap（名为 entitiesByName，用于用实体名称作为键，存储同样的实体集合）。
			 * initialize() 会创建两个事件队列 —— 一个是 future（未来事件队列），另一个是 deferred（延迟事件队列）。
			 * initialize() 还会创建一个 HashMap，键是整数，值是谓词（Predicates），用于从延迟队列中筛选特定事件。
			 * initialize() 会将模拟时钟设置为 0，并将运行标志（running，布尔值）设为 false。
			 * 一旦 initialize() 执行完毕（此时我们回到 initCommonVariable() 方法中），会创建一个 CloudSimShutDown 实例（继承自 SimEntity），
			 * 它的参数为 numuser = 1，名字为 CloudSimShutDown，id 为 -1，状态为 RUNNABLE（可运行）。
			 * 然后这个新实体会被加入到模拟中，在加入过程中，其 id 会从 -1 变为 0。
			 * 上面提到的两个集合 —— entities 和 entitiesByName —— 会添加这个 SimEntity 实例。
			 * shutdownId 的默认值是 -1，此时变成了 0。
			 * 当 initCommonVariable() 返回后（我们此时仍在 init() 方法中），又会创建一个 CloudInformationService 实例（同样继承自 SimEntity），
			 * 名字为 CloudInformationService，id 为 -1，状态为 RUNNABLE。
			 * 该实体被加入到模拟中时，id 会从 -1 改为 1（因为这是下一个可用的 id）。
			 * entities 和 entitiesByName 两个集合也会更新这个 SimEntity。
			 * 此时 cisId（原默认值为 -1）变成了 1。
			 * 注释结束 - Dinesh Bhagwat
			 */

			CloudSim.init(num_user, calendar, trace_flag);

			// 第二步：创建数据中心
			// 数据中心是 CloudSim 中的资源提供者。我们至少需要一个数据中心来运行 CloudSim 模拟。

			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// 第三步：创建代理
			broker = new DatacenterBroker("Broker");;
			int brokerId = broker.getId();

			// 第四步：创建虚拟机器列表
			vmlist = new ArrayList<>();

			// 对 VM 的描述
			int vmid = 0;
			int mips = 1000; // 每秒能执行多少百万条指令，这里是每秒能执行十亿条指令
			long size = 10000; // 虚拟机磁盘镜像的大小，单位是MB，一般用于表示VM所需的持久存储容量，比如系统盘或应用镜像
			int ram = 512; // 虚拟机分配的内存大小，单位是 MB。
			long bw = 1000; // 带宽（Bandwidth），单位是 Mb/s（兆位每秒）。
			int pesNumber = 1; // 虚拟机拥有的处理核心数量（Processing Elements）。
			String vmm = "Xen"; // 这台虚拟机使用的 虚拟机监控器（VMM, Virtual Machine Monitor） 的名称

			// 创建 VM
			Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			// add the VM to the vmList
			vmlist.add(vm);

			// submit vm list to the broker
			broker.submitGuestList(vmlist);

			// 第五步：创建一个任务
			cloudletList = new ArrayList<>();

			// Cloudlet properties
			int id = 0;
			long length = 400000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize,
                                        outputSize, utilizationModel, utilizationModel, 
                                        utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setGuestId(vmid);

			// add the cloudlet to the list
			cloudletList.add(cloudlet);

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			Log.println("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// 创建一个 PowerDatacenter 所需的步骤如下：
		// 1. 我们需要创建一个列表来存储我们的机器（主机）

		List<Host> hostList = new ArrayList<>();

		// 2. 一台机器（Machine）包含一个或多个 PE（处理单元）或 CPU/核心。
		// 在这个示例中，每台机器只有一个核心。
		List<Pe> peList = new ArrayList<>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(
			new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		); // This is our machine

		// 5. 创建一个 DatacenterCharacteristics 对象，用于存储数据中心的属性：
		// 架构（architecture）、操作系统（OS）、机器列表（Machines）、
		// 分配策略（时间共享或空间共享）、时区（time zone）以及价格（以 G$/每个 PE 的时间单位计）。
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				Log.print("SUCCESS");

				Log.println(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getGuestId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getExecFinishTime()));
			}
		}
	}
}