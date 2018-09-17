package com.neurotec.tutorials.biometrics;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * A Json utility class.
 *
 */
public class JsonUtil {
    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}
