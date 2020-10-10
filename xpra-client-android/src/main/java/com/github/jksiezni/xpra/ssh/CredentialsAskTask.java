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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jksiezni.xpra.R;

import androidx.appcompat.app.AlertDialog;

final class CredentialsAskTask extends UiTask<Void, Boolean> {

    private final Context context;
    private final String[] prompt;
    private final boolean[] echo;
    private final String[] answers;

    private PasswordDialogBuilder dialog;


    public CredentialsAskTask(Context context, String passwordPrompt) {
        this(context, new String[]{passwordPrompt}, new boolean[]{false});
    }

    public CredentialsAskTask(Context context, String[] prompt, boolean[] echo) {
        this.context = context;
        this.prompt = prompt;
        this.echo = echo;
        this.answers = new String[prompt.length];
    }

    @Override
    protected void doOnUIThread(Void... params) {
        dialog = new PasswordDialogBuilder(context);
        dialog.show();
    }

    public String getPassword() {
        return dialog != null ? dialog.getPassword() : null;
    }

    public String[] getAnswers() {
        return dialog != null ? dialog.getAnswers() : null;
    }


    class PasswordDialogBuilder extends AlertDialog.Builder {

        private LinearLayout layout;

        public PasswordDialogBuilder(Context context) {
            super(context);
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            buildPrompts(layout);

            setView(layout);
            setPositiveButton("OK", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dumpAnswers();
                    postResult(true);
                }
            });
            setNegativeButton("CANCEL", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(false);
                }
            });
            setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    postResult(false);
                }
            });
        }

        protected void dumpAnswers() {
            for (int i = 0; i < prompt.length; ++i) {
                EditText passwdEditText = getPasswordEditText(i);
                answers[i] = passwdEditText.getText().toString();
            }
        }

        private EditText getPasswordEditText(int i) {
            return (EditText) layout.findViewWithTag(prompt[i]).findViewById(R.id.passwdEditText);
        }

        private void buildPrompts(LinearLayout layout) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            for (int i = 0; i < prompt.length; ++i) {
                final View credentialsView = inflater.inflate(R.layout.credentials_item, layout);
                final TextView promptView = credentialsView.findViewById(R.id.promptTextView);
                final EditText editView = credentialsView.findViewById(R.id.passwdEditText);
                promptView.setText(prompt[i]);
                credentialsView.setTag(prompt[i]);
                if (echo[i]) {
                    editView.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    editView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
            final CheckBox checkbox = new CheckBox(context);
            checkbox.setText("Show password");
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPasswords(checkbox.isChecked());
                }
            });
            layout.addView(checkbox);
        }

        protected void showPasswords(boolean show) {
            for (int i = 0; i < prompt.length; ++i) {
                if (!echo[i]) {
                    EditText passEditText = getPasswordEditText(i);
                    passEditText.setInputType(show ?
                        (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                        : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passEditText.setSelection(passEditText.length());
                }
            }
        }

        public String getPassword() {
            return answers[0];
        }

        public String[] getAnswers() {
            return answers;
        }

    }
}
