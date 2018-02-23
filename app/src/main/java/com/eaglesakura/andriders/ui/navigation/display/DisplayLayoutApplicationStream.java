package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.sloth.app.lifecycle.SlothLiveData;

/**
 * レイアウト編集対象のApplicationを指定したStream
 */
public class DisplayLayoutApplicationStream extends SlothLiveData<DisplayLayoutApplication> {
    void onUpdate(DisplayLayoutApplication app) {
        syncValue(app, false);
    }
}
