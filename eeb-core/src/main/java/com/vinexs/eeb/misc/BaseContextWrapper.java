package com.vinexs.eeb.misc;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class BaseContextWrapper extends ContextWrapper {

    private Context baseContext;
    SharedPreferences sharePref;

    public BaseContextWrapper(Context base) {
        super(base);
        baseContext = base;
        sharePref = PreferenceManager.getDefaultSharedPreferences(this);

        recoverLocale();
    }

    /**
     * Override application original locale.
     */
    public void recoverLocale() {
        String defaultLocale = Locale.getDefault().toString();
        String appLocale = sharePref.getString("locale", defaultLocale);
        if (!appLocale.isEmpty() && !defaultLocale.equals(appLocale)) {
            Locale locale;
            if (appLocale.contains("_")) {
                String[] localePart = appLocale.split("_");
                locale = new Locale(localePart[0], localePart[1]);
            } else {
                locale = new Locale(appLocale);
            }
            Locale.setDefault(locale);
            Configuration config = baseContext.getResources().getConfiguration();
            setSystemLocale(config, locale);
        }
    }

    @SuppressWarnings("deprecation")
    public void setSystemLocale(Configuration config, Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            baseContext = baseContext.createConfigurationContext(config);
        } else {
            config.locale = locale;
            baseContext.getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }


}
