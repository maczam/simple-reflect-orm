package info.hexin.simple.reflect.orm.spring;

import info.hexin.simple.reflect.orm.persistence.annotation.Column;
import info.hexin.simple.reflect.orm.persistence.annotation.ColumnType;
import info.hexin.simple.reflect.orm.reflect.ReflectUtil;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


/**
 * 
 * 利用反射，和自己实现的注解。生成Spring的RowMapper. 默认查出来的字段都是都是大写。 model 属性一定要 一定要使用Column注释 <br>
 * 
 * 遗留问题，这里赋值没有考虑select后面跟的列的个数
 * 
 * @author maczam@163.com
 * 
 */
public class AutoRowMapper<T> implements RowMapper {

	private Class<T> clazz;

	public AutoRowMapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapRow(ResultSet rs, int k) throws SQLException {
		T t = null;
		try {
			List<Field> list = ReflectUtil.getAnnoFieldList(clazz, Column.class);
			t = clazz.newInstance();
			//TODO 性能还可以再优化
			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount();
			for (int i = 1; i <= count; i++) {
				String columnName = metaData.getColumnLabel(i);
				for (Field field : list) {
					String annoColumnName = getAnnoColumnName(field);
					String methodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					// sql里面的字段名称，和model注解配置的字段名称一样才给赋值。否则不赋值
					if (annoColumnName.equalsIgnoreCase(columnName)) {
						Object value = null;
						Method method = null;
						Column annotation = field.getAnnotation(Column.class);
						ColumnType type = annotation.type();
						switch (type){
							case Varchar:
								value = rs.getString(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,String.class);
								break;
							case Boolean:
								value = rs.getBoolean(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Boolean.class);
								if(method == null){
									method = ReflectUtil.getAccessibleMethod(clazz,methodName,boolean.class);
								}
								break;
							case Int:
								value = rs.getInt(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Integer.class);
								if(method == null){
									method = ReflectUtil.getAccessibleMethod(clazz,methodName,int.class);
								}
								break;
							case Long:
								value = rs.getLong(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Long.class);
								if(method == null){
									method = ReflectUtil.getAccessibleMethod(clazz,methodName,long.class);
								}
								break;
							case Double:
								value = rs.getDouble(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Double.class);
								if(method == null){
									method = ReflectUtil.getAccessibleMethod(clazz,methodName,double.class);
								}
								break;
							case Date2Long:
								value = rs.getDate(annoColumnName).getTime();
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Long.class);
								if(method == null){
									method = ReflectUtil.getAccessibleMethod(clazz,methodName,long.class);
								}
								break;
							case Date:
								value = rs.getDate(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Date.class);
								break;
							case Time:
								value = rs.getTimestamp(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,Date.class);
								break;
							default:
								value = rs.getString(annoColumnName);
								method = ReflectUtil.getAccessibleMethod(clazz,methodName,String.class);
								break;
						}
						ReflectUtil.setFieldValue(t,method,field,value);
						continue;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return t;
	}

	/**
	 * 获取Column 注解配置的数据库字段名称，只是标记没有配置的话，取大写的属性名称
	 * 
	 * @param field
	 * @return
	 */
	private String getAnnoColumnName(Field field) {
		Column column = field.getAnnotation(Column.class);
		String annoColumnName = column.value();
		if (annoColumnName == null || "".equals(annoColumnName)) {
			annoColumnName = field.getName().toUpperCase();
		}
		return annoColumnName;
	}
}
