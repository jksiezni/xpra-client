/**
 * 
 */
package com.github.jksiezni.xpra.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jksiezni.xpra.R;

/**
 * @author Jakub Księżniak
 *
 */
public class XpraDiscoveryFragment extends Fragment implements DiscoveryListener {

	private static final String XPRA_TCP = "_xpra._tcp";
	private NsdManager nsd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.servers_fragment, container, false);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		nsd = (NsdManager) getActivity().getSystemService(Context.NSD_SERVICE);
	}

	@Override
	public void onResume() {
		super.onResume();
		nsd.discoverServices(XPRA_TCP, NsdManager.PROTOCOL_DNS_SD, this);
	}
	
	@Override
	public void onPause() {
		nsd.stopServiceDiscovery(this);
		super.onPause();
	}

	@Override
	public void onStartDiscoveryFailed(String serviceType, int errorCode) {
		// TODO Auto-generated method stub
		System.out.println("onStartDiscoveryFailed(): ");
	}

	@Override
	public void onStopDiscoveryFailed(String serviceType, int errorCode) {
		// TODO Auto-generated method stub
		System.out.println("onStopDiscoveryFailed(): ");
	}

	@Override
	public void onDiscoveryStarted(String serviceType) {
		// TODO Auto-generated method stub
		System.out.println("onDiscoveryStarted(): ");
	}

	@Override
	public void onDiscoveryStopped(String serviceType) {
		// TODO Auto-generated method stub
		System.out.println("onDiscoveryStopped(): ");
	}

	@Override
	public void onServiceFound(NsdServiceInfo serviceInfo) {
		// TODO Auto-generated method stub
		System.out.println("Found: " + serviceInfo);
		nsd.resolveService(serviceInfo, new ResolveListener() {
			
			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				// TODO Auto-generated method stub
				System.out.println("onServiceResolved: " + serviceInfo);
				
			}
			
			@Override
			public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
				// TODO Auto-generated method stub
				System.out.println("onResolveFailed(): " + serviceInfo + " error code: " + errorCode);
				
			}
		});
	}

	@Override
	public void onServiceLost(NsdServiceInfo serviceInfo) {
		// TODO Auto-generated method stub
		System.out.println("Lost: " + serviceInfo);
		
	}
}
