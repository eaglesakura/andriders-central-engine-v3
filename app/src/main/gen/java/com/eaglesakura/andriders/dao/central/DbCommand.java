package com.eaglesakura.andriders.dao.central;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DB_COMMAND".
 */
@Entity
public class DbCommand {

    @Id
    @NotNull
    @Unique
    private String commandKey;

    @Index
    private int category;

    @NotNull
    private String packageName;

    @NotNull
    private byte[] iconPng;
    private String commandData;
    private String intentData;

    @Generated
    public DbCommand() {
    }

    public DbCommand(String commandKey) {
        this.commandKey = commandKey;
    }

    @Generated
    public DbCommand(String commandKey, int category, String packageName, byte[] iconPng, String commandData, String intentData) {
        this.commandKey = commandKey;
        this.category = category;
        this.packageName = packageName;
        this.iconPng = iconPng;
        this.commandData = commandData;
        this.intentData = intentData;
    }

    @NotNull
    public String getCommandKey() {
        return commandKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCommandKey(@NotNull String commandKey) {
        this.commandKey = commandKey;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @NotNull
    public String getPackageName() {
        return packageName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPackageName(@NotNull String packageName) {
        this.packageName = packageName;
    }

    @NotNull
    public byte[] getIconPng() {
        return iconPng;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setIconPng(@NotNull byte[] iconPng) {
        this.iconPng = iconPng;
    }

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public String getIntentData() {
        return intentData;
    }

    public void setIntentData(String intentData) {
        this.intentData = intentData;
    }

}
