package info.hexin.simple.reflect.orm.persistence.annotation;

/**
 * 
 * 程序指定主键， 不考虑用日期作为主键情况
 * 
 * @author maczam@163.com
 *
 */
public enum GeneratorType {
	/**
	 * sequence 机制生成主键
	 */
	sequence,
	/**
	 * uuid 有jdk正常的uuid 去掉”-“，只有一共有32个字节
	 */
	uuid,
	/**
	 * 有程序员指定，获取的是getMethod的值，不考虑用日期作为主键情况
	 */
	assigned,
	/**
	 * 主要使用在mysql中，自增，插入的时候不需要关联
	 */
	increment
}
