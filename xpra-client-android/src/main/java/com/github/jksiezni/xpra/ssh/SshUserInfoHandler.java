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

import android.app.Activity;
import android.widget.Toast;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author Jakub Księżniak
 *
 */
public class SshUserInfoHandler implements UserInfo, UIKeyboardInteractive {

	private final Activity activity;
	
	private CredentialsAskTask passwordTask;

	public SshUserInfoHandler(Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean promptPassphrase(String message) {
		try {
			passwordTask = new CredentialsAskTask(activity, message);
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
			passwordTask = new CredentialsAskTask(activity, message);
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
			passwordTask = new CredentialsAskTask(activity, prompt, echo);
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
			return new YesNoAskTask(activity)
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
