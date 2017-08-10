package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.inAppBillingUtil.IabBroadcastReceiver;
import charlesli.com.personalvocabbuilder.inAppBillingUtil.IabHelper;
import charlesli.com.personalvocabbuilder.inAppBillingUtil.IabResult;
import charlesli.com.personalvocabbuilder.inAppBillingUtil.Inventory;
import charlesli.com.personalvocabbuilder.inAppBillingUtil.Purchase;

public class Subscription extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {

    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    String SKU_MONTHLY_TTS = "monthly_tts";
    String SKU_YEARLY_TTS = "yearly_tts";

    String mPurchasedInfiniteTTSSku = "";
    boolean mSubscribedToInfiniteTTS = false;
    int mTTSMonthlyLimit = 50;
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d("IAB", "Query inventory finished.");

            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d("IAB", "Failed to query inventory: " + result);
                return;
            }

            if (inventory.getSkuDetails(SKU_MONTHLY_TTS) != null
                    && inventory.getSkuDetails(SKU_YEARLY_TTS) != null) {
                String monthlyTTSPriceWithCurrency =
                        inventory.getSkuDetails(SKU_MONTHLY_TTS).getPrice();
                Log.d("IAB", "monthlyTTSPriceWithCurrency " + monthlyTTSPriceWithCurrency);

                String yearlyTTSPriceWithCurrency =
                        inventory.getSkuDetails(SKU_YEARLY_TTS).getPrice();
                Log.d("IAB", "yearlyTTSPriceWithCurrency " + yearlyTTSPriceWithCurrency);

                Button monthlySubButton = (Button) findViewById(R.id.monthlySubButton);
                String monthlyPriceInfo = monthlyTTSPriceWithCurrency + " / Month";
                monthlySubButton.setText(monthlyPriceInfo);

                Button yearlySubButton = (Button) findViewById(R.id.yearlySubButton);
                String yearlyPriceInfo = yearlyTTSPriceWithCurrency + " / Year";
                yearlySubButton.setText(yearlyPriceInfo);

                Pattern pricePattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                Matcher priceMatcher = pricePattern.matcher(yearlyTTSPriceWithCurrency);
                if (priceMatcher.find()) {
                    String yearlyPrice = priceMatcher.group();
                    float yearlyPricePerMonth = Float.parseFloat(yearlyPrice) / 12.0f;
                    String yearlyPricePerMonthWithCurrency =
                            priceMatcher.replaceFirst(String.valueOf(yearlyPricePerMonth));
                    yearlyPriceInfo = yearlyPricePerMonthWithCurrency + " / Month";
                    yearlySubButton.setText(yearlyPriceInfo);
                }
            }
            else {
                Log.d("IAB", "ttsPrice " + "no SKU inventory");
            }


            Log.d("IAB", "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */
            Purchase ttsMonthly = inventory.getPurchase(SKU_MONTHLY_TTS);
            if (ttsMonthly != null) {
                mPurchasedInfiniteTTSSku = SKU_MONTHLY_TTS;
            }
            Purchase ttsYearly = inventory.getPurchase(SKU_YEARLY_TTS);
            if (ttsYearly != null) {
                mPurchasedInfiniteTTSSku = SKU_YEARLY_TTS;
            }
            mSubscribedToInfiniteTTS = (ttsMonthly != null && verifyDeveloperPayload(ttsMonthly))
                    || (ttsYearly != null && verifyDeveloperPayload(ttsYearly));
            Log.d("IAB", "User " + (mSubscribedToInfiniteTTS ? "HAS" : "DOES NOT HAVE")
                    + " infinite tts subscription.");

            if (mSubscribedToInfiniteTTS) mTTSMonthlyLimit = Integer.MAX_VALUE;

            Log.d("IAB", "Initial inventory query finished; enabling main UI.");
        }
    };
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("IAB", "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d("IAB", "Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Log.d("IAB", "Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d("IAB", "Purchase successful.");

            if (purchase.getSku().equals(SKU_MONTHLY_TTS) || purchase.getSku().equals(SKU_YEARLY_TTS)) {
                Log.d("IAB", "Infinite tts subscription purchased.");
                mSubscribedToInfiniteTTS = true;
                mTTSMonthlyLimit = Integer.MAX_VALUE;
                Log.d("IAB", "Limit is now " + mTTSMonthlyLimit);
            }
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String compiledKy = reverse(getBaseContext().getString(R.string.firstR))
                + getBaseContext().getString(R.string.middle)
                + reverse(getBaseContext().getString(R.string.lastR));

        mHelper = new IabHelper(this, compiledKy);

        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.d("Subscription", "InSetUpFinished: " + result);
                if (!result.isSuccess()) {
                    Log.d("Subscription", "Problem setting up In-app Billing: " + result);
                    return;
                }

                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(Subscription.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d("IAB", "Setup successful. Querying inventory.");
                try {
                    List<String> additionalSkuList = new ArrayList<String>();
                    additionalSkuList.add(SKU_MONTHLY_TTS);
                    additionalSkuList.add(SKU_YEARLY_TTS);
                    mHelper.queryInventoryAsync(true, null, additionalSkuList, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("IAB", "Error querying inventory. Another async operation in progress.");
                }
            }
        });

        Button monthlySubButton = (Button) findViewById(R.id.monthlySubButton);
        monthlySubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    Log.d("IAB","Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                try {
                    List<String> oldSku = null;
                    if (!TextUtils.isEmpty(mPurchasedInfiniteTTSSku)
                            && !mPurchasedInfiniteTTSSku.equals(SKU_MONTHLY_TTS)) {
                        // The user currently has a valid subscription, any purchase action is going to
                        // replace that subscription
                        oldSku = new ArrayList<String>();
                        oldSku.add(mPurchasedInfiniteTTSSku);
                    }
                    mHelper.launchPurchaseFlow(Subscription.this, SKU_MONTHLY_TTS, IabHelper.ITEM_TYPE_SUBS,
                            oldSku, 10001, mPurchaseFinishedListener, "");
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("IAB", e.getMessage());
                }
            }
        });

        Button yearlySubButton = (Button) findViewById(R.id.yearlySubButton);
        yearlySubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    Log.d("IAB","Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                try {
                    List<String> oldSku = null;
                    if (!TextUtils.isEmpty(mPurchasedInfiniteTTSSku)
                            && !mPurchasedInfiniteTTSSku.equals(SKU_YEARLY_TTS)) {
                        // The user currently has a valid subscription, any purchase action is going to
                        // replace that subscription
                        oldSku = new ArrayList<String>();
                        oldSku.add(mPurchasedInfiniteTTSSku);
                    }
                    mHelper.launchPurchaseFlow(Subscription.this, SKU_YEARLY_TTS, IabHelper.ITEM_TYPE_SUBS,
                            oldSku, 10001, mPurchaseFinishedListener, "");
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("IAB", e.getMessage());
                }
            }
        });
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        Log.d("IAB", "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("IAB", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d("IAB", "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d("IAB", "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.d("IAB", "Error querying inventory. Another async operation in progress.");
        }
    }
}
