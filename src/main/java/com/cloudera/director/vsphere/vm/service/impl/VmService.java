/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import java.util.Map;

import com.cloudera.director.vsphere.resources.VcNetwork;
import com.vmware.vim25.mo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.vm.service.intf.IVmService;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualHardware;

public class VmService implements IVmService {
   private static final Logger logger = LoggerFactory.getLogger(VmService.class);

   private final ServiceInstance serviceInstance;
   private final Folder rootFolder;

   public VmService (VSphereCredentials credentials){
      this.serviceInstance = credentials.getServiceInstance();
      this.rootFolder = this.serviceInstance.getRootFolder();
   }

   /**
    * @return the serviceInstance
    */
   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

   /**
    * @return the rootFolder
    */
   public Folder getRootFolder() {
      return rootFolder;
   }

   @Override
   public boolean clone(String sourceVmName, String targetVmName, int numCPUs, long memoryGB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) throws Exception {

      VirtualMachine vm = getVirtualMachine(sourceVmName);

      VmCloneService vMCloneService = new VmCloneService(vm, targetVmName, numCPUs, memoryGB, targetDatastore, targetHost, targetPool);
      return vMCloneService.run();
   }

   @Override
   public void powerOps(String vmName, String operation) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmPowerOperationService vMPowerOperationsService = new VmPowerOperationService(vm, operation);
      vMPowerOperationsService.run();
   }

   @Override
   public String getIpAddress(String vmName) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      if(vm == null) {
         logger.error("No VM " + vmName + " found");
         return null;
      }

      String ipAddress = null;

      while (ipAddress == null || ipAddress.contains(":")) {
         ipAddress = vm.getGuest().getIpAddress();
         Thread.sleep(3000);
      }

      return ipAddress;
   }

   @Override
   public void addDataDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmDiskOperationService vmDiskOperationService = new VmDiskOperationService(vm);
      vmDiskOperationService.addDataDisk(targetDatastoreName, diskSize, diskMode);

   }

   @Override
   public void addSwapDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmDiskOperationService vmDiskOperationService = new VmDiskOperationService(vm);
      vmDiskOperationService.addSwapDisk(targetDatastoreName, diskSize, diskMode);
   }

   /**
    * @param node
    */
   @Override
   public void configureVm(Node node) throws Exception {
      VirtualMachine vm = getVirtualMachine(node.getVmName());

      VmReconfigService vmReconfigService = new VmReconfigService(vm);
      vmReconfigService.changeDisks(node);

      configNetworks(node);
   }

   /**
    * @param node
    */
   @Override
   public void configNetworks(Node node) throws Exception {
         ManagedObjectReference mob = node.getTargetHost().getMor();
         HostSystem hostSystem = new HostSystem(rootFolder.getServerConnection(), mob);

         Network[] networks = hostSystem.getNetworks();
         logger.info("The host  is " + hostSystem.getName());
         boolean tag = false;
         for (Network network : networks) {
            //the setting network for Cloudera director exists in Esx hosts netowrk which the node vm is located in
            logger.info("The ESX host network is " + network.getName());
            if (node.getNetwork().equals(network.getName())) {
               tag = true;
               VirtualMachine vm = getVirtualMachine(node.getVmName());
               if(vm.getNetworks().length == 0){
                  VcNetwork vcNet = new VcNetwork();
                  vcNet.update(rootFolder.getServerConnection(), network);
                  nicOps(node.getVmName(), "add", vcNet, node.getNetwork(), null);
                  break;
               }else {
                  //edit existing network
                  VcNetwork vcNet = new VcNetwork();
                  vcNet.update(rootFolder.getServerConnection(), network);
                  nicOps(vm.getName(), "edit", vcNet, vm.getNetworks()[0].getName(), node.getNetwork());
                  break;
               }
            }
         }

         if(tag == false)
            throw new Exception("Network " + node.getNetwork() + " is not defined on ESX hosts");
   }

   @Override
   public void nicOps(String vmName, String operation, VcNetwork vcNet, String netName, String newNetwork) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmNicOperationService vmNicOperationService = new VmNicOperationService(vm, operation, vcNet, netName, newNetwork);
      vmNicOperationService.run();
   }

   @Override
   public long getTemplateStorageUsage(String vmName) throws Exception {
      long templateStorageUsage = 0;

      VirtualMachine vm =  getVirtualMachine(vmName);
      VirtualHardware virtualHardware = vm.getConfig().getHardware();
      VirtualDevice[] virtualDevices = virtualHardware.getDevice();
      for (VirtualDevice virtualDevice : virtualDevices) {
         if (virtualDevice.getDeviceInfo().getLabel().split("Hard disk").length > 1) {
            VirtualDisk virtualDisk = (VirtualDisk) virtualDevice;
            templateStorageUsage += virtualDisk.getCapacityInKB();
         }
      }
      templateStorageUsage = templateStorageUsage / 1024 / 1024;  // Convert to GB

      return templateStorageUsage;
   }

   @Override
   public VirtualMachine getVirtualMachine(String vmName) throws Exception {
      VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmName);
      return vm;
   }

   @Override
   public void setMachineIdVariables(String vmName, Map<String, Object> guestVariables) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmReconfigService vmReconfigService = new VmReconfigService(vm);
      vmReconfigService.setMachineIdVariables(guestVariables);
   }

}
