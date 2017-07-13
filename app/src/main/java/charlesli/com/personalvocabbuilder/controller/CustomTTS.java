package charlesli.com.personalvocabbuilder.controller;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by charles on 2017-06-11.
 */

public class CustomTTS extends TextToSpeech {

    CustomTTS(Context context, OnInitListener listener, String engine) {
        super(context, listener, engine);
    }

    public HashMap<String, Locale> getSupportedDisplayNameToLocaleMapping() {
        HashMap<String, Locale> displayToLocaleHashMap = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Locale locale : getAvailableLanguages()) {
                displayToLocaleHashMap.put(locale.getDisplayName(), locale);
            }
        }
        else {
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale : locales) {
                int result = isLanguageAvailable(locale);
                if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE
                        || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                    displayToLocaleHashMap.put(locale.getDisplayName(), locale);
                }
            }
        }
        return displayToLocaleHashMap;
    }

}
