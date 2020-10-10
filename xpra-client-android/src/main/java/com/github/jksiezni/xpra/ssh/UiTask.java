/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.jksiezni.xpra.ssh;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;
import android.os.Looper;


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
