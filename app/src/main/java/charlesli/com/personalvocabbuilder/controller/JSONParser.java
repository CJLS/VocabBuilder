package charlesli.com.personalvocabbuilder.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by charles on 2016-05-24.
 */
public class JSONParser {

    public static String parseJSONForTranslation(String jsonString) {
        try {
            JSONObject object = (JSONObject) new JSONTokener(jsonString).nextValue();
            return object.getJSONObject("data").getJSONArray("translations").
                    getJSONObject(0).getString("translatedText");
        }
        catch (JSONException e) {
            return null;
        }
    }
}
