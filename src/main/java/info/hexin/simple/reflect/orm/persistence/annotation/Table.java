package info.hexin.simple.reflect.orm.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * table 名称注解，如果tableName没有填写，默认为的model名称
 * 
 * @author maczam@163.com
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Table {
	/**
	 * table 名称,如果没有填写，默认为的model名称
	 * @return
	 */
	public String value() default "";
}
