package info.hexin.simple.reflect.orm.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性注解，目前一般的字段和主键都用这个。基本使用就足够了。<br>
 *  
 * 
 * @author maczam@163.com
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Column {

	/**
	 * 是否是主键
	 * 
	 * @return
	 */
	public boolean isPk() default false;


	/**
	 * 对应的数据库表名
	 * 
	 * @return
	 */
	public String value() default "";

	/**
	 * 字段类型
	 * 
	 * @return
	 */
	public ColumnType type() default ColumnType.varchar;
}
