package info.hexin.simple.reflect.orm.persistence.annotation;

/**
 * 数据库的类型 三个基本的就足够了，如果需要的话，在sql中调整
 *
 * @author maczam@163.com
 */
public enum ColumnType {

    /**
     * 只有date 形如:2012-05-13
     */
    Date,

    /**
     * 数据库保存的是date类型，但是实体类接受的是long，rs.getDate("").getTime();
     */
    Date2Long,

    /**
     * 时间 形如：2012-05-13 22:10:11
     */
    Time,

    /**
     * 返回整形
     */
    Int,


    Long,

    Double,

    Boolean,

    /**
     * 字符串形式
     */
    Varchar;
}
