package com.eaglesakura.andriders.dao.display;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DB_DISPLAY_LAYOUT".
 */
@Entity
public class DbDisplayLayout {

    @Id
    @NotNull
    @Unique
    @Index
    private String uniqueId;

    @NotNull
    private String targetPackage;
    private int slotId;

    @NotNull
    private String extensionId;

    @NotNull
    private String valueId;

    @Generated
    public DbDisplayLayout() {
    }

    public DbDisplayLayout(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Generated
    public DbDisplayLayout(String uniqueId, String targetPackage, int slotId, String extensionId, String valueId) {
        this.uniqueId = uniqueId;
        this.targetPackage = targetPackage;
        this.slotId = slotId;
        this.extensionId = extensionId;
        this.valueId = valueId;
    }

    @NotNull
    public String getUniqueId() {
        return uniqueId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUniqueId(@NotNull String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @NotNull
    public String getTargetPackage() {
        return targetPackage;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTargetPackage(@NotNull String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @NotNull
    public String getExtensionId() {
        return extensionId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setExtensionId(@NotNull String extensionId) {
        this.extensionId = extensionId;
    }

    @NotNull
    public String getValueId() {
        return valueId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setValueId(@NotNull String valueId) {
        this.valueId = valueId;
    }

}
