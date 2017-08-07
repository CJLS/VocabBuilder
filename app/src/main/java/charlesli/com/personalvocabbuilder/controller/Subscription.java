package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
    String SKU_TTS = "tts";

    // Does the user have an active subscription to the infinite tts plan?
    boolean mSubscribedToInfiniteTTS = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d("IAB", "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.d("IAB", "Failed to query inventory: " + result);
                return;
            }

            if (inventory.getSkuDetails(SKU_TTS) != null) {
                String ttsPrice =
                        inventory.getSkuDetails(SKU_TTS).getPrice();

                Log.d("IAB", "ttsPrice " + ttsPrice);
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


            // First find out which subscription is auto renewing
            Purchase ttsMonthly = inventory.getPurchase(SKU_TTS);
            if (ttsMonthly != null && ttsMonthly.isAutoRenewing()) {
                mAutoRenewEnabled = true;
            } else {
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteTTS = (ttsMonthly != null && verifyDeveloperPayload(ttsMonthly));
            Log.d("IAB", "User " + (mSubscribedToInfiniteTTS ? "HAS" : "DOES NOT HAVE")
                    + " infinite tts subscription.");

            //if (mSubscribedToInfiniteTTS) mTank = TANK_MAX;

            //updateUi();
            //setWaitScreen(false);
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

            if (purchase.getSku().equals(SKU_TTS)) {
                // bought the infinite tts subscription
                Log.d("IAB", "Infinite tts subscription purchased.");
                mSubscribedToInfiniteTTS = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
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
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("IAB", "Error querying inventory. Another async operation in progress.");
                }
                /*
                else {
                    Log.d("Subscription", "Success setting up In-app Billing: " + result);
                    List<String> additionalSkuList = new ArrayList<String>();
                    additionalSkuList.add(SKU_TTS);
                    try {
                        mHelper.queryInventoryAsync(true, null, additionalSkuList, mQueryFinishedListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        Toast.makeText(Subscription.this, "Sorry something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
                */
            }
        });

        Button monthlySubButton = (Button) findViewById(R.id.yearlySubButtons);
        monthlySubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHelper.subscriptionsSupported()) {
                    Log.d("IAB","Subscriptions not supported on your device yet. Sorry!");
                    return;
                }
                try {
                    mHelper.launchPurchaseFlow(Subscription.this, SKU_TTS, 10001, mPurchaseFinishedListener, "");
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d("IAB", e.getMessage());
                }
            }
        });



    }

    // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
    // flow for subscription.

    /*
    public void onInfiniteGasButtonClicked(View arg0) {
        if (!mHelper.subscriptionsSupported()) {
            Log.d("IAB","Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        CharSequence[] options = new CharSequence[1];
        options[0] = "Unlimited TTS";
        if (!mSubscribedToInfiniteTTS || !mAutoRenewEnabled) {
            mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
        }

        int titleResId;
        if (!mSubscribedToInfiniteTTS) {
            titleResId = R.string.subscription_period_prompt;
        } else if (!mAutoRenewEnabled) {
            titleResId = R.string.subscription_resignup_prompt;
        } else {
            titleResId = R.string.subscription_update_prompt;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId)
                .setSingleChoiceItems(options, 0, this)
                .setPositiveButton(R.string.subscription_prompt_continue, this)
                .setNegativeButton(R.string.subscription_prompt_cancel, this);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    */

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
