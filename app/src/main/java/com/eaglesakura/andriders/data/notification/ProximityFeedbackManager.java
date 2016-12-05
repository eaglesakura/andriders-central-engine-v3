package com.eaglesakura.andriders.data.notification;

/**
 * 近接コマンドの操作を行う
 */
public class ProximityFeedbackManager {
//    @NonNull
//    Context mContext;
//
//    @NonNull
//    final ClockTimer mTimer;
//
//    /**
//     * 近接状態である場合true
//     */
//    boolean mProximityState;
//
//    /**
//     * 最後の近接状態チェック
//     */
//    boolean mLastProximityState = false;
//
//    @NonNull
//    final SensorManager mSensorManager;
//
//    @Nullable
//    CommandData mCommandData;
//
//    ScreenEventReceiver mScreenEventReceiver;
//
//    ProximityCommandController mCommandController;
//
//    final PendingCallbackQueue mCallbackQueue;
//
//    final CommandDataManager mCommandDataManager;
//
//    int mTaskId = 0;
//
//    @ColorInt
//    final int[] BACKGROUND_COLOR_TABLE = {
//            0xFFff4500,
//            0xFF87cefa,
//            0xFF228b22,
//            0xFF7cfc00,
//            0xFFffff00,
//    };
//
//    Bitmap mIcon;
//
//    public ProximityFeedbackManager(@NonNull Context context, @NonNull Clock clock, @NonNull PendingCallbackQueue callbackQueue) {
//        mContext = context;
//        mTimer = new ClockTimer(clock);
//        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
//        mScreenEventReceiver = new ScreenEventReceiver(mContext);
//        mCommandDataManager = new CommandDataManager(mContext);
//        mCallbackQueue = callbackQueue;
//
//        Garnet.inject(this);
//    }
//
//    /**
//     * コントローラに
//     */
//    public void bind(@NonNull ProximityCommandController commandController) {
//        commandController.setProximityListener(mProximityFeedbackListener);
//        mCommandController = commandController;
//    }
//
//
//    /**
//     * 近接センサーに接続する
//     */
//    @UiThread
//    public void connect() {
//        mScreenEventReceiver.connect();
//
//        // 近接コマンドに接続する
//        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
//        if (!sensorList.isEmpty()) {
//            mSensorManager.registerListener(mProximityListener, sensorList.get(0), SensorManager.SENSOR_DELAY_NORMAL);
//        }
//
//    }
//
//    /**
//     * 近接センサーから切断する
//     */
//    @UiThread
//    public void disconnect() {
//        mScreenEventReceiver.disconnect();
//
//        try {
//            mSensorManager.unregisterListener(mProximityListener);
//        } catch (Exception e) {
//            AppLog.report(e);
//        }
//    }
//
//    /**
//     * ディスプレイON/OFFとフィードバックON/OFFを連動させる
//     */
//    private boolean isScreenLink() {
//        return true;
//    }
//
//
//    /**
//     * センサーイベントの受理を行う
//     */
//    private SensorEventListener mProximityListener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(final SensorEvent event) {
//            float value = event.values[0];
//
//            if (isScreenLink() && !mScreenEventReceiver.isScreenPowerOn()) {
//                // スクリーンリンクされていて、スクリーンOFFならコマンドに反応しない
//                value = 1000;
//            }
//
//            AppLog.proximity("Proximity[%.1f]", value);
//
//            mLastProximityState = mProximityState;
//            mProximityState = (value < COMMAND_INPUT_VALUE);
//            if (mLastProximityState == mProximityState) {
//                // ステートが変わってない
//                return;
//            }
//
//            // ステートが変わった
//            ++mTaskId;
//            if (mProximityState) {
//                // 近接状態の監視を行う
//                startProximityCheckTask(mTaskId);
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//    };
//
//    /**
//     * 近接状態の監視を行う
//     *
//     * MEMO: 近接コマンド実行中は監視スレッドを立てて常に実行チェックを行う
//     */
//    @UiThread
//    void startProximityCheckTask(int taskId) {
//        mTimer.start();
//        new BackgroundTaskBuilder<>(mCallbackQueue)
//                .callbackOn(CallbackTime.Alive)
//                .executeOn(ExecuteTarget.NewThread)
//                .async(task -> {
//                    try {
//                        mCommandController.onStartCount(mCommandDataManager.loadFromCategory(CommandData.CATEGORY_PROXIMITY));
//                        while (!task.isCanceled()) {
//                            task.waitTime(100);
//                        }
//                    } finally {
//                        mCommandController.onEndCount();
//                    }
//                    return this;
//                })
//                .cancelSignal(task -> {
//                    return taskId != mTaskId || !mProximityState;
//                })
//                .failed((error, task) -> {
//                    AppLog.printStackTrace(error);
//                })
//                .start();
//    }
//
//    /**
//     * UIのレンダリングを行う
//     */
//    @UiThread
//    public void rendering(Graphics graphics) {
//        if (!mProximityState) {
//            // 近接状態に無いのでレンダリングしない
//            return;
//        }
//
//        int CURRENT_TIME_SEC = (int) (mTimer.end() / 1000);
//        graphics.setColorARGB(0xFFFFFFFF);
//
//        final double WINDOW_WIDTH = graphics.getWidth();
//        final double WINDOW_HEIGHT = graphics.getHeight();
//
//        final double WINDOW_CENTER_X = WINDOW_WIDTH / 2;
//        final double WINDOW_CENTER_Y = WINDOW_HEIGHT / 2;
//
//        final double ROUND_RADIUS = Math.min(WINDOW_WIDTH, WINDOW_HEIGHT) * 0.4;
//
//        @ColorInt
//        int backgroundColor = BACKGROUND_COLOR_TABLE[0];
//        if (CURRENT_TIME_SEC < BACKGROUND_COLOR_TABLE.length) {
//            backgroundColor = BACKGROUND_COLOR_TABLE[CURRENT_TIME_SEC];
//        }
//
//        // 中心円を表示する
//        {
//            // 待機時間
//            final float CURRENT_WEIGHT = CURRENT_TIME_SEC > 0 ? 1.0f : (float) (mTimer.end() % 1000) / 1000.0f;
//
//            // 中心を指定色で塗りつぶす
//            graphics.setColorARGB(backgroundColor);
//            graphics.fillRoundRect(
//                    (int) (WINDOW_CENTER_X - CURRENT_WEIGHT * ROUND_RADIUS),
//                    (int) (WINDOW_CENTER_Y - CURRENT_WEIGHT * ROUND_RADIUS),
//                    (int) (CURRENT_WEIGHT * ROUND_RADIUS * 2), (int) (CURRENT_WEIGHT * ROUND_RADIUS * 2),
//                    (float) (CURRENT_WEIGHT * ROUND_RADIUS * 0.1f));
//        }
//
//        // アイコンをレンダリングする
//        if (CURRENT_TIME_SEC > 0) {
//            if (mIcon != null) {
//                // アイコンのレンダリング
//                graphics.setColorARGB(0xFFFFFFFF);
//                final double ICON_RADIUS = ROUND_RADIUS * 0.75;
//                graphics.drawBitmap(mIcon, (int) (WINDOW_CENTER_X - ICON_RADIUS), (int) (WINDOW_CENTER_Y - ICON_RADIUS), (int) (ICON_RADIUS * 2), (int) (ICON_RADIUS * 2));
//            } else {
//                graphics.setFontSize(new Font().calcFontSize("0", (int) (WINDOW_HEIGHT * 0.65)));
//                graphics.setColorARGB(0xEF000000);
//                graphics.drawString(String.valueOf(CURRENT_TIME_SEC), (int) WINDOW_CENTER_X, (int) WINDOW_CENTER_Y, -1, -1, Graphics.STRING_CENTER_X | Graphics.STRING_CENTER_Y);
//            }
//        }
//    }
//
//    /**
//     * 近接コマンドのハンドリング状況に応じて処理を行う
//     */
//    ProximityCommandController.ProximityListener mProximityFeedbackListener = new ProximityCommandController.ProximityListener() {
//        @Override
//        public void onRequestUserFeedback(ProximityCommandController self, int sec, @Nullable CommandData data) {
//            mCallbackQueue.run(CallbackTime.Alive, () -> {
//                if (!mProximityState) {
//                    // 近接してないので何もしない
//                    mCommandData = null;
//                    mIcon = null;
//                    return;
//                }
//
//                mCommandData = data;
//                if (data != null) {
//                    AppLog.proximity("BootCommand[%s]", data.getPackageName());
//                    mIcon = data.decodeIcon();
//                } else {
//                    mIcon = null;
//                }
//
//                if (sec == 0) {
//                    // 開始フィードバック
//                    VibrateManager.vibrate(mContext, VibrateManager.VIBRATE_TIME_SHORT_MS * 2);
//                } else {
//                    // 中途フィードバック
//                    VibrateManager.vibrateLong(mContext);
//                }
//            });
//
//        }
//
//        @Override
//        public void onProximityTimeOver(ProximityCommandController self, int sec) {
//            mCommandData = null;
//            mIcon = null;
//        }
//    };
}