package com.demo.ipc;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ipc.extend.test.AreaBean;
import com.ipc.extend.test.BaseResponse;
import com.ipc.extend.test.Code;
import com.ipc.extend.test.Event;
import com.ipc.extend.test.InfoService;
import com.ipc.extend.test.UserInfo;
import com.zclever.ipc.core.Result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class InfoServiceManagerJava implements InfoService {
    private static final String TAG = "InfoServiceManagerJava";

    @Override
    public void sum(int a, int b, int c, @NotNull Result<Integer> result) {
        result.onData(a + b + c);
    }

    @Override
    public void sendBigData(@NotNull byte[] data) {
        Log.i(TAG, "sendBigData: " + Arrays.toString(data));
    }

    @NotNull
    @Override
    public Code getEnum(Code code) {
        return Code.SUCCESS;
    }

    @Override
    public void setEventCallBack(@NotNull Result<Event> callBack) {

    }

    @Override
    public void setResponseCallBack(@NonNull Result<BaseResponse<Event>> callBack) {

    }

    @Override
    public void testBigData(int a, @NonNull byte[] data, @NonNull Result<byte[]> callBack) {

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
                callBack.onData(new UserInfo("asyncGetUserInfo", 24));
            }
        });

    }

    @NotNull
    @Override
    public UserInfo syncGetUserInfo() {
        return new UserInfo("syncGetUserInfo", 18);
    }


    @Override
    public int transformAreaBeans(@NonNull ArrayList<AreaBean> areaBeanList) {
        return 0;
    }

    @NonNull
    @Override
    public byte[] getBigByteArray() {
        return new byte[0];
    }


    @Override
    public void asyncGetBigByteArray(@NonNull Result<byte[]> callBack) {

    }
}
