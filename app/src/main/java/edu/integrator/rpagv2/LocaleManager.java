package edu.integrator.rpagv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.StringDef;

import java.util.Locale;

public class LocaleManager {
    @StringDef({ ENGLISH, SPANISH, AUTO })
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = { ENGLISH, SPANISH, AUTO };
    }
    static final String ENGLISH = "en";
    static final String SPANISH = "es";
    static final String AUTO = "auto";

    private static final String LANGUAGE_KEY = "language_key"; //Key para SharedPreferences

    /**
     * Recuperar el dato con LANGUAGE_KEY de SharedPreferences. De no encontrarlo retornar AUTO
     */
    public static String getLanguagePref(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(LANGUAGE_KEY, AUTO);
    }

    /**
     * Recibir el localeKey y guardarlo bajo LANGUAGE_KEY en SharedPreferences
     */
    private static void setLanguagePref(Context context, String localeKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(LANGUAGE_KEY, localeKey).apply();
    }

    /**
     * Recupera el lenguaje del sistema
     */
    public static String getSystemLanguage() {
        String language = Locale.getDefault().getISO3Language();
        switch (language){
            case "eng":
                return ENGLISH;
            case "spa":
                return SPANISH;
            default:
                return ENGLISH;
        }
    }




    /**
     * Cambia el lenguaje en la Configuration del context dado.
     */
    private static Context updateResources(Context context, String language) {
        if(language.equals(AUTO)){
            language = getSystemLanguage();
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    /**
     * Actualiza los recursos de acuerdo al Locale del Context dado
     */
    public static Context setLocale(Context mContext) {
        return updateResources(mContext, getLanguagePref(mContext));
    }

    /**
     * Actualiza el Context dado con el nuevo lenguaje y recarga los recursos
     */
    public static Context setNewLocale(Context mContext, @LocaleDef String language) {
        setLanguagePref(mContext, language);
        return updateResources(mContext, language);
    }
}
