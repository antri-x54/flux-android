package org.nikanikoo.flux.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatDelegate;
import org.nikanikoo.flux.R;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_THEME_STYLE = "theme_style";
    private static final String KEY_CONTRAST_MODE = "contrast_mode";
    private static final String KEY_DYNAMIC_COLORS = "dynamic_colors";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_MATERIAL_YOU = 1;
    public static final int STYLE_GREEN = 2;
    public static final int STYLE_PURPLE = 3;
    public static final int STYLE_RED = 4;

    public static final int CONTRAST_NORMAL = 0;
    public static final int CONTRAST_MEDIUM = 1;
    public static final int CONTRAST_HIGH = 2;
    
    private static ThemeManager instance;
    private final SharedPreferences prefs;
    private final Context context;
    
    private ThemeManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        applyTheme(mode);
    }
    
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public boolean isDarkMode() {
        int mode = getThemeMode();
        if (mode == THEME_DARK) {
            return true;
        } else if (mode == THEME_LIGHT) {
            return false;
        } else {
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            return nightMode == AppCompatDelegate.MODE_NIGHT_YES;
        }
    }

    public static void applySystemBarsAppearance(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController windowInsetsController = activity.getWindow().getInsetsController();
            if (windowInsetsController != null) {
                ThemeManager themeManager = ThemeManager.getInstance(activity);
                boolean isLightTheme = !themeManager.isDarkMode();
                windowInsetsController.setSystemBarsAppearance(
                        isLightTheme ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }

    public void setThemeStyle(int style) {
        prefs.edit().putInt(KEY_THEME_STYLE, style).apply();
    }
    
    public int getThemeStyle() {
        return prefs.getInt(KEY_THEME_STYLE, STYLE_DEFAULT);
    }

    public void setContrastMode(int contrast) {
        prefs.edit().putInt(KEY_CONTRAST_MODE, contrast).apply();
    }
    
    public int getContrastMode() {
        return prefs.getInt(KEY_CONTRAST_MODE, CONTRAST_NORMAL);
    }

    public void setDynamicColors(boolean enabled) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply();
    }
    
    public boolean isDynamicColorsEnabled() {
        return prefs.getBoolean(KEY_DYNAMIC_COLORS, false);
    }
    
    public boolean isDynamicColorsAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
    
    private void applyTheme(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    public void applySavedTheme() {
        applyTheme(getThemeMode());
    }

    public void applyThemeToActivity(Activity activity) {
        int themeResId = getThemeResourceId();
        int style = getThemeStyle();

        Logger.d("ThemeManager", "Applying theme - Style: " + style +
            " (MATERIAL_YOU=" + STYLE_MATERIAL_YOU + "), Theme ID: " + themeResId);
        
        activity.setTheme(themeResId);
    }

    public int getThemeResourceId() {
        int style = getThemeStyle();
        int contrast = getContrastMode();

        Logger.d("ThemeManager", "getThemeResourceId - style: " + style +
            ", STYLE_MATERIAL_YOU: " + STYLE_MATERIAL_YOU + 
            ", isDynamicColorsAvailable: " + isDynamicColorsAvailable());

        if (style == STYLE_MATERIAL_YOU && isDynamicColorsAvailable()) {
            Logger.d("ThemeManager", "Returning DynamicColors theme");
            return getContrastTheme(R.style.Theme_Flux_DynamicColors, contrast);
        }

        switch (style) {
            case STYLE_GREEN:
                Logger.d("ThemeManager", "Returning Green theme");
                return getContrastTheme(R.style.Theme_Flux_Green, contrast);
            case STYLE_PURPLE:
                Logger.d("ThemeManager", "Returning Purple theme");
                return getContrastTheme(R.style.Theme_Flux_Purple, contrast);
            case STYLE_RED:
                Logger.d("ThemeManager", "Returning Red theme");
                return getContrastTheme(R.style.Theme_Flux_Red, contrast);
            case STYLE_DEFAULT:
            default:
                Logger.d("ThemeManager", "Returning Default (Flux) theme");
                return getContrastTheme(R.style.Theme_Flux, contrast);
        }
    }

    private int getContrastTheme(int baseTheme, int contrast) {
        switch (contrast) {
            case CONTRAST_HIGH:
                if (baseTheme == R.style.Theme_Flux) {
                    return R.style.Theme_Flux_HighContrast;
                }
                break;
            case CONTRAST_MEDIUM:
                if (baseTheme == R.style.Theme_Flux) {
                    return R.style.Theme_Flux_MediumContrast;
                }
                break;
        }
        return baseTheme;
    }
    
    public String getThemeName(int mode) {
        switch (mode) {
            case THEME_LIGHT:
                return "Светлая";
            case THEME_DARK:
                return "Темная";
            case THEME_SYSTEM:
            default:
                return "Системная";
        }
    }
    
    public String getStyleName(int style) {
        switch (style) {
            case STYLE_MATERIAL_YOU:
                return "Material You";
            case STYLE_GREEN:
                return "Зеленая";
            case STYLE_PURPLE:
                return "Фиолетовая";
            case STYLE_RED:
                return "Красная";
            case STYLE_DEFAULT:
            default:
                return "Flux (по умолчанию)";
        }
    }
    
    public String getContrastName(int contrast) {
        switch (contrast) {
            case CONTRAST_HIGH:
                return "Высокая";
            case CONTRAST_MEDIUM:
                return "Средняя";
            case CONTRAST_NORMAL:
            default:
                return "Обычная";
        }
    }
}
