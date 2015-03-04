/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * IntentService是用于提供异步请求（表示为意图）服务的基类。客户端通过android.content.Context.startService
 *（意向）调用发送请求;该Service再需要时候才启动，通过使用一个woker thread按照次序处理每一个Intent，在处理完所有
 * 工作后，他将停止。
 * 
 * <p>这样的"工作队列处理"模式通常被用来替代一个程序主线程中的任务，该IntentService类的存在是为了简化这个模式，
 * 并优化其结构。要是用它，扩展IntentService并且实现{@link #onHandleIntent(Intent)},IntentService将收到Intents,
 * 启动一个workerThread，并再适当的时候停止Service。
 * 
 * <p>所有的request再一个单一的worker thread中处理-- 他们可以根据需要使用任意长的时间（并且不会阻塞程序的主线程）），
 * 但是同一时间只会处理一个请求。 
 * 
 * 
 * <p>
 * * * * * * ** * * * * ** * * * * ** * * * * ** * * * * ** * * * * ** * * * * *
 * <p>
 * IntentService is a base class for {@link Service}s that handle asynchronous
 * requests (expressed as {@link Intent}s) on demand.  Clients send requests
 * through {@link android.content.Context#startService(Intent)} calls; the
 * service is started as needed, handles each Intent in turn using a worker
 * thread, and stops itself when it runs out of work.
 *
 * <p>This "work queue processor" pattern is commonly used to offload tasks
 * from an application's main thread.  The IntentService class exists to
 * simplify this pattern and take care of the mechanics.  To use it, extend
 * IntentService and implement {@link #onHandleIntent(Intent)}.  IntentService
 * will receive the Intents, launch a worker thread, and stop the service as
 * appropriate.
 *
 * <p>All requests are handled on a single worker thread -- they may take as
 * long as necessary (and will not block the application's main loop), but
 * only one request will be processed at a time.
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For a detailed discussion about how to create services, read the
 * <a href="{@docRoot}guide/topics/fundamentals/services.html">Services</a> developer guide.</p>
 * </div>
 *
 * @see android.os.AsyncTask
 */
public abstract class IntentService extends Service {
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;//IntentService的Service名字
    private boolean mRedelivery;//重新交付

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
            stopSelf(msg.arg1);
        }
    }

    /**
     *创建一个IntentService，在你的实现类中需要调用该方法。
     *
     *
     *
     *<p>
     * * * * * * * 
     *  <p>* * 
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * 
     * @param name Used to name the worker thread, important only for debugging.
     *             用来命名给worker thread，只有在debugging时候重要*
     */
    public IntentService(String name) {
        super();
        mName = name;
    }

    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     *
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     *
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    /**
     * You should not override this method for your IntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the IntentService
     * receives a start request.
     * 
     * 在IntentService你不需要override这个方法，而是去override{@link #onHandleIntent}，onHandleIntent方法将
     * 在接收到一个start request的时候被系统调用*
     * * * 
     * @see android.app.Service#onStartCommand
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    /**
     * 因为默认的实现方法返回null，除非你要提供对service的绑定，你不需要实现这个方法。
     * * *
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null. 
     * @see android.app.Service#onBind
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 该方法再worker thread线程中被调用，用来处理一个请求。在一个时间只能处理一个Intent，但是这些请求
     * 是在合程序其他逻辑相独立的一个线程中处理的。所以，若果这里的代码需要处理很长时间，将会导致其他发送到该service的
     * 请求被挂起。当所有的请求都被处理完了，这个IntentService将自行停止。所以，你不需要自己来调用{@link #stopSelf}
     * * *
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     */
    protected abstract void onHandleIntent(Intent intent);
}
