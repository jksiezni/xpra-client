/**
 * 
 */
package com.github.jksiezni.xpra;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Looper;

/**
 * @author Jakub Księżniak
 *
 */
public abstract class UiTask<Params, Result> implements Runnable {

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>();
	
	private volatile Params[] params;

	public UiTask() {
	}
	
	@SuppressWarnings("unchecked")
	protected abstract void doOnUIThread(Params... params);

	@Override
	public final void run() {
		doOnUIThread(params);
	}
	
	protected void postResult(Result result) {
		resultQueue.add(result);
	}
	
	public final UiTask<Params, Result> execute() {
		this.params = null;
		handler.post(this);
		return this;
	}
	
	@SafeVarargs
	public final UiTask<Params, Result> execute(Params... params) {
		this.params = params;
		handler.post(this);
		return this;
	}
	
	public Result get() throws InterruptedException {
		return resultQueue.take();
	}
	
}
