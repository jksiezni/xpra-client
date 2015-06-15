package com.github.jksiezni.xpra.ssh;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.github.jksiezni.xpra.UiTask;

class YesNoAskTask extends UiTask<String, Boolean> {
	
	private final FragmentManager fm;

	public YesNoAskTask(FragmentManager fm) {
		this.fm = fm;
	}
	
	@Override
	protected void doOnUIThread(String... params) {
		new YesNoDialog(params[0]).show(fm, "prompt");		
	}
	
  private class YesNoDialog extends DialogFragment {
  
  	private final String message;
  
  	public YesNoDialog(String message) {
  		this.message = message;
  	}
  	
  	@Override
  	public Dialog onCreateDialog(Bundle savedInstanceState) {
  		return new AlertDialog.Builder(getActivity())
  		.setMessage(message)
  		.setPositiveButton("YES", new OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				postResult(true);
  			}
  		})
  		.setNegativeButton("NO", new OnClickListener() {
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				postResult(false);
  			}
  		})
  		.create();
  	}
  	
  	@Override
  	public void onCancel(DialogInterface dialog) {
  		super.onCancel(dialog);
  		postResult(false);
  	}
  	
  }
}

