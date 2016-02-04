/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import com.cloudera.director.vsphere.exception.VcException;
import com.cloudera.director.vsphere.utils.VmConfigUtil;
import com.cloudera.director.vsphere.utils.VsphereDirectorAssert;
import com.vmware.vim25.VirtualController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualSCSIController;

/**
 * @author chiq
 *
 */
public class DeviceId {
   public String controllerType;
   public Integer busNum;
   public Integer unitNum;

   public DeviceId(String typeName, Integer busNum, Integer unitNum) {
      controllerType = typeName;
      this.busNum = busNum;
      this.unitNum = unitNum;
   }

   public DeviceId(String deviceId) {
      String parts[] = deviceId.split(":");
      VsphereDirectorAssert.check(parts.length == 2 || parts.length == 3);
      controllerType = parts[0];
      getTypeClass(); // Validate the controller type
      busNum = new Integer(parts[1]);
      unitNum = parts.length > 2 ? new Integer(parts[2]) : null;
   }

   public DeviceId(VirtualDevice controller, VirtualDevice device)
   {
      controllerType = null;
      for (VmConfigUtil.ScsiControllerType type: VmConfigUtil.ScsiControllerType.values()) {
         if (type.implClass.isInstance(controller)) {
            controllerType = type.implClass.getSimpleName();
            break;
         }
      }
      if (controllerType == null) {
         throw VcException.UNSUPPORTED_CONTROLLER_TYPE(controllerType);
      }
      busNum = ((VirtualController)controller).getBusNumber();
      unitNum = device.getUnitNumber();
   }

   public Class<? extends VirtualSCSIController> getTypeClass() {
      for (VmConfigUtil.ScsiControllerType type: VmConfigUtil.ScsiControllerType.values()) {
         if (type.implClass.getSimpleName().equals(controllerType)) {
            return type.implClass;
         }
      }
      throw VcException.UNSUPPORTED_CONTROLLER_TYPE(controllerType);
   }

   static public boolean isSupportedController(VirtualDevice controller) {
      for (VmConfigUtil.ScsiControllerType type: VmConfigUtil.ScsiControllerType.values()) {
         if (type.implClass.isInstance(controller)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString() {
      // format string can handle null -- it will print "null"
      if (unitNum != null) {
         return String.format("%s:%d:%d", controllerType, busNum, unitNum);
      } else {
         return String.format("%s:%d", controllerType, busNum);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DeviceId) {
         DeviceId id = (DeviceId)obj;

         return (controllerType == id.controllerType || controllerType != null && controllerType.equals(id.controllerType))
             && (busNum == id.busNum || busNum != null && busNum.equals(id.busNum))
             && (unitNum == id.unitNum || unitNum != null && unitNum.equals(id.unitNum));
      }
      return false;
   }

   @Override
   public int hashCode() {
       return toString().hashCode();
   }
}
