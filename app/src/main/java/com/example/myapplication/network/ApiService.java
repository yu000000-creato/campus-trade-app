package com.example.myapplication.network;

import android.util.Log;

import com.example.myapplication.model.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiService {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson;

    private static ApiService instance;

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public Gson getGson() {
        return gson;
    }

    public String getRaw(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("网络请求失败: " + response.code());
            }
            String json = response.body().string();
            Log.d("ApiService", "GET Response: " + json);
            return json;
        }
    }

    public <T> Result<T> get(String endpoint, Class<T> clazz) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return new Result<>(500, "网络请求失败", null);
            }
            String json = response.body().string();
            Log.d("ApiService", "GET Response: " + json);
            // 使用 ParameterizedType 来处理泛型
            java.lang.reflect.Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, Result.class, clazz);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            Log.e("ApiService", "GET Error: " + e.getMessage());
            return new Result<>(500, "网络异常: " + e.getMessage(), null);
        }
    }

    public <T> Result<List<T>> getList(String endpoint, Class<T> clazz) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return new Result<>(500, "网络请求失败", null);
            }
            String json = response.body().string();
            Log.d("ApiService", "GET List Response: " + json);
            // 使用 ParameterizedType 来处理嵌套泛型 Result<List<T>>
            java.lang.reflect.Type listType = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, List.class, clazz);
            java.lang.reflect.Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, Result.class, listType);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            Log.e("ApiService", "GET List Error: " + e.getMessage());
            return new Result<>(500, "网络异常: " + e.getMessage(), null);
        }
    }

    public <T> Result<T> post(String endpoint, String jsonBody, Class<T> clazz) {
        String fullUrl = BASE_URL + endpoint;
        Log.d("ApiService", "POST URL: " + fullUrl);
        Log.d("ApiService", "POST Body: " + jsonBody);
        
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Log.d("ApiService", "Response Code: " + response.code());
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null";
                Log.e("ApiService", "Request failed, code: " + response.code() + ", body: " + errorBody);
                return new Result<>(response.code(), "请求失败: " + errorBody, null);
            }
            String json = response.body().string();
            Log.d("ApiService", "POST Response: " + json);
            // 使用 ParameterizedType 来处理泛型
            java.lang.reflect.Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, Result.class, clazz);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            Log.e("ApiService", "POST IOException: " + e.getMessage());
            Log.e("ApiService", "Error Class: " + e.getClass().getName());
            String errorMsg = "网络异常: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                errorMsg = "连接被拒绝，请检查服务器是否启动";
            } else if (e.getMessage() != null && e.getMessage().contains("timed out")) {
                errorMsg = "连接超时，请检查网络";
            }
            return new Result<>(500, errorMsg, null);
        }
    }

    public <T> Result<T> postForm(String endpoint, String formData, Class<T> clazz) {
        RequestBody body = RequestBody.create(formData, MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return new Result<>(500, "网络请求失败", null);
            }
            String json = response.body().string();
            Log.d("ApiService", "POST Form Response: " + json);
            // 使用 ParameterizedType 来处理泛型
            java.lang.reflect.Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, Result.class, clazz);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            Log.e("ApiService", "POST Form Error: " + e.getMessage());
            return new Result<>(500, "网络异常: " + e.getMessage(), null);
        }
    }

    public <T> Result<T> delete(String endpoint, Class<T> clazz) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return new Result<>(500, "网络请求失败", null);
            }
            String json = response.body().string();
            Log.d("ApiService", "DELETE Response: " + json);
            // 使用 ParameterizedType 来处理泛型
            java.lang.reflect.Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, Result.class, clazz);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            Log.e("ApiService", "DELETE Error: " + e.getMessage());
            return new Result<>(500, "网络异常: " + e.getMessage(), null);
        }
    }

    private static class LocalDateTimeTypeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        private static final java.time.format.DateTimeFormatter FORMATTER = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.format(FORMATTER));
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateTimeStr = in.nextString();
            try {
                return LocalDateTime.parse(dateTimeStr, FORMATTER);
            } catch (Exception e) {
                Log.e("ApiService", "LocalDateTime parse error: " + e.getMessage());
                return null;
            }
        }
    }
}