package utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.SignBean;
import net.sf.json.JSONArray;

import java.util.List;

public class JsonUtil {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将对象转换成json字符串。
     * @param data
     * @return
     */
    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @return
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     * <p>Title: jsonToList</p>
     * <p>Description: </p>
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> String Array2Json(List<T> beans) {
        //创建json集合
        for (T bean : beans) {
            System.out.println(objectToJson(bean));
        }
        JSONArray jsonArray = JSONArray.fromObject(beans);
        return jsonArray.toString();
    }

    public static <T> List<T> Json2Array(String jsonData, Class<T> beanType) {
        if (jsonData.isEmpty()) {
            return null;
        }
        JSONArray jsonArray = JSONArray.fromObject(jsonData);
        //Java集合
        List<T> list = (List<T>) jsonArray.toCollection(jsonArray, beanType);
        return list;
    }

    public static SignBean readSignInfo(String signConfig) {
        String jsonData = FileUtil.readContent(signConfig);
        JSONObject jsonObject = JSONObject.parseObject(jsonData);
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String jarPath = jsonObject.getString("jarPath");
        SignBean signBean = new SignBean(username, password, jarPath);
        return signBean;
    }
}
