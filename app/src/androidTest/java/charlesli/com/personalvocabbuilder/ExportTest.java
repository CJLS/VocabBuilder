package charlesli.com.personalvocabbuilder;

import android.content.Intent;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import charlesli.com.personalvocabbuilder.controller.MainActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


/**
 * Created by charles on 2017-04-15.
 */
@RunWith(AndroidJUnit4.class)
public class ExportTest {

    private static String TEXT_DENY = "Deny";
    private static String TEXT_ALLOW = "Allow";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);


    //TODO: Add TEST cases
    //T1: No apps can send intent
    //T2: External Storage is unavailable
    //T3: Permission request is denied with never show again
    //T4: Check if all apps can send file intent properly with file
    //Make sure that test start with permission revoked
    UiDevice uiDevice;

    public static void assertViewWithTextIsVisible(UiDevice device, String text) {
        UiObject allowButton = device.findObject(new UiSelector().text(text));
        if (!allowButton.exists()) {
            throw new AssertionError("View with text <" + text + "> not found!");
        }
    }

    public static void denyCurrentPermission(UiDevice device) throws UiObjectNotFoundException {
        UiObject denyButton = device.findObject(new UiSelector().text(TEXT_DENY));
        denyButton.click();
    }

    public static void allowCurrentPermission(UiDevice device) throws UiObjectNotFoundException {
        UiObject allowButton = device.findObject(new UiSelector().text(TEXT_ALLOW));
        allowButton.click();
    }

    @Before
    public void setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm revoke " + getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void clickExportOKButton_requestExternalStoragePermission() throws Exception {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(getTargetContext().getString(R.string.export)))
                .perform(click());
        onView(withId(R.id.exportListView))
                .check(matches(isDisplayed()));
        UiObject okButton = uiDevice.findObject(new UiSelector().text("OK"));
        okButton.clickAndWaitForNewWindow();
        assertViewWithTextIsVisible(uiDevice, TEXT_DENY);
        assertViewWithTextIsVisible(uiDevice, TEXT_ALLOW);
        denyCurrentPermission(uiDevice);
    }

    @Test
    public void denyExternalStoragePermissionRequest_showRequestDeniedToastMessage() throws Exception {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(getTargetContext().getString(R.string.export)))
                .perform(click());
        UiObject okButton = uiDevice.findObject(new UiSelector().text("OK"));
        okButton.clickAndWaitForNewWindow();
        denyCurrentPermission(uiDevice);

        MainActivity activity = mainActivityActivityTestRule.getActivity();
        onView(withText(getTargetContext().getString(R.string.externalStoragePermissionDenied)))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void allowExternalStoragePermissionRequest_fireActionSendIntent() throws Exception {
        Intents.init();
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(getTargetContext().getString(R.string.export)))
                .perform(click());
        UiObject okButton = uiDevice.findObject(new UiSelector().text("OK"));
        okButton.clickAndWaitForNewWindow();
        allowCurrentPermission(uiDevice);

        intended(hasAction(Intent.ACTION_CHOOSER));
        Intents.release();
    }

}
