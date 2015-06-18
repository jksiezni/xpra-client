package com.github.jksiezni.xpra.preference;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.preference.DialogPreference;
import android.support.v7.internal.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.jksiezni.xpra.R;

/**
 * 
 * @author Jakub Księżniak
 *
 */
public class FileChooserPreference extends DialogPreference {

	private ListView listView;
	private TextView pathTextView;
	private FileExplorerAdapter fileExplorer;

	public FileChooserPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected View onCreateDialogView() {
		fileExplorer = new FileExplorerAdapter();
		
		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		
		pathTextView = new TextView(getContext());
		pathTextView.setText(fileExplorer.getCurrentPath());
		
		listView = new ListViewCompat(getContext());
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(fileExplorer);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(fileExplorer.changeDir(position)) {
					listView.setItemChecked(listView.getCheckedItemPosition(), false);
					pathTextView.setText(fileExplorer.getCurrentPath());
				}
			}
		});
		
		layout.addView(pathTextView);
		layout.addView(listView);
		return layout;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult) {
			String value = null;
			final int position = listView.getCheckedItemPosition();
			if(position != AdapterView.INVALID_POSITION) {
  			final File file = fileExplorer.getItem(position);
  			value = file.getAbsolutePath();
			}
			if(callChangeListener(value)) {
				persistString(value);
			}
		}
	}

	private static class FileExplorerAdapter extends BaseAdapter {

		private File currentDir;
		private File[] files;

		public FileExplorerAdapter() {
			currentDir = Environment.getExternalStorageDirectory();
			files = currentDir.listFiles();
		}
		
		public String getCurrentPath() {
			return currentDir.getAbsolutePath();
		}

		public boolean changeDir(int position) {
			if(position == 0) {
				currentDir = currentDir.getParentFile();
				files = currentDir.listFiles();
				notifyDataSetChanged();
				return true;
			}
			else if(0 < position && position <= files.length
					&& files[position-1].isDirectory()
					&& files[position-1].canRead()) {
				currentDir = files[position-1];
				files = currentDir.listFiles();
				notifyDataSetChanged();
				return true;
			}
			return false;
		}

		@Override
		public int getCount() {
			return files.length+1;
		}

		@Override
		public File getItem(int position) {
			return position == 0 ? currentDir : files[position-1];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) convertView;
			if(view == null) {
				view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.select_dialog_singlechoice_material, parent, false);
			}
			if(position == 0) {
				view.setText("/..");
			} else {
				File f = getItem(position);
				view.setText(f.isDirectory() ? "/" + f.getName() : f.getName());
			}
			return view;
		}
		
	}
}
