package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.ui.navigation.NavigationActivityTest;

public class DisplaySettingFragmentMainTest extends NavigationActivityTest {

//    @Test
//    public void Fragmentが起動できる() throws Throwable {
//        DisplaySettingFragmentMain fragment = getNavigationFragment(DisplaySettingFragmentMain.class);
//        assertNotNull(fragment);
//
//        sleep(1000);
//    }
//
//    @Test
//    public void インストール済みアプリ一覧を取得する() throws Throwable {
//        validate(ScenarioContext.findFragment(AppTargetSelectFragment.class))
//                .check(it -> {
//                    assertNotNull(it);
//                    validate(it.listInstalledApplications().list())
//                            .notEmpty()
//                            .allNotNull()
//                            .checkFirst(that -> {
//                                // 最初のオブジェクトはACEである
//                                assertEquals(that.info.packageName, BuildConfig.APPLICATION_ID);
//                            })
//                            .each((index, that) -> {
//                                // loaded
//                                AppLog.test("installed[%d] package[%s] label[%s]", index, that.info.packageName, that.info.loadLabel(getContext().getPackageManager()));
//                            });
//                });
//    }
//
//    @Test
//    public void 対象アプリ選択UIを開く() throws Throwable {
//        getNavigationActivity(DisplaySettingFragmentMain.class);
//
//        UiScenario.clickFromId(R.id.Setting_CycleComputer_TargetApplication_Root).step();
//        ScenarioContext.pressBack().step();
//    }
}