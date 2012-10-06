package com.sofurry.base.interfaces;

public interface IAddObjectCallback<T> {
	/**
	 * Return value is boolean to be compatible with ArrayList type. By default should return true
	 * @param obj
	 * @return
	 */
	public boolean add(final T obj);
}