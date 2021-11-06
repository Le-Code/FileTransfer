package utils;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

    public static JSONObject readJsonFile(String signConfig) {
        String jsonData = FileUtil.readContent(signConfig);
        if (jsonData == null) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonData);
        return jsonObject;
    }
}
