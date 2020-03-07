package com.mec.dataBase.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.mec.dataBase.annon.Column;
import com.mec.dataBase.annon.Table;
import com.mec.util.XMLParse;

class TableClassFactory {
	private static Map<String, ClassTableDefination> tablePool;
	
	
	static {
		tablePool = new HashMap<String, ClassTableDefination>();
	}
	
	TableClassFactory() {
	}

	static void scanTableClassMapping(String XMLPath) {
		new XMLParse() {
			
			@Override
			public void dealElement(Element element, int index) {
				String table = element.getAttribute("table");
				String klassName = element.getAttribute("class");
				String key = element.getAttribute("key");
				
				ClassTableDefination ctd = new ClassTableDefination();
				ctd.setKlass(klassName);
   				ctd.setTable(table);
				
				new XMLParse() {
					
					@Override
					public void dealElement(Element element, int index) {
						String propertyName = element.getAttribute("property");
						String column = element.getAttribute("column_name");
						ctd.getProperty(propertyName).setColumn(column);
					}
				}.parseTagByElement(element, "column");
				
				Property primarykey = ctd.getProperty(key);
				if (primarykey == null) {
					System.out.println("不存在关键字[" + key + "]");
				} else {
					ctd.setPrimarykey(primarykey);
				}
				tablePool.put(klassName, ctd);
			}
		}.parseTagByDocument(XMLParse.getDocument(XMLPath), "Mapping");
	}
	
	static void scannTableClass(Class<?> tableClass) {
		ClassTableDefination ctd =  new ClassTableDefination();
		ctd.setKlass(tableClass);
		Table table = tableClass.getAnnotation(Table.class);
		ctd.setTable(table.table());
		String key = table.primarykey();
		
		Field[] fields = tableClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				Column column = field.getAnnotation(Column.class);
				Property property = new Property();
				property.setColumn(column.value());
				property.setProperty(field);
				ctd.addProperty(field.getName(), property);
			}
		}
		Property primarykey = ctd.getProperty(key);
		if (primarykey == null) {
			System.out.println("不存在关键字[" + key + "]");
		} else {
			ctd.setPrimarykey(primarykey);
		}
		tablePool.put(tableClass.getName(), ctd);
	}
	
	
	
	static ClassTableDefination getClassTable(String className) {
		try {
			Class<?> klass = Class.forName(className);
			return getClassTable(klass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	static ClassTableDefination getClassTable(Class<?> klass) {
		if (!tablePool.containsKey(klass.getName())) {
			scannTableClass(klass);
		}
		
		return tablePool.get(klass.getName());
	}
	
}
