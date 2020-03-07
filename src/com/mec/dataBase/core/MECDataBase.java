package com.mec.dataBase.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mec.util.PropertiesParse;

public class MECDataBase {
	private static Connection connection;
	
	private static final int SUCCESSFULL_INSERT = 1;

	public MECDataBase() {
	}

	public static void loadClassTableFile(String XMLPath) {
		TableClassFactory.scanTableClassMapping(XMLPath);
	}

	public static void loadMECDataBaseConfigure(String filePath) throws SQLException {
			PropertiesParse pp = new PropertiesParse();
			pp.loadProperties(filePath);
			connection = DriverManager.getConnection(pp.value("url"), pp.value("user"), pp.value("passWord"));
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getDataList(Class<?> klass) {
		StringBuffer cmd = new StringBuffer("SELECT ");
		ClassTableDefination ctf = TableClassFactory.getClassTable(klass);
		cmd.append(ctf.getPropertyNames()).append(" ").append("FROM ").append(ctf.getTable());
		List<T> result = new ArrayList<T>();

		try {
			PreparedStatement state = connection.prepareStatement(cmd.toString());
			ResultSet rs = state.executeQuery();

			List<Property> properties = ctf.getPropertyList();
			while (rs.next()) {
				Object obj = klass.newInstance();

				for (Property property : properties) {
					Object value = rs.getObject(property.getColumn());
					setValue(klass, obj, property, value);
				}

				result.add((T) obj);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Class<?> klass, Object keyValue) {
		StringBuffer cmd = new StringBuffer("SELECT ");
		ClassTableDefination ctf = TableClassFactory.getClassTable(klass);
		cmd.append(ctf.getPropertyNames()).append(" ").append("FROM ").append(ctf.getTable()).append(" ")
				.append("WHERE").append(" ");
		Property primarykey = ctf.getPrimarykey();
		cmd.append(ctf.getTable()).append(".").append(primarykey.getColumn()).append("=?");
		T data = null;
		try {
			PreparedStatement state = connection.prepareStatement(cmd.toString());
			state.setObject(1, keyValue);
			ResultSet rs = state.executeQuery();

			List<Property> properties = ctf.getPropertyList();
			while (rs.next()) {
				data = (T) klass.newInstance();

				for (Property property : properties) {
					Object value = rs.getObject(property.getColumn());
					setValue(klass, data, property, value);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return data;
	}

	private void setValue(Class<?> klass, Object obj, Property property, Object value) {
		try {
			String methodName = "set" + property.getPropertyName().substring(0, 1).toUpperCase()
					+ property.getPropertyName().substring(1);
			Method method = klass.getDeclaredMethod(methodName, property.getPropertyType());
			method.invoke(obj, value);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public boolean setData(Object infor) {
		ClassTableDefination ctd = TableClassFactory.getClassTable(infor.getClass());
		List<Property> properties = ctd.getPropertyList();
		StringBuffer values = new StringBuffer();
		for (Property property : properties) {
			if (values.length() != 0) {
				values.append(",").append(getValue(infor.getClass(), infor, property));
				continue;
			}
			values.append(getValue(infor.getClass(), infor, property));
		}

		StringBuffer cmd = new StringBuffer("INSERT IGNORE");
		cmd.append(" ").append("INTO").append(" ").append(ctd.getTable()).append("(").append(ctd.getPropertyNames())
				.append(")").append(" ").append("VALUES").append("(").append(values.toString()).append(")");
		try {
			PreparedStatement state = connection.prepareStatement(cmd.toString());
		
			return state.executeUpdate() == SUCCESSFULL_INSERT;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getValue(Class<?> klass, Object obj, Property property) {
		String methodName = "get" + property.getPropertyName().substring(0, 1).toUpperCase()
				+ property.getPropertyName().substring(1);
		try {
			Method method = klass.getMethod(methodName);
			return "'" + method.invoke(obj).toString() + "'";
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return "";
	}

}
