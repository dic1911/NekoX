package org.telegram.messenger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.util.Pair;


import org.json.JSONObject;
import org.telegram.messenger.utils.BillingUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.PremiumPreviewFragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BillingController {

    private static BillingController instance;
    private String lastPremiumTransaction = "";
    private String lastPremiumToken = "";
    public static boolean billingClientEmpty;

    public static BillingController getInstance() {
        if (instance == null) {
            instance = new BillingController(ApplicationLoader.applicationContext);
        }
        return instance;
    }

    private BillingController(Context ctx) {

    }

    public void setOnCanceled(Runnable onCanceled) {
    }

    public String getLastPremiumTransaction() {
        return lastPremiumTransaction;
    }

    public String getLastPremiumToken() {
        return lastPremiumToken;
    }

    public String formatCurrency(long amount, String currency) {
        return formatCurrency(amount, currency, getCurrencyExp(currency));
    }

    public String formatCurrency(long amount, String currency, int exp) {
        return formatCurrency(amount, currency, exp, false);
    }

    public String formatCurrency(long amount, String currency, int exp, boolean rounded) {
        if (currency == null || currency.isEmpty()) {
            return String.valueOf(amount);
        }
        Currency cur = Currency.getInstance(currency);
        if (cur != null) {
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
            numberFormat.setCurrency(cur);
            if (rounded) {
                return numberFormat.format(Math.round(amount / Math.pow(10, exp)));
            }
            return numberFormat.format(amount / Math.pow(10, exp));
        }
        return amount + " " + currency;
    }

    public int getCurrencyExp(String currency) {
        return 0;
    }

    public void startConnection() {
    }

    private void switchToInvoice() {
        if (billingClientEmpty) {
            return;
        }
        billingClientEmpty = true;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.billingProductDetailsUpdated);
    }

    private void parseCurrencies(JSONObject obj) {
    }

    public boolean startManageSubscription(Context ctx, String productId) {
        return false;
    }

    public boolean isReady() {
        return false;
    }
}
