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

package com.github.jksiezni.xpra.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

public class AndroidXpraWindow extends XpraWindow implements OnTouchListener, OnKeyListener {

    protected final Context context;
    private final Handler uiHandler;

    private final List<XpraWindowListener> listeners = new ArrayList<>();

    private final Renderer renderer;
    private TextureView textureView;

    // window properties
    private String title;
    private Bitmap icon;

    AndroidXpraWindow(NewWindow window, Context context) {
        super(window);
        this.context = context;
        this.renderer = new Renderer();
        this.uiHandler = new Handler(Looper.getMainLooper());
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
        Timber.v("onStop() windowId=%s", getId());
        renderer.quit();
    }

    @Override
    protected void onMetadataUpdate(WindowMetadata metadata) {
        super.onMetadataUpdate(metadata);
        final String title = metadata.getAsString("title");
        if (title != null) {
            this.title = title;
        }
        fireOnMetadataChanged();
    }

    @Override
    protected void onIconUpdate(WindowIcon windowIcon) {
        super.onIconUpdate(windowIcon);
        if (windowIcon.encoding == null) {
            return;
        }
        switch (windowIcon.encoding) {
            case png:
            case pngL:
            case pngP:
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

    public Handler getHandler() {
        return uiHandler;
    }

    public String getTitle() {
        return title != null ? title : "Undefined";
    }

    public Drawable getIconDrawable() {
        if (icon == null) {
            return null;
        }
        final BitmapDrawable drawable = new BitmapDrawable(context.getResources(), icon);
        drawable.setBounds(0, 0, icon.getWidth(), icon.getHeight());
        return drawable;
    }

    void setTargetView(TextureView targetView) {
        this.textureView = targetView;
        this.textureView.setSurfaceTextureListener(renderer);
        this.textureView.setOnTouchListener(this);
        this.textureView.setOnKeyListener(this);
        this.textureView.setPivotX(0);
        this.textureView.setPivotY(0);
    }

    View getView() {
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
                    for (XpraWindowListener l : listeners) {
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
                    for (XpraWindowListener l : listeners) {
                        l.onIconChanged(AndroidXpraWindow.this);
                    }
                }
            }
        });
    }

    @Override
    public void draw(DrawPacket packet) {
        if (packet.encoding == null) {
            Timber.w("Missing picture encoding: %s", packet);
            return;
        }
        renderer.postDraw(packet);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //event.offsetLocation(v.getX(), v.getY());
        final int x = (int) Math.max(event.getRawX(), 0);
        final int y = (int) Math.max(event.getRawY(), 0);
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
        if (event.isSystem()) {
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

    public void close() {
        closeWindow();
    }

    private class Renderer extends HandlerThread implements SurfaceTextureListener, Handler.Callback {
        private static final int MSG_DRAW = 1;

        private Handler handler;
        private final Object locker = new Object();
        private final Rect dirty = new Rect();

        public Renderer() {
            super("Renderer");
            start();
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Timber.i("onSurfaceTextureAvailable(): " + width + "x" + height);

            synchronized (locker) {
                handler = new Handler(getLooper(), this);
                locker.notifyAll();
            }
            final int x = (int) getView().getX();
            final int y = (int) getView().getY();
            mapWindow(x, y, width, height);
            setFocused(true);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Timber.i("onSurfaceTextureSizeChanged(): " + width + "x" + height);
            final int x = (int) getView().getX();
            final int y = (int) getView().getY();
            mapWindow(x, y, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Timber.i("onSurfaceTextureDestroyed(): ");
            setFocused(false);
            unmapWindow();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Timber.i("onSurfaceTextureUpdated(): ");
        }

        public void postDraw(DrawPacket packet) {
            getHandler().obtainMessage(MSG_DRAW, packet).sendToTarget();
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DRAW:
                    onDraw((DrawPacket) msg.obj);
            }
            return true;
        }

        private void onDraw(DrawPacket packet) {
            final long startTime = System.currentTimeMillis();
            dirty.set(packet.x, packet.y, packet.x + packet.w, packet.y + packet.h);
            try {
                Canvas canvas = textureView.lockCanvas(dirty);
                switch (packet.encoding) {
                    case png:
                    case pngL:
                    case pngP:
                    case jpeg:
                        Bitmap bitmap = BitmapFactory.decodeByteArray(packet.data, 0, packet.data.length);
                        canvas.drawBitmap(bitmap, packet.x, packet.y, null);
                        bitmap.recycle();
                        break;
                    case rgb24:
                        byte[] pixels = packet.readPixels();
                        Paint p = new Paint();
                        for (int y = 0; y < packet.h; ++y) {
                            for (int x = 0; x < packet.w; ++x) {
                                int r = pixels[3 * (y * packet.w + x)] & 0xff;
                                int g = pixels[3 * (y * packet.w + x) + 1] & 0xff;
                                int b = pixels[3 * (y * packet.w + x) + 2] & 0xff;
                                p.setColor(Color.rgb(r, g, b));
                                canvas.drawPoint(packet.x + x, packet.y + y, p);
                            }
                        }
                        break;
                    default:
                        Timber.e("Unable to draw: %s", packet.encoding);
                        break;
                }
                textureView.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                Timber.e(e);
            } finally {
                sendDamageSequence(packet, System.currentTimeMillis() - startTime);
            }
        }

        public Handler getHandler() {
            synchronized (locker) {
                try {
                    while (handler == null) locker.wait();
                } catch (InterruptedException e) {
                    Timber.w(e);
                }
            }
            return handler;
        }
    }

    public interface XpraWindowListener {
        void onMetadataChanged(AndroidXpraWindow window);

        void onIconChanged(AndroidXpraWindow window);
    }

}
