package com.eaglesakura.android.rx;

/**
 * 実行スレッド選定
 */
public enum SubscribeTarget {
    /**
     * 直列化されたパイプラインで制御する
     */
    Pipeline {
        @Override
        int getKeepAliveMs() {
            return 1000 * 5;
        }

        @Override
        int getThreadPoolNum() {
            return 1;
        }
    },

    /**
     * 並列化されたスレッドプールで制御する
     */
    Parallels {
        @Override
        int getKeepAliveMs() {
            return 1000 * 1;
        }

        @Override
        int getThreadPoolNum() {
            return 5;
        }
    },

    /**
     * プロセス内で共有される直列化された処理
     */
    GlobalPipeline {
        @Override
        int getKeepAliveMs() {
            return 1000 * 1;
        }

        @Override
        int getThreadPoolNum() {
            return 1;
        }
    },

    /**
     * プロセス内で共有される並列化された処理
     */
    GlobalParallels {
        @Override
        int getKeepAliveMs() {
            return 1000 * 1;
        }

        @Override
        int getThreadPoolNum() {
            return 1;
        }
    },

    /**
     * ネットワーク処理用スレッド
     *
     * これはグローバルで共有され、適度なスレッド数に保たれる
     */
    Network {
        @Override
        int getKeepAliveMs() {
            return 500;
        }

        @Override
        int getThreadPoolNum() {
            return 3;
        }
    };

    /**
     * スレッドキャッシュ時間を取得する
     */
    abstract int getKeepAliveMs();

    /**
     * 最大スレッド数を取得する
     */
    abstract int getThreadPoolNum();
}
