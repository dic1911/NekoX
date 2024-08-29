package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLObject;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CollapseTextCell;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import tw.nekomimi.nekogram.NekoConfig;

public class BlacklistUrlQueryBottomSheet extends BottomSheetWithRecyclerListView {
    private UniversalAdapter adapter;

    final private SelectorBtnCell buttonContainer;
    final private TextView actionButton;

    private String url = null;

    private boolean[] checks;

    private ArrayList<Action> queries = new ArrayList<>();

    private class Action {
        int id;
        String query;

        ArrayList<TLObject> options;
        boolean[] checks;
        boolean[] filter;
        boolean collapsed;
        int totalCount;
        int filteredCount;
        int selectedCount;

        Action(int id, String query) {
            this.id = id;
            this.query = query;
        }

        int getCount() {
            if (filter != null) {
                return filteredCount;
            } else {
                return totalCount;
            }
        }

        boolean isExpandable() {
            return getCount() > 1;
        }

        void collapseOrExpand() {
            collapsed = !collapsed;
            adapter.update(true);
        }

        void forEach(Utilities.IndexedConsumer<TLObject> action) {
            for (int i = 0; i < totalCount; i++) {
                if (filter == null || filter[i]) {
                    action.accept(options.get(i), i);
                }
            }
        }
    }

    public BlacklistUrlQueryBottomSheet(BaseFragment fragment, String url) {
        super(fragment.getContext(), fragment, false, false, false, true, ActionBarType.SLIDING, fragment.getResourceProvider());
        this.url = url;
        setShowHandle(true);
        fixNavigationBar();
        this.takeTranslationIntoAccount = true;
        recyclerListView.setPadding(backgroundPaddingLeft, headerTotalHeight, backgroundPaddingLeft, dp(68));
        recyclerListView.setOnItemClickListener((view, position, x, y) -> {
            UItem item = adapter.getItem(position - 1);
            if (item == null) return;
            onClick(item, view, position, x, y);
        });
        this.takeTranslationIntoAccount = true;
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                containerView.invalidate();
            }
        };
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        recyclerListView.setItemAnimator(itemAnimator);

        buttonContainer = new SelectorBtnCell(getContext(), resourcesProvider, null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));

        actionButton = new TextView(getContext());
        actionButton.setLines(1);
        actionButton.setSingleLine(true);
        actionButton.setGravity(Gravity.CENTER_HORIZONTAL);
        actionButton.setEllipsize(TextUtils.TruncateAt.END);
        actionButton.setGravity(Gravity.CENTER);
        actionButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        actionButton.setTypeface(AndroidUtilities.bold());
        actionButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        actionButton.setText(getString(R.string.Blacklist));
        actionButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        actionButton.setOnClickListener(e -> proceed());
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        containerView.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, backgroundPaddingLeft, 0, backgroundPaddingLeft, 0));

        adapter.fillItems = this::fillItems;
        adapter.update(false);
        actionBar.setTitle(getTitle());
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.BlacklistUrlQueryTitle);
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return adapter = new UniversalAdapter(listView, getContext(), currentAccount, getBaseFragment().getClassGuid(), true, null, resourcesProvider);
    }

    @Override
    public void show() {
        if (this.url == null) return;
        super.show();
        Bulletin.hideVisible();
    }

    @Override
    protected boolean canHighlightChildAt(View child, float x, float y) {
        return !(child instanceof CollapseTextCell);
    }

    private void fillAction(ArrayList<UItem> items, Action action) {
        if (!action.isExpandable()) {
            items.add(UItem.asRoundCheckbox(action.id, action.query)
                    .setChecked(action.selectedCount > 0));
        } else {
            items.add(UItem.asUserGroupCheckbox(action.id, action.query, String.valueOf(action.selectedCount > 0 ? action.selectedCount : action.getCount()))
                    .setChecked(action.selectedCount > 0)
                    .setCollapsed(action.collapsed)
                    .setClickCallback((v) -> {
                        saveScrollPosition();
                        action.collapseOrExpand();
                        applyScrolledPosition(true);
                    }));
            if (!action.collapsed) {
                action.forEach((userOrChat, i) -> {
                    items.add(UItem.asUserCheckbox(action.id << 24 | i, userOrChat)
                            .setChecked(action.checks[i])
                            .setPad(1));
                });
            }
        }
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        Uri uri = null;
        try {
            uri = Uri.parse(url);
        } catch (Exception ex) {
            Log.e("030-url", "Error parsing url", ex);
            return;
        }

        Set<String> queryKeys = uri.getQueryParameterNames();
        if (checks == null) checks = new boolean[queryKeys.size()];
        for (String q : queryKeys) {
            Action a = new Action(queries.size(), q);
            queries.add(a);
            fillAction(items, a);
        }
        for (int i = 0; i < checks.length; ++i) {
            items.get(i).setChecked(checks[i]);
        }
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        item.checked = !item.checked;
        checks[position - 1] = !checks[position - 1];
        adapter.update(false);
    }

    private void proceed() {
        dismiss();

        StringBuilder sb = new StringBuilder();
        Set<String> newBlacklistSet = new HashSet<>();
        String oldBlacklistString = NekoConfig.customGetQueryBlacklist.String();
        if (!oldBlacklistString.trim().isEmpty()) {
            newBlacklistSet.addAll(Arrays.asList(oldBlacklistString.split(",")));
        }

        for (int i = 0; i < checks.length; ++i) {
            String q = queries.get(i).query.trim();
            if (!checks[i] || q.isEmpty()) continue;
            sb.append(q).append(", ");
            newBlacklistSet.add(q);
        }
        if (sb.indexOf(", ") > -1) sb.setLength(sb.length() - 2);

        NekoConfig.replaceCustomGetQueryBlacklist(newBlacklistSet);
        BulletinFactory.of(getBaseFragment()).createSimpleBulletin(R.raw.done, getString(R.string.BlacklistedQuery), sb.toString()).show();
    }
}
