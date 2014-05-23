package info.hexin.simple.reflect.orm.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 主键生成策略,默认使用uuid
 * 
 * @author maczam@163.com
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Generator {
	
	/**
	 * 主键生成方式 
	 * @return
	 */
	public GeneratorType type() default GeneratorType.uuid;
	
	/**
	 * 
	 * @return
	 */
	public String value() default "";
}
