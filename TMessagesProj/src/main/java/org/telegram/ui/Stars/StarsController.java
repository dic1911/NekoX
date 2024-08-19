package org.telegram.ui.Stars;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import androidx.annotation.Nullable;

// import com.android.billingclient.api.BillingClient;
// import com.android.billingclient.api.BillingFlowParams;
// import com.android.billingclient.api.ProductDetails;
// import com.android.billingclient.api.QueryProductDetailsParams;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DialogObject;
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
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;
// import org.telegram.ui.PaymentFormActivity;
import org.telegram.ui.bots.BotWebViewSheet;

import java.util.ArrayList;
import java.util.Objects;

import tw.nekomimi.nekogram.NekoConfig;

public class StarsController {

    public static final String currency = "XTR";

    public static final int PERIOD_MONTHLY = 2592000;
    // test backend only:
    public static final int PERIOD_MINUTE = 60;
    public static final int PERIOD_5MINUTES = 300;

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
                boolean updatedSubscriptions = false;
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

                    if (subscriptions.isEmpty()) {
                        subscriptions.addAll(r.subscriptions);
                        subscriptionsLoading = false;
                        subscriptionsOffset = r.subscriptions_next_offset;
                        subscriptionsEndReached = (r.flags & 4) == 0;
                        updatedSubscriptions = true;
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
                if (updatedSubscriptions) {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starSubscriptionsLoaded);
                }

                if (loaded != null) {
                    loaded.run();
                }
            }));
        }
        return balance;
    }

    public void invalidateBalance() {
        balanceLoaded = false;
        getBalance();
        balanceLoaded = true;
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

    private boolean giftOptionsLoading, giftOptionsLoaded;
    private ArrayList<TLRPC.TL_starsGiftOption> giftOptions;
    public ArrayList<TLRPC.TL_starsGiftOption> getGiftOptionsCached() {
        return giftOptions;
    }
    public ArrayList<TLRPC.TL_starsGiftOption> getGiftOptions() {
        if (giftOptions == null) giftOptions = new ArrayList<>();
        return giftOptions;
    }

    private void bulletinError(TLRPC.TL_error err, String str) {
        bulletinError(err == null ? str : err.text);
    }
    private void bulletinError(String err) {
        BaseFragment fragment = LaunchActivity.getLastFragment();
        BulletinFactory b = fragment != null && fragment.visibleDialog == null ? BulletinFactory.of(fragment) : BulletinFactory.global();
        b.createSimpleBulletin(R.raw.error, formatString(R.string.UnknownErrorCode, err)).show();
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

    public final ArrayList<TLRPC.StarsSubscription> subscriptions = new ArrayList<>();
    public String subscriptionsOffset;
    public boolean subscriptionsLoading, subscriptionsEndReached;

    public boolean hasSubscriptions() {
        return balanceAvailable() && !subscriptions.isEmpty();
    }

    public void invalidateSubscriptions(boolean load) {
        if (subscriptionsLoading) return;
        subscriptions.clear();
        subscriptionsOffset = null;
        subscriptionsLoading = false;
        subscriptionsEndReached = false;
        if (load) loadSubscriptions();
    }

    public void loadSubscriptions() {
        if (subscriptionsLoading || subscriptionsEndReached) return;
        subscriptionsLoading = true;
        TLRPC.TL_getStarsSubscriptions req = new TLRPC.TL_getStarsSubscriptions();
        req.peer = new TLRPC.TL_inputPeerSelf();
        req.offset = subscriptionsOffset;
        if (req.offset == null) {
            req.offset = "";
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            subscriptionsLoading = false;
            if (res instanceof TLRPC.TL_payments_starsStatus) {
                TLRPC.TL_payments_starsStatus r = (TLRPC.TL_payments_starsStatus) res;
                MessagesController.getInstance(currentAccount).putUsers(r.users, false);
                MessagesController.getInstance(currentAccount).putChats(r.chats, false);

                subscriptions.addAll(r.subscriptions);
                subscriptionsEndReached = (r.flags & 4) == 0;
                subscriptionsOffset = r.subscriptions_next_offset;

                updateBalance(r.balance);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starSubscriptionsLoaded);
            }
        }));
    }
    public boolean isLoadingSubscriptions() {
        return subscriptionsLoading;
    }
    public boolean didFullyLoadSubscriptions() {
        return subscriptionsEndReached;
    }


    public final ArrayList<TLRPC.StarsSubscription> insufficientSubscriptions = new ArrayList<>();
    private boolean insufficientSubscriptionsLoading;
    public void loadInsufficientSubscriptions() {
        if (insufficientSubscriptionsLoading) return;
        insufficientSubscriptionsLoading = true;
        TLRPC.TL_getStarsSubscriptions req = new TLRPC.TL_getStarsSubscriptions();
        req.peer = new TLRPC.TL_inputPeerSelf();
        req.missing_balance = true;
        req.offset = "";
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            insufficientSubscriptionsLoading = false;
            if (res instanceof TLRPC.TL_payments_starsStatus) {
                TLRPC.TL_payments_starsStatus r = (TLRPC.TL_payments_starsStatus) res;
                MessagesController.getInstance(currentAccount).putUsers(r.users, false);
                MessagesController.getInstance(currentAccount).putChats(r.chats, false);
                insufficientSubscriptions.addAll(r.subscriptions);
                updateBalance(r.balance);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.starSubscriptionsLoaded);
            }
        }));
    }
    public void invalidateInsufficientSubscriptions(boolean load) {
        if (insufficientSubscriptionsLoading) return;
        insufficientSubscriptions.clear();
        insufficientSubscriptionsLoading = false;
        if (load) loadInsufficientSubscriptions();
    }
    public boolean hasInsufficientSubscriptions() {
        return !insufficientSubscriptions.isEmpty();
    }

    public Theme.ResourcesProvider getResourceProvider() {
        BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
        if (lastFragment != null) {
            return lastFragment.getResourceProvider();
        }
        return null;
    }

    public void showStarsTopup(Activity activity, long amount, String purpose) {
        if (!balanceAvailable()) {
            getBalance(() -> {
                showStarsTopupInternal(activity, amount, purpose);
            });
            return;
        }
        showStarsTopupInternal(activity, amount, purpose);
    }

    private void showStarsTopupInternal(Activity activity, long amount, String purpose) {
        if (getBalance() >= amount || amount <= 0) {
            BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
            if (lastFragment == null) return;
            BulletinFactory.of(lastFragment).createSimpleBulletin(R.raw.stars_topup, getString(R.string.StarsTopupLinkEnough), getString(R.string.StarsTopupLinkTopupAnyway), () -> {
                BaseFragment lastFragment2 = LaunchActivity.getSafeLastFragment();
                if (lastFragment2 == null) return;
                lastFragment2.presentFragment(new StarsIntroActivity());
            }).setDuration(Bulletin.DURATION_PROLONG).show(true);
            return;
        }
        new StarsIntroActivity.StarsNeededSheet(activity, null, amount, StarsIntroActivity.StarsNeededSheet.TYPE_LINK, purpose, () -> {

        }).show();
    }

    public void buy(Activity activity, TLRPC.TL_starsTopupOption option, Utilities.Callback2<Boolean, String> whenDone) {
        // 030: fuck this shit
        showNoSupportDialog(activity, getResourceProvider());
    }

    public void buyGift(Activity activity, TLRPC.TL_starsGiftOption option, long user_id, Utilities.Callback2<Boolean, String> whenDone) {
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

    public void subscribeTo(String hash, TLRPC.ChatInvite chatInvite, Utilities.Callback2<String, Long> whenAllDone) {
        if (chatInvite == null || chatInvite.subscription_pricing == null) return;

        final Context context = LaunchActivity.instance != null ? LaunchActivity.instance : ApplicationLoader.applicationContext;
        final Theme.ResourcesProvider resourcesProvider = getResourceProvider();
        final long stars = chatInvite.subscription_pricing.amount;

        if (context == null) return;

        final int currentAccount = UserConfig.selectedAccount;

        final boolean[] allDone = new boolean[] { false };
        StarsIntroActivity.openStarsChannelInviteSheet(context, resourcesProvider, currentAccount, chatInvite, whenDone -> {
            if (balance < stars) {
                if (!MessagesController.getInstance(currentAccount).starsPurchaseAvailable()) {
                    paymentFormOpened = false;
                    if (whenDone != null) {
                        whenDone.run(false);
                    }
                    if (!allDone[0] && whenAllDone != null) {
                        whenAllDone.run("cancelled", 0L);
                        allDone[0] = true;
                    }
                    showNoSupportDialog(context, resourcesProvider);
                    return;
                }
                final boolean[] purchased = new boolean[] { false };
                StarsIntroActivity.StarsNeededSheet sheet = new StarsIntroActivity.StarsNeededSheet(context, resourcesProvider, stars, StarsIntroActivity.StarsNeededSheet.TYPE_SUBSCRIPTION_BUY, chatInvite.title, () -> {
                    purchased[0] = true;
                    payAfterConfirmed(hash, chatInvite, (did, success) -> {
                        allDone[0] = true;
                        if (whenAllDone != null) {
                            whenAllDone.run(success ? "paid" : "failed", did);
                        }
                        if (whenDone != null) {
                            whenDone.run(true);
                        }
                    });
                });
                sheet.setOnDismissListener(d -> {
                    if (whenDone != null && !purchased[0]) {
                        whenDone.run(false);
                        paymentFormOpened = false;
                        if (!allDone[0] && whenAllDone != null) {
                            whenAllDone.run("cancelled", 0L);
                            allDone[0] = true;
                        }
                    }
                });
                sheet.show();
            } else {
                payAfterConfirmed(hash, chatInvite, (did, success) -> {
                    if (whenDone != null) {
                        whenDone.run(true);
                    }
                    allDone[0] = true;
                    if (whenAllDone != null) {
                        whenAllDone.run(success ? "paid" : "failed", did);
                    }
                });
            }
        }, () -> {
            paymentFormOpened = false;
            if (!allDone[0] && whenAllDone != null) {
                whenAllDone.run("cancelled", 0L);
                allDone[0] = true;
            }
        });
    }

    private void showNoSupportDialog(Context context, Theme.ResourcesProvider resourcesProvider) {
        new AlertDialog.Builder(context, resourcesProvider)
            .setTitle(getString(R.string.StarsNotAvailableTitle))
            .setMessage(getString(R.string.StarsNotAvailableText))
            .setPositiveButton(getString(R.string.OK), null)
            .show();
    }

    private void payAfterConfirmed(MessageObject messageObject, TLRPC.InputInvoice inputInvoice, TLRPC.TL_payments_paymentFormStars form, Utilities.Callback<Boolean> whenDone) {
        if (form == null) {
            return;
        }

        final Context context = ApplicationLoader.applicationContext;
        final Theme.ResourcesProvider resourcesProvider = getResourceProvider();

        if (context == null) {
            return;
        }

        long _stars = 0;
        for (TLRPC.TL_labeledPrice price : form.invoice.prices) {
            _stars += price.amount;
        }
        final long stars = _stars;
        final long dialogId;
        if (messageObject != null) {
            long did;
            if (messageObject.messageOwner != null && messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.from_id != null) {
                did = DialogObject.getPeerDialogId(messageObject.messageOwner.fwd_from.from_id);
            } else {
                did = messageObject.getDialogId();
            }
            if (did < 0 && messageObject.getFromChatId() > 0) {
                final TLRPC.User _user = MessagesController.getInstance(currentAccount).getUser(messageObject.getFromChatId());
                if (_user != null && _user.bot) {
                    did = _user.id;
                }
            }
            dialogId = did;
        } else {
            dialogId = form.bot_id;
        }
        final String bot;
        if (dialogId >= 0) {
            bot = UserObject.getUserName(MessagesController.getInstance(currentAccount).getUser(dialogId));
        } else {
            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialogId);
            bot = chat == null ? "" : chat.title;
        }
        final String product = form.title;

        TLRPC.TL_payments_sendStarsForm req2 = new TLRPC.TL_payments_sendStarsForm();
        req2.form_id = form.form_id;
        req2.invoice = inputInvoice;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req2, (res2, err2) -> AndroidUtilities.runOnUIThread(() -> {
            paymentFormOpened = false;
            BaseFragment fragment = LaunchActivity.getLastFragment();
            BulletinFactory b = fragment != null && fragment.visibleDialog == null ? BulletinFactory.of(fragment) : BulletinFactory.global();
            if (res2 instanceof TLRPC.TL_payments_paymentResult) {
                if (whenDone != null) {
                    whenDone.run(true);
                }

                TLRPC.TL_payments_paymentResult result = (TLRPC.TL_payments_paymentResult) res2;
                MessagesController.getInstance(currentAccount).processUpdates(result.updates, false);

                final boolean media = messageObject != null && messageObject.messageOwner != null && messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPaidMedia;
                if (media) {
                    Drawable starDrawable = context.getResources().getDrawable(R.drawable.star_small_inner).mutate();
                    b.createSimpleBulletin(starDrawable, getString(R.string.StarsMediaPurchaseCompleted), AndroidUtilities.replaceTags(formatPluralString("StarsMediaPurchaseCompletedInfo", (int) stars, bot))).show();
                } else {
                    b.createSimpleBulletin(R.raw.stars_send, getString(R.string.StarsPurchaseCompleted), AndroidUtilities.replaceTags(formatPluralString("StarsPurchaseCompletedInfo", (int) stars, product, bot))).show();
                }
                if (LaunchActivity.instance != null && LaunchActivity.instance.getFireworksOverlay() != null) {
                    LaunchActivity.instance.getFireworksOverlay().start(true);
                }

                final boolean isStarsGift = inputInvoice instanceof TLRPC.TL_inputInvoiceStars && ((TLRPC.TL_inputInvoiceStars) inputInvoice).purpose instanceof TLRPC.TL_inputStorePaymentStarsGift;
                if (!isStarsGift) {
                    invalidateTransactions(true);
                }

                if (messageObject != null) {
                    TLRPC.TL_messages_getExtendedMedia req = new TLRPC.TL_messages_getExtendedMedia();
                    req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialogId);
                    req.id.add(messageObject.getId());
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, null);
                }
            } else if (err2 != null && "BALANCE_TOO_LOW".equals(err2.text)) {
                if (!MessagesController.getInstance(currentAccount).starsPurchaseAvailable()) {
                    if (whenDone != null) {
                        whenDone.run(false);
                    }
                    showNoSupportDialog(context, resourcesProvider);
                    return;
                }
                final boolean[] purchased = new boolean[] { false };
                StarsIntroActivity.StarsNeededSheet sheet = new StarsIntroActivity.StarsNeededSheet(context, resourcesProvider, stars, StarsIntroActivity.StarsNeededSheet.TYPE_BOT, bot, () -> {
                    purchased[0] = true;
                    payAfterConfirmed(messageObject, inputInvoice, form, success -> {
                        if (whenDone != null) {
                            whenDone.run(success);
                        }
                    });
                });
                sheet.setOnDismissListener(d -> {
                    if (whenDone != null && !purchased[0]) {
                        whenDone.run(false);
                    }
                });
                sheet.show();
            } else if (err2 != null && "FORM_EXPIRED".equals(err2.text)) {
                TLRPC.TL_payments_getPaymentForm req = new TLRPC.TL_payments_getPaymentForm();
                final JSONObject themeParams = BotWebViewSheet.makeThemeParams(resourcesProvider);
                if (themeParams != null) {
                    req.theme_params = new TLRPC.TL_dataJSON();
                    req.theme_params.data = themeParams.toString();
                    req.flags |= 1;
                }
                req.invoice = inputInvoice;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res3, err3) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res3 instanceof TLRPC.TL_payments_paymentFormStars) {
                        payAfterConfirmed(messageObject, inputInvoice, (TLRPC.TL_payments_paymentFormStars) res3, whenDone);
                    } else {
                        if (whenDone != null) {
                            whenDone.run(false);
                        }
                        b.createSimpleBulletin(R.raw.error, formatString(R.string.UnknownErrorCode, err3 != null ? err3.text : "FAILED_GETTING_FORM")).show();
                    }
                }));
            } else {
                if (whenDone != null) {
                    whenDone.run(false);
                }
                b.createSimpleBulletin(R.raw.error, formatString(R.string.UnknownErrorCode, err2 != null ? err2.text : "FAILED_SEND_STARS")).show();

                if (messageObject != null) {
                    TLRPC.TL_messages_getExtendedMedia req = new TLRPC.TL_messages_getExtendedMedia();
                    req.peer = MessagesController.getInstance(currentAccount).getInputPeer(dialogId);
                    req.id.add(messageObject.getId());
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, null);
                }
            }
        }));
    }

    private void payAfterConfirmed(String hash, TLRPC.ChatInvite chatInvite, Utilities.Callback2<Long, Boolean> whenDone) {
        if (chatInvite == null || chatInvite.subscription_pricing == null) {
            return;
        }

        final Context context = ApplicationLoader.applicationContext;
        final Theme.ResourcesProvider resourcesProvider = getResourceProvider();

        if (context == null) {
            return;
        }

        final long stars = chatInvite.subscription_pricing.amount;
        final String channel = chatInvite.title;

        TLRPC.TL_inputInvoiceChatInviteSubscription inputInvoice = new TLRPC.TL_inputInvoiceChatInviteSubscription();
        inputInvoice.hash = hash;

        TLRPC.TL_payments_sendStarsForm req2 = new TLRPC.TL_payments_sendStarsForm();
        req2.form_id = chatInvite.subscription_form_id;
        req2.invoice = inputInvoice;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req2, (res2, err2) -> AndroidUtilities.runOnUIThread(() -> {
            paymentFormOpened = false;
            BaseFragment fragment = LaunchActivity.getLastFragment();
            BulletinFactory b = !AndroidUtilities.hasDialogOnTop(fragment) ? BulletinFactory.of(fragment) : BulletinFactory.global();
            if (res2 instanceof TLRPC.TL_payments_paymentResult) {
                TLRPC.TL_payments_paymentResult result = (TLRPC.TL_payments_paymentResult) res2;
                MessagesController.getInstance(currentAccount).processUpdates(result.updates, false);

                long dialogId = 0;
                if (result.updates.update instanceof TLRPC.TL_updateChannel) {
                    TLRPC.TL_updateChannel upd = (TLRPC.TL_updateChannel) result.updates.update;
                    dialogId = -upd.channel_id;
                }
                if (result.updates.updates != null) {
                    for (int i = 0; i < result.updates.updates.size(); ++i) {
                        if (result.updates.updates.get(i) instanceof TLRPC.TL_updateChannel) {
                            TLRPC.TL_updateChannel upd = (TLRPC.TL_updateChannel) result.updates.updates.get(i);
                            dialogId = -upd.channel_id;
                        }
                    }
                }

                if (whenDone != null) {
                    whenDone.run(dialogId, true);
                }

                if (dialogId == 0) {
                    b.createSimpleBulletin(R.raw.stars_send, getString(R.string.StarsSubscriptionCompleted), AndroidUtilities.replaceTags(formatPluralString("StarsSubscriptionCompletedText", (int) stars, channel))).show();
                }
                if (LaunchActivity.instance != null && LaunchActivity.instance.getFireworksOverlay() != null) {
                    LaunchActivity.instance.getFireworksOverlay().start(true);
                }

                invalidateTransactions(true);
                invalidateSubscriptions(true);
            } else if (err2 != null && "BALANCE_TOO_LOW".equals(err2.text)) {
                if (!MessagesController.getInstance(currentAccount).starsPurchaseAvailable()) {
                    if (whenDone != null) {
                        whenDone.run(0L, false);
                    }
                    showNoSupportDialog(context, resourcesProvider);
                    return;
                }
                final boolean[] purchased = new boolean[] { false };
                StarsIntroActivity.StarsNeededSheet sheet = new StarsIntroActivity.StarsNeededSheet(context, resourcesProvider, stars, StarsIntroActivity.StarsNeededSheet.TYPE_SUBSCRIPTION_BUY, chatInvite.title, () -> {
                    purchased[0] = true;
                    payAfterConfirmed(hash, chatInvite, (did, success) -> {
                        if (whenDone != null) {
                            whenDone.run(did, success);
                        }
                    });
                });
                sheet.setOnDismissListener(d -> {
                    if (whenDone != null && !purchased[0]) {
                        whenDone.run(0L, false);
                    }
                });
                sheet.show();
            } else {
                if (whenDone != null) {
                    whenDone.run(0L, false);
                }
                b.createSimpleBulletin(R.raw.error, formatString(R.string.UnknownErrorCode, err2 != null ? err2.text : "FAILED_SEND_STARS")).show();
            }
        }));
    }

    public void updateMediaPrice(MessageObject msg, long price, Runnable done) {
        updateMediaPrice(msg, price, done, false);
    }

    private void updateMediaPrice(MessageObject msg, long price, Runnable done, boolean afterFileRef) {
    }

    public static final long REACTIONS_TIMEOUT = 5_000;
    public PendingPaidReactions currentPendingReactions;

    public static class MessageId {
        public long did;
        public int mid;
        private MessageId(long did, int mid) {
            this.did = did;
            this.mid = mid;
        }
        public static MessageId from(long did, int mid) {
            return new MessageId(did, mid);
        }
        public static MessageId from(MessageObject msg) {
            if (msg.messageOwner.isThreadMessage && msg.messageOwner.fwd_from != null) {
                return new MessageId(msg.getFromChatId(), msg.messageOwner.fwd_from.saved_from_msg_id);
            } else {
                return new MessageId(msg.getDialogId(), msg.getId());
            }
        }
        @Override
        public int hashCode() {
            return Objects.hash(did, mid);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof MessageId) {
                MessageId id = (MessageId) obj;
                return id.did == did && id.mid == mid;
            }
            return false;
        }
    }

    public boolean arePaidReactionsAnonymous(MessageObject messageObject) {
        if (currentPendingReactions != null && currentPendingReactions.message.equals(MessageId.from(messageObject)) && currentPendingReactions.anonymous != null) {
            return currentPendingReactions.anonymous;
        }
        Boolean messageSettings = messageObject == null ? null : messageObject.isMyPaidReactionAnonymous();
        if (messageSettings != null) {
            return messageSettings;
        }
        final long did = messageObject == null ? 0 : messageObject.getDialogId();
        final SharedPreferences prefs = MessagesController.getInstance(currentAccount).getMainSettings();
        if (prefs.contains("anon_react_" + did)) {
            return prefs.getBoolean("anon_react_" + did, false);
        }
        if (prefs.contains("anon_react_" + 0)) {
            return prefs.getBoolean("anon_react_" + 0, false);
        }
        return false;
    }

    public boolean arePaidReactionsAnonymous(MessageId id, TLRPC.MessageReactions reactions) {
        if (currentPendingReactions != null && currentPendingReactions.message.equals(id) && currentPendingReactions.anonymous != null) {
            return currentPendingReactions.anonymous;
        }
        Boolean messageSettings = MessageObject.isMyPaidReactionAnonymous(reactions);
        if (messageSettings != null) {
            return messageSettings;
        }
        final SharedPreferences prefs = MessagesController.getInstance(currentAccount).getMainSettings();
        if (prefs.contains("anon_react_" + id.did)) {
            return prefs.getBoolean("anon_react_" + id.did, false);
        }
        if (prefs.contains("anon_react_" + 0)) {
            return prefs.getBoolean("anon_react_" + 0, false);
        }
        return false;
    }

    public void saveAnonymous(MessageObject messageObject, boolean value) {
        final SharedPreferences prefs = MessagesController.getInstance(currentAccount).getMainSettings();
        prefs.edit().putBoolean("anon_react_" + (messageObject == null ? 0 : messageObject.getDialogId()), value).putBoolean("anon_react_0", value).apply();
    }

    public class PendingPaidReactions {

        public MessageId message;
        public MessageObject messageObject;
        public long random_id;
        public ChatActivity chatActivity;
        public Bulletin bulletin;
        public Bulletin.TwoLineAnimatedLottieLayout bulletinLayout;
        public Bulletin.UndoButton bulletinButton;
        public Bulletin.TimerView timerView;

        public boolean wasChosen;

        public long amount;
        public long lastTime;
        public boolean committed = false;
        public boolean cancelled = false;

        public long not_added;
        public boolean applied;

        public Boolean anonymous = null;
        public boolean isAnonymous() {
            if (anonymous != null) return anonymous;
            return arePaidReactionsAnonymous(messageObject);
        }

        private void saveAnonymous() {
            StarsController.this.saveAnonymous(messageObject, isAnonymous());
        }

        public StarReactionsOverlay overlay;
        public void setOverlay(StarReactionsOverlay overlay) {
            this.overlay = overlay;
        }

        public PendingPaidReactions(
            MessageId message,
            MessageObject messageObject,
            ChatActivity chatActivity,
            long currentTime,
            boolean affect
        ) {
            this.message = message;
            this.messageObject = messageObject;
            this.random_id = Utilities.random.nextLong() & 0xFFFFFFFFL | (currentTime << 32);
            this.chatActivity = chatActivity;

            final Context context = getContext(chatActivity);
            bulletinLayout = new Bulletin.TwoLineAnimatedLottieLayout(context, chatActivity.themeDelegate);
            bulletinLayout.setAnimation(R.raw.stars_topup);
            bulletinLayout.titleTextView.setText(LocaleController.getString(R.string.StarsSentTitle));
            bulletinButton = new Bulletin.UndoButton(context, true, false, chatActivity.themeDelegate);
            bulletinButton.setText(LocaleController.getString(R.string.StarsSentUndo));
            bulletinButton.setUndoAction(this::cancel);
            timerView = new Bulletin.TimerView(context, chatActivity.themeDelegate);
            timerView.timeLeft = REACTIONS_TIMEOUT;
            timerView.setColor(Theme.getColor(Theme.key_undo_cancelColor, chatActivity.themeDelegate));
            bulletinButton.addView(timerView, LayoutHelper.createFrame(20, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 12, 0));
            bulletinButton.undoTextView.setPadding(dp(12), dp(8), dp(20 + 10), dp(8));
            bulletinLayout.setButton(bulletinButton);
            bulletin = BulletinFactory.of(chatActivity).create(bulletinLayout, -1);
            bulletin.hideAfterBottomSheet = false;
            if (affect) bulletin.show(true);
            bulletin.setOnHideListener(closeRunnable);

            this.amount = 0;
            this.lastTime = System.currentTimeMillis();

            wasChosen = messageObject.isPaidReactionChosen();
        }

        public void add(long amount, boolean affect) {
            if (committed || cancelled) {
                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    throw new RuntimeException("adding more amount to committed reactions");
                } else {
                    return;
                }
            }
            this.amount += amount;
            this.lastTime = System.currentTimeMillis();

            bulletinLayout.subtitleTextView.cancelAnimation();
            bulletinLayout.subtitleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatPluralString("StarsSentText", (int) this.amount)), true);

            timerView.timeLeft = REACTIONS_TIMEOUT;
            AndroidUtilities.cancelRunOnUIThread(closeRunnable);
            AndroidUtilities.runOnUIThread(closeRunnable, REACTIONS_TIMEOUT);

            if (affect) {
                applied = true;
                messageObject.addPaidReactions((int) +amount, true, isAnonymous());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);
            } else {
                applied = false;
                if (messageObject.ensurePaidReactionsExist(true)) {
                    not_added--;
                }
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);
                not_added += amount;
            }
        }

        public void apply() {
            if (applied) return;
            applied = true;

            timerView.timeLeft = REACTIONS_TIMEOUT;
            AndroidUtilities.cancelRunOnUIThread(closeRunnable);
            AndroidUtilities.runOnUIThread(closeRunnable, REACTIONS_TIMEOUT);

            messageObject.addPaidReactions((int) +not_added, true, isAnonymous());
            not_added = 0;
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);
            bulletin.show(true);
            bulletin.setOnHideListener(closeRunnable);
        }

        public final Runnable closeRunnable = this::close;
        public void close() {
            AndroidUtilities.cancelRunOnUIThread(closeRunnable);

            if (applied) {
                commit();
            } else {
                cancelled = true;
                messageObject.addPaidReactions((int) -amount, wasChosen, isAnonymous());
            }
            bulletin.hide();
            if (overlay != null && overlay.isShowing(messageObject)) {
                overlay.hide();
            }

            if (currentPendingReactions == this) {
                currentPendingReactions = null;
            }
        }

        public final Runnable cancelRunnable = this::cancel;
        public void cancel() {
            AndroidUtilities.cancelRunOnUIThread(closeRunnable);

            cancelled = true;
            bulletin.hide();
            if (overlay != null) {
                overlay.hide();
            }

            messageObject.addPaidReactions((int) -amount, wasChosen, isAnonymous());
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);

            if (currentPendingReactions == this) {
                currentPendingReactions = null;
            }
        }

        public void commit() {
            if (committed || cancelled) {
                return;
            }

            final StarsController starsController = StarsController.getInstance(currentAccount);
            final MessagesController messagesController = MessagesController.getInstance(currentAccount);
            final ConnectionsManager connectionsManager = ConnectionsManager.getInstance(currentAccount);

            final long totalStars = amount;
            if (starsController.balanceAvailable() && starsController.getBalance() < totalStars) {
                if (NekoConfig.removePremiumAnnoyance.Bool()) {
                    BulletinFactory.of(chatActivity).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.NoStarsForReaction)).show(true);
                    return;
                }
                cancelled = true;

                messageObject.addPaidReactions((int) -amount, wasChosen, isAnonymous());
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);

                String name;
                if (message.did >= 0) {
                    TLRPC.User user = chatActivity.getMessagesController().getUser(message.did);
                    name = UserObject.getForcedFirstName(user);
                } else {
                    TLRPC.Chat chat = chatActivity.getMessagesController().getChat(-message.did);
                    name = chat == null ? "" : chat.title;
                }
                Context context = chatActivity.getContext();
                if (context == null) context = LaunchActivity.instance;
                if (context == null) context = ApplicationLoader.applicationContext;
                new StarsIntroActivity.StarsNeededSheet(context, chatActivity.getResourceProvider(), totalStars, StarsIntroActivity.StarsNeededSheet.TYPE_REACTIONS, name, () -> {
                    sendPaidReaction(messageObject, chatActivity, totalStars, true, true, anonymous);
                }).show();

                return;
            }

            committed = true;

            TLRPC.TL_messages_sendPaidReaction req = new TLRPC.TL_messages_sendPaidReaction();
            req.peer = messagesController.getInputPeer(message.did);
            req.msg_id = message.mid;
            req.random_id = random_id;
            req.count = (int) amount;
            req.isPrivate = isAnonymous();
            saveAnonymous();

            connectionsManager.sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                if (response != null) {
                    messagesController.processUpdates((TLRPC.Updates) response, false);
                } else if (error != null) {
                    messageObject.addPaidReactions((int) -amount, wasChosen, isAnonymous());
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didUpdateReactions, messageObject.getDialogId(), messageObject.getId(), messageObject.messageOwner.reactions);

                    if ("BALANCE_TOO_LOW".equals(error.text)) {
                        if (NekoConfig.removePremiumAnnoyance.Bool()) {
                            BulletinFactory.of(chatActivity).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.NoStarsForReaction)).show(true);
                            return;
                        }
                        String name;
                        if (message.did >= 0) {
                            TLRPC.User user = chatActivity.getMessagesController().getUser(message.did);
                            name = UserObject.getForcedFirstName(user);
                        } else {
                            TLRPC.Chat chat = chatActivity.getMessagesController().getChat(-message.did);
                            name = chat == null ? "" : chat.title;
                        }
                        Context context = chatActivity.getContext();
                        if (context == null) context = LaunchActivity.instance;
                        if (context == null) context = ApplicationLoader.applicationContext;
                        new StarsIntroActivity.StarsNeededSheet(context, chatActivity.getResourceProvider(), totalStars, StarsIntroActivity.StarsNeededSheet.TYPE_REACTIONS, name, () -> {
                            sendPaidReaction(messageObject, chatActivity, totalStars, true, true, anonymous);
                        }).show();
                    }

                    invalidateTransactions(false);
                    invalidateBalance();
                }
            }));
        }
    }

    public StarsController.PendingPaidReactions sendPaidReaction(MessageObject messageObject, ChatActivity chatActivity) {
        return sendPaidReaction(messageObject, chatActivity, +1, true, true, null);
    }

    public Context getContext(ChatActivity chatActivity) {
        if (chatActivity != null && chatActivity.getContext() != null)
            return chatActivity.getContext();
        if (LaunchActivity.instance != null && !LaunchActivity.instance.isFinishing())
            return LaunchActivity.instance;
        if (ApplicationLoader.applicationContext != null)
            return ApplicationLoader.applicationContext;
        return null;
    }

    public StarsController.PendingPaidReactions sendPaidReaction(
        MessageObject messageObject,
        ChatActivity chatActivity,
        long amount,
        boolean affect,
        boolean checkBalance,
        Boolean anonymous
    ) {
        final MessageId key = MessageId.from(messageObject);
        final StarsController s = StarsController.getInstance(currentAccount);
        final long totalStars = amount;
        final Context context = getContext(chatActivity);
        if (context == null) return null;
        if (checkBalance && s.balanceAvailable() && s.getBalance() <= 0) {
            if (NekoConfig.removePremiumAnnoyance.Bool()) {
                currentPendingReactions = new PendingPaidReactions(key, messageObject, chatActivity, ConnectionsManager.getInstance(currentAccount).getCurrentTime(), affect);;
                currentPendingReactions.add(amount, affect);
                currentPendingReactions.anonymous = true;
                return currentPendingReactions;
            }
            final long dialogId = chatActivity.getDialogId();
            String name;
            if (dialogId >= 0) {
                TLRPC.User user = chatActivity.getMessagesController().getUser(dialogId);
                name = UserObject.getForcedFirstName(user);
            } else {
                TLRPC.Chat chat = chatActivity.getMessagesController().getChat(-dialogId);
                name = chat == null ? "" : chat.title;
            }
            new StarsIntroActivity.StarsNeededSheet(chatActivity.getContext(), chatActivity.getResourceProvider(), totalStars, StarsIntroActivity.StarsNeededSheet.TYPE_REACTIONS, name, () -> {
                sendPaidReaction(messageObject, chatActivity, totalStars, true, true, anonymous);
            }).show();
            return null;
        }
        if (messageObject != null && !messageObject.doesPaidReactionExist()) {
            affect = true;
        }
        if (currentPendingReactions == null || !currentPendingReactions.message.equals(key)) {
            if (currentPendingReactions != null) {
                currentPendingReactions.close();
            }
            currentPendingReactions = new PendingPaidReactions(key, messageObject, chatActivity, ConnectionsManager.getInstance(currentAccount).getCurrentTime(), affect);
            currentPendingReactions.anonymous = anonymous;
        }
        if (currentPendingReactions.amount + amount > MessagesController.getInstance(currentAccount).starsPaidReactionAmountMax) {
            currentPendingReactions.close();
            currentPendingReactions = new PendingPaidReactions(key, messageObject, chatActivity, ConnectionsManager.getInstance(currentAccount).getCurrentTime(), affect);
        }
        final long totalStars2 = currentPendingReactions.amount + amount;
        if (checkBalance && s.balanceAvailable() && s.getBalance() < totalStars2) {
            currentPendingReactions.cancel();
            final long dialogId = chatActivity.getDialogId();
            String name;
            if (dialogId >= 0) {
                TLRPC.User user = chatActivity.getMessagesController().getUser(dialogId);
                name = UserObject.getForcedFirstName(user);
            } else {
                TLRPC.Chat chat = chatActivity.getMessagesController().getChat(-dialogId);
                name = chat == null ? "" : chat.title;
            }
            new StarsIntroActivity.StarsNeededSheet(context, chatActivity.getResourceProvider(), totalStars2, StarsIntroActivity.StarsNeededSheet.TYPE_REACTIONS, name, () -> {
                sendPaidReaction(messageObject, chatActivity, totalStars2, true, true, anonymous);
            }).show();
            return null;
        }
        currentPendingReactions.add(amount, affect);
        currentPendingReactions.anonymous = anonymous;
        return currentPendingReactions;
    }

    public void undoPaidReaction() {
        if (currentPendingReactions != null) {
            currentPendingReactions.cancel();
        }
    }

    public void commitPaidReaction() {
        if (currentPendingReactions != null) {
            currentPendingReactions.close();
        }
    }

    public boolean hasPendingPaidReactions(MessageObject messageObject) {
        if (currentPendingReactions == null) return false;
        if (messageObject == null) return false;
        final MessageId key = MessageId.from(messageObject);
        if (currentPendingReactions.message.did != key.did || currentPendingReactions.message.mid != key.mid) return false;
        if (!currentPendingReactions.applied) return false;
        return true;
    }

    public long getPendingPaidReactions(MessageObject messageObject) {
        if (messageObject == null || messageObject.messageOwner == null) return 0;
        if (messageObject.messageOwner.isThreadMessage && messageObject.messageOwner.fwd_from != null) {
            return getPendingPaidReactions(messageObject.getFromChatId(), messageObject.messageOwner.fwd_from.saved_from_msg_id);
        } else {
            return getPendingPaidReactions(messageObject.getDialogId(), messageObject.getId());
        }
    }

    public long getPendingPaidReactions(long dialogId, int messageId) {
        if (currentPendingReactions == null) return 0;
        if (currentPendingReactions.message.did != dialogId || currentPendingReactions.message.mid != messageId) return 0;
        if (!currentPendingReactions.applied) return 0;
        return currentPendingReactions.amount;
    }
}
