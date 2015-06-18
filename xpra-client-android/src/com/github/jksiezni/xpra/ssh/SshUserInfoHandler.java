/**
 * 
 */
package com.github.jksiezni.xpra.ssh;

import android.app.Activity;
import android.widget.Toast;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author Jakub Księżniak
 *
 */
public class SshUserInfoHandler implements UserInfo, UIKeyboardInteractive {

	final Activity activity;
	
	private CredentialsAskTask passwordTask;

	public SshUserInfoHandler(Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean promptPassphrase(String message) {
		try {
			passwordTask = new CredentialsAskTask(activity.getFragmentManager(), message);
			return passwordTask.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getPassphrase() {
		return passwordTask.getPassword();
	}

	@Override
	public boolean promptPassword(String message) {
		try {
			passwordTask = new CredentialsAskTask(activity.getFragmentManager(), message);
			return passwordTask.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getPassword() {
		return passwordTask.getPassword();
	}

	@Override
	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
			boolean[] echo) {
		try {
			passwordTask = new CredentialsAskTask(activity.getFragmentManager(), prompt, echo);
			boolean success = passwordTask.execute().get();
			if(success) {
				return passwordTask.getAnswers();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean promptYesNo(String message) {
		try {
			return new YesNoAskTask(activity.getFragmentManager())
			.execute(message)
			.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void showMessage(String message) {
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
	}
	
}
