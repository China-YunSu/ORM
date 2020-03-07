package com.mec.dataBase.core;

import java.lang.reflect.Field;

public class Property {
	private Field property;
	private String column;
	
	public Property() {
	}

	public Field getProperty() {
		return property;
	}

	public void setProperty(Field property) {
		this.property = property;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getPropertyName() {
		return property.getName();
	}
	
	public Class<?> getPropertyType() {
		return property.getType();
	}
}
