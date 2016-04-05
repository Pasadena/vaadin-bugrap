package com.example.bugrap.util;

import com.vaadin.server.VaadinService;

public class SessionUtils {
	
	public static void storeValueToSession(final String key, final Object value) {
		VaadinService.getCurrentRequest().getWrappedSession().setAttribute(key, value);
	}
	
	public static boolean containsKey(final String key) {
		return VaadinService.getCurrentRequest().getWrappedSession().getAttribute(key) != null;
	}
	
	public static <T> T getValueFromSession(final String key) {
		Object value = VaadinService.getCurrentRequest().getWrappedSession().getAttribute(key);
		try {
			@SuppressWarnings("unchecked") T concreteValue = (T)value;
			return concreteValue;
		} catch(ClassCastException cce) {
			throw new RuntimeException("Cannot cast value for key " + key + " to concrete instance", cce);
		}
	}

}
