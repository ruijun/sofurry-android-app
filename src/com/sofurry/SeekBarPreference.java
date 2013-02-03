package com.sofurry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	private TextView valueTextView;
	 private int currentValue = 0;
	 private int defaultValue = 0;
	 private int max;
	 private int min = 0;

	//конструктор, вытаскивает спецальные атрибуты настройки
	 public SeekBarPreference(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 max = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry", "max", 99);
	 min = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry", "min", 0);
	 defaultValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry",	 "defaultValue", min);
	 
//	 currentValue = getSharedPreferences().getInt(getKey(), defaultValue);
	 }

	//Переопределяем процедуру создания View для этой настройки
	 @Override
	 protected View onCreateView(ViewGroup parent) {

	RelativeLayout layout = (RelativeLayout) LayoutInflater.from(
	 getContext())
	 .inflate(R.layout.seek_bar_preference_layout, null);

	((TextView) layout.findViewById(R.id.title)).setText(getTitle());

	SeekBar bar = (SeekBar) layout.findViewById(R.id.seekBar);
	 bar.setMax(max - min);
	 bar.setProgress(currentValue - min);
	 bar.setOnSeekBarChangeListener(this);

	valueTextView = (TextView) layout.findViewById(R.id.value);
	 valueTextView.setText(currentValue+"");

	return layout;
	 }

	//Функция, вызываемая каждый раз при перемещении ползунка
	 public void onProgressChanged(SeekBar seekBar, int progress,
	 boolean fromUser) {
	 valueTextView.setText( (min + progress)+"");
	 valueTextView.invalidate();
	 }

	public void onStartTrackingTouch(SeekBar seekBar) {
	 }

	//Функция, вызываемая после окончания движения пользователем
	 public void onStopTrackingTouch(SeekBar seekBar) {
	 currentValue = min + seekBar.getProgress();
	 updatePreference(currentValue);
	 notifyChanged();
	 }

	//Сохранение значения настройки
	 private void updatePreference(int newValue) {
	 SharedPreferences.Editor editor = getEditor();
	 editor.putInt(getKey(), newValue);
	 editor.commit();
	 }

	    @Override
	    protected void onSetInitialValue(boolean restoreValue, Object defVal) {
	     
	     int temp = restoreValue ? getPersistedInt(defaultValue) : (Integer)defVal;
	     
	      if(!restoreValue)
	        persistInt(temp);
	     
	      currentValue = temp;
	    }	
}
