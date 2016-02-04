/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.lang.reflect.Field;

public class DiskSpec implements Comparable<DiskSpec> {
   // GB
   private long size;

   private String name;

   private String parentNode;

   private String targetDs;

   private boolean separable;

   // os/data/swap
   private DiskType diskType;

   private DiskScsiControllerType controller;

   // TODO support split policy
   private DiskSplitPolicy splitPolicy;

   // thick/thin
   private String allocType;

   private String externalAddress;

   // independent_persistent
   private String diskMode;

   private String vmdkPath;

   private long id;

   public DiskSpec() {
      super();
   }

   public DiskSpec(DiskSpec other) {
      this.size = other.size;
      this.name = other.name;
      this.parentNode = other.parentNode;
      this.targetDs = other.targetDs;
      this.separable = other.separable;
      this.diskType = other.diskType;
      this.controller = other.controller;
      this.splitPolicy = other.splitPolicy;
      this.allocType = other.allocType;
      this.externalAddress = other.externalAddress;
      this.diskMode = other.diskMode;
      this.vmdkPath = other.vmdkPath;
   }

   public DiskSpec(String name, long size, String parentNode, boolean separable,
         DiskType diskType, DiskScsiControllerType controller,
         DiskSplitPolicy splitPolicy, String allocType,
         String externalAddress, String diskMode, String vmdkPath) {
      super();
      this.size = size;
      this.name = name;
      this.parentNode = parentNode;
      this.separable = separable;
      this.diskType = diskType;
      this.controller = controller;
      this.splitPolicy = splitPolicy;
      this.allocType = allocType;
      this.externalAddress = externalAddress;
      this.diskMode = diskMode;
      this.vmdkPath = vmdkPath;
   }

   public long getSize() {
      return size;
   }

   public void setSize(long size) {
      this.size = size;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getParentNode() {
      return parentNode;
   }

   public void setParentNode(String parentNode) {
      this.parentNode = parentNode;
   }

   public String getTargetDs() {
      return targetDs;
   }

   public void setTargetDs(String targetDs) {
      this.targetDs = targetDs;
   }

   public boolean isSeparable() {
      return separable;
   }

   public void setSeparable(boolean separable) {
      this.separable = separable;
   }

   public DiskSplitPolicy getSplitPolicy() {
      return splitPolicy;
   }

   public void setSplitPolicy(DiskSplitPolicy splitPolicy) {
      this.splitPolicy = splitPolicy;
   }

   public DiskType getDiskType() {
      return diskType;
   }

   public void setDiskType(DiskType diskType) {
      this.diskType = diskType;
   }

   public DiskScsiControllerType getController() {
      return controller;
   }

   public void setController(DiskScsiControllerType controller) {
      this.controller = controller;
   }

   public String getAllocType() {
      return allocType;
   }

   public void setAllocType(String allocType) {
      this.allocType = allocType;
   }

   public boolean isSystemDisk() {
      return DiskType.SYSTEM_DISK.equals(this.diskType);
   }

   public boolean isDataDisk() {
      return DiskType.DATA_DISK.equals(this.diskType);
   }

   public boolean isSwapDisk() {
      return DiskType.SWAP_DISK.equals(this.diskType);
   }

   public String getExternalAddress() {
      return externalAddress;
   }

   public void setExternalAddress(String externalAddress) {
      this.externalAddress = externalAddress;
   }

   public String getDiskMode() {
      return diskMode;
   }

   public void setDiskMode(String diskMode) {
      this.diskMode = diskMode;
   }

   public String getVmdkPath() {
      return vmdkPath;
   }

   public void setVmdkPath(String vmdkPath) {
      this.vmdkPath = vmdkPath;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      String newLine = System.getProperty("line.separator");

      result.append(this.getClass().getName());
      result.append(" Object {");
      result.append(newLine);

      //determine fields declared in this class only (no fields of superclass)
      Field[] fields = this.getClass().getDeclaredFields();

      //print field names paired with their values
      for (Field field : fields) {
         result.append("  ");
         try {
            result.append(field.getName());
            result.append(": ");
            // requires access to private field:
            result.append(field.get(this));
         } catch (IllegalAccessException ex) {
            System.out.println(ex);
         }
         result.append(newLine);
      }
      result.append("}");

      return result.toString();
   }

   @Override
   public int compareTo(DiskSpec other) {
      if (this.size == other.size)
         return 0;
      else if (this.size < other.size)
         return -1;
      else
         return 1;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      DiskSpec other = (DiskSpec) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      return true;
   }

   public long getId() {
      return id;
   }

   public void setId(long id1) {
      id = id1;
   }
}
