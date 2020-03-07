package com.mec.dataBase.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassTableDefination {
	private Class<?> klass;
	private String table;
	private Property primarykey;
	private static Map<String, Property> propertyPool;
	
	static {
		propertyPool = new HashMap<String, Property>();
	}
	
	public ClassTableDefination() {
	}

	public void setPrimarykey(Property primarykey) {
		this.primarykey = primarykey;
	}
	
	public Property getPrimarykey() {
		return primarykey;
	}
	
	public void setKlass(Class<?> klass) {
		this.klass = klass;
	}
	
	public void setKlass(String klassName) {
		try {
			klass = Class.forName(klassName);
			
			Field[] fields = klass.getDeclaredFields();
			
			for (Field field : fields) {
				String fieldName = field.getName();
				Property property = new Property();
				property.setColumn(fieldName);
				property.setProperty(field);
				propertyPool.put(fieldName, property);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void addProperty(String fieldName, Property property) {
		propertyPool.put(fieldName, property);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	public Property getProperty(String key) {
		return propertyPool.get(key);
	}
	
	public List<Property> getPropertyList() {
		Set<String> keys = propertyPool.keySet();
		List<Property> list = new ArrayList<Property>();
		for(String key : keys) {
			list.add(propertyPool.get(key));
		}
		
		return list;
	}
	
	public String getPropertyNames() {
		List<Property> properties = getPropertyList();
		StringBuffer language = new StringBuffer();
		boolean isFirst = true;
		
		for (Property property : properties) {
			language.append(isFirst == true ? property.getColumn() : ", " + property.getColumn());
			isFirst = false;
		}
		
		return language.toString();
	}
	
}
