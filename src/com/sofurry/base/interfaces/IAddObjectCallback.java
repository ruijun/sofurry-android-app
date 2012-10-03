package com.sofurry.base.interfaces;

import com.sofurry.model.Submission;

public interface IAddObjectCallback<T> {
	public void AddObject(final T obj);
}