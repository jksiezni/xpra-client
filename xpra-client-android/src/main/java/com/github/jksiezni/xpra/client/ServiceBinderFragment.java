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

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
@SuppressWarnings("deprecation")
public class ServiceBinderFragment extends Fragment implements ServiceConnection {

    private static final String TAG = "ServiceBinderFragment";

    private boolean bindSuccessful;
    private XpraAPI api;

    private final List<OnServiceAvailableListener<XpraAPI>> listeners = new ArrayList<>();

    public static ServiceBinderFragment obtain(Activity activity) {
        ServiceBinderFragment frag = (ServiceBinderFragment) activity.getFragmentManager().findFragmentByTag(TAG);
        if (frag == null) {
            frag = new ServiceBinderFragment();
            activity.getFragmentManager()
                .beginTransaction()
                .add(frag, TAG)
                .commit();
        }
        return frag;
    }

    public ServiceBinderFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), XpraService.class);
        bindSuccessful = getActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bindSuccessful) {
            getActivity().getApplicationContext().unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.api = (XpraAPI) service;
        synchronized (listeners) {
            for (OnServiceAvailableListener<XpraAPI> l : listeners) {
                l.onServiceAvailable(api);
            }
            listeners.clear();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        synchronized (listeners) {
            listeners.clear();
        }
        api = null;
    }

    @Nullable
    public XpraAPI getXpraAPI() {
        return api;
    }

    public void whenXpraAvailable(OnServiceAvailableListener<XpraAPI> listener) {
        if (api != null) {
            listener.onServiceAvailable(api);
        } else {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    public interface OnServiceAvailableListener<T> {
        void onServiceAvailable(@NonNull T service);
    }

}
