package com.sofurry;

import java.io.File;

import com.sofurry.storage.FileStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FilenamePreference extends Preference implements View.OnClickListener{
	private String defaultValue;
	private String currentValue;
	private Boolean folderSelect;
	private TextView valueText;
	
	public FilenamePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		defaultValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.sofurry", "defaultValue");
		if ( defaultValue == null) {
			defaultValue = "%EXTERNALSTORAGE%/";
		}

		try {
			defaultValue = defaultValue.replaceAll("%EXTERNALSTORAGE%", FileStorage.getExternalMediaRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentValue = defaultValue;
		
		folderSelect = attrs.getAttributeBooleanValue( "http://schemas.android.com/apk/res/com.sofurry", "folder_select", false);

//		SharedPreferences sh = getSharedPreferences();
//		currentValue = sh.getString(getKey(), defaultValue);	
	}
	
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defVal) {
     
     String temp = restoreValue ? getPersistedString(defaultValue) : (String)defVal;
     
      if(!restoreValue)
        persistString(temp);
     
      currentValue = temp;
    }	
  
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
	    return a.getString(index);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {

		LinearLayout layout = (LinearLayout) LayoutInflater.from(getContext())
				.inflate(R.layout.filename_preference_layout, null);

		((TextView) layout.findViewById(R.id.TitleText)).setText(getTitle());
		valueText = ((TextView) layout.findViewById(R.id.ValueText));
		valueText.setText(currentValue);
		layout.setOnClickListener(this);

		return layout;
	}
	
	@Override
	public void onClick(View v) {
		Context context = getContext();
		FileDialog fileDialog = new FileDialog(context, new File(currentValue) );

		fileDialog.setSelectDirectoryOption(folderSelect);
		if (folderSelect) {
			fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
				
				@Override
				public void directorySelected(File directory) {
	            	updatePreference(directory.toString());
					notifyChanged();
	                Log.d(getClass().getName(), "selected directory " + directory.toString());
				}
			});
		};

		fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
            	updatePreference(file.toString());
				notifyChanged();
                Log.d(getClass().getName(), "selected file " + file.toString());
            }
        });

		fileDialog.showDialog();
	}

	private void updatePreference(String newValue) {
		 SharedPreferences.Editor editor = getEditor();
		 editor.putString(getKey(), newValue);
		 editor.commit();

		 currentValue = newValue;
		 valueText.setText(currentValue);
	 }

}
