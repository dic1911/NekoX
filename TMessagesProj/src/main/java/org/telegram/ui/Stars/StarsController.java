package org.telegram.ui.Stars;

import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.getCurrencyExpDivider;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;

// import com.android.billingclient.api.BillingClient;
// import com.android.billingclient.api.BillingFlowParams;
// import com.android.billingclient.api.ProductDetails;
// import com.android.billingclient.api.QueryProductDetailsParams;

import org.json.JSONObject;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BillingController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileRefController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;
// import org.telegram.ui.PaymentFormActivity;
import org.telegram.ui.bots.BotWebViewSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StarsController {

    public static final String currency = "XTR";

    private static volatile StarsController[] Instance = new StarsController[UserConfig.MAX_ACCOUNT_COUNT];
    private static final Object[] lockObjects = new Object[UserConfig.MAX_ACCOUNT_COUNT];
    static {
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            lockObjects[i] = new Object();
        }
    }

    public static StarsController getInstance(int num) {
        StarsController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (lockObjects[num]) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new StarsController(num);
                }
            }
        }
        return localInstance;
    }

    public final int currentAccount;

    private StarsController(int account) {
        currentAccount = account;
    }

    private long lastBalanceLoaded;
    private boolean balanceLoading, balanceLoaded;
    public long balance;
    public long getBalance() {
        return getBalance(null);
    }

    public long getBalance(Runnable loaded) {
        if ((!balanceLoaded || System.currentTimeMillis() - lastBalanceLoaded > 1000 * 60) && !balanceLoading) {
            balanceLoading = true;
            TLRPC.TL_payments_getStarsStatus req = new TLRPC.TL_payments_getStarsStatus();
            req.peer = new TLRPC.TL_inputPeerSelf();
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                boolean updatedTransactions = false;
                boolean updatedBalance = !balanceLoaded;
                lastBalanceLoaded = System.currentTimeMillis();
                if (res instanceof TLRPC.TL_payments_starsStatus) {
                    TLRPC.TL_payments_starsStatus r = (TLRPC.TL_payments_starsStatus) res;
                    MessagesController.getInstance(currentAccount).putUsers(r.users, false);
                    MessagesController.getInstance(currentAccount).putChats(r.chats, false);

                    if (transactions[ALL_TRANSACTIONS].isEmpty()) {
                        for (TLRPC.StarsTransaction t : r.history) {
                            transactions[ALL_TRANSACTIONS].add(t);
                            transactions[t.stars > 0 ? INCOMING_TRANSACTIONS : OUTGOING_TRANSACTIONS].add(t);
                        }
                        for (int i = 0; i < 3; ++i) {
                            transactionsExist[i] = !transactions[i].isEmpty() || transactionsExist[i];
                            endReached[i] = (r.flags & 1) == 0;
                            if (endReached[i]) {
                                loading[i] = false;
                            }
                            offset[i] = endReached[i] ? null : r.next_offset;
                        }
                        updatedTransactions = true;
                    }

                    if (this.balance != r.balance) {
                        updatedBalance = true;
                    }
                    this.balance = r.balance;
                }
                balanceLoading = false;
                balanceLoaded = true;
                if (updatedBalance) {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starBalanceUpdated);
                }
                if (updatedTransactions) {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starTransactionsLoaded);
                }

                if (loaded != null) {
                    loaded.run();
                }
            }));
        }
        return balance;
    }

    public void updateBalance(long balance) {
        if (this.balance != balance) {
            this.balance = balance;
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starBalanceUpdated);
        }
    }

    public boolean balanceAvailable() {
        return balanceLoaded;
    }

    private static boolean isCollapsed(long stars) {
        return (
            stars != 15 &&
            stars != 75 &&
            stars != 250 &&
            stars != 500 &&
            stars != 1000 &&
            stars != 2500
        );
    }

    private boolean optionsLoading, optionsLoaded;
    private ArrayList<TLRPC.TL_starsTopupOption> options;
    public ArrayList<TLRPC.TL_starsTopupOption> getOptionsCached() {
        return options;
    }

    public ArrayList<TLRPC.TL_starsTopupOption> getOptions() {
        // 030: fuck this shit
        if (options == null) options = new ArrayList<>();
        return options;
    }

    private void bulletinError(TLRPC.TL_error err, String str) {
        bulletinError(err == null ? str : err.text);
    }
    private void bulletinError(String err) {
        BaseFragment fragment = LaunchActivity.getLastFragment();
        BulletinFactory b = fragment != null && fragment.visibleDialog == null ? BulletinFactory.of(fragment) : BulletinFactory.global();
        b.createSimpleBulletin(R.raw.error, LocaleController.formatString(R.string.UnknownErrorCode, err)).show();
    }

    public static final int ALL_TRANSACTIONS = 0;
    public static final int INCOMING_TRANSACTIONS = 1;
    public static final int OUTGOING_TRANSACTIONS = 2;

    public final ArrayList<TLRPC.StarsTransaction>[] transactions = new ArrayList[] { new ArrayList<>(), new ArrayList<>(), new ArrayList<>() };
    public final boolean[] transactionsExist = new boolean[3];
    private final String[] offset = new String[3];
    private final boolean[] loading = new boolean[3];
    private final boolean[] endReached = new boolean[3];

    public void invalidateTransactions(boolean load) {
        for (int i = 0; i < 3; ++i) {
            if (loading[i]) continue;
            transactions[i].clear();
            offset[i] = null;
            loading[i] = false;
            endReached[i] = false;
            if (load)
                loadTransactions(i);
        }
    }

    public void preloadTransactions() {
        for (int i = 0; i < 3; ++i) {
            if (!loading[i] && !endReached[i] && offset[i] == null) {
                loadTransactions(i);
            }
        }
    }

    public void loadTransactions(int type) {
        if (loading[type] || endReached[type]) {
            return;
        }

        loading[type] = true;

        TLRPC.TL_payments_getStarsTransactions req = new TLRPC.TL_payments_getStarsTransactions();
        req.peer = new TLRPC.TL_inputPeerSelf();
        req.inbound = type == INCOMING_TRANSACTIONS;
        req.outbound = type == OUTGOING_TRANSACTIONS;
        req.offset = offset[type];
        if (req.offset == null) {
            req.offset = "";
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            loading[type] = false;
            if (res instanceof TLRPC.TL_payments_starsStatus) {
                TLRPC.TL_payments_starsStatus r = (TLRPC.TL_payments_starsStatus) res;
                MessagesController.getInstance(currentAccount).putUsers(r.users, false);
                MessagesController.getInstance(currentAccount).putChats(r.chats, false);

                transactions[type].addAll(r.history);
                transactionsExist[type] = !transactions[type].isEmpty() || transactionsExist[type];
                endReached[type] = (r.flags & 1) == 0;
                offset[type] = endReached[type] ? null : r.next_offset;

                updateBalance(r.balance);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starTransactionsLoaded);
            }
        }));
    }

    public boolean isLoadingTransactions(int type) {
        return loading[type];
    }

    public boolean didFullyLoadTransactions(int type) {
        return endReached[type];
    }

    public boolean hasTransactions() {
        return hasTransactions(ALL_TRANSACTIONS);
    }
    public boolean hasTransactions(int type) {
        return balanceAvailable() && !transactions[type].isEmpty();
    }

    public Theme.ResourcesProvider getResourceProvider() {
        BaseFragment lastFragment = LaunchActivity.getLastFragment();
        if (lastFragment != null) {
            return lastFragment.getResourceProvider();
        }
        return null;
    }

    public void buy(Activity activity, TLRPC.TL_starsTopupOption option, Utilities.Callback2<Boolean, String> whenDone) {
        // 030: fuck this shit
        showNoSupportDialog(activity, getResourceProvider());
    }

    public Runnable pay(MessageObject messageObject, Runnable whenShown) {
        // 030: fuck this shit
        return null;
    }

    private boolean paymentFormOpened;

    public void openPaymentForm(MessageObject messageObject, TLRPC.InputInvoice inputInvoice, TLRPC.TL_payments_paymentFormStars form, Runnable whenShown, Utilities.Callback<String> whenAllDone) {
    }

    private void showNoSupportDialog(Context context, Theme.ResourcesProvider resourcesProvider) {
        new AlertDialog.Builder(context, resourcesProvider)
            .setTitle(getString(R.string.StarsNotAvailableTitle))
            .setMessage(getString(R.string.StarsNotAvailableText))
            .setPositiveButton(getString(R.string.OK), null)
            .show();
    }

    private void payAfterConfirmed(MessageObject messageObject, TLRPC.InputInvoice inputInvoice, TLRPC.TL_payments_paymentFormStars form, Utilities.Callback<Boolean> whenDone) {
    }

    public void updateMediaPrice(MessageObject msg, long price, Runnable done) {
        updateMediaPrice(msg, price, done, false);
    }

    private void updateMediaPrice(MessageObject msg, long price, Runnable done, boolean afterFileRef) {
    }

}
