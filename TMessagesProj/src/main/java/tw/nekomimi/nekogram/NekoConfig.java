package tw.nekomimi.nekogram;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import android.util.Base64;
import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import cn.hutool.core.util.StrUtil;
import tw.nekomimi.nekogram.config.ConfigItem;

import static tw.nekomimi.nekogram.config.ConfigItem.*;

@SuppressLint("ApplySharedPref")
public class NekoConfig {

    public static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nkmrcfg", Context.MODE_PRIVATE);
    public static final Object sync = new Object();
    public static final String channelAliasPrefix = "channelAliasPrefix_";

    private static boolean configLoaded = false;
    private static final ArrayList<ConfigItem> configs = new ArrayList<>();

    // Configs
    public static ConfigItem migrate = addConfig("NekoConfigMigrate", configTypeBool, false);
    public static ConfigItem largeAvatarInDrawer = addConfig(R.string.AvatarAsBackground, "AvatarAsBackground", configTypeInt, 0); // 0:TG Default 1:NekoX Default 2:Large Avatar
    public static ConfigItem unreadBadgeOnBackButton = addConfig(R.string.unreadBadgeOnBackButton, "unreadBadgeOnBackButton", configTypeBool, false);
//    public static ConfigItem customPublicProxyIP = addConfig("customPublicProxyIP", configTypeString, "");
    public static ConfigItem update_download_soucre = addConfig(R.string.update_download_soucre, "update_download_soucre", configTypeInt, 0); // 0: Github 1: Channel 2:CDNDrive, removed
    public static ConfigItem useCustomEmoji = addConfig(R.string.useCustomEmoji, "useCustomEmoji", configTypeBool, false);
    public static ConfigItem repeatConfirm = addConfig(R.string.repeatConfirm, "repeatConfirm", configTypeBool, false);
    public static ConfigItem disableInstantCamera = addConfig(R.string.DisableInstantCamera, "DisableInstantCamera", configTypeBool, false);
    public static ConfigItem showSeconds = addConfig(R.string.showSeconds, "showSeconds", configTypeBool, false);

    public static ConfigItem enablePublicProxy = addConfig(R.string.enablePublicProxy, "enablePublicProxy", configTypeBool, false);
    public static ConfigItem autoUpdateSubInfo = addConfig(R.string.autoUpdateSubInfo, "autoUpdateSubInfo", configTypeBool, true);
    public static ConfigItem lastUpdateCheckTime = addConfig("lastUpdateCheckTime", configTypeLong, 0L);

    // From NekoConfig
    public static ConfigItem useIPv6 = addConfig(R.string.IPv6, "IPv6", configTypeBool, false);
    public static ConfigItem hidePhone = addConfig(R.string.HidePhone, "HidePhone", configTypeBool, true);
    public static ConfigItem ignoreBlocked = addConfig(R.string.IgnoreBlocked, "IgnoreBlocked", configTypeBool, false);
    public static ConfigItem tabletMode = addConfig(R.string.TabletMode, "TabletMode", configTypeInt, 0);
    public static ConfigItem inappCamera = addConfig(R.string.DebugMenuEnableCamera, "DebugMenuEnableCamera", configTypeBool, true); // fake
    public static ConfigItem smoothKeyboard = addConfig("DebugMenuEnableSmoothKeyboard", configTypeBool, false);// fake

    public static ConfigItem typeface = addConfig(R.string.TypefaceUseDefault, "TypefaceUseDefault", configTypeBool, false);
    public static ConfigItem nameOrder = addConfig(R.string.NameOrder, "NameOrder", configTypeInt, 1);
    public static ConfigItem mapPreviewProvider = addConfig(R.string.MapPreviewProvider, "MapPreviewProvider", configTypeInt, 0);
    public static ConfigItem transparentStatusBar = addConfig(R.string.TransparentStatusBar, "TransparentStatusBar", configTypeBool, true);
    public static ConfigItem forceBlurInChat = addConfig(R.string.forceBlurInChat, "forceBlurInChat", configTypeBool, false);
    public static ConfigItem chatBlueAlphaValue = addConfig("forceBlurInChatAlphaValue", configTypeInt, 127);
    public static ConfigItem hideProxySponsorChannel = addConfig(R.string.HideProxySponsorChannel, "HideProxySponsorChannel", configTypeBool, false);
    public static ConfigItem showAddToSavedMessages = addConfig("showAddToSavedMessages", configTypeBool, true);
    public static ConfigItem showReport = addConfig("showReport", configTypeBool, true);
    public static ConfigItem showViewHistory = addConfig("showViewHistory", configTypeBool, true);
    public static ConfigItem showAdminActions = addConfig("showAdminActions", configTypeBool, true);
    public static ConfigItem showChangePermissions = addConfig("showChangePermissions", configTypeBool, true);
    public static ConfigItem showDeleteDownloadedFile = addConfig("showDeleteDownloadedFile", configTypeBool, true);
    public static ConfigItem showMessageDetails = addConfig("showMessageDetails", configTypeBool, false);
    public static ConfigItem showTranslate = addConfig("showTranslate", configTypeBool, true);
    public static ConfigItem showRepeat = addConfig("showRepeat", configTypeBool, false);
    public static ConfigItem showShareMessages = addConfig("showShareMessages", configTypeBool, false);
    public static ConfigItem showMessageHide = addConfig("showMessageHide", configTypeBool, false);

    public static ConfigItem eventType = addConfig("eventType", configTypeInt, 0);
    public static ConfigItem actionBarDecoration = addConfig(R.string.ActionBarDecoration, "ActionBarDecoration", configTypeInt, 0);
    public static ConfigItem newYear = addConfig(R.string.ChristmasHat, "ChristmasHat", configTypeBool, false);
    public static ConfigItem stickerSize = addConfig(R.string.StickerSize, "stickerSize", configTypeFloat, 14.0f);
    public static ConfigItem unlimitedFavedStickers = addConfig(R.string.UnlimitedFavoredStickers, "UnlimitedFavoredStickers", configTypeBool, false);
    public static ConfigItem unlimitedPinnedDialogs = addConfig(R.string.UnlimitedPinnedDialogs, "UnlimitedPinnedDialogs", configTypeBool, false);
    public static ConfigItem disablePhotoSideAction = addConfig(R.string.DisablePhotoViewerSideAction, "DisablePhotoViewerSideAction", configTypeBool, false);
    public static ConfigItem openArchiveOnPull = addConfig(R.string.OpenArchiveOnPull, "OpenArchiveOnPull", configTypeBool, false);
    public static ConfigItem hideKeyboardOnChatScroll = addConfig(R.string.HideKeyboardOnChatScroll, "HideKeyboardOnChatScroll", configTypeBool, false);
    public static ConfigItem avatarBackgroundBlur = addConfig(R.string.BlurAvatarBackground, "BlurAvatarBackground", configTypeBool, false);
    public static ConfigItem avatarBackgroundDarken = addConfig(R.string.DarkenAvatarBackground, "DarkenAvatarBackground", configTypeBool, false);
    public static ConfigItem useSystemEmoji = addConfig("EmojiUseDefault", configTypeBool, false);
    public static ConfigItem showTabsOnForward = addConfig(R.string.ShowTabsOnForward, "ShowTabsOnForward", configTypeBool, false);
    public static ConfigItem rearVideoMessages = addConfig(R.string.RearVideoMessages, "RearVideoMessages", configTypeBool, false);
//    public static ConfigItem pressTitleToOpenAllChats = addConfig("pressTitleToOpenAllChats", configTypeBool, false);

    public static ConfigItem disableChatAction = addConfig(R.string.DisableChatAction, "DisableChatAction", configTypeBool, false);
    public static ConfigItem sortByUnread = addConfig("sort_by_unread", configTypeBool, false);
    public static ConfigItem sortByUnmuted = addConfig("sort_by_unmuted", configTypeBool, true);
    public static ConfigItem sortByUser = addConfig("sort_by_user", configTypeBool, true);
    public static ConfigItem sortByContacts = addConfig("sort_by_contacts", configTypeBool, true);

    public static ConfigItem disableUndo = addConfig(R.string.DisableUndo, "DisableUndo", configTypeBool, false);

    public static ConfigItem filterUsers = addConfig("filter_users", configTypeBool, true);
    public static ConfigItem filterContacts = addConfig("filter_contacts", configTypeBool, true);
    public static ConfigItem filterGroups = addConfig("filter_groups", configTypeBool, true);
    public static ConfigItem filterChannels = addConfig("filter_channels", configTypeBool, true);
    public static ConfigItem filterBots = addConfig("filter_bots", configTypeBool, true);
    public static ConfigItem filterAdmins = addConfig("filter_admins", configTypeBool, true);
    public static ConfigItem filterUnmuted = addConfig("filter_unmuted", configTypeBool, true);
    public static ConfigItem filterUnread = addConfig("filter_unread", configTypeBool, true);
    public static ConfigItem filterUnmutedAndUnread = addConfig("filter_unmuted_and_unread", configTypeBool, true);

    public static ConfigItem disableSystemAccount = addConfig(R.string.DisableSystemAccount, "DisableSystemAccount", configTypeBool, false);
    public static ConfigItem skipOpenLinkConfirm = addConfig(R.string.SkipOpenLinkConfirm, "SkipOpenLinkConfirm", configTypeBool, false);

    public static ConfigItem ignoreMutedCount = addConfig(R.string.IgnoreMutedCount, "IgnoreMutedCount", configTypeBool, true);
    public static ConfigItem showIdAndDc = addConfig(R.string.ShowIdAndDc, "ShowIdAndDc", configTypeBool, false);

    public static ConfigItem cachePath = addConfig(R.string.StoragePath, "cache_path", configTypeString, "");
    public static ConfigItem customSavePath = addConfig(R.string.customSavePath, "customSavePath", configTypeString, "NekoX");

    public static ConfigItem translationProvider = addConfig(R.string.TranslationProvider, "translationProvider", configTypeInt, 1);
    public static ConfigItem translateToLang = addConfig(R.string.TransToLang, "TransToLang", configTypeString, ""); // "" -> translate to current language (MessageTrans.kt & Translator.kt)
    public static ConfigItem translateInputLang = addConfig(R.string.TransInputToLang, "TransInputToLang", configTypeString, "en");
    public static ConfigItem useTelegramTranslateInChat = addConfig(R.string.useTelegramTranslateInChat, "useTelegramTranslateInChat", configTypeBool, false);
    public static ConfigItem googleCloudTranslateKey = addConfig(R.string.GoogleCloudTransKey, "GoogleCloudTransKey", configTypeString, "");
    public static ConfigItem preferredTranslateTargetLang = addConfig(R.string.PreferredTranslateTargetLangTitle, "PreferredTranslateTargetLangTitle", configTypeString, "");
    public static ArrayList<String> preferredTranslateTargetLangList = new ArrayList<>();


    public static ConfigItem disableNotificationBubbles = addConfig(R.string.disableNotificationBubbles, "disableNotificationBubbles", configTypeBool, false);

    public static ConfigItem ccToLang = addConfig("opencc_to_lang", configTypeString, "");
    public static ConfigItem ccInputLang = addConfig("opencc_input_to_lang", configTypeString, "");

    public static ConfigItem tabsTitleType = addConfig(R.string.TabTitleType, "TabTitleType", configTypeInt, NekoXConfig.TITLE_TYPE_TEXT);
    public static ConfigItem confirmAVMessage = addConfig(R.string.ConfirmAVMessage, "ConfirmAVMessage", configTypeBool, false);
    public static ConfigItem askBeforeCall = addConfig(R.string.AskBeforeCalling, "AskBeforeCalling", configTypeBool, false);
    public static ConfigItem disableNumberRounding = addConfig(R.string.DisableNumberRounding, "DisableNumberRounding", configTypeBool, false);

    public static ConfigItem useSystemDNS = addConfig(R.string.useSystemDNS, "useSystemDNS", configTypeBool, false);
    public static ConfigItem customDoH = addConfig(R.string.customDoH, "customDoH", configTypeString, "");
    public static ConfigItem hideProxyByDefault = addConfig(R.string.HideProxyByDefault, "HideProxyByDefault", configTypeBool, false);
    public static ConfigItem useProxyItem = addConfig(R.string.UseProxyItem, "UseProxyItem", configTypeBool, true);

    public static ConfigItem disableAppBarShadow = addConfig(R.string.DisableAppBarShadow, "DisableAppBarShadow", configTypeBool, false);
    public static ConfigItem mediaPreview = addConfig(R.string.MediaPreview, "MediaPreview", configTypeBool, true);

    public static ConfigItem proxyAutoSwitch = addConfig(R.string.ProxyAutoSwitch, "ProxyAutoSwitch", configTypeBool, false);

    public static ConfigItem usePersianCalendar = addConfig(R.string.UsePersiancalendar, "UsePersiancalendar", configTypeBool, false);
    public static ConfigItem displayPersianCalendarByLatin = addConfig(R.string.DisplayPersianCalendarByLatin, "DisplayPersianCalendarByLatin", configTypeBool, false);
    public static ConfigItem openPGPApp = addConfig(R.string.OpenPGPApp, "OpenPGPApp", configTypeString, "");
    public static ConfigItem openPGPKeyId = addConfig(R.string.OpenPGPKey, "OpenPGPKey", configTypeLong, 0L);

    public static ConfigItem disableVibration = addConfig(R.string.DisableVibration, "DisableVibration", configTypeBool, false);
    public static ConfigItem autoPauseVideo = addConfig(R.string.AutoPauseVideo, "AutoPauseVideo", configTypeBool, false);
    public static ConfigItem disableProximityEvents = addConfig(R.string.DisableProximityEvents, "DisableProximityEvents", configTypeBool, false);

    public static ConfigItem ignoreContentRestrictions = addConfig(R.string.IgnoreContentRestrictions, "ignoreContentRestrictions", configTypeBool, !BuildVars.isPlay);
    public static ConfigItem useChatAttachMediaMenu = addConfig(R.string.UseChatAttachEnterMenu, "UseChatAttachEnterMenu", configTypeBool, true);
    public static ConfigItem disableLinkPreviewByDefault = addConfig(R.string.DisableLinkPreviewByDefault, "DisableLinkPreviewByDefault", configTypeBool, false);
    public static ConfigItem sendCommentAfterForward = addConfig(R.string.SendCommentAfterForward, "SendCommentAfterForward", configTypeBool, true);
//    public static ConfigItem increaseVoiceMessageQuality = addConfig("IncreaseVoiceMessageQuality", configTypeBool, true);
    public static ConfigItem disableTrending = addConfig(R.string.DisableTrending, "DisableTrending", configTypeBool, true);
    public static ConfigItem dontSendGreetingSticker = addConfig(R.string.DontSendGreetingSticker, "DontSendGreetingSticker", configTypeBool, false);
    public static ConfigItem hideTimeForSticker = addConfig(R.string.HideTimeForSticker, "HideTimeForSticker", configTypeBool, false);
    public static ConfigItem takeGIFasVideo = addConfig(R.string.TakeGIFasVideo, "TakeGIFasVideo", configTypeBool, false);
    public static ConfigItem maxRecentEmojiCount = addConfig(R.string.maxRecentEmojiCount, "maxRecentEmojiCount", configTypeInt, 48);
    public static ConfigItem maxRecentStickerCount = addConfig(R.string.maxRecentStickerCount, "maxRecentStickerCount", configTypeInt, 20);
    public static ConfigItem disableSwipeToNext = addConfig(R.string.disableSwipeToNextChannel, "disableSwipeToNextChannel", configTypeBool, true);
    public static ConfigItem disableRemoteEmojiInteractions = addConfig(R.string.disableRemoteEmojiInteractions, "disableRemoteEmojiInteractions", configTypeBool, true);
    public static ConfigItem disableChoosingSticker = addConfig(R.string.disableChoosingSticker, "disableChoosingSticker", configTypeBool, false);
    public static ConfigItem hideGroupSticker = addConfig(R.string.hideGroupSticker, "hideGroupSticker", configTypeBool, false);
    public static ConfigItem disablePremiumStickerAnimation = addConfig(R.string.disablePremiumStickerAnimation, "disablePremiumStickerAnimation", configTypeBool, false);
    public static ConfigItem hideSponsoredMessage = addConfig(R.string.hideSponsoredMessage, "hideSponsoredMessage", configTypeBool, false);
    public static ConfigItem rememberAllBackMessages = addConfig(R.string.rememberAllBackMessages, "rememberAllBackMessages", configTypeBool, false);
    public static ConfigItem hideSendAsChannel = addConfig(R.string.hideSendAsChannel, "hideSendAsChannel", configTypeBool, false);
    public static ConfigItem showSpoilersDirectly = addConfig(R.string.showSpoilersDirectly, "showSpoilersDirectly", configTypeBool, false);

    public static ConfigItem alwaysUseSpoilerForMedia = addConfig(R.string.AlwaysUseSpoilerForMedia, "AlwaysUseSpoilerForMedia", configTypeString, "");
    public static ArrayList<Long> alwaysUseSpoilerForMediaChats = new ArrayList<>();

    public static ConfigItem reactions = addConfig(R.string.reactions, "reactions", configTypeInt, 0);
    public static String[] reactionsOptions = null;

    public static ConfigItem disableReactionsWhenSelecting = addConfig(R.string.disableReactionsWhenSelecting, "disableReactionsWhenSelecting", configTypeBool, true);
    public static ConfigItem showBottomActionsWhenSelecting = addConfig(R.string.showBottomActionsWhenSelecting, "showBottomActionsWhenSelecting", configTypeBool, false);

    public static ConfigItem labelChannelUser = addConfig(R.string.labelChannelUser, "labelChannelUser", configTypeBool, false);
    public static ConfigItem channelAlias = addConfig(R.string.channelAlias, "channelAlias", configTypeBool, false);

    public static ConfigItem disableAutoDownloadingWin32Executable = addConfig(R.string.Win32ExecutableFiles, "Win32ExecutableFiles", configTypeBool, true);
    public static ConfigItem disableAutoDownloadingArchive = addConfig(R.string.ArchiveFiles, "ArchiveFiles", configTypeBool, true);

    public static ConfigItem enableStickerPin = addConfig(R.string.EnableStickerPin, "EnableStickerPin", configTypeBool, false);
    public static ConfigItem useMediaStreamInVoip = addConfig(R.string.UseMediaStreamInVoip, "UseMediaStreamInVoip", configTypeBool, false);
    public static ConfigItem customAudioBitrate = addConfig("customAudioBitrate", configTypeInt, 32);
    public static ConfigItem disableGroupVoipAudioProcessing = addConfig("disableGroupVoipAudioProcessing", configTypeBool, false);
    public static ConfigItem enhancedFileLoader = addConfig(R.string.enhancedFileLoader, "enhancedFileLoader", configTypeBool, false);
    public static ConfigItem useOSMDroidMap = addConfig(R.string.useOSMDroidMap, "useOSMDroidMap", configTypeBool, !BuildVars.isGServicesCompiled);
    public static ConfigItem mapDriftingFixForGoogleMaps = addConfig(R.string.mapDriftingFixForGoogleMaps, "mapDriftingFixForGoogleMaps", configTypeBool, true);
    public static ConfigItem disableStories = addConfig(R.string.DisableStories, "DisableStories", ConfigItem.configTypeBool, false);
    public static ConfigItem disableSendReadStories = addConfig(R.string.DisableSendReadStories, "DisableSendReadStories", ConfigItem.configTypeBool, false);
    public static ConfigItem ignoreAllReactions = addConfig(R.string.IgnoreAllReactions, "IgnoreAllReactions", ConfigItem.configTypeBool, false);
    public static ConfigItem confirmToSendCommandByClick = addConfig(R.string.ConfirmToSendCommandToggle, "ConfirmToSendCommandToggle", ConfigItem.configTypeBool, false);
    public static ConfigItem showCopyPhoto = addConfig(R.string.CopyPhoto, "CopyPhoto", ConfigItem.configTypeBool, false);
    public static ConfigItem increasedMaxPhotoResolution = addConfig(R.string.IncreasedMaxPhotoResolution, "IncreasedMaxPhotoResolution", ConfigItem.configTypeBool, false);
    public static ConfigItem showSelfInsteadOfSavedMessages = addConfig(R.string.ShowSelfInsteadOfSavedMessages, "ShowSelfInsteadOfSavedMessages", ConfigItem.configTypeBool, false);
    public static ConfigItem closeWebViewWithoutConfirmation = addConfig(R.string.CloseWebViewWithoutConfirmation, "CloseWebViewWithoutConfirmation", ConfigItem.configTypeBool, false);
    public static ConfigItem openWebViewTabWithoutBot = addConfig(R.string.OpenWebViewTabWithoutBot, "OpenWebViewTabWithoutBot", ConfigItem.configTypeBool, false);
    public static ConfigItem showBotWebViewSettings = addConfig(R.string.ShowBotWebViewSettings, "ShowBotWebViewSettings", ConfigItem.configTypeBool, false);
    public static ConfigItem disableWebViewGeolocation = addConfig(R.string.DisableWebViewGeolocation, "DisableWebViewGeolocation", ConfigItem.configTypeBool, false);
    public static ConfigItem hideWebViewTabOverlayWhenSharing = addConfig(R.string.HideWebViewTabOverlayWhenSharing, "HideWebViewTabOverlayWhenSharing", ConfigItem.configTypeBool, true);
    public static ConfigItem hideWebViewTabOverlayInChat = addConfig(R.string.HideWebViewTabOverlayInChat, "HideWebViewTabOverlayInChat", ConfigItem.configTypeBool, true);
    public static ConfigItem preventPullDownWebview = addConfig(R.string.PreventPullDownWebview, "PreventPullDownWebview", ConfigItem.configTypeBool, false);
    public static ConfigItem useBotWebviewForGames = addConfig(R.string.UseBotWebviewForGames, "UseBotWebviewForGames", ConfigItem.configTypeBool, false);
    public static ConfigItem confirmOpenLinkInWebView = addConfig(R.string.ConfirmOpenLinkInWebView, "ConfirmOpenLinkInWebView", ConfigItem.configTypeBool, false);
    public static ConfigItem disableCustomWallpaperUser = addConfig(R.string.DisableCustomWallpaperUser, "DisableCustomWallpaperUser", ConfigItem.configTypeBool, false);
    public static ConfigItem disableCustomWallpaperChannel = addConfig(R.string.DisableCustomWallpaperChannel, "DisableCustomWallpaperChannel", ConfigItem.configTypeBool, false);
    public static ConfigItem fasterReconnectHack = addConfig(R.string.FasterReconnectHack, "FasterReconnectHack", ConfigItem.configTypeBool, false);
    public static ConfigItem autoArchiveAndMute = addConfig(R.string.AutoArchiveAndMute, "AutoArchiveAndMute", ConfigItem.configTypeBool, false);
    public static ConfigItem appendOriginalTimestamp = addConfig(R.string.AppendOriginalTimestamp, "AppendOriginalTimestamp", ConfigItem.configTypeBool, false);
    public static ConfigItem alwaysShowDownloads = addConfig(R.string.AlwaysShowDownloads, "AlwaysShowDownloads", ConfigItem.configTypeBool, false);
    public static ConfigItem openAvatarInsteadOfExpand = addConfig(R.string.OpenAvatarInsteadOfExpand, "OpenAvatarInsteadOfExpand", ConfigItem.configTypeBool, false);
    public static ConfigItem customTitleText = addConfig(R.string.CustomTitleText, "CustomTitleText", configTypeString, "Nekogram X");
    public static ConfigItem forceAllowChooseBrowser = addConfig(R.string.ForceAllowChooseBrowser, "ForceAllowChooseBrowser", configTypeBool, false);
    public static ConfigItem patchAndCleanupLinks = addConfig(R.string.PatchAndCleanupLinks, "PatchAndCleanupLinks", configTypeBool, false);
    public static ConfigItem showSharedMediaOnOpeningProfile = addConfig(R.string.ShowSharedMediaOnOpeningProfile, "ShowSharedMediaOnOpeningProfile", configTypeBool, false);
    public static ConfigItem disableAddBirthdayReminder = addConfig(R.string.DisableAddBirthdayReminder, "DisableAddBirthdayReminder", configTypeBool, false);
    public static ConfigItem disableBirthdayReminder = addConfig(R.string.DisableBirthdayReminder, "DisableBirthdayReminder", configTypeBool, false);
    public static ConfigItem alwaysHideBotCommandButton = addConfig(R.string.AlwaysHideBotCommandButton, "AlwaysHideBotCommandButton", configTypeBool, false);
    public static ConfigItem alwaysShowBotCommandButton = addConfig(R.string.AlwaysShowBotCommandButton, "AlwaysShowBotCommandButton", configTypeBool, false);
    public static ConfigItem alwaysDisableSafeBrowsingInWebView = addConfig(R.string.AlwaysDisableSafeBrowsingInWebView, "AlwaysDisableSafeBrowsingInWebView", configTypeBool, false);
    public static ConfigItem removePremiumAnnoyance = addConfig(R.string.RemovePremiumAnnoyance, "RemovePremiumAnnoyance", configTypeBool, false);
    public static ConfigItem showEditTimeInPopupMenu = addConfig(R.string.ShowEditTimestampInPopupMenu, "ShowEditTimestampInPopupMenu", configTypeBool, false);
    public static ConfigItem showForwardTimeInPopupMenu = addConfig(R.string.ShowForwardTimestampInPopupMenu, "ShowForwardTimestampInPopupMenu", configTypeBool, false);
    public static ConfigItem customSearchEngine = addConfig(R.string.CustomSearchEngine, "CustomSearchEngine", configTypeString, "https://www.startpage.com/sp/search?query=");
    public static ConfigItem overrideSettingBoolean = addConfig(R.string.OverrideSettingBoolean, "OverrideSettingBoolean", configTypeString, "");
    public static ConfigItem overrideSettingInteger = addConfig(R.string.OverrideSettingInteger, "OverrideSettingInteger", configTypeString, "");
    public static ConfigItem overrideSettingString = addConfig(R.string.OverrideSettingString, "OverrideSettingString", configTypeString, "");
    public static ConfigItem overrideSettingLong = addConfig(R.string.OverrideSettingLong, "OverrideSettingLong", configTypeString, "");
    public static ConfigItem overrideSettingFloat = addConfig(R.string.OverrideSettingFloat, "OverrideSettingFloat", configTypeString, "");
    public static ConfigItem marqueeForLongChatTitles = addConfig(R.string.MarqueeForLongChatTitles, "MarqueeForLongChatTitles", configTypeBool, true);
    public static ConfigItem dontSendRightAfterTranslated = addConfig(R.string.DontSendRightAfterTranslated, "DontSendRightAfterTranslated", configTypeBool, true);
    public static ConfigItem hideOriginalTextAfterTranslate = addConfig(R.string.HideOriginalTextAfterTranslate, "HideOriginalTextAfterTranslate", configTypeBool, false);
    public static ConfigItem autoTranslate = addConfig(R.string.AutomaticTranslation, "AutomaticTranslation", configTypeBool, false);
    public static ConfigItem useCustomProviderForAutoTranslate = addConfig(R.string.UseCustomProviderForAutoTranslate, "UseCustomProviderForAutoTranslate", configTypeBool, true);
    public static ConfigItem dontAutoPlayNextMessage = addConfig(R.string.DontAutoPlayNextMessage, "DontAutoPlayNextMessage", configTypeBool, false);
    public static ConfigItem chatListFontSizeFollowChat = addConfig(R.string.ChatListFontSizeFollowChat, "ChatListFontSizeFollowChat", configTypeBool, false);
    public static ConfigItem dontShareNumberWhenAddContactByDefault = addConfig(R.string.DontShareNumberWhenAddContactByDefault, "DontShareNumberWhenAddContactByDefault", configTypeBool, true);
    public static ConfigItem autoSendMessageIfBlockedBySlowMode = addConfig(R.string.AutoSendMessageIfBlockedBySlowMode, "AutoSendMessageIfBlockedBySlowMode", configTypeBool, false);
    public static ConfigItem replyAsQuoteByDefault = addConfig(R.string.ReplyAsQuoteByDefault, "ReplyAsQuoteByDefault", configTypeBool, false);
    public static ConfigItem customLingvaInstance = addConfig(R.string.CustomLingvaInstance, "CustomLingvaInstance", configTypeString, "");
    public static ConfigItem disableAutoWebLogin = addConfig(R.string.DisableAutoWebLogin, "DisableAutoWebLogin", configTypeBool, false);
    public static ConfigItem forceHideShowAsList = addConfig(R.string.ForceHideShowAsList, "ForceHideShowAsList", configTypeBool, false);
    public static ConfigItem ignoreFilterEmoticonUpdate = addConfig(R.string.IgnoreFilterEmoticonUpdate, "IgnoreFilterEmoticonUpdate", configTypeBool, false);

    public static ConfigItem customGetQueryBlacklist = addConfig(R.string.BlacklistUrlQueryTitle, "BlacklistUrlQueryTitle", configTypeString, "");
    public static ArrayList<String> customGetQueryBlacklistData = new ArrayList<>();

    public static ConfigItem searchBlacklist = addConfig(R.string.SearchBlacklist, "searchBlackList", configTypeString, "");
    public static ArrayList<Long> searchBlacklistData = new ArrayList<>();

    static {
        loadConfig(false);
        checkMigrate(false);
    }

    public static ConfigItem addConfig(String k, int t, Object d) {
        ConfigItem a = new ConfigItem(k, t, d);
        configs.add(a);
        return a;
    }

    public static ConfigItem addConfig(int id, String k, int t, Object d) {
        ConfigItem a = new ConfigItem(id, k, t, d);
        configs.add(a);
        return a;
    }

    public static void loadConfig(boolean force) {
        synchronized (sync) {
            if (configLoaded && !force) {
                return;
            }
            for (int i = 0; i < configs.size(); i++) {
                ConfigItem o = configs.get(i);

                if (o.type == configTypeBool) {
                    o.value = preferences.getBoolean(o.key, (boolean) o.defaultValue);
                }
                if (o.type == configTypeInt) {
                    o.value = preferences.getInt(o.key, (int) o.defaultValue);
                }
                if (o.type == configTypeLong) {
                    o.value = preferences.getLong(o.key, (Long) o.defaultValue);
                }
                if (o.type == configTypeFloat) {
                    o.value = preferences.getFloat(o.key, (Float) o.defaultValue);
                }
                if (o.type == configTypeString) {
                    o.value = preferences.getString(o.key, (String) o.defaultValue);
                }
                if (o.type == configTypeSetInt) {
                    Set<String> ss = preferences.getStringSet(o.key, new HashSet<>());
                    HashSet<Integer> si = new HashSet<>();
                    for (String s : ss) {
                        si.add(Integer.parseInt(s));
                    }
                    o.value = si;
                }
                if (o.type == configTypeMapIntInt) {
                    String cv = preferences.getString(o.key, "");
                    // Log.e("NC", String.format("Getting pref %s val %s", o.key, cv));
                    if (cv.length() == 0) {
                        o.value = new HashMap<Integer, Integer>();
                    } else {
                        try {
                            byte[] data = Base64.decode(cv, Base64.DEFAULT);
                            ObjectInputStream ois = new ObjectInputStream(
                                    new ByteArrayInputStream(data));
                            o.value = (HashMap<Integer, Integer>) ois.readObject();
                            if (o.value == null) {
                                o.value = new HashMap<Integer, Integer>();
                            }
                            ois.close();
                        } catch (Exception e) {
                            o.value = new HashMap<Integer, Integer>();
                        }
                    }
                }
            }
            configLoaded = true;
        }
    }

    public static void checkMigrate(boolean force) {
        // TODO remove this after some versions.
        if (migrate.Bool() || force)
            return;

        migrate.setConfigBool(true);

        // NekoConfig.java read & migrate
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nekoconfig", Activity.MODE_PRIVATE);

        if (preferences.contains("typeface"))
            typeface.setConfigBool(preferences.getInt("typeface", 0) != 0);
        if (preferences.contains("nameOrder"))
            nameOrder.setConfigInt(preferences.getInt("nameOrder", 1));
        if (preferences.contains("mapPreviewProvider"))
            mapPreviewProvider.setConfigInt(preferences.getInt("mapPreviewProvider", 0));
        if (preferences.contains("transparentStatusBar"))
            transparentStatusBar.setConfigBool(preferences.getBoolean("transparentStatusBar", false));
        if (preferences.contains("hideProxySponsorChannel"))
            hideProxySponsorChannel.setConfigBool(preferences.getBoolean("hideProxySponsorChannel", false));
        if (preferences.contains("showAddToSavedMessages"))
            showAddToSavedMessages.setConfigBool(preferences.getBoolean("showAddToSavedMessages", true));
        if (preferences.contains("showReport"))
            showReport.setConfigBool(preferences.getBoolean("showReport", true));
        if (preferences.contains("showViewHistory"))
            showViewHistory.setConfigBool(preferences.getBoolean("showViewHistory", true));
        if (preferences.contains("showAdminActions"))
            showAdminActions.setConfigBool(preferences.getBoolean("showAdminActions", true));
        if (preferences.contains("showChangePermissions"))
            showChangePermissions.setConfigBool(preferences.getBoolean("showChangePermissions", true));
        if (preferences.contains("showDeleteDownloadedFile"))
            showDeleteDownloadedFile.setConfigBool(preferences.getBoolean("showDeleteDownloadedFile", true));
        if (preferences.contains("showMessageDetails"))
            showMessageDetails.setConfigBool(preferences.getBoolean("showMessageDetails", false));
        if (preferences.contains("showTranslate"))
            showTranslate.setConfigBool(preferences.getBoolean("showTranslate", true));
        if (preferences.contains("showRepeat"))
            showRepeat.setConfigBool(preferences.getBoolean("showRepeat", false));
        if (preferences.contains("showShareMessages"))
            showShareMessages.setConfigBool(preferences.getBoolean("showShareMessages", false));
        if (preferences.contains("showMessageHide"))
            showMessageHide.setConfigBool(preferences.getBoolean("showMessageHide", false));

        if (preferences.contains("eventType"))
            eventType.setConfigInt(preferences.getInt("eventType", 0));
        if (preferences.contains("actionBarDecoration"))
            actionBarDecoration.setConfigInt(preferences.getInt("actionBarDecoration", 0));
        if (preferences.contains("newYear"))
            newYear.setConfigBool(preferences.getBoolean("newYear", false));
        if (preferences.contains("stickerSize"))
            stickerSize.setConfigFloat(preferences.getFloat("stickerSize", 14.0f));
        if (preferences.contains("unlimitedFavedStickers"))
            unlimitedFavedStickers.setConfigBool(preferences.getBoolean("unlimitedFavedStickers", false));
        if (preferences.contains("unlimitedPinnedDialogs"))
            unlimitedPinnedDialogs.setConfigBool(preferences.getBoolean("unlimitedPinnedDialogs", false));
        if (preferences.contains("translationProvider"))
            translationProvider.setConfigInt(preferences.getInt("translationProvider", 1));
        if (preferences.contains("disablePhotoSideAction"))
            disablePhotoSideAction.setConfigBool(preferences.getBoolean("disablePhotoSideAction", true));
        if (preferences.contains("openArchiveOnPull"))
            openArchiveOnPull.setConfigBool(preferences.getBoolean("openArchiveOnPull", false));
        if (preferences.contains("showHiddenFeature"))             //showHiddenFeature.setConfigBool(preferences.getBoolean("showHiddenFeature", false));
            if (preferences.contains("hideKeyboardOnChatScroll"))
                hideKeyboardOnChatScroll.setConfigBool(preferences.getBoolean("hideKeyboardOnChatScroll", false));
        if (preferences.contains("avatarBackgroundBlur"))
            avatarBackgroundBlur.setConfigBool(preferences.getBoolean("avatarBackgroundBlur", false));
        if (preferences.contains("avatarBackgroundDarken"))
            avatarBackgroundDarken.setConfigBool(preferences.getBoolean("avatarBackgroundDarken", false));
        if (preferences.contains("useSystemEmoji"))
            useSystemEmoji.setConfigBool(preferences.getBoolean("useSystemEmoji", false));
        if (preferences.contains("showTabsOnForward"))
            showTabsOnForward.setConfigBool(preferences.getBoolean("showTabsOnForward", false));
        if (preferences.contains("rearVideoMessages"))
            rearVideoMessages.setConfigBool(preferences.getBoolean("rearVideoMessages", false));
        if (preferences.contains("disable_chat_action"))
            disableChatAction.setConfigBool(preferences.getBoolean("disable_chat_action", false));
        if (preferences.contains("sort_by_unread"))
            sortByUnread.setConfigBool(preferences.getBoolean("sort_by_unread", false));
        if (preferences.contains("sort_by_unmuted"))
            sortByUnmuted.setConfigBool(preferences.getBoolean("sort_by_unmuted", true));
        if (preferences.contains("sort_by_user"))
            sortByUser.setConfigBool(preferences.getBoolean("sort_by_user", true));
        if (preferences.contains("sort_by_contacts"))
            sortByContacts.setConfigBool(preferences.getBoolean("sort_by_contacts", true));

        if (preferences.contains("disable_undo"))
            disableUndo.setConfigBool(preferences.getBoolean("disable_undo", false));

        if (preferences.contains("filter_users"))
            filterUsers.setConfigBool(preferences.getBoolean("filter_users", true));
        if (preferences.contains("filter_contacts"))
            filterContacts.setConfigBool(preferences.getBoolean("filter_contacts", true));
        if (preferences.contains("filter_groups"))
            filterGroups.setConfigBool(preferences.getBoolean("filter_groups", true));
        if (preferences.contains("filter_channels"))
            filterChannels.setConfigBool(preferences.getBoolean("filter_channels", true));
        if (preferences.contains("filter_bots"))
            filterBots.setConfigBool(preferences.getBoolean("filter_bots", true));
        if (preferences.contains("filter_admins"))
            filterAdmins.setConfigBool(preferences.getBoolean("filter_admins", true));
        if (preferences.contains("filter_unmuted"))
            filterUnmuted.setConfigBool(preferences.getBoolean("filter_unmuted", true));
        if (preferences.contains("filter_unread"))
            filterUnread.setConfigBool(preferences.getBoolean("filter_unread", true));
        if (preferences.contains("filter_unmuted_and_unread"))
            filterUnmutedAndUnread.setConfigBool(preferences.getBoolean("filter_unmuted_and_unread", true));

        if (preferences.contains("disable_system_account"))
            disableSystemAccount.setConfigBool(preferences.getBoolean("disable_system_account", false));
        if (preferences.contains("skip_open_link_confirm"))
            skipOpenLinkConfirm.setConfigBool(preferences.getBoolean("skip_open_link_confirm", false));

        if (preferences.contains("ignore_muted_count"))
            ignoreMutedCount.setConfigBool(preferences.getBoolean("ignore_muted_count", true));
//        if (preferences.contains("use_default_theme"))
//            useDefaultTheme.setConfigBool(preferences.getBoolean("use_default_theme", false));
        if (preferences.contains("show_id_and_dc"))
            showIdAndDc.setConfigBool(preferences.getBoolean("show_id_and_dc", false));

        if (preferences.contains("google_cloud_translate_key"))
            googleCloudTranslateKey.setConfigString(preferences.getString("google_cloud_translate_key", null));
        if (preferences.contains("cache_path"))
            cachePath.setConfigString(preferences.getString("cache_path", null));

        if (preferences.contains("trans_to_lang"))
            translateToLang.setConfigString(preferences.getString("trans_to_lang", ""));
        if (preferences.contains("trans_input_to_lang"))
            translateInputLang.setConfigString(preferences.getString("trans_input_to_lang", "en"));

        if (preferences.contains("opencc_to_lang"))
            ccToLang.setConfigString(preferences.getString("opencc_to_lang", null));
        if (preferences.contains("opencc_input_to_lang"))
            ccInputLang.setConfigString(preferences.getString("opencc_input_to_lang", null));

        if (preferences.contains("tabsTitleType"))
            tabsTitleType.setConfigInt(preferences.getInt("tabsTitleType", NekoXConfig.TITLE_TYPE_TEXT));
        if (preferences.contains("confirmAVMessage"))
            confirmAVMessage.setConfigBool(preferences.getBoolean("confirmAVMessage", false));
        if (preferences.contains("askBeforeCall"))
            askBeforeCall.setConfigBool(preferences.getBoolean("askBeforeCall", false));
        if (preferences.contains("disableNumberRounding"))
            disableNumberRounding.setConfigBool(preferences.getBoolean("disableNumberRounding", false));

        if (preferences.contains("useSystemDNS"))
            useSystemDNS.setConfigBool(preferences.getBoolean("useSystemDNS", false));
        if (preferences.contains("customDoH"))
            customDoH.setConfigString(preferences.getString("customDoH", ""));
        if (preferences.contains("hide_proxy_by_default"))
            hideProxyByDefault.setConfigBool(preferences.getBoolean("hide_proxy_by_default", false));
        if (preferences.contains("use_proxy_item"))
            useProxyItem.setConfigBool(preferences.getBoolean("use_proxy_item", true));

        if (preferences.contains("disableAppBarShadow"))
            disableAppBarShadow.setConfigBool(preferences.getBoolean("disableAppBarShadow", false));
        if (preferences.contains("mediaPreview"))
            mediaPreview.setConfigBool(preferences.getBoolean("mediaPreview", true));

        if (preferences.contains("proxy_auto_switch"))
            proxyAutoSwitch.setConfigBool(preferences.getBoolean("proxy_auto_switch", false));

        if (preferences.contains("openPGPApp"))
            openPGPApp.setConfigString(preferences.getString("openPGPApp", ""));
        if (preferences.contains("openPGPKeyId"))
            openPGPKeyId.setConfigLong(preferences.getLong("openPGPKeyId", 0L));

        if (preferences.contains("disableVibration"))
            disableVibration.setConfigBool(preferences.getBoolean("disableVibration", false));
        if (preferences.contains("autoPauseVideo"))
            autoPauseVideo.setConfigBool(preferences.getBoolean("autoPauseVideo", false));
        if (preferences.contains("disableProximityEvents"))
            disableProximityEvents.setConfigBool(preferences.getBoolean("disableProximityEvents", false));

        if (preferences.contains("ignoreContentRestrictions"))
            ignoreContentRestrictions.setConfigBool(preferences.getBoolean("ignoreContentRestrictions", !BuildVars.isPlay));
        if (preferences.contains("useChatAttachMediaMenu"))
            useChatAttachMediaMenu.setConfigBool(preferences.getBoolean("useChatAttachMediaMenu", true));
        if (preferences.contains("disableLinkPreviewByDefault"))
            disableLinkPreviewByDefault.setConfigBool(preferences.getBoolean("disableLinkPreviewByDefault", false));
        if (preferences.contains("sendCommentAfterForward"))
            sendCommentAfterForward.setConfigBool(preferences.getBoolean("sendCommentAfterForward", true));
//        if (preferences.contains("increaseVoiceMessageQuality"))
//            increaseVoiceMessageQuality.setConfigBool(preferences.getBoolean("increaseVoiceMessageQuality", true));
        if (preferences.contains("disableTrending"))
            disableTrending.setConfigBool(preferences.getBoolean("disableTrending", true));
        if (preferences.contains("dontSendGreetingSticker"))
            dontSendGreetingSticker.setConfigBool(preferences.getBoolean("dontSendGreetingSticker", false));
        if (preferences.contains("hideTimeForSticker"))
            hideTimeForSticker.setConfigBool(preferences.getBoolean("hideTimeForSticker", false));
        if (preferences.contains("takeGIFasVideo"))
            takeGIFasVideo.setConfigBool(preferences.getBoolean("takeGIFasVideo", false));
        if (preferences.contains("maxRecentStickerCount"))
            maxRecentStickerCount.setConfigInt(preferences.getInt("maxRecentStickerCount", 20));
        if (preferences.contains("disableSwipeToNext"))
            disableSwipeToNext.setConfigBool(preferences.getBoolean("disableSwipeToNext", true));
        if (preferences.contains("disableRemoteEmojiInteractions"))
            disableRemoteEmojiInteractions.setConfigBool(preferences.getBoolean("disableRemoteEmojiInteractions", true));
        if (preferences.contains("disableChoosingSticker"))
            disableChoosingSticker.setConfigBool(preferences.getBoolean("disableChoosingSticker", false));

        if (preferences.contains("disableAutoDownloadingWin32Executable"))
            disableAutoDownloadingWin32Executable.setConfigBool(preferences.getBoolean("disableAutoDownloadingWin32Executable", true));
        if (preferences.contains("disableAutoDownloadingArchive"))
            disableAutoDownloadingArchive.setConfigBool(preferences.getBoolean("disableAutoDownloadingArchive", true));

        if (preferences.contains("enableStickerPin"))
            enableStickerPin.setConfigBool(preferences.getBoolean("enableStickerPin", false));
        if (preferences.contains("useMediaStreamInVoip"))
            useMediaStreamInVoip.setConfigBool(preferences.getBoolean("useMediaStreamInVoip", false));
        if (preferences.contains("customAudioBitrate"))
            customAudioBitrate.setConfigInt(preferences.getInt("customAudioBitrate", 32));
        if (preferences.contains("disableGroupVoipAudioProcessing"))
            disableGroupVoipAudioProcessing.setConfigBool(preferences.getBoolean("disableGroupVoipAudioProcessing", false));
    }

    public static boolean fixDriftingForGoogleMaps() {
        return BuildVars.isGServicesCompiled && !useOSMDroidMap.Bool() && mapDriftingFixForGoogleMaps.Bool();
    }

    public static void initStrings() {
        reactionsOptions = new String[]{
            getString(R.string.doubleTapSendReaction),
            getString(R.string.doubleTapShowReactionsMenu),
            getString(R.string.doubleTapDisable),
            getString(R.string.Translate),
            getString(R.string.Reply),
            getString(R.string.AddToSavedMessages),
        };
    }

    public static void updateUseSpoilerMediaChatList() {
        alwaysUseSpoilerForMediaChats.clear();
        String str = alwaysUseSpoilerForMedia.String();
        if ((str = StrUtil.trim(str)).isEmpty()) return;

        String[] chatIds = str.split(",");
        for (String chatId : chatIds) {
            try {
                alwaysUseSpoilerForMediaChats.add(Long.parseLong(chatId));
            } catch (Exception ex) {
                Log.e("nx-spoiler", String.format("failed to parse '%s' to long", chatId));
            }
        }
    }

    public static void updatePreferredTranslateTargetLangList() {
        AndroidUtilities.runOnUIThread(() -> {
            preferredTranslateTargetLangList.clear();
            String str = preferredTranslateTargetLang.String();
            if ((str = StrUtil.trim(str)).isEmpty()) return;

            String[] languages = str.split(",");
            if (languages.length == 0 || languages[0].trim().isEmpty()) return;
            for (String lang : languages) {
                lang = StrUtil.trim(lang).toLowerCase();
                preferredTranslateTargetLangList.add(lang);
            }
        }, 1000);
    }

    public static void applyOverriddenValue() {
        int type = 0;
        String target = null;
        ConfigItem[] checklist = new ConfigItem[] {
                overrideSettingBoolean,
                overrideSettingInteger,
                overrideSettingString,
                overrideSettingLong,
                overrideSettingFloat
        };
        for (int i = 0; i < checklist.length; ++i) {
            String s = checklist[i].String().trim();
            if (!StrUtil.isEmpty(s)) {
                target = s;
                type = i;
                break;
            }
        }
        if (target == null) return;
        Log.d("030-override", String.format("%d %s", type, target));
        String[] input = target.split(" ");
        if (input.length != 2) return;

        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        try {
            switch (type) {
                case 0:
                    edit.putBoolean(input[0], Boolean.parseBoolean(input[1])).commit();
                    break;
                case 1:
                    edit.putInt(input[0], Integer.parseInt(input[1])).commit();
                    break;
                case 2:
                    edit.putString(input[0], input[1]).commit();
                    break;
                case 3:
                    edit.putLong(input[0], Long.parseLong(input[1])).commit();
                    break;
                case 4:
                    edit.putFloat(input[0], Float.parseFloat(input[1])).commit();
            }
        } catch (Exception ex) {
            Log.e("030-override", "error parsing input", ex);
        }

        final int ftype = type;
        AndroidUtilities.runOnUIThread(() -> {
            checklist[ftype].setConfigString("");
        }, 100);
    }

    public static void applyCustomGetQueryBlacklist() {
        String[] queries = customGetQueryBlacklist.String().split(",");
        customGetQueryBlacklistData.clear();
        for (String q : queries) {
            customGetQueryBlacklistData.add(q.trim());
        }
    }

    public static void replaceCustomGetQueryBlacklist(Collection<String> newList) {
        customGetQueryBlacklistData.clear();
        customGetQueryBlacklistData.addAll(newList);
        String str = Arrays.toString(customGetQueryBlacklistData.toArray()).replace(" ", "");
        customGetQueryBlacklist.setConfigString(str.substring(1, str.length() - 1));
    }

    public static void applySearchBlacklist() {
        String[] queries = searchBlacklist.String().split(",");
        searchBlacklistData.clear();
        if (queries.length <= 1) return;
        for (String q : queries) {
            if (q.isBlank()) continue;
            try {
                searchBlacklistData.add(Long.valueOf(q.trim()));
            } catch (Exception ignored) {}
        }
        saveSearchBlacklist();
    }

    public static void saveSearchBlacklist() {
        String str = Arrays.toString(searchBlacklistData.toArray()).replace(" ", "");
        searchBlacklist.setConfigString(str.substring(1, str.length() - 1));
    }

    public static void init() {
        initStrings();
        try {
            updateUseSpoilerMediaChatList();
            updatePreferredTranslateTargetLangList();
            applyCustomGetQueryBlacklist();
            applySearchBlacklist();
        } catch (Exception ex) {
            Log.e("030-neko", "failed to load part of neko config", ex);
        }
    }

}
