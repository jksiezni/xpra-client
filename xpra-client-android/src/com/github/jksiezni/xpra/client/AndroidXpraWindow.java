/**
 * 
 */
package com.github.jksiezni.xpra.client;

import java.util.ArrayList;
import java.util.List;

import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author Jakub Księżniak
 *
 */
public class AndroidXpraWindow extends XpraWindow implements OnTouchListener, OnKeyListener {
	
	private final Paint paint = new Paint();
	private final TextureView textureView;
	private final Handler uiHandler;

	private final Renderer renderer;
	
	private List<XpraWindowListener> listeners = new ArrayList<>();
	
	// window properties
	private String title;
	private Bitmap icon;

	public AndroidXpraWindow(NewWindow wnd, Context context) {
		super(wnd);
		this.renderer = new Renderer();
		this.uiHandler = new Handler(context.getMainLooper());
		this.textureView = new TextureView(context);
		this.textureView.setSurfaceTextureListener(renderer);
		this.textureView.setOnTouchListener(this);
		this.textureView.setOnKeyListener(this);
		this.textureView.setPivotX(0);
		this.textureView.setPivotY(0);
	}

	@Override
	protected void onStart(NewWindow wndPacket) {
		super.onStart(wndPacket);
		Log.i(getClass().getSimpleName(), "onStart() windowId=" + getId());
		final LayoutParams params = wndPacket.isOverrideRedirect() ? 
				buildExactParams(wndPacket) :
				buildFullscreenParams(wndPacket);
		textureView.setLayoutParams(params);
	}
	
	private LayoutParams buildExactParams(final NewWindow wndPacket) {
		final LayoutParams params = new RelativeLayout.LayoutParams(wndPacket.getWidth(), wndPacket.getHeight());
		params.leftMargin = wndPacket.getX();
		params.topMargin = wndPacket.getY();
		return params;
	}

	private LayoutParams buildFullscreenParams(final NewWindow wndPacket) {
		final LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		return params;
	}

	@Override
	protected void onStop() {
		Log.i(getClass().getSimpleName(), "onStop() windowId=" + getId());
	}
	
	@Override
	protected void onMetadataUpdate(WindowMetadata metadata) {
		super.onMetadataUpdate(metadata);
		final String title = metadata.getAsString("title");
		if(title != null) {
			this.title = title;
		}
		fireOnMetadataChanged();
	}
	
	@Override
	protected void onIconUpdate(WindowIcon windowIcon) {
		super.onIconUpdate(windowIcon);
		switch (windowIcon.encoding) {
		case png:
		case jpeg:
			icon = BitmapFactory.decodeByteArray(windowIcon.data, 0, windowIcon.data.length);
			fireOnIconChanged();
			break;

		default:
			break;
		}
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
	
	public String getTitle() {
		return title != null ? title : "Undefined";
	}
	
	public Drawable getIconDrawable() {
		if(icon == null) {
			return null;
		}
		final BitmapDrawable drawable = new BitmapDrawable(textureView.getResources(), icon);
		drawable.setBounds(0, 0, icon.getWidth(), icon.getHeight());
		return drawable;
	}

	public View getView() {
		return textureView;
	}
	
	public void addOnMetadataListener(XpraWindowListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	protected void fireOnMetadataChanged() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for(XpraWindowListener l : listeners) {
						l.onMetadataChanged(AndroidXpraWindow.this);
					}
				}
			}
		});
	}
	
	protected void fireOnIconChanged() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for(XpraWindowListener l : listeners) {
						l.onIconChanged(AndroidXpraWindow.this);
					}
				}
			}
		});
	}

	@Override
	public void draw(DrawPacket packet) {
		Rect dirty = new Rect(packet.x, packet.y, packet.x + packet.w, packet.y + packet.h);
		try {
			Canvas canvas = textureView.lockCanvas(dirty);
			switch (packet.encoding) {
			case png:
			case jpeg:
				Bitmap bitmap = BitmapFactory.decodeByteArray(packet.data, 0, packet.data.length);
				canvas.drawBitmap(bitmap, packet.x, packet.y, paint);
				bitmap.recycle();
				break;

			default:
				break;
			}
			textureView.unlockCanvasAndPost(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		event.offsetLocation(v.getX(), v.getY());
		final int x = (int) Math.max(event.getX(), 0);
		final int y = (int) Math.max(event.getY(), 0);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setFocused(true);
			mouseAction(1, true, x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			mouseAction(1, true, x, y);
			break;
		case MotionEvent.ACTION_UP:
			mouseAction(1, false, x, y);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		System.out.println(event);
		if(event.isSystem()) {
			System.out.println("isSystem event");
			return false;
		}
		System.out.println(event.getUnicodeChar());
		switch (event.getAction()) {
		case KeyEvent.ACTION_DOWN:
			keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), true);
			break;

		case KeyEvent.ACTION_UP:
			keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), false);
		default:
			break;
		}
		return true;
	}

	private class Renderer extends HandlerThread implements SurfaceTextureListener {

		public Renderer() {
			super("Renderer");
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			Log.i(getClass().getSimpleName(), "onSurfaceTextureAvailable(): " + width + "x" + height);

			final int x = (int) getView().getX();
			final int y = (int) getView().getY();
			mapWindow(x, y, width, height);
			setFocused(true);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
			Log.i(getClass().getSimpleName(), "onSurfaceTextureSizeChanged(): " + width + "x" + height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			Log.i(getClass().getSimpleName(), "onSurfaceTextureDestroyed(): ");
			setFocused(false);
			unmapWindow();
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			Log.i(getClass().getSimpleName(), "onSurfaceTextureUpdated(): ");
		}

	}
	
	public interface XpraWindowListener {
		void onMetadataChanged(AndroidXpraWindow window);
		void onIconChanged(AndroidXpraWindow window);
	}

	public void close() {
		closeWindow();
	}

}
