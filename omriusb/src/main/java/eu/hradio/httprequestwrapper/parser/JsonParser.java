package eu.hradio.httprequestwrapper.parser;

import eu.hradio.httprequestwrapper.annotations.json.*;
import org.json.*;
import eu.hradio.httprequestwrapper.exception.*;
import java.util.*;
import eu.hradio.httprequestwrapper.util.*;
import java.lang.reflect.*;

public class JsonParser
{
    private boolean allowNullValues;
    
    public JsonParser() {
        this(true);
    }
    
    public JsonParser(final boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }
    
    public <T> JSONObject toJSON(final T obj) throws JsonEncoderTypeMismatch {
        if (obj == null) {
            return null;
        }
        final JSONObject jsonObject = new JSONObject();
        final Class<?> tClass = obj.getClass();
        final Field[] declaredFields;
        final Field[] fields = declaredFields = tClass.getDeclaredFields();
        for (final Field f : declaredFields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    if (Modifier.isPrivate(f.getModifiers())) {
                        f.setAccessible(true);
                    }
                    try {
                        if (f.get(obj) == null && !this.allowNullValues) {
                            throw new NullPointerException("No such element: " + f.getName());
                        }
                        final Class<?> type = f.getType();
                        String propertyName = f.getName();
                        if (f.get(obj) != null) {
                            final JsonProperty annotatedName = f.getAnnotation(JsonProperty.class);
                            if (annotatedName != null) {
                                propertyName = annotatedName.name();
                            }
                            if (Map.class.isAssignableFrom(type)) {
                                final Map<?, ?> objects = (Map<?, ?>)f.get(obj);
                                final JSONObject jsonRep = new JSONObject();
                                if (objects != null) {
                                    for (final Map.Entry<?, ?> entry : objects.entrySet()) {
                                        jsonRep.put(this.resolveMapKey(entry.getKey()), (Object)entry.getValue());
                                    }
                                }
                                jsonObject.put(propertyName, (Object)jsonRep);
                            }
                            else if (type.isPrimitive()) {
                                jsonObject.put(propertyName, f.get(obj));
                            }
                            else if (type.getSimpleName().equals(String.class.getSimpleName())) {
                                jsonObject.put(propertyName, f.get(obj));
                            }
                            else if (type.isArray()) {
                                jsonObject.put(propertyName, (Object)this.toJSONArray(f, obj));
                            }
                            else if (type.isEnum()) {
                                jsonObject.put(propertyName, f.get(obj));
                            }
                            else {
                                jsonObject.put(propertyName, (Object)this.toJSON(f.get(obj)));
                            }
                        }
                    }
                    catch (JSONException e) {
                        throw new JsonEncoderTypeMismatch(obj, (Throwable)e);
                    }
                    catch (IllegalAccessException e2) {
                        throw new JsonEncoderTypeMismatch(obj, e2);
                    }
                }
            }
        }
        return jsonObject;
    }
    
    private String resolveMapKey(final Object o) throws JsonEncoderTypeMismatch {
        final String typeName = o.getClass().getSimpleName();
        if (typeName.equals(String.class.getSimpleName())) {
            return (String)o;
        }
        if (typeName.equals(Integer.TYPE.getSimpleName()) || typeName.equals(Integer.class.getSimpleName()) || typeName.equals(Boolean.TYPE.getSimpleName()) || typeName.equals(Boolean.class.getSimpleName()) || typeName.equals(Long.TYPE.getSimpleName()) || typeName.equals(Long.class.getSimpleName()) || typeName.equals(Double.TYPE.getSimpleName()) || typeName.equals(Double.class.getSimpleName()) || typeName.equals(Date.class.getSimpleName())) {
            return String.valueOf(o);
        }
        return this.toJSON(o).toString();
    }
    
    private <T> JSONArray toJSONArray(final Field f, final T obj) throws IllegalAccessException, JsonEncoderTypeMismatch {
        final JSONArray jsonArray = new JSONArray();
        if (f.get(obj) == null) {
            return null;
        }
        for (int i = 0; i < Array.getLength(Objects.requireNonNull(f.get(obj))); ++i) {
            if (f.getType().getComponentType().isPrimitive()) {
                jsonArray.put(Array.get(Objects.requireNonNull(f.get(obj)), i));
            }
            else if (f.getType().getComponentType().getSimpleName().equals(String.class.getSimpleName())) {
                jsonArray.put(Array.get(Objects.requireNonNull(f.get(obj)), i));
            }
            else {
                jsonArray.put((Object)this.toJSON(Array.get(Objects.requireNonNull(f.get(obj)), i)));
            }
        }
        return jsonArray;
    }
    
    public <T> T parse(final Class<T> objType, final JSONObject jsonObject) throws JsonDecoderTypeMismatch {
        try {
            final T res = objType.newInstance();
            final Field[] declaredFields;
            final Field[] fields = declaredFields = objType.getDeclaredFields();
            for (final Field f : declaredFields) {
                Label_0244: {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        if (!Modifier.isFinal(f.getModifiers())) {
                            if (Modifier.isPrivate(f.getModifiers())) {
                                f.setAccessible(true);
                            }
                            String propertyName = f.getName();
                            final JsonProperty annotation = f.getAnnotation(JsonProperty.class);
                            if (annotation != null) {
                                propertyName = annotation.name();
                                if (annotation.isJsonArray()) {
                                    String jsonString = jsonObject.getString(propertyName);
                                    if (jsonString.isEmpty() || jsonString.equals("null")) {
                                        jsonString = "[]";
                                    }
                                    this.parseArray(f, res, new JSONArray(jsonString));
                                    break Label_0244;
                                }
                                if (annotation.isJsonObject()) {
                                    final JSONObject json = new JSONObject(jsonObject.getString(propertyName));
                                    final JSONObject newJson = new JSONObject();
                                    newJson.put(propertyName, (Object)json);
                                    this.parseField(f, res, new JSONObject(), propertyName);
                                    break Label_0244;
                                }
                            }
                            this.parseField(f, res, jsonObject, propertyName);
                        }
                    }
                }
            }
            return res;
        }
        catch (IllegalAccessException e) {
            throw new JsonDecoderTypeMismatch(jsonObject.toString(), e);
        }
        catch (InstantiationException e2) {
            throw new JsonDecoderTypeMismatch(jsonObject.toString(), e2);
        }
        catch (JSONException e3) {
            throw new JsonDecoderTypeMismatch(jsonObject.toString(), (Throwable)e3);
        }
    }
    
    private <T> void parseField(final Field f, final T res, final JSONObject jsonObject, final String propertyName) throws JSONException, IllegalAccessException, JsonDecoderTypeMismatch {
        if ((!jsonObject.has(propertyName) || jsonObject.isNull(propertyName)) && this.allowNullValues) {
            return;
        }
        final Class<?> paramType = f.getType();
        final String typeName = paramType.getSimpleName();
        if (Map.class.isAssignableFrom(paramType)) {
            final ParameterizedType type = (ParameterizedType)f.getGenericType();
            final Type key = type.getActualTypeArguments()[0];
            final Type value = type.getActualTypeArguments()[1];
            final Map<Object, Object> map = new HashMap<Object, Object>();
            if (jsonObject.has(propertyName)) {
                final JSONObject mapAsJson = jsonObject.getJSONObject(propertyName);
                final Iterator<String> objectIterator = (Iterator<String>)mapAsJson.keys();
                while (objectIterator.hasNext()) {
                    final String keyString = objectIterator.next();
                    final Object entryValue = (mapAsJson.get(keyString) instanceof JSONObject) ? this.parse(value.getClass(), mapAsJson.getJSONObject(keyString)) : mapAsJson.get(keyString);
                    final Object entryKey = this.isValidJson(keyString) ? this.parse(key.getClass(), new JSONObject(keyString)) : this.primitiveWrapperFromString(keyString, key.getClass());
                    map.put(entryKey, entryValue);
                }
            }
            f.set(res, map);
        }
        else if (typeName.equals(Integer.TYPE.getSimpleName()) || typeName.equals(Integer.class.getSimpleName())) {
            f.set(res, jsonObject.getInt(propertyName));
        }
        else if (typeName.equals(Boolean.TYPE.getSimpleName()) || typeName.equals(Boolean.class.getSimpleName())) {
            f.set(res, jsonObject.getBoolean(propertyName));
        }
        else if (typeName.equals(Long.TYPE.getSimpleName()) || typeName.equals(Long.class.getSimpleName())) {
            f.set(res, jsonObject.getLong(propertyName));
        }
        else if (typeName.equals(Double.TYPE.getSimpleName()) || typeName.equals(Double.class.getSimpleName())) {
            f.set(res, jsonObject.getDouble(propertyName));
        }
        else if (typeName.equals(String.class.getSimpleName())) {
            f.set(res, jsonObject.getString(propertyName));
        }
        else if (typeName.equals(Date.class.getSimpleName())) {
            f.set(res, TimeUtils.dateFromString(jsonObject.getString(propertyName)));
        }
        else if (paramType.isArray()) {
            this.parseArray(f, res, jsonObject.getJSONArray(propertyName));
        }
        else if (paramType.isEnum()) {
            f.set(res, Enum.valueOf(paramType.asSubclass(Enum.class), jsonObject.getString(propertyName)));
        }
        else {
            f.set(res, this.parse(paramType, jsonObject.getJSONObject(propertyName)));
        }
    }
    
    private Object primitiveWrapperFromString(final String keyString, final Class<?> clazz) {
        if (clazz.getSimpleName().equals(Integer.class.getSimpleName())) {
            return Integer.valueOf(keyString);
        }
        if (clazz.getSimpleName().equals(Double.class.getSimpleName())) {
            return Double.valueOf(keyString);
        }
        if (clazz.getSimpleName().equals(Long.class.getSimpleName())) {
            return Long.valueOf(keyString);
        }
        if (clazz.getSimpleName().equals(Boolean.class.getSimpleName())) {
            return Boolean.valueOf(keyString);
        }
        if (clazz.getSimpleName().equals(Float.class.getSimpleName())) {
            return Float.valueOf(keyString);
        }
        return keyString;
    }
    
    private boolean isValidJson(final String keyString) {
        try {
            new JSONObject(keyString);
            return true;
        }
        catch (JSONException e) {
            return false;
        }
    }
    
    private <T> void parseArray(final Field f, final T obj, final JSONArray jsonArray) throws IllegalAccessException, JSONException, JsonDecoderTypeMismatch {
        final Class<?> componentType = f.getType().getComponentType();
        f.set(obj, Array.newInstance(componentType, jsonArray.length()));
        final String componentTypeName = componentType.getSimpleName();
        for (int i = 0; i < jsonArray.length(); ++i) {
            if (componentTypeName.equals(Integer.TYPE.getSimpleName())) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, jsonArray.getInt(i));
            }
            else if (componentTypeName.equals(Boolean.TYPE.getSimpleName())) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, jsonArray.getBoolean(i));
            }
            else if (componentTypeName.equals(Long.TYPE.getSimpleName())) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, jsonArray.getLong(i));
            }
            else if (componentTypeName.equals(Double.TYPE.getSimpleName())) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, jsonArray.getDouble(i));
            }
            else if (componentTypeName.equals(String.class.getSimpleName())) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, jsonArray.getString(i));
            }
            else if (componentType.isEnum()) {
                Array.set(Objects.requireNonNull(f.get(obj)), i, Enum.valueOf(componentType.asSubclass(Enum.class), jsonArray.getString(i)));
            }
            else {
                Array.set(Objects.requireNonNull(f.get(obj)), i, this.parse(componentType, jsonArray.getJSONObject(i)));
            }
        }
    }
}
