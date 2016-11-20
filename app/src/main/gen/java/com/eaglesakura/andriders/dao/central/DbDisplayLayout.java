package com.eaglesakura.andriders.dao.central;

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
    private java.util.Date updatedDate;

    @NotNull
    private String appPackageName;
    private int slotId;
    private String pluginId;
    private String valueId;

    @Generated
    public DbDisplayLayout() {
    }

    public DbDisplayLayout(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Generated
    public DbDisplayLayout(String uniqueId, java.util.Date updatedDate, String appPackageName, int slotId, String pluginId, String valueId) {
        this.uniqueId = uniqueId;
        this.updatedDate = updatedDate;
        this.appPackageName = appPackageName;
        this.slotId = slotId;
        this.pluginId = pluginId;
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
    public java.util.Date getUpdatedDate() {
        return updatedDate;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUpdatedDate(@NotNull java.util.Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @NotNull
    public String getAppPackageName() {
        return appPackageName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAppPackageName(@NotNull String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

}
