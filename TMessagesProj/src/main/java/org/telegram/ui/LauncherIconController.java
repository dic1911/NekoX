package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    private static final boolean adaptiveIconSupported = Build.VERSION.SDK_INT > 32;

    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.string.AppIconDefault),
        COLORED("ColoredIcon", R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.string.AppIconColored),
        OLD_CLASSIC("ClassicIcon", R.drawable.ic_launcher_classic_foreground, adaptiveIconSupported ? R.mipmap.ic_launcher_classic : R.drawable.ic_launcher_classic_foreground, R.string.AppIconColoredClassic),
        THEMED_CLASSIC("ThemedClassicIcon", R.color.ic_launcher_background, R.drawable.icon_preview_neko_classic, R.string.AppIconThemedClassic),
        THEMED_XEYE("ThemedXEyeIcon", R.color.ic_launcher_background, R.drawable.icon_preview_neko_xeye_foreground, R.string.AppIconThemedXEye),
        NEKO_AQUA("NekoAquaIcon", R.drawable.icon_4_background_sa, R.drawable.icon_preview_neko_xeye_foreground, R.string.AppIconAqua),
        NEKO_PREMIUM("NekoPremiumIcon", R.drawable.icon_3_background_sa, R.drawable.icon_preview_neko_wink_foreground, R.string.AppIconPremium),
        NEKO_TURBO("NekoTurboIcon", R.drawable.icon_5_background_sa, R.drawable.icon_preview_neko_wink_foreground, R.string.AppIconTurbo),
        NEKO_PUSHEEN("NekoPusheenIcon", R.mipmap.ic_launcher_pusheen_background, R.drawable.icon_neko_pusheen_foreground_round, R.string.AppIconPusheen),
        NEKO_MUSHEEN("NekoMusheenIcon", R.mipmap.ic_launcher_musheen_background, R.drawable.icon_neko_musheen_foreground_round, R.string.AppIconMusheen),
        NEKO_RAINBOW("NekoRainbowIcon", R.mipmap.ic_launcher_rainbow_background, R.drawable.icon_neko_rainbow_foreground_round, R.string.AppIconRainbow),
        NEKO_SPACE("NekoSpaceIcon", R.mipmap.ic_launcher_space_background, R.drawable.icon_neko_space_foreground_round, R.string.AppIconSpace),
        NEKO_NEON("NekoNeonIcon", R.mipmap.ic_launcher_neon_background, R.drawable.icon_neko_neon_foreground_round, R.string.AppIconNeon),
        VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, R.mipmap.icon_6_foreground_sa, R.string.AppIconVintage),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconAqua),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground_sa, R.string.AppIconPremium),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground_sa, R.string.AppIconTurbo),
        NOX("NoxIcon", R.mipmap.icon_2_background_sa, R.mipmap.icon_foreground_sa, R.string.AppIconNox);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }

        public boolean isNekoX() {
            return this == DEFAULT || this == COLORED || this == OLD_CLASSIC;
        }
    }
}
