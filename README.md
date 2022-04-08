## android-ipc
这是一个在安卓平台上运行的ipc的库，让ipc通信更加简单。它具有以下特点：

1. 支持自定义接口来实现跨进程通信，比传统的aidl的方式更简单
2. 支持异步回调的方式返回数据，也支持设置监听器的方式
3. 服务注册支持自动注册
4. 突破binder驱动限制，支持大数据传输(目前还在完善中，目前已支持客户端进程向服务端进程传输大于1M以上的字节数据)

## 引入库

**1.**添加jitpack仓库到工程当中

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

 **2.**添加依赖

```groovy
dependencies {
	implementation 'com.github.zhanggaoming.android-ipc:ipc-core:3.0.0'
}
```



## 如何使用

- ##### 统一定义通信接口，接口用BindImpl注解修饰，注解的值为服务端实现这个接口的全类名：

```kotlin
@BindImpl("com.demo.ipc.InfoServiceManager")
interface InfoService {

    fun asyncGetUserInfo(callBack: Result<UserInfo>)

    fun syncGetUserInfo(): UserInfo

    fun sum(a: Int, b: Int, c: Int, result: Result<Int>)

    fun sendBigData(@BigData data: ByteArray)

    fun getEnum(code: Code): Code

    fun setEventCallBack(callBack: Result<Event>)

}

enum class Code {
    SUCCESS, FAILURE
}


data class Event(val id: Int)


data class UserInfo(val name: String, val age: Int)
```

上述接口定义支持回调的方式，定义回调的接口返回，必须使用**Result**类来承载

- ##### 服务端进程：

首先服务端进程需要声明在AndroidManifest.xml里面注册两个service：

```xml
<service android:name="com.zclever.ipc.core.server.VideoCenter" android:exported="true"/>
<service android:name="com.zclever.ipc.core.server.ServiceCenter" android:exported="true"/>
```



服务端实现上述接口，kotlin代码需要是object类：

```kotlin
object InfoServiceManager : InfoService {

    //获取userInfo，走的是回调的方式
    override fun asyncGetUserInfo(callBack: Result<UserInfo>) {
        thread {
            callBack.onData(UserInfo("asyncGetUserInfo", 20))
        }
    }


    override fun syncGetUserInfo(): UserInfo {
        return UserInfo("syncGetUserInfo", 18)
    }


    override fun sum(a: Int, b: Int, c: Int, result: Result<Int>) {
        result.onData(a + b + c)
    }

    override fun sendBigData(data: ByteArray) {
        Log.i(TAG, "sendBigData: ${data.contentToString()}")
    }

    override fun getEnum(code: Code): Code {
        Log.i(TAG, "getEnum: $code")
        return Code.SUCCESS
    }

    private var count=0

    private var mCallBack: Result<Event>? = null

    init {

        thread {//模拟回调事件回复客户端
            while (true) {
                mCallBack?.onData(Event(count++))

                Thread.sleep(2000)
            }
        }
    }

    override fun setEventCallBack(callBack: Result<Event>) {
        mCallBack = callBack
    }
}
```

java代码必须要写**getInstance**方法，返回自身，使用单例模式：

```java
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
}


```

服务端需要把服务注册到IpcManager当中，这里支持手动和自动注册：

- 手动注册，通过IpcManager注册，手动注册要提前，建议是在Application当中注册：

```kotlin
IpcManager.register(InfoService::class) //注册服务
```

- 自动注册，通过注解处理器来处理，在module中引入ipc-compiler模块：

```groovy
plugins {
    ...
    id 'kotlin-kapt'
}

或者

applay plugin:'kotlin-kapt'

dependencies {
	...
    kapt 'com.github.zhanggaoming.android-ipc:ipc-compiler:2.4'
}

```

引入ipc-compiler模块后，会自动找BindImpl注解修饰的接口并注册到IpcManager当中

- ##### 客户端进程

客户端进程主要就是使用IpcManager这个类来寻找服务：

**1.**初始化并连接服务端进程：

```kotlin
 IpcManager.init(this)//传入上下文
 IpcManager.open("com.demo.ipcdemo")//连接服务端，传入的是服务端的包名
```

**2.**发现服务：

*kotlin*

```kotlin
IpcManager.getService(InfoService::class)
IpcManager.getService<InfoService>()
```

*java*

```java
IpcManager.INSTANCE.getService(InfoService.class);
```

以下是客户端demo的主要代码：

```kotlin
class CommonActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CommonActivity"
    }

    val instance by lazy { IpcManager.getService<InfoService>() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        IpcManager.config(Config.builder().configDebug(true).build())
        IpcManager.init(this)
        IpcManager.open("com.demo.ipcdemo")
    }

    fun syncGetUserInfo(view: View) {

        Toast.makeText(this, instance.syncGetUserInfo().toString(), Toast.LENGTH_LONG).show()

        Log.i(TAG, "syncGetUserInfo: ->${instance.getEnum(Code.FAILURE)}")

    }


    fun asyncGetUserInfo(view: View) {

        instance.asyncGetUserInfo(object : Result<UserInfo>() {

            override fun onData(data: UserInfo) {
                runOnUiThread {

                    Toast.makeText(this@CommonActivity, data.toString(), Toast.LENGTH_LONG).show()
                }

            }

        })
    }

    fun sum(view: View) {

        instance.sum(1, 2, 3, object : Result<Int>() {
            override fun onData(data: Int) {
                runOnUiThread {
                    Toast.makeText(this@CommonActivity, "the sum is $data", Toast.LENGTH_LONG)
                        .show()
                }
            }
        })
    }

    fun sendBigData(view: View) {

        instance.sendBigData(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    }



    fun setEventCallBack(view: View) {

        instance.setEventCallBack(object : Result<Event>() {
            override fun onData(data: Event) {
                Log.i(TAG, "onData: ${data.id}")
            }
        })

    }


}
```

**3.**传输大数据：

- 传输大数据需要设计接口的时候，形参使用***BigData***修饰就可以了。需要注意的是在一个函数里面只能出现一次，而且修饰的类型必须为字节数组，不能是其他类型，考虑到需要传输大数据的场景用字节数组就足够了，接口设计如下*sendBigData*示例：

```
@BindImpl("com.demo.ipc.InfoServiceManager")
interface InfoService {

    fun sendBigData(@BigData data: ByteArray)

}
```

想要详细了解如何使用请参考demo：

https://github.com/zhanggaoming/android-ipc/tree/master/ipc-app-test

