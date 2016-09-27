package com.eaglesakura.andriders.dao.central;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DB_BLE_FITNESS_DEVICE".
 */
@Entity
public class DbBleFitnessDevice {

    @Id
    @NotNull
    @Unique
    private String address;
    private int selectedCount;
    private int deviceType;

    @Generated
    public DbBleFitnessDevice() {
    }

    public DbBleFitnessDevice(String address) {
        this.address = address;
    }

    @Generated
    public DbBleFitnessDevice(String address, int selectedCount, int deviceType) {
        this.address = address;
        this.selectedCount = selectedCount;
        this.deviceType = deviceType;
    }

    @NotNull
    public String getAddress() {
        return address;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAddress(@NotNull String address) {
        this.address = address;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

}
