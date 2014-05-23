package info.hexin.simple.reflect.orm.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 反射工具类 ,没有考虑到方法的重载（over-write）。因为复写(over-load)的话，没有问题
 * 
 * @author maczam@163.com
 * 
 */
public class ReflectUtil {
	/**
	 * 获取所有有注释的字段,支持多重继承,支持多个注解
	 */
	public static List<Field> getAnnoFieldList(Class<?> clazz, Class<? extends Annotation>[] annoClass) {
		List<Field> list = new ArrayList<Field>();
		Class<?> superClass = clazz.getSuperclass();
		while (true) {
			if (superClass != null) {
				Field[] superFields = superClass.getDeclaredFields();
				if (superFields != null && superFields.length > 0) {
					for (Field field : superFields) {

						for (Class<? extends Annotation> anno : annoClass) {
							if (field.isAnnotationPresent(anno)) {
								list.add(field);
							}
						}

					}
				}
				superClass = superClass.getSuperclass();
			} else {
				break;
			}
		}
		Field[] objFields = clazz.getDeclaredFields();
		if (objFields != null && objFields.length > 0) {
			for (Field field : objFields) {
				for (Class<? extends Annotation> anno : annoClass) {
					if (field.isAnnotationPresent(anno)) {
						list.add(field);
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * 返回field 的值,获取的是原始值，支持多重继承， 考虑到发杂的属性的返回值，目前只能是String
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static String getFieldValue(Object obj, Field field) {
		String value = null;
		String name = field.getName();
		String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		Method method = null;
		Object methodValue = null;
		try {
			method = obj.getClass().getMethod(methodName);
		} catch (Exception e) {

		}
		if (method != null) {
			try {
				methodValue = method.invoke(obj);
			} catch (Exception e) {
			}
			if (methodValue != null) {
				value = methodValue.toString();
			} else {
				Class<?> objSuperClass = obj.getClass().getSuperclass();
				while (true) {
					if (objSuperClass != null) {
						try {
							methodValue = method.invoke(objSuperClass);
						} catch (Exception e) {
						}
						if (methodValue != null) {
							value = methodValue.toString();
							break;
						} else {
							objSuperClass = objSuperClass.getSuperclass();
						}
					} else {
						break;
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * 给属性复制。值考虑String
	 * @param obj
	 * @param field
	 * @param value
	 */
	public static void setFieldValue(Object obj, Field field,String value){
		String name = field.getName();
		String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		Method method = null;
		try {
			method = obj.getClass().getMethod(methodName,field.getType());
			if (method != null) {
				method.invoke(obj,value);
			} else {
				Class<?> objSuperClass = obj.getClass().getSuperclass();
				method = objSuperClass.getMethod(methodName,field.getType());
				if(method != null){
					method.invoke(obj,value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据属性的名称给属性复制
	 * @param obj
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(Object obj, String fieldName, String value) {
		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			setFieldValue(obj, field, value);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
}
