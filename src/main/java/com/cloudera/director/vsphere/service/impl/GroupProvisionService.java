/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.cloudera.director.vsphere.compute.apitypes.Group;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.service.intf.IGroupProvisionService;
import com.cloudera.director.vsphere.service.intf.IPlacementPlanner;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.impl.VmService;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * @author chiq
 *
 */
public class GroupProvisionService implements IGroupProvisionService {

   private Group group;
   private Folder rootFolder;
   private VmService vmService;
   private Map<String, String> allocations;

   public GroupProvisionService() {

   }

   public GroupProvisionService(VSphereCredentials credentials, VSphereComputeInstanceTemplate template, String prefix, Collection<String> instanceIds, int minCount) throws Exception {
      ServiceInstance serviceInstance = credentials.getServiceInstance();
      this.rootFolder = serviceInstance.getRootFolder();
      this.group = new Group(instanceIds, template, prefix, minCount, VmUtil.getVirtualMachine(serviceInstance, template.getTemplateVm()));
      this.vmService = new VmService(credentials);
      this.allocations = new HashMap<String, String>();
   }

   /**
    * @return the group
    */
   public Group getGroup() {
      return group;
   }


   /**
    * @param group the group to set
    */
   public void setGroup(Group group) {
      this.group = group;
   }


   /**
    * @return the rootFolder
    */
   public Folder getRootFolder() {
      return rootFolder;
   }


   /**
    * @param rootFolder the rootFolder to set
    */
   public void setRootFolder(Folder rootFolder) {
      this.rootFolder = rootFolder;
   }


   /**
    * @return the vmService
    */
   public VmService getVmService() {
      return vmService;
   }


   /**
    * @param vmService the vmService to set
    */
   public void setVmService(VmService vmService) {
      this.vmService = vmService;
   }


   /**
    * @return the allocations
    */
   @Override
   public Map<String, String> getAllocations() {
      return allocations;
   }


   /**
    * @param allocations the allocations to set
    */
   public void setAllocations(Map<String, String> allocations) {
      this.allocations = allocations;
   }

   @Override
   public void provision() throws Exception {
      getPlacementPlan();
      cloneVms();
      reconfigVms();
      startVms();
      getVmsIpAddress();
   }

   public void getPlacementPlan() throws Exception {
      IPlacementPlanner placementPlanner = new PlacementPlanner(rootFolder, group);
      placementPlanner.init();
      placementPlanner.initNodeDisks();
      placementPlanner.placeDisk();
   }

   private void cloneVms() throws Exception {
      for (Node node : group.getNodes()) {

         String templateVmName = node.getTemplateVm();

         // Use the first cloning vm in a host to source template vm to improve vms cloning performance.
         Map<String, String> hostTemplateMap = group.getHostTemplateMap();
         String targetHostName = node.getTargetHost().getName();
         if (hostTemplateMap.get(targetHostName) != null) {
            templateVmName = hostTemplateMap.get(targetHostName);
         }

         boolean isCloned = vmService.clone(templateVmName, node.getVmName(), node.getNumCPUs(), node.getMemorySizeGB(), node.getTargetDatastore().getMor(), node.getTargetHost().getMor(), node.getTargetPool().getMor());

         if (isCloned && hostTemplateMap.get(targetHostName) == null) {
            hostTemplateMap.put(targetHostName, node.getVmName());
            group.setHostTemplateMap(hostTemplateMap);
         }
      }
   }

   private void reconfigVms() throws Exception {
      for (Node node : group.getNodes()) {
         vmService.configureVm(node);
      }
   }

   private void startVms() throws Exception {
      for (Node node : group.getNodes()) {
         vmService.powerOps(node.getVmName(), "poweron");
      }
   }

   private void getVmsIpAddress() throws Exception {
      for (Node node : group.getNodes()) {
         String ipAddress = vmService.getIpAddress(node.getVmName());
         allocations.put(node.getInstanceId(), ipAddress);
      }
   }

}