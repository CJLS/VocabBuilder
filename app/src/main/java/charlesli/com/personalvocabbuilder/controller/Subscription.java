package charlesli.com.personalvocabbuilder.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.inAppBilling.IabBroadcastReceiver;
import charlesli.com.personalvocabbuilder.inAppBilling.IabHelper;
import charlesli.com.personalvocabbuilder.inAppBilling.IabResult;
import charlesli.com.personalvocabbuilder.inAppBilling.Inventory;
import charlesli.com.personalvocabbuilder.inAppBilling.Purchase;

public class Subscription extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {

    public static final String SKU_MONTHLY_TTS = "monthly_tts";
    public static final String SKU_YEARLY_TTS = "yearly_tts";
    public static final int MONTHLY_DEFAULT_TTS_QUOTA = 60;
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    String mSubscribedInfiniteTTSSku = "";
    boolean mSubscribedToInfiniteTTS = false;
    Button monthlySubButton;
    Button yearlySubButton;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null || result.isFailure()) return;

            if (purchase.getSku().equals(SKU_MONTHLY_TTS) || purchase.getSku().equals(SKU_YEARLY_TTS)) {
                mSubscribedToInfiniteTTS = true;
                mSubscribedInfiniteTTSSku = purchase.getSku();

                SharedPreferences sharedPreferencesTTS = getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferencesTTS.edit();
                editor.putBoolean(getString(R.string.isSubscribed), true);
                editor.putBoolean(getString(R.string.autoRenewed), true);
                editor.putInt(getString(R.string.remainingTTSQuota), MONTHLY_DEFAULT_TTS_QUOTA);
                editor.putString(getString(R.string.subscribedTTS), mSubscribedInfiniteTTSSku);
                editor.apply();

                monthlySubButton.setAlpha(1f);
                monthlySubButton.setClickable(true);
                yearlySubButton.setAlpha(1f);
                yearlySubButton.setClickable(true);
                if (mSubscribedInfiniteTTSSku.equals(SKU_MONTHLY_TTS)) {
                    monthlySubButton.setAlpha(0.5f);
                    monthlySubButton.setClickable(false);
                }
                else {
                    yearlySubButton.setAlpha(0.5f);
                    yearlySubButton.setClickable(false);
                }

                alert("Thank you for subscribing! You can now enjoy unlimited text-to-speech.");
            }
        }
    };
    private String monthlyTTSPrice;
    private String yearlyTTSPrice;
    private Long monthlyTTSPriceAmountMicro;
    private Long yearlyTTSPriceAmountMicro;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null || result.isFailure()) return;

            // Set subscription buttons text if it hasn't been setup yet
            if ((monthlyTTSPrice.equals("") || yearlyTTSPrice.equals(""))
                    && (inventory.getSkuDetails(SKU_MONTHLY_TTS) != null
                    && inventory.getSkuDetails(SKU_YEARLY_TTS) != null)) {
                monthlyTTSPrice =
                        inventory.getSkuDetails(SKU_MONTHLY_TTS).getPrice();
                yearlyTTSPrice =
                        inventory.getSkuDetails(SKU_YEARLY_TTS).getPrice();
                monthlyTTSPriceAmountMicro =
                        inventory.getSkuDetails(SKU_MONTHLY_TTS).getPriceAmountMicros();
                yearlyTTSPriceAmountMicro =
                        inventory.getSkuDetails(SKU_YEARLY_TTS).getPriceAmountMicros();
                setSubscriptionButtonsText(monthlyTTSPrice, yearlyTTSPrice,
                        monthlyTTSPriceAmountMicro, yearlyTTSPriceAmountMicro);
            }

            SharedPreferences sharedPreferencesTTS = getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferencesTTS.edit();

            Purchase ttsMonthly = inventory.getPurchase(SKU_MONTHLY_TTS);
            Purchase ttsYearly = inventory.getPurchase(SKU_YEARLY_TTS);
            if (ttsMonthly != null) {
                mSubscribedInfiniteTTSSku = SKU_MONTHLY_TTS;
                yearlySubButton.setAlpha(1f);
                yearlySubButton.setClickable(true);
                if (ttsMonthly.isAutoRenewing()) {
                    monthlySubButton.setAlpha(0.5f);
                    monthlySubButton.setClickable(false);
                }
                editor.putBoolean(getString(R.string.isSubscribed), true);
                editor.putBoolean(getString(R.string.autoRenewed), ttsMonthly.isAutoRenewing());
                editor.putInt(getString(R.string.remainingTTSQuota), MONTHLY_DEFAULT_TTS_QUOTA);
                editor.putString(getString(R.string.subscribedTTS), SKU_MONTHLY_TTS);
                editor.apply();
            }
            else if (ttsYearly != null) {
                mSubscribedInfiniteTTSSku = SKU_YEARLY_TTS;
                monthlySubButton.setAlpha(1f);
                monthlySubButton.setClickable(true);
                if (ttsYearly.isAutoRenewing()) {
                    yearlySubButton.setAlpha(0.5f);
                    yearlySubButton.setClickable(false);
                }
                editor.putBoolean(getString(R.string.isSubscribed), true);
                editor.putBoolean(getString(R.string.autoRenewed), ttsYearly.isAutoRenewing());
                editor.putInt(getString(R.string.remainingTTSQuota), MONTHLY_DEFAULT_TTS_QUOTA);
                editor.putString(getString(R.string.subscribedTTS), SKU_YEARLY_TTS);
                editor.apply();
            }
            else {
                mSubscribedInfiniteTTSSku = "";
                editor.putBoolean(getString(R.string.isSubscribed), false);
                editor.putBoolean(getString(R.string.autoRenewed), false);
                editor.apply();
            }

            mSubscribedToInfiniteTTS = (ttsMonthly != null) || (ttsYearly != null);
        }
    };

    @NonNull
    public static String reverse(String forward) {
        StringBuilder builder = new StringBuilder(forward);
        return builder.reverse().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.subscription_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferencesTTS = getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);

        monthlyTTSPrice = sharedPreferencesTTS.getString(getString(R.string.monthlyTTSPrice), "");
        yearlyTTSPrice = sharedPreferencesTTS.getString(getString(R.string.yearlyTTSPrice), "");
        monthlyTTSPriceAmountMicro = sharedPreferencesTTS.getLong(getString(R.string.monthlyTTSPriceAmountMicros), -1);
        yearlyTTSPriceAmountMicro = sharedPreferencesTTS.getLong(getString(R.string.yearlyTTSPriceAmountMicros), -1);
        setSubscriptionButtonsText(monthlyTTSPrice, yearlyTTSPrice, monthlyTTSPriceAmountMicro, yearlyTTSPriceAmountMicro);

        boolean isSubscribed = sharedPreferencesTTS.getBoolean(getString(R.string.isSubscribed), false);
        boolean isAutoRenewed = sharedPreferencesTTS.getBoolean(getString(R.string.autoRenewed), false);
        String subscribedTTS = sharedPreferencesTTS.getString(getString(R.string.subscribedTTS), "");

        monthlySubButton = (Button) findViewById(R.id.monthlySubButton);
        yearlySubButton = (Button) findViewById(R.id.yearlySubButton);

        if (isSubscribed && isAutoRenewed) {
            if (subscribedTTS.equals(SKU_MONTHLY_TTS)) {
                monthlySubButton.setAlpha(0.5f);
                monthlySubButton.setClickable(false);
            }
            else {
                yearlySubButton.setAlpha(0.5f);
                yearlySubButton.setClickable(false);
            }
        }

        String compiledKy = reverse(getBaseContext().getString(R.string.firstR))
                + getBaseContext().getString(R.string.middle)
                + reverse(getBaseContext().getString(R.string.lastR));

        mHelper = new IabHelper(this, compiledKy);

        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (mHelper == null || !result.isSuccess()) return;

                mBroadcastReceiver = new IabBroadcastReceiver(Subscription.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                try {
                    List<String> additionalSkuList = new ArrayList<String>();
                    additionalSkuList.add(SKU_MONTHLY_TTS);
                    additionalSkuList.add(SKU_YEARLY_TTS);
                    mHelper.queryInventoryAsync(true, null, additionalSkuList, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException ignored) {
                }
            }
        });


        monthlySubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) return;
                try {
                    List<String> oldSku = null;
                    if (!TextUtils.isEmpty(mSubscribedInfiniteTTSSku)
                            && !mSubscribedInfiniteTTSSku.equals(SKU_MONTHLY_TTS)) {
                        // The user currently has a valid subscription, any purchase action is going to
                        // replace that subscription
                        oldSku = new ArrayList<String>();
                        oldSku.add(mSubscribedInfiniteTTSSku);
                    }
                    mHelper.launchPurchaseFlow(Subscription.this, SKU_MONTHLY_TTS, IabHelper.ITEM_TYPE_SUBS,
                            oldSku, 10001, mPurchaseFinishedListener, "");
                } catch (IabHelper.IabAsyncInProgressException ignored) {
                }
            }
        });

        yearlySubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) return;
                try {
                    List<String> oldSku = null;
                    if (!TextUtils.isEmpty(mSubscribedInfiniteTTSSku)
                            && !mSubscribedInfiniteTTSSku.equals(SKU_YEARLY_TTS)) {
                        // The user currently has a valid subscription, any purchase action is going to
                        // replace that subscription
                        oldSku = new ArrayList<String>();
                        oldSku.add(mSubscribedInfiniteTTSSku);
                    }
                    mHelper.launchPurchaseFlow(Subscription.this, SKU_YEARLY_TTS, IabHelper.ITEM_TYPE_SUBS,
                            oldSku, 10001, mPurchaseFinishedListener, "");
                } catch (IabHelper.IabAsyncInProgressException ignored) {
                }
            }
        });

        TextView noSubButton = (TextView) findViewById(R.id.noSubButton);
        noSubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setSubscriptionButtonsText(String monthlyTTSPrice, String yearlyTTSPrice,
                                            Long monthlyTTSPriceAmountMicros, Long yearlyTTSPriceAmountMicros) {
        if (monthlyTTSPrice.equals("") || yearlyTTSPrice.equals("") ||
                monthlyTTSPriceAmountMicros <= 0 || yearlyTTSPriceAmountMicros <= 0) {
            return;
        }
        Button monthlySubButton = (Button) findViewById(R.id.monthlySubButton);
        String monthlyPriceInfo = monthlyTTSPrice + " / Month";
        monthlySubButton.setText(monthlyPriceInfo);

        Button yearlySubButton = (Button) findViewById(R.id.yearlySubButton);
        String yearlyPriceInfo = yearlyTTSPrice + " / Year";
        yearlySubButton.setText(yearlyPriceInfo);

        Pattern pricePattern = Pattern.compile("[0-9,.]+");
        Matcher yearlyPriceMatcher = pricePattern.matcher(yearlyTTSPrice);
        if (yearlyPriceMatcher.find()) {
            float yearlyPricePerMonth = yearlyTTSPriceAmountMicros / 1000000f / 12f;
            String yearlyPricePerMonthWithCurrency =
                    yearlyPriceMatcher.replaceFirst(String.format(Locale.CANADA, "%.2f", yearlyPricePerMonth));
            yearlyPriceInfo = yearlyPricePerMonthWithCurrency + " / Month";
            yearlySubButton.setText(yearlyPriceInfo);
        }
        Matcher monthlyPriceMatcher = pricePattern.matcher(monthlyTTSPrice);
        if (monthlyPriceMatcher.find()) {
            float monthlyPricePerMonth = monthlyTTSPriceAmountMicros / 1000000f;
            String monthlyPricePerMonthWithCurrency =
                    monthlyPriceMatcher.replaceFirst(String.format(Locale.getDefault(), "%.2f", monthlyPricePerMonth));
            monthlyPriceInfo = monthlyPricePerMonthWithCurrency + " / Month";
            monthlySubButton.setText(monthlyPriceInfo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException ignored) {
        }
    }
}
