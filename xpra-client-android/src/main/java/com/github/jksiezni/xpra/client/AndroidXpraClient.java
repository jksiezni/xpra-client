/*
 * Copyright (C) 2017 Jakub Ksiezniak
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

package com.github.jksiezni.xpra.client;

import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.github.jksiezni.xpra.R;

import java.util.ArrayDeque;
import java.util.Deque;

import xpra.client.XpraClient;
import xpra.client.XpraWindow;
import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.NewWindow;

/**
 * @author Jakub Księżniak
 *
 */
public class AndroidXpraClient extends XpraClient {
	private static final int ADD_WINDOW = 1;
	private static final int DEL_WINDOW = 2;
	private static final int PUSH_WINDOW = 3;
	private static final int POP_WINDOW = 4;
	private static final int CLOSE_WINDOW = 5;

	private static final PictureEncoding[] PICTURE_ENCODINGS = { PictureEncoding.png, PictureEncoding.pngL, PictureEncoding.pngP, PictureEncoding.jpeg };

	private final RelativeLayout workspace;
	private OnStackListener stackListener;

	private final Handler handler = new Handler(new Handler.Callback() {
		private final Deque<AndroidXpraWindow> stack = new ArrayDeque<>();

		@Override
		public boolean handleMessage(Message msg) {
			AndroidXpraWindow window;
			switch (msg.what) {
			case ADD_WINDOW:
				window = (AndroidXpraWindow) msg.obj;
				if(!window.hasParent()) {
					pushToStack(window);
				} else {
					showWindow(window);
				}
				break;
			case DEL_WINDOW:
				window = (AndroidXpraWindow) msg.obj;
				hideWindow(window);
				break;
			case PUSH_WINDOW:
				window = (AndroidXpraWindow) msg.obj;
				pushToStack(window);
				break;
			case POP_WINDOW:
				window = (AndroidXpraWindow) msg.obj;
				removeFromStack(window);
				break;
			case CLOSE_WINDOW:
				window = stack.peek();
				removeFromStack(window);
				if(window != null) {
					window.close();
				}
			}
			return true;
		}

		private void hideWindow(AndroidXpraWindow window) {
			if(window != null) {
  			workspace.removeView(window.getView());
  			removeFromStack(window);
			}
		}

		private void showWindow(AndroidXpraWindow window) {
			if(window != null) {
				if(!window.getView().isShown()) {
					workspace.addView(window.getView());
				}
			}
		}
		
		protected void pushToStack(AndroidXpraWindow window) {
			AndroidXpraWindow prev = stack.peek();
			if(window == prev) {
				return;
			}
			removeFromStack(window);
			stack.push(window);
			showWindow(window);
			if (stackListener != null) {
				stackListener.onWindowPushed(window);
			}
		}

		protected void removeFromStack(AndroidXpraWindow window) {
			if (stack.remove(window)) {
				hideWindow(window);
				if(stackListener != null) {
					stackListener.onWindowPoped(window);
				}
			}
		}
	});

	/**
	 * @param workspaceView The workspace where all windows are placed.
	 */
	public AndroidXpraClient(RelativeLayout workspaceView) {
		super(0, 0, PICTURE_ENCODINGS, new AndroidXpraKeyboard());
		this.workspace = workspaceView;

		final DisplayMetrics dm = workspaceView.getContext().getResources().getDisplayMetrics();
		setDesktopSize(dm.widthPixels, dm.heightPixels);
		//setDpi(dm.densityDpi, (int) dm.xdpi, (int) dm.ydpi);
	}

	@Override
	protected XpraWindow onCreateWindow(NewWindow wnd) {
		return new AndroidXpraWindow(wnd, workspace.getContext());
	}

	@Override
	protected void onWindowStarted(XpraWindow window) {
		super.onWindowStarted(window);
		handler.obtainMessage(ADD_WINDOW, window).sendToTarget();
	}

	@Override
	protected void onDestroyWindow(XpraWindow window) {
		super.onDestroyWindow(window);
		handler.obtainMessage(DEL_WINDOW, window).sendToTarget();
	}
	
	@Override
	protected void onCursorUpdate(CursorPacket cursorPacket) {
		super.onCursorUpdate(cursorPacket);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_close:
			handler.obtainMessage(CLOSE_WINDOW).sendToTarget();
			return true;
		case R.id.action_zoomin:
			return true;
		case R.id.action_zoomout:
			return true;

		default:
			return false;
		}
	}

	public boolean onWindowSelected(int windowId) {
		final XpraWindow window = getWindow(windowId);
		if(window != null) {
			handler.obtainMessage(PUSH_WINDOW, window).sendToTarget();
			return true;
		}
		return false;
	}

	public void setStackListener(OnStackListener listener) {
		this.stackListener = listener;
	}

	public interface OnStackListener {
		void onWindowPushed(AndroidXpraWindow window);
		void onWindowPoped(AndroidXpraWindow window);
	}

	public void onResume() {
		
	}

	public void onPause() {
		
	}

}
