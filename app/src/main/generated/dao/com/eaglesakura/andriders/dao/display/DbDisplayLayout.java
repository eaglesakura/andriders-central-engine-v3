package com.eaglesakura.andriders.dao.display;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table DB_DISPLAY_LAYOUT.
 */
public class DbDisplayLayout {

    /** Not-null value. */
    private String uniqueId;
    /** Not-null value. */
    private String targetPackage;
    private int slotId;
    /** Not-null value. */
    private String extensionId;
    /** Not-null value. */
    private String valueId;

    public DbDisplayLayout() {
    }

    public DbDisplayLayout(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public DbDisplayLayout(String uniqueId, String targetPackage, int slotId, String extensionId, String valueId) {
        this.uniqueId = uniqueId;
        this.targetPackage = targetPackage;
        this.slotId = slotId;
        this.extensionId = extensionId;
        this.valueId = valueId;
    }

    /** Not-null value. */
    public String getUniqueId() {
        return uniqueId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /** Not-null value. */
    public String getTargetPackage() {
        return targetPackage;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    /** Not-null value. */
    public String getExtensionId() {
        return extensionId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    /** Not-null value. */
    public String getValueId() {
        return valueId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

}
