package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationActivityTest;
import com.eaglesakura.andriders.util.AppLog;

import org.junit.Test;

public class DisplaySettingFragmentMainTest extends NavigationActivityTest {

    @Test
    public void Fragmentが起動できる() throws Throwable {
        DisplaySettingFragmentMain fragment = getNavigationFragment(DisplaySettingFragmentMain.class);
        assertNotNull(fragment);

        sleep(1000);
    }

    @Test
    public void インストール済みアプリ一覧を取得する() throws Throwable {
        validate(getNavigationActivity(DisplaySettingFragmentMain.class), AppTargetSelectFragment.class)
                .check(AppTargetSelectFragment.class, it -> {
                    validate(it.listInstalledApplications().list())
                            .notEmpty()
                            .allNotNull()
                            .checkFirst(that -> {
                                // 最初のオブジェクトはACEである
                                assertEquals(that.info.packageName, BuildConfig.APPLICATION_ID);
                            })
                            .eachWithIndex((index, that) -> {
                                // loaded
                                AppLog.test("installed[%d] package[%s] label[%s]", index, that.info.packageName, that.info.loadLabel(getContext().getPackageManager()));
                            });
                });
    }

    @Test
    public void 対象アプリ選択UIを開く() throws Throwable {
        getNavigationActivity(DisplaySettingFragmentMain.class);

        newScenario()
                .viewWithId(R.id.Setting_CycleComputer_TargetApplication_Root)
                .click()
                .doneView()
                .pressBack();
    }
}