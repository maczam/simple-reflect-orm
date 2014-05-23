package info.hexin.simple.reflect.orm.persistence.annotation;

/**
 * 数据库的类型 三个基本的就足够了，如果需要的话，在sql中调整
 * 
 * @author maczam@163.com
 * 
 */
public enum ColumnType {
	
	/**
	 * 只有date 形如:2012-05-13
	 */
	date,

	/**
	 * 时间 形如：2012-05-13 22:10:11
	 */
	time,

	/**
	 * 字符串形式
	 * 
	 */
	varchar;
}
