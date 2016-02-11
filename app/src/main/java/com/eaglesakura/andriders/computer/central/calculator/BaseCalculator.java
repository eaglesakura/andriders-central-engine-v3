package com.eaglesakura.andriders.computer.central.calculator;

import com.eaglesakura.andriders.db.Settings;

public class BaseCalculator {
    protected Settings getSettings() {
        return Settings.getInstance();
    }
}
