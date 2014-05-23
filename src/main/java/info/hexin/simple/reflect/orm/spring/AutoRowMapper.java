package info.hexin.simple.reflect.orm.spring;

import info.hexin.simple.reflect.orm.persistence.annotation.Column;
import info.hexin.simple.reflect.orm.reflect.ReflectUtil;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
			List<Field> list = ReflectUtil.getAnnoFieldList(clazz, new Class[]{Column.class});
			t = clazz.newInstance();
			//TODO 性能还可以再优化
			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount();
			for (int i = 1; i <= count; i++) {
				String columnName = metaData.getColumnLabel(i);
				for (Field field : list) {
					String annoColumnName = getAnnoColumnName(field);
					// sql里面的字段名称，和model注解配置的字段名称一样才给赋值。否则不赋值
					if (annoColumnName.equals(columnName)) {
						String value = rs.getString(annoColumnName);
						ReflectUtil.setFieldValue(t, field, value);
						continue;
					}
				}
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
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
