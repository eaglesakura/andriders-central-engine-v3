package com.eaglesakura.andriders.data.sensor;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.model.ble.BleDeviceCache;
import com.eaglesakura.andriders.model.ble.BleDeviceType;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.collection.StringFlag;

import org.junit.Test;

public class SensorDataManagerTest extends AppUnitTestCase {

    @Test
    public void デバイス情報の保存ができる() throws Throwable {
        SensorDataManager instance = Garnet.instance(AppManagerProvider.class, SensorDataManager.class);
        assertNotNull(instance);

        BleDeviceCache hrCache = instance.save(new BleDeviceCache("AA:BB:CC:DD:EE:FF", "HRCache", new StringFlag(BleDeviceType.ID_HEARTRATE_MONITOR)));
        assertNotNull(hrCache);

        BleDeviceCache scCache = instance.save(new BleDeviceCache("FF:EE:DD:CC:BB:DD", "SCCache", new StringFlag(BleDeviceType.ID_SPEED_AND_CADENCE)));
        assertNotNull(scCache);

        validate(instance.load(BleDeviceType.HEARTRATE_MONITOR).list()).sizeIs(1).contains(hrCache);
        validate(instance.load(BleDeviceType.SPEED_CADENCE_SENSOR).list()).sizeIs(1).contains(scCache);
    }

}