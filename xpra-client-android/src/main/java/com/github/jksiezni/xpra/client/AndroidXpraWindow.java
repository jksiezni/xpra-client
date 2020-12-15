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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.github.jksiezni.xpra.gl.GLComposer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;
import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

public class AndroidXpraWindow extends XpraWindow {

    protected final Context context;
    private final Handler uiHandler;

    private final GLComposer composer;
    private final AndroidXpraWindow parent;
    public final List<AndroidXpraWindow> children = new CopyOnWriteArrayList<>();

    private final List<XpraWindowListener> listeners = new ArrayList<>();

    // window properties
    private Bitmap icon;

    public float scale;

    AndroidXpraWindow(NewWindow newWindow, Context context, GLComposer composer) {
        this(newWindow, context, composer, null);
    }

    AndroidXpraWindow(NewWindow newWindow, Context context, GLComposer composer, AndroidXpraWindow parent) {
        super(newWindow);
        this.context = context;
        this.composer = composer;
        this.parent = parent;
        this.scale = context.getResources().getDisplayMetrics().density;
        this.uiHandler = new Handler(Looper.getMainLooper());
        composer.createDrawingTarget(getId());
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public void release() {
        composer.destroyDrawingTarget(getId());
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    @Override
    public boolean isShown() {
        if (parent != null) {
            return parent.isShown() && super.isShown();
        }
        return super.isShown();
    }

    @Override
    protected void onStop() {
        Timber.v("onStop() windowId=%s", getId());
        fireOnLost();
    }

    @Override
    protected void onMetadataUpdate(WindowMetadata metadata) {
        super.onMetadataUpdate(metadata);
        fireOnMetadataChanged();
    }

    @Override
    protected void onIconUpdate(WindowIcon windowIcon) {
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
                Timber.w("Unsupported encoding for icon: %s", windowIcon.encoding);
                break;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getTitle();
    }

    public Handler getHandler() {
        return uiHandler;
    }

    @Override
    public String getTitle() {
        return super.getTitle() != null ? super.getTitle() : "Undefined";
    }

    @Nullable
    public Drawable getIconDrawable() {
        if (icon == null) {
            return null;
        }
        final BitmapDrawable drawable = new BitmapDrawable(context.getResources(), icon);
        drawable.setBounds(0, 0, icon.getWidth(), icon.getHeight());
        return drawable;
    }

    public int getScaledX() {
        return (int) (getX() * scale);
    }

    public int getScaledY() {
        return (int) (getY() * scale);
    }

    public void show(SurfaceTexture surfaceTexture, int width, int height) {
        composer.addSurface(getId(), surfaceTexture);
        if (!isOverrideRedirect()) {
            int w = (int) (width / scale);
            int h = (int) (height / scale);
            int x = hasParent() ? getX() : 0;
            int y = hasParent() ? getY() : 0;
            mapWindow(x, y, w, h);
            setFocused(true);
        }
    }

    public void hide(SurfaceTexture surfaceTexture) {
        composer.removeSurface(getId(), surfaceTexture);
        if (!isOverrideRedirect()) {
            unmapWindow();
        }
    }

    public void addWindowListener(XpraWindowListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeWindowListener(XpraWindowListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void fireOnMetadataChanged() {
        uiHandler.post(() -> {
            synchronized (listeners) {
                for (XpraWindowListener l : listeners) {
                    l.onMetadataChanged(AndroidXpraWindow.this);
                }
            }
        });
    }

    protected void fireOnIconChanged() {
        uiHandler.post(() -> {
            synchronized (listeners) {
                for (XpraWindowListener l : listeners) {
                    l.onIconChanged(AndroidXpraWindow.this);
                }
            }
        });
    }

    protected void fireOnLost() {
        uiHandler.post(() -> {
            synchronized (listeners) {
                for (XpraWindowListener l : listeners) {
                    l.onLost(AndroidXpraWindow.this);
                }
            }
        });
    }

    @Override
    public void onDraw(DrawPacket packet) {
        if (packet.encoding == null) {
            Timber.w("Missing picture encoding for packet: %s", packet);
            return;
        }
        composer.queueToDraw(packet);
    }

    public void close() {
        closeWindow();
    }

    @Nullable
    public AndroidXpraWindow getParent() {
        return parent;
    }

    public boolean hasParent(int windowId) {
        AndroidXpraWindow current = parent;
        while (current != null) {
            if (current.getId() == windowId) return true;
            current = current.parent;
        }
        return false;
    }

    public interface XpraWindowListener {
        void onMetadataChanged(AndroidXpraWindow window);

        void onIconChanged(AndroidXpraWindow window);

        void onLost(AndroidXpraWindow window);
    }

}
