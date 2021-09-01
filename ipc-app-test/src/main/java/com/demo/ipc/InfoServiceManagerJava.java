package com.demo.ipc;

import android.util.Log;

import com.ipc.extend.test.InfoService;
import com.ipc.extend.test.UserInfo;
import com.zclever.ipc.core.Result;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class InfoServiceManagerJava implements InfoService {
    private static final String TAG = "InfoServiceManagerJava";

    @Override
    public void sum(int a, int b, int c, @NotNull Result<Integer> result) {
        result.onSuccess(a + b + c);
    }

    @Override
    public void sendBigData(@NotNull byte[] data) {
        Log.i(TAG, "sendBigData: " + Arrays.toString(data));
    }

    private static final class Holder {
        private static final InfoServiceManagerJava instance = new InfoServiceManagerJava();
    }

    private InfoServiceManagerJava() {

    }

    public static InfoServiceManagerJava getInstance() {
        return Holder.instance;
    }

    @Override
    public void asyncGetUserInfo(@NotNull Result<UserInfo> callBack) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callBack.onSuccess(new UserInfo("asyncGetUserInfo", 24));
            }
        });

    }

    @NotNull
    @Override
    public UserInfo syncGetUserInfo() {
        return new UserInfo("syncGetUserInfo", 18);
    }
}
