package com.eaglesakura.andriders.service.command;

import com.eaglesakura.sloth.app.lifecycle.SlothLiveData;
import com.eaglesakura.util.DateUtil;

import java.util.Date;

/**
 * 近接情報を流すためのStream
 */
public class ProximityStream extends SlothLiveData<ProximityState> {

    /**
     * 近接状態が更新された
     *
     * @param proximity 新しい近接状態, trueで手を端末に近づけている
     * @return 値が更新されたらtrue
     */
    boolean onUpdate(boolean proximity) {
        ProximityState value = getValue();
        // まだ値が入っていないか、値が切り替わったら送信する
        if (value == null || value.isProximity() != proximity) {
            syncValue(new ProximityState(new Date(DateUtil.currentTimeMillis()), proximity), false);
            return true;
        } else {
            return false;
        }
    }
}
