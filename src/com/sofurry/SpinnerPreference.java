package com.sofurry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpinnerPreference extends Preference implements OnClickListener {
	private TextView valueTextView;
	private int currentValue = 0;
//	private int defaultValue = 0;
	private int max = 99;
	private int min = 0;

	//конструктор, вытаскивает спецальные атрибуты настройки
	 public SpinnerPreference(Context context, AttributeSet attrs) {
		 super(context, attrs);
		 max = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry", "max", Integer.MAX_VALUE);
		 min = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry", "min", Integer.MIN_VALUE);
//		 defaultValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.sofurry",	 "defaultValue", min);
		 
//		 currentValue = getSharedPreferences().getInt(getKey(), defaultValue);
	 }

	//Переопределяем процедуру создания View для этой настройки
	 @Override
	 protected View onCreateView(ViewGroup parent) {
		 RelativeLayout layout = (RelativeLayout) LayoutInflater.from(getContext())
				 .inflate(R.layout.spinner_preference_layout, null);

		 ((TextView) layout.findViewById(R.id.title)).setText(getTitle());

		 Button inc_btn = (Button) layout.findViewById(R.id.inc_btn);
		 Button dec_btn = (Button) layout.findViewById(R.id.dec_btn);
		 inc_btn.setOnClickListener(this);
		 dec_btn.setOnClickListener(this);

	 	valueTextView = (TextView) layout.findViewById(R.id.value);
	 	valueTextView.setText(currentValue+"");

	 	return layout;
	 }

	//Сохранение значения настройки
	 private void updatePreference(int newValue) {
		 SharedPreferences.Editor editor = getEditor();
		 editor.putInt(getKey(), newValue);
		 editor.commit();
	 }


		@Override
	    protected void onSetInitialValue(boolean restoreValue, Object defVal) {
			int temp = min;
//		     int temp = restoreValue ? getPersistedInt(defaultValue) : (Integer)defVal;
			try {
			     temp = restoreValue ? getPersistedInt(0) : (Integer)defVal;
			} catch (Exception e) {
			}
	     
	      if(!restoreValue)
	        persistInt(temp);
	     
	      currentValue = temp;
	    }

		@Override
		protected Object onGetDefaultValue(TypedArray a, int index) {
		    return a.getInteger(index, 0);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.inc_btn:
					if (currentValue < max )
						currentValue++;
//					 valueTextView.setText( (min + progress)+"");
//					 valueTextView.invalidate();
					 updatePreference(currentValue);
					 notifyChanged();
					break;
					
				case R.id.dec_btn:
					if (currentValue > min )
						currentValue--;
					 updatePreference(currentValue);
					 notifyChanged();
					break;
			}
		}	

}
