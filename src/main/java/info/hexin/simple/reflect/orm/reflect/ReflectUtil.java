package info.hexin.simple.reflect.orm.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
	public static List<Field> getAnnoFieldList(Class<?> clazz, Class<? extends Annotation> annoClass) {
		List<Field> list = new ArrayList<>();
		for (Class<?> acls = clazz; acls != null; acls = acls.getSuperclass()) {
			Field[] superFields = acls.getDeclaredFields();
			for (Field field : superFields) {
				if (field.isAnnotationPresent(annoClass)) {
					list.add(field);
				}
			}
		}
		return list;
	}


	public static Method getAccessibleMethod(final Class<?> cls, final String methodName,
											 final Class<?>... parameterTypes) {
		try {
			return getAccessibleMethod(cls.getMethod(methodName,parameterTypes));
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	public static Method getAccessibleMethod(Method method) {
		if (method != null && Modifier.isPublic(method.getModifiers()) && !method.isSynthetic()) {
			return null;
		}
		// If the declaring class is public, we are done
		final Class<?> cls = method.getDeclaringClass();
		if (Modifier.isPublic(cls.getModifiers())) {
			return method;
		}
		final String methodName = method.getName();
		final Class<?>[] parameterTypes = method.getParameterTypes();

		// Check the implemented interfaces and subinterfaces
		method = getAccessibleMethodFromInterfaceNest(cls, methodName,parameterTypes);

		// Check the superclass chain
		if (method == null) {
			method = getAccessibleMethodFromSuperclass(cls, methodName,parameterTypes);
		}
		return method;
	}

	private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls,
															   final String methodName, final Class<?>... parameterTypes) {
		// Search up the superclass chain
		for (; cls != null; cls = cls.getSuperclass()) {

			// Check the implemented interfaces of the parent class
			final Class<?>[] interfaces = cls.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				// Is this interface public?
				if (!Modifier.isPublic(interfaces[i].getModifiers())) {
					continue;
				}
				// Does the method exist on this interface?
				try {
					return interfaces[i].getDeclaredMethod(methodName,
							parameterTypes);
				} catch (final NoSuchMethodException e) { // NOPMD
                    /*
                     * Swallow, if no method is found after the loop then this
                     * method returns null.
                     */
				}
				// Recursively check our parent interfaces
				Method method = getAccessibleMethodFromInterfaceNest(interfaces[i],
						methodName, parameterTypes);
				if (method != null) {
					return method;
				}
			}
		}
		return null;
	}

	private static Method getAccessibleMethodFromSuperclass(final Class<?> cls,
															final String methodName, final Class<?>... parameterTypes) {
		Class<?> parentClass = cls.getSuperclass();
		while (parentClass != null) {
			if (Modifier.isPublic(parentClass.getModifiers())) {
				try {
					return parentClass.getMethod(methodName, parameterTypes);
				} catch (final NoSuchMethodException e) {
					return null;
				}
			}
			parentClass = parentClass.getSuperclass();
		}
		return null;
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
	 * @param method
	 * @param field
     * @param value
     */
	public static void setFieldValue(Object obj, Method method, Field field, Object value) {
		try {
			if (method != null) {
				method.invoke(obj, value);
			} else {
				System.err.println("field>>>>" + field + ",method>>>》》》》》》null" + ";value>>>" + value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("field>>>>" + field + ",method>>>" + method + ";value>>>" + value);
		}
	}
}
