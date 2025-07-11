/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.VmAllocationPolicy.GuestMapping;
import org.cloudbus.cloudsim.core.*;

/**
 * Datacenter class is a CloudResource whose hostList are virtualized. It deals with processing of
 * VM queries (i.e., handling of VMs) instead of processing Cloudlet-related queries.
 * So, even though an AllocPolicy will be instantiated (in the init() method of the superclass, 
 * it will not be used, as processing of cloudlets are handled by the CloudletScheduler and 
 * processing of VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * //todo In fact, there isn't the method init() in the super class, as stated in
 * the documentation here. An AllocPolicy isn't being instantiated there.
 * The last phrase of the class documentation appears to be out-of-date or wrong.
 */
public class Datacenter extends SimEntity {

	/** The characteristics. */
	private DatacenterCharacteristics characteristics;

	/** The regional Cloud Information Service (CIS) name. 
         * @see org.cloudbus.cloudsim.core.CloudInformationService
         */
	private String regionalCisName;

	/** The vm provisioner. */
	private VmAllocationPolicy vmAllocationPolicy;

	/** The last time some cloudlet was processed in the datacenter. */
	private double lastProcessTime;

	/** The storage list. */
	private List<Storage> storageList;

	/** The vm list. */
	private List<? extends GuestEntity> vmList;

	/** The scheduling delay to process each datacenter received event. */
	private double schedulingInterval;

	/**
	 * Allocates a new Datacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by the super class)
	 * @param characteristics the characteristics of the datacenter to be created
	 * @param storageList a List of storage elements, for data simulation
	 * @param vmAllocationPolicy the policy to be used to allocate VMs into hosts
         * @param schedulingInterval the scheduling delay to process each datacenter received event
	 * @throws Exception when one of the following scenarios occur:
	 *  <ul>
	 *    <li>creating this entity before initializing CloudSim package
	 *    <li>this entity name is <tt>null</tt> or empty
	 *    <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br/>
	 *    No PEs mean the Cloudlets can't be processed. A CloudResource must contain 
	 *    one or more Machines. A Machine must contain one or more PEs.
	 *  </ul>
         * 
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public Datacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name);

		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<>());
		setSchedulingInterval(schedulingInterval);

		for (HostEntity host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}

		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0 && !getCharacteristics().getHostList().isEmpty()) {
                    throw new Exception(super.getName()
                        + " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		
		if(getCharacteristics().getNumberOfPes()==0 && getCharacteristics().getHostList().isEmpty()) {
			Log.printlnConcat(name,": inter-cloud networking topology created...");
		}

		// stores id of this class
		getCharacteristics().setId(super.getId());
	}

	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link # body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
         * 
         * //@TODO This method doesn't appear to be used
	 */
	protected void registerOtherEntity() {
		// empty. This should be override by a child class
	}

	@Override
	public void processEvent(SimEvent ev) {
		int srcId = -1;
		CloudSimTags tag = ev.getTag();

        // Resource characteristics inquiry
        if (tag == CloudActionTags.RESOURCE_CHARACTERISTICS) {
            srcId = (Integer) ev.getData();
            sendNow(srcId, tag, getCharacteristics());

            // Resource dynamic info inquiry
        } else if (tag == CloudActionTags.RESOURCE_DYNAMICS) {
            srcId = (Integer) ev.getData();
            sendNow(srcId, tag, 0);
        } else if (tag == CloudActionTags.RESOURCE_NUM_PE) {
            srcId = (Integer) ev.getData();
            int numPE = getCharacteristics().getNumberOfPes();
            sendNow(srcId, tag, numPE);
        } else if (tag == CloudActionTags.RESOURCE_NUM_FREE_PE) {
            srcId = (Integer) ev.getData();
            int freePesNumber = getCharacteristics().getNumberOfFreePes();
            sendNow(srcId, tag, freePesNumber);

            // New Cloudlet arrives
        } else if (tag == CloudActionTags.CLOUDLET_SUBMIT) {
            processCloudletSubmit(ev, false);

            // New Cloudlet arrives, but the sender asks for an ack
        } else if (tag == CloudActionTags.CLOUDLET_SUBMIT_ACK) {
            processCloudletSubmit(ev, true);

            // Cancels a previously submitted Cloudlet
        } else if (tag == CloudActionTags.CLOUDLET_CANCEL) {
            processCloudlet(ev, CloudActionTags.CLOUDLET_CANCEL);

            // Pauses a previously submitted Cloudlet
        } else if (tag == CloudActionTags.CLOUDLET_PAUSE) {
            processCloudlet(ev, CloudActionTags.CLOUDLET_PAUSE);

            // Pauses a previously submitted Cloudlet, but the sender
            // asks for an acknowledgement
        } else if (tag == CloudActionTags.CLOUDLET_PAUSE_ACK) {
            processCloudlet(ev, CloudActionTags.CLOUDLET_PAUSE_ACK);

            // Resumes a previously submitted Cloudlet
        } else if (tag == CloudActionTags.CLOUDLET_RESUME) {
            processCloudlet(ev, CloudActionTags.CLOUDLET_RESUME);

            // Resumes a previously submitted Cloudlet, but the sender
            // asks for an acknowledgement
        } else if (tag == CloudActionTags.CLOUDLET_RESUME_ACK) {
            processCloudlet(ev, CloudActionTags.CLOUDLET_RESUME_ACK);

            // Moves a previously submitted Cloudlet to a different resource
        } else if (tag == CloudActionTags.CLOUDLET_MOVE) {
            processCloudletMove((int[]) ev.getData(), CloudActionTags.CLOUDLET_MOVE);

            // Moves a previously submitted Cloudlet to a different resource
        } else if (tag == CloudActionTags.CLOUDLET_MOVE_ACK) {
            processCloudletMove((int[]) ev.getData(), CloudActionTags.CLOUDLET_MOVE_ACK);

            // Checks the status of a Cloudlet
        } else if (tag == CloudActionTags.CLOUDLET_STATUS) {
            processCloudletStatus(ev);

            // Ping packet
        } else if (tag == CloudActionTags.INFOPKT_SUBMIT) {
            processPingRequest(ev);
        } else if (tag == CloudActionTags.VM_CREATE) {
            processVmCreate(ev, false);
        } else if (tag == CloudActionTags.VM_CREATE_ACK) {
            processVmCreate(ev, true);
        } else if (tag == CloudActionTags.VM_DESTROY) {
            processVmDestroy(ev, false);
        } else if (tag == CloudActionTags.VM_DESTROY_ACK) {
            processVmDestroy(ev, true);
        } else if (tag == CloudActionTags.VM_MIGRATE) {
            processVmMigrate(ev, false);
        } else if (tag == CloudActionTags.VM_MIGRATE_ACK) {
            processVmMigrate(ev, true);
        } else if (tag == CloudActionTags.VM_DATA_ADD) {
            processDataAdd(ev, false);
        } else if (tag == CloudActionTags.VM_DATA_ADD_ACK) {
            processDataAdd(ev, true);
        } else if (tag == CloudActionTags.VM_DATA_DEL) {
            processDataDelete(ev, false);
        } else if (tag == CloudActionTags.VM_DATA_DEL_ACK) {
            processDataDelete(ev, true);
        } else if (tag == CloudActionTags.VM_DATACENTER_EVENT) {
            updateCloudletProcessing();
            checkCloudletCompletion();

            // other unknown tags are processed by this method
        } else {
            processOtherEvent(ev);
        }
	}

	/**
	 * Process a file deletion request.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 */
	protected void processDataDelete(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] data = (Object[]) ev.getData();
		if (data == null) {
			return;
		}

		String filename = (String) data[0];
		int req_source = (Integer) data[1];
		CloudSimTags tag = CloudActionTags.BLANK;

		// check if this file can be deleted (do not delete is right now)
		DataCloudTags msg = deleteFileFromStorage(filename);
		if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
			tag = DataCloudTags.CTLG_DELETE_MASTER;
		} else { // if an error occured, notify user
			tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
		}

		if (ack) {
			// send back to sender
			Object[] pack = new Object[2];
			pack[0] = filename;
			pack[1] = msg;

			sendNow(req_source, tag, pack);
		}
	}

	/**
	 * Process a file inclusion request.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 */
	protected void processDataAdd(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] pack = (Object[]) ev.getData();
		if (pack == null) {
			return;
		}

		File file = (File) pack[0]; // get the file
		file.setMasterCopy(true); // set the file into a master copy
		int sentFrom = (Integer) pack[1]; // get sender ID

		/**
		 * // DEBUG Log.printLine(super.get_name() + ".addMasterFile(): " + file.getName() +
		 * " from " + CloudSim.getEntityName(sentFrom));
		 */

		Object[] data = new Object[3];
		data[0] = file.getName();

		DataCloudTags msg = addFile(file); // add the file

		if (ack) {
			data[1] = -1; // no sender id
			data[2] = msg; // the result of adding a master file
			sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
		}
	}

	/**
	 * Processes a ping request.
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processPingRequest(SimEvent ev) {
		InfoPacket pkt = (InfoPacket) ev.getData();
		pkt.setTag(CloudActionTags.INFOPKT_RETURN);
		pkt.setDestId(pkt.getSrcId());

		// sends back to the sender
		sendNow(pkt.getSrcId(), CloudActionTags.INFOPKT_RETURN, pkt);
	}

	/**
	 * Process the event for an User/Broker who wants to know the status of a Cloudlet. This
	 * Datacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletStatus(SimEvent ev) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;
		Cloudlet.CloudletStatus status;

		try {
			// if a sender using cloudletXXX() methods
			int[] data = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];

			status = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId).getCloudletScheduler()
					.getCloudletStatus(cloudletId);
		}

		// if a sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();

				status = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId)
						.getCloudletScheduler().getCloudletStatus(cloudletId);
			} catch (Exception e) {
				Log.printlnConcat(getName(), ": Error in processing CloudActionTags.CLOUDLET_STATUS");
				Log.println(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printlnConcat(getName(), ": Error in processing CloudActionTags.CLOUDLET_STATUS");
			Log.println(e.getMessage());
			return;
		}

		int[] array = new int[3];
		array[0] = getId();
		array[1] = cloudletId;
		array[2] = status.ordinal();

		sendNow(userId, CloudActionTags.CLOUDLET_STATUS, array);
	}

	/**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev information about the event just happened
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printlnConcat(getName(), ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this Datacenter. This
	 * Datacenter will then send the status back to the User/Broker.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev, boolean ack) {
		GuestEntity guest = (GuestEntity) ev.getData();

		boolean result;
		HostEntity userPreferredHost = guest.getHost();
		if (userPreferredHost != null && getVmAllocationPolicy().getHostList().contains(userPreferredHost)) {
			result = getVmAllocationPolicy().allocateHostForGuest(guest, userPreferredHost);
		} else {
			result = getVmAllocationPolicy().allocateHostForGuest(guest);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = guest.getId();
			data[2] = result ? CloudSimTags.TRUE : CloudSimTags.FALSE;
			send(guest.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudActionTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(guest);

			if (guest.isBeingInstantiated()) {
				guest.setBeingInstantiated(false);
			}

			guest.updateCloudletsProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(guest).getGuestScheduler()
					.getAllocatedMipsForGuest(guest));
		} else {
			Log.printlnConcat(CloudSim.clock(), ": Datacenter.guestAllocator: 无法为 ", guest.getClassName(), " #", guest.getId(), " 找到可用的主机");
		}
	}

	/**
	 * Process the event for an User/Broker who wants to destroy a VM previously created in this
	 * Datacenter. This Datacenter may send, upon request, the status back to the
	 * User/Broker.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		GuestEntity vm = (GuestEntity) ev.getData();
		getVmAllocationPolicy().deallocateHostForGuest(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudActionTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);
	}

	/**
	 * Process the event for an User/Broker who wants to migrate a VM. This Datacenter will
	 * then send the status back to the User/Broker.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		Object tmp = ev.getData();
		if (!(tmp instanceof GuestMapping migrate)) {
			throw new ClassCastException("The data object must be GuestMapping");
		}

        GuestEntity vm = migrate.vm();
		HostEntity host = migrate.host();
		
		//destroy VM in src host
		getVmAllocationPolicy().deallocateHostForGuest(vm);
		host.removeMigratingInGuest(vm);

		// @TODO: what happens to the vmId / containerId of the cloudlets hosted on the Vm?
		// create VM in dest host
		boolean result = getVmAllocationPolicy().allocateHostForGuest(vm, host);
		if (!result) {
			Log.println("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;
			sendNow(ev.getSourceId(), CloudActionTags.VM_CREATE_ACK, data);
		}

		Log.formatLine(
				"%.2f: Migration of VM #%d to Host #%d is completed",
				CloudSim.clock(),
				vm.getId(),
				host.getId());
		vm.setInMigration(false);
	}

	/**
	 * Processes a Cloudlet based on the event type.
	 *
	 * @param ev  information about the event just happened
	 * @param tag event type
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudlet(SimEvent ev, CloudActionTags tag) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int[] data = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getGuestId();
			} catch (Exception e) {
				Log.printlnConcat(super.getName(), ": Error in processing Cloudlet");
				Log.println(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printlnConcat(super.getName(), ": Error in processing a Cloudlet.");
			Log.println(e.getMessage());
			return;
		}

		// begins executing ....
		switch (tag) {
			case CLOUDLET_CANCEL -> processCloudletCancel(cloudletId, userId, vmId);
			case CLOUDLET_PAUSE -> processCloudletPause(cloudletId, userId, vmId, false);
			case CLOUDLET_PAUSE_ACK -> processCloudletPause(cloudletId, userId, vmId, true);
			case CLOUDLET_RESUME -> processCloudletResume(cloudletId, userId, vmId, false);
			case CLOUDLET_RESUME_ACK -> processCloudletResume(cloudletId, userId, vmId, true);
			default -> {
			}
		}

	}

	/**
	 * Process the event for an User/Broker who wants to move a Cloudlet.
	 *
	 * @param receivedData information about the migration
	 * @param tag          event type
	 * @pre receivedData != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudletMove(int[] receivedData, CloudActionTags tag) {
		updateCloudletProcessing();

        int cloudletId = receivedData[0];
		int userId = receivedData[1];
		int vmId = receivedData[2];
		int vmDestId = receivedData[3];
		int destId = receivedData[4];

		// get the cloudlet
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);

		boolean failed = false;
		if (cl == null) {// cloudlet doesn't exist
			failed = true;
		} else {
			// has the cloudlet already finished?
			if (cl.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {// if yes, send it back to user
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cloudletId;
				data[2] = 0;
				sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
				sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);
			}

			// prepare cloudlet for migration
			cl.setGuestId(vmDestId);

			// the cloudlet will migrate from one vm to another does the destination VM exist?
			if (destId == getId()) {
				GuestEntity vm = getVmAllocationPolicy().getHost(vmDestId, userId).getGuest(vmDestId,userId);
				if (vm == null) {
					failed = true;
				} else {
					// time to transfer the files
					double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
					vm.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);
				}
			} else {// the cloudlet will migrate from one resource to another
				CloudActionTags newTag = ((tag == CloudActionTags.CLOUDLET_MOVE_ACK) ? CloudActionTags.CLOUDLET_SUBMIT_ACK
						: CloudActionTags.CLOUDLET_SUBMIT);
				sendNow(destId, newTag, cl);
			}
		}

		if (tag == CloudActionTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (failed) {
				data[2] = 0;
			} else {
				data[2] = 1;
			}
			sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printlnConcat(getName(), ": Warning - ",cl.getClass().getSimpleName()," #", cl.getCloudletId(), " owned by ", name,
						" is already completed/finished.");
				Log.println("Therefore, it is not being executed again");
				Log.println();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
				}

				sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
                                getCharacteristics().getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getGuestId();

			// time to transfer the files
			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

			HostEntity host = getVmAllocationPolicy().getHost(vmId, userId);
			GuestEntity vm = host.getGuest(vmId, userId);
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
				estimatedFinishTime += fileTransferTime;
				send(getId(), estimatedFinishTime, CloudActionTags.VM_DATACENTER_EVENT);
			}
			/*else {
				Log.printlnConcat(CloudSim.clock(), ": [",getName(), "]: Warning - ", cl.getClass().getSimpleName()," #", cl.getCloudletId(),
						" is paused because not enough free PEs on ", vm.getClassName(), " #", vm.getId());
			}*/

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
			}
		} catch (ClassCastException c) {
			Log.printlnConcat(getName(), ".processCloudletSubmit(): ", "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printlnConcat(getName(), ".processCloudletSubmit(): ", "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	/**
	 * Predict the total time to transfer a list of files.
	 * 
	 * @param requiredFiles the files to be transferred
	 * @return the predicted time
	 */
	protected double predictFileTransferTime(List<String> requiredFiles) {
		double time = 0.0;

		for (String fileName : requiredFiles) {
			for (int i = 0; i < getStorageList().size(); i++) {
				Storage tempStorage = getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					break;
				}
			}
		}
		return time;
	}        

	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId ID of the cloudlet to be resumed
	 * @param userId ID of the cloudlet's owner
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 * @param vmId the id of the VM where the cloudlet has to be resumed
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId)
				.getCloudletScheduler().cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { // if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudActionTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			data[2] = status ? CloudSimTags.TRUE : CloudSimTags.FALSE;
			sendNow(userId, CloudActionTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId ID of the cloudlet to be paused
	 * @param userId ID of the cloudlet's owner
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
	 * @param vmId the id of the VM where the cloudlet has to be paused
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId)
				.getCloudletScheduler().cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			data[2] = status ? CloudSimTags.TRUE : CloudSimTags.FALSE;
			sendNow(userId, CloudActionTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet cancel request.
	 * 
	 * @param cloudletId ID of the cloudlet to be canceled
	 * @param userId ID of the cloudlet's owner
	 * @param vmId the id of the VM where the cloudlet has to be canceled
         * 
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId,userId)
				.getCloudletScheduler().cloudletCancel(cloudletId);
		sendNow(userId, CloudActionTags.CLOUDLET_CANCEL, cl);
	}

	/**
	 * Updates processing of each cloudlet running in this Datacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not sim entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() {
		// if some time passed since last processing
		// R: for term is to allow loop at simulation start. Otherwise, one initial
		// simulation step is skipped and schedulers are not properly initialized
		if (CloudSim.clock() < 0.111 || CloudSim.clock() >= getLastProcessTime() + CloudSim.getMinTimeBetweenEvents()) {
			double smallerTime = Double.MAX_VALUE;
			for (HostEntity host : getVmAllocationPolicy().getHostList()) {
				// inform VMs to update processing
				double time = host.updateCloudletsProcessing(CloudSim.clock());
				// what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			// gurantees a minimal interval before scheduling the event
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01) {
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01;
			}
			if (smallerTime != Double.MAX_VALUE) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudActionTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(CloudSim.clock());
		}
	}

	/**
	 * Verifies if some cloudlet inside this Datacenter already finished. 
         * If yes, send it to the User/Broker
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void checkCloudletCompletion() {
		for (HostEntity host : getVmAllocationPolicy().getHostList()) {
			for (GuestEntity vm : host.getGuestList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}

	/**
	 * Adds a file into the resource's storage before the experiment starts.
	 * If the file is a master file, then it will be registered to the RC
	 * when the experiment begins.
	 *
	 * @param file a DataCloud file
	 * @return a tag number denoting whether this operation is a success or not
	 */
	public DataCloudTags addFile(File file) {
		if (file == null) {
			return DataCloudTags.FILE_ADD_ERROR_EMPTY;
		}

		if (contains(file.getName())) {
			return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
		}

		// check storage space first
		if (getStorageList().size() <= 0) {
			return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
		}

		Storage tempStorage = null;
		DataCloudTags msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			if (tempStorage.getAvailableSpace() >= file.getSize()) {
				tempStorage.addFile(file);
				msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
				break;
			}
		}

		return msg;
	}

	/**
	 * Checks whether the datacenter has the given file.
	 * 
	 * @param file a file to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(File file) {
		if (file == null) {
			return false;
		}
		return contains(file.getName());
	}

	/**
	 * Checks whether the datacenter has the given file.
	 * 
	 * @param fileName a file name to be searched
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return false;
		}

		Iterator<Storage> it = getStorageList().iterator();
		Storage storage = null;
		boolean result = false;

		while (it.hasNext()) {
			storage = it.next();
			if (storage.contains(fileName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Deletes the file from the storage.
	 * Also, check whether it is possible to delete the file from the storage.
	 *
	 * @param fileName the name of the file to be deleted
	 * @return the tag denoting the status of the operation,
	 * either {@link DataCloudTags#FILE_DELETE_ERROR} or
	 * {@link DataCloudTags#FILE_DELETE_SUCCESSFUL}
	 */
	private DataCloudTags deleteFileFromStorage(String fileName) {
		Storage tempStorage = null;
		File tempFile = null;
		DataCloudTags msg = DataCloudTags.FILE_DELETE_ERROR;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			tempFile = tempStorage.getFile(fileName);
			tempStorage.deleteFile(fileName, tempFile);
			msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
		} // end for

		return msg;
	}

	@Override
	public void startEntity() {
		super.startEntity();
		// this resource should register to regional CIS.
		// However, if not specified, then register to system CIS (the
		// default CloudInformationService) entity.
		int gisID = CloudSim.getEntityId(regionalCisName);
		if (gisID == -1) {
			gisID = CloudSim.getCloudInfoServiceEntityId();
		}

		// send the registration to CIS
		sendNow(gisID, CloudActionTags.REGISTER_RESOURCE, getId());
		// Below method is for a child class to override
		registerOtherEntity();
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	public <T extends HostEntity> List<T> getHostList() {
		return getCharacteristics().getHostList();
	}

	/**
	 * Gets the datacenter characteristics.
	 * 
	 * @return the datacenter characteristics
	 */
	protected DatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**
	 * Sets the datacenter characteristics.
	 * 
	 * @param characteristics the new datacenter characteristics
	 */
	protected void setCharacteristics(DatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**
	 * Gets the regional Cloud Information Service (CIS) name. 
	 * 
	 * @return the regional CIS name
	 */
	protected String getRegionalCisName() {
		return regionalCisName;
	}

	/**
	 * Sets the regional cis name.
	 * 
	 * @param regionalCisName the new regional cis name
	 */
	protected void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}

	/**
	 * Gets the vm allocation policy.
	 * 
	 * @return the vm allocation policy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * Sets the vm allocation policy.
	 * 
	 * @param vmAllocationPolicy the new vm allocation policy
	 */
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	/**
	 * Gets the last time some cloudlet was processed in the datacenter.
	 * 
	 * @return the last process time
	 */
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	/**
	 * Sets the last process time.
	 * 
	 * @param lastProcessTime the new last process time
	 */
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}

	/**
	 * Gets the storage list.
	 * 
	 * @return the storage list
	 */
	protected List<Storage> getStorageList() {
		return storageList;
	}

	/**
	 * Sets the storage list.
	 * 
	 * @param storageList the new storage list
	 */
	protected void setStorageList(List<Storage> storageList) {
		this.storageList = storageList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends GuestEntity> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param vmList the new vm list
	 */
	protected <T extends GuestEntity> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

}
