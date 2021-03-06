package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.inAppBilling.IabHelper;
import charlesli.com.personalvocabbuilder.inAppBilling.IabResult;
import charlesli.com.personalvocabbuilder.inAppBilling.Inventory;
import charlesli.com.personalvocabbuilder.inAppBilling.Purchase;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.DailyReviewNotification;
import charlesli.com.personalvocabbuilder.ui.EditCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ModifyMyWordBankCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ReviewDialog;

import static charlesli.com.personalvocabbuilder.controller.InternetConnection.isNetworkAvailable;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.alarmNotificationNotScheduled;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.cancelAlarm;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.scheduleAlarm;
import static charlesli.com.personalvocabbuilder.controller.Subscription.MONTHLY_DEFAULT_TTS_QUOTA;
import static charlesli.com.personalvocabbuilder.controller.Subscription.SKU_MONTHLY_TTS;
import static charlesli.com.personalvocabbuilder.controller.Subscription.SKU_YEARLY_TTS;
import static charlesli.com.personalvocabbuilder.controller.Subscription.reverse;


public class MainActivity extends AppCompatActivity {

    private CategoryCursorAdapter mCategoryAdapter;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MainActivity.this);
    private CustomTTS textToSpeech;
    private IabHelper mHelper;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null || result.isFailure()) return;

            SharedPreferences sharedPreferencesTTS = getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferencesTTS.edit();

            if (inventory.getSkuDetails(SKU_MONTHLY_TTS) != null
                    && inventory.getSkuDetails(SKU_YEARLY_TTS) != null) {
                editor.putString(getString(R.string.monthlyTTSPrice),
                        inventory.getSkuDetails(SKU_MONTHLY_TTS).getPrice());
                editor.putString(getString(R.string.yearlyTTSPrice),
                        inventory.getSkuDetails(SKU_YEARLY_TTS).getPrice());
                editor.putLong(getString(R.string.monthlyTTSPriceAmountMicros),
                        inventory.getSkuDetails(SKU_MONTHLY_TTS).getPriceAmountMicros());
                editor.putLong(getString(R.string.yearlyTTSPriceAmountMicros),
                        inventory.getSkuDetails(SKU_YEARLY_TTS).getPriceAmountMicros());
            }

            Purchase ttsMonthly = inventory.getPurchase(SKU_MONTHLY_TTS);
            Purchase ttsYearly = inventory.getPurchase(SKU_YEARLY_TTS);

            if (ttsYearly != null) {
                editor.putBoolean(getString(R.string.isSubscribed), true);
                editor.putBoolean(getString(R.string.autoRenewed), ttsYearly.isAutoRenewing());
                editor.putInt(getString(R.string.remainingTTSQuota), MONTHLY_DEFAULT_TTS_QUOTA);
                editor.putString(getString(R.string.subscribedTTS), SKU_YEARLY_TTS);
                editor.apply();
            }
            else if (ttsMonthly != null) {
                editor.putBoolean(getString(R.string.isSubscribed), true);
                editor.putBoolean(getString(R.string.autoRenewed), ttsMonthly.isAutoRenewing());
                editor.putInt(getString(R.string.remainingTTSQuota), MONTHLY_DEFAULT_TTS_QUOTA);
                editor.putString(getString(R.string.subscribedTTS), SKU_MONTHLY_TTS);
                editor.apply();
            }
            else {
                editor.putBoolean(getString(R.string.autoRenewed), false);
                editor.putBoolean(getString(R.string.isSubscribed), false);
                editor.apply();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        String compiledKy = reverse(getBaseContext().getString(R.string.firstR))
                + getBaseContext().getString(R.string.middle)
                + reverse(getBaseContext().getString(R.string.lastR));

        mHelper = new IabHelper(this, compiledKy);
        mHelper.enableDebugLogging(false);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (mHelper == null || !result.isSuccess()) {
                    return;
                }

                try {
                    List<String> additionalSkuList = new ArrayList<String>();
                    additionalSkuList.add(SKU_MONTHLY_TTS);
                    additionalSkuList.add(SKU_YEARLY_TTS);
                    mHelper.queryInventoryAsync(true, null, additionalSkuList, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException ignored) {
                }
            }
        });

        ListView mCategoryListView = (ListView) findViewById(R.id.mainListView);
        Cursor cursor = mDbHelper.getCategoryCursor();
        mCategoryAdapter = new CategoryCursorAdapter(this, cursor, 0);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                Intent intent = new Intent(MainActivity.this, MyVocab.class);
                intent.putExtra("Category", categoryName);
                categoryCursor.close();
                startActivity(intent);
            }
        });

        mCategoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                String categoryDesc = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_DESCRIPTION));
                categoryCursor.close();
                editCategoryAlertDialog(categoryName, categoryDesc);
                return true;
            }
        });

        FloatingActionButton reviewFAB = (FloatingActionButton) findViewById(R.id.categoryFAB);
        if (reviewFAB != null) {
            reviewFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectReviewType();
                }
            });
        }

        DailyReviewNotification.createNotificationChannel(this);

        textToSpeech = new CustomTTS(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        }, "com.google.android.tts");

        refreshTTSQuota(MONTHLY_DEFAULT_TTS_QUOTA);

        SharedPreferences sharedPreferencesDailyReview =
                getSharedPreferences(getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);
        boolean isDailyReviewOn = sharedPreferencesDailyReview.getBoolean(getString(R.string.sharedPrefDailyReviewSwitchKey), true);
        if (isDailyReviewOn) {
            if (alarmNotificationNotScheduled(this)) {
                scheduleAlarm(this);
            }
        }
        else {
            cancelAlarm(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCategoryAdapter.changeCursor(mDbHelper.getCategoryCursor());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_category_button) {
            addCategory();
        }
        else if (id == R.id.settings_button) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }
        else if (id == R.id.export_button) {
            Intent intent = new Intent(this, ExportActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.import_button) {
            Intent intent = new Intent(this, ImportActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.upgrade_button) {
            if (isNetworkAvailable(getBaseContext())) {
                Intent intent = new Intent(this, Subscription.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "The upgrade feature is unavailable offline.", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void addCategory() {
        AddCategoryDialog dialog = new AddCategoryDialog(this, mCategoryAdapter, textToSpeech);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void selectReviewType() {
        ReviewDialog dialog = new ReviewDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void editCategoryAlertDialog(final String selectedCategory, final String selectedDesc) {
        if (selectedCategory.equals("My Word Bank")) {
            ModifyMyWordBankCategoryDialog dialog = new ModifyMyWordBankCategoryDialog(this);
            dialog.show();
            dialog.changeButtonsToAppIconColor();
            return;
        }

        EditCategoryDialog dialog = new EditCategoryDialog(this, mCategoryAdapter, selectedCategory, selectedDesc);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void refreshTTSQuota(int monthlyQuota) {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences sharedPreferencesTTS =
                getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
        int currentMonth = calendar.get(Calendar.MONTH);
        int lastUpdatedMonth = sharedPreferencesTTS.getInt(getString(R.string.lastUpdatedMonth), -1);
        if (currentMonth != lastUpdatedMonth) {
            SharedPreferences.Editor editor = sharedPreferencesTTS.edit();
            editor.putInt(getString(R.string.lastUpdatedMonth), currentMonth);
            editor.putInt(getString(R.string.remainingTTSQuota), monthlyQuota);
            editor.apply();
        }
    }

}
