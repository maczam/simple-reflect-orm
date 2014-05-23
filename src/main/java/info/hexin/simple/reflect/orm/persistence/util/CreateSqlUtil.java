package info.hexin.simple.reflect.orm.persistence.util;

import info.hexin.simple.reflect.orm.persistence.annotation.*;
import info.hexin.simple.reflect.orm.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;


/**
 * 使用反射和注解动态生成sql语句，实现对单表的增删改查，没有使用jpa注解，主要是太复杂，如果要实现，那么底层都要实现<br>
 * 如果类没有主键的话，默认手动设置主键，比如：mergeSql.onColumn,deleteSql.whereColumn 1.支持oracle
 * 的Merge语句 2.主键支持生成策略sequence 3.支持符合主键
 *
 * @author maczam@163.com
 */
public class CreateSqlUtil {

    /**
     * 生成update sql,按照主键匹配where后面的语句
     *
     * @param model
     * @return
     */
    public static String updateSql(Object model) {
        return updateSql(model, null, null);
    }

    /**
     * 生成update sql,按照主键匹配where后面的语句
     *
     * @param model
     * @param whereColumn
     * @return
     */
    public static String updateSql(Object model, String[] whereColumn) {
        return updateSql(model, whereColumn, null);
    }

    /**
     * 生成update sql,按照主键匹配where后面的语句
     *
     * @param model
     * @param whereParams
     * @return
     */
    public static String updateSql(Object model, Map<String, String> whereParams) {
        return updateSql(model, null, whereParams);
    }

    /**
     * 生成update sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @param whereColumn
     * @param whereParams
     * @return
     */
    public static String updateSql(Object model, String[] whereColumn, Map<String, String> whereParams) {
        String updateSql = null;
        String tableName = getTableName(model.getClass());
        StringBuilder sqlStr = new StringBuilder("UPDATE " + tableName + " SET ");
        StringBuilder whereSb = new StringBuilder(" WHERE 1 = 1");
        List<String> whereColumnList = new ArrayList<String>();

        List<Field> annoFieldList = getAnnoFieldList(model.getClass());
        List<Field> pkAnnoFieldList = getPKAnnoFieldList(annoFieldList);
        Map<String, Field> pkAnnoFieldMap = getPKAnnoFieldMap(annoFieldList);

        // 存在条件
        if (whereParams != null && whereParams.size() > 0) {
            Set<Entry<String, String>> fixedSet = whereParams.entrySet();
            for (Entry<String, String> entry : fixedSet) {
                whereColumnList.add(entry.getKey());
                whereSb.append("  AND ").append(entry.getKey()).append("='").append(entry.getValue()).append("'");
            }
        }

        // 传入where后面的字段
        if (whereColumn != null && whereColumn.length > 0) {
            for (String column : whereColumn) {
                for (Field field : annoFieldList) {
                    String columnName = getColumnName(field);
                    if (column.equals(columnName)) {
                        whereColumnList.add(columnName);

                        String columnValue = null;
                        // 如果是主键，取值不一样
                        if (pkAnnoFieldMap.containsKey(columnName)) {
                            String fieldValue = getFieldValue(model, field);
                            if (!isBlank(fieldValue)) {
                                columnValue = "'" + fieldValue + "'";
                            }
                        } else {
                            columnValue = getColumnValue(model, field);
                        }

                        if (!isBlank(columnValue)) {
                            whereSb.append(" AND ");
                            whereSb.append(columnName);
                            whereSb.append(" = ");
                            whereSb.append(columnValue);
                        }
                    }
                }
            }
        }

        // 使用主键
        if ((whereParams == null || whereParams.size() == 0) && (whereColumn == null || whereColumn.length == 0)) {
            if (pkAnnoFieldList.size() > 0) {
                for (Field field : pkAnnoFieldList) {
                    String columnValue = getFieldValue(model, field);
                    if (!isBlank(columnValue)) {
                        String columnName = getColumnName(field);
                        whereColumnList.add(columnName);
                        whereSb.append(" AND ");
                        whereSb.append(columnName);
                        whereSb.append(" = ");
                        whereSb.append("'" + columnValue + "'");
                    } else {
                        throw new RuntimeException("没有配置where语句，主键为空，不能生成sql");
                    }
                }
            } else {
                throw new RuntimeException("where 条件不能生成！");
            }
        }

        // 生成set后面
        for (int i = 0, k = 0; i < annoFieldList.size(); i++) {
            Field field = annoFieldList.get(i);
            String columnName = getColumnName(field);
            // 没有在where后面的话，如果有值都加入到set后面
            if (!whereColumnList.contains(columnName)) {
                if (k > 0) {
                    sqlStr.append(" ,");
                }

                String columnValue = null;
                // 如果是主键字段，直接获取他的值
                if (pkAnnoFieldMap.containsKey(columnName)) {
                    String fieldValue = getFieldValue(model, field);
                    if (!isBlank(fieldValue)) {
                        columnValue = "'" + fieldValue + "'";
                    }
                } else {
                    columnValue = getColumnValue(model, field);
                }

                // 塞值
                if (!isBlank(columnValue)) {
                    k++;
                    sqlStr.append(columnName);
                    sqlStr.append(" = ");
                    sqlStr.append(columnValue);
                }
            }
        }

        updateSql = sqlStr.toString() + whereSb.toString();
        return updateSql;
    }

    /**
     * 生成delete sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @return
     */
    public static String deleteSql(Object model) {
        return deleteSql(model, null, null);
    }

    /**
     * 生成delete sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @param whereColumn
     * @return
     */
    public static String deleteSql(Object model, String[] whereColumn) {
        return deleteSql(model, whereColumn, null);
    }

    /**
     * 生成delete sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @param whereParams
     * @return
     */
    public static String deleteSql(Object model, Map<String, String> whereParams) {
        return deleteSql(model, null, whereParams);
    }

    /**
     * 按照whereParams 后前的瞧见生成delete语句
     *
     * @param model
     * @param whereColumn
     * @param whereParams
     * @return
     */
    public static String deleteSql(Object model, String[] whereColumn, Map<String, String> whereParams) {
        String deleteSql = null;
        String tableName = getTableName(model.getClass());
        StringBuffer sqlStr = new StringBuffer("DELETE FROM " + tableName + " WHERE 1 = 1");

        // 存在条件
        if (whereParams != null && whereParams.size() > 0) {
            Set<Entry<String, String>> fixedSet = whereParams.entrySet();
            for (Entry<String, String> entry : fixedSet) {
                sqlStr.append(" AND ").append(entry.getKey()).append("='").append(entry.getValue()).append("'");
            }
        }

        // 存在where 字段
        if (whereColumn != null && whereColumn.length > 0) {
            List<Field> annoFieldList = getAnnoFieldList(model.getClass());
            for (String column : whereColumn) {
                for (Field field : annoFieldList) {
                    String columnName = getColumnName(field);
                    if (column.equals(columnName)) {
                        String columnValue = getFieldValue(model, field);
                        sqlStr.append(" AND ");
                        sqlStr.append(columnName);
                        sqlStr.append(" = '");
                        sqlStr.append(columnValue);
                        sqlStr.append("'");
                    }
                }
            }
        }

        // 使用主键
        if ((whereParams == null || whereParams.size() == 0) && (whereColumn == null || whereColumn.length == 0)) {
            List<Field> annoFieldList = getAnnoFieldList(model.getClass());
            List<Field> pkAnnoFieldList = getPKAnnoFieldList(annoFieldList);
            if (pkAnnoFieldList.size() > 0) {
                for (Field field : pkAnnoFieldList) {
                    String columnName = getColumnName(field);
                    String columnValue = getFieldValue(model, field);
                    sqlStr.append(" AND ");
                    sqlStr.append(columnName);
                    sqlStr.append(" = '");
                    sqlStr.append(columnValue).append("'");
                }
            } else {
                throw new RuntimeException("where 条件不能生成！");
            }
        }
        deleteSql = sqlStr.toString();
        return deleteSql;
    }

    /**
     * 生成merge sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @return
     */
    public static String mergeSql(Object model) {
        return mergeSql(model, null, null);
    }

    /**
     * 生成merge sql,如果onColumn没有设置的话，默认按照主键匹配
     *
     * @param model
     * @param onColumn
     * @return
     */
    public static String mergeSql(Object model, String[] onColumn) {
        return mergeSql(model, onColumn, null);
    }

    /**
     * 根据onColumn 生成带有？的sql
     * 对于复合主键 的话，一定要当做普通列类赋值
     *
     * @param clazz
     * @return
     */
    public static String findSql(Class<?> clazz) {
        return findSql(clazz, null);
    }

    /**
     * 根据map参数生成带where条件的sql
     *
     * @param clazz
     * @param params
     * @return
     */
    public static String findSql(Class<?> clazz, Map<String, String> params) {
        StringBuffer sqlStr = new StringBuffer("SELECT ");
        String tableName = getTableName(clazz);
        List<Field> annoFieldList = getAnnoFieldList(clazz);

        for (int i = 0; i < annoFieldList.size(); i++) {
            sqlStr.append(formatColumnName(annoFieldList.get(i)));
            if (i + 1 < annoFieldList.size()) {
                sqlStr.append(",");
            }
        }

        sqlStr.append(" FROM ");
        sqlStr.append(tableName);

        if (params != null && params.size() > 0) {
            sqlStr.append(" WHERE 1=1 ");
            Map<String, Field> annoFieldMap = getAnnoFieldMap(annoFieldList);
            Map<String, Field> pkAnnoFieldMap = getPKAnnoFieldMap(annoFieldList);

            for (Entry<String, String> entry : params.entrySet()) {
                String whereColumn = entry.getKey();
                if (annoFieldMap.containsKey(whereColumn)) {
                    sqlStr.append(" AND ");
                    sqlStr.append(whereColumn);
                    sqlStr.append("= '");
                    sqlStr.append(params.get(whereColumn));
                    sqlStr.append("'");
                } else if ("id".equalsIgnoreCase(whereColumn)) {
                    if (pkAnnoFieldMap.size() == 1) {
                        for (Entry<String, Field> e : pkAnnoFieldMap.entrySet()) {
                            sqlStr.append(" AND ");
                            sqlStr.append(e.getKey());
                            sqlStr.append("= '");
                            sqlStr.append(params.get(whereColumn));
                            sqlStr.append("'");
                        }
                    }
                }
            }

        }
        return sqlStr.toString();
    }

    /**
     * 生成merge sql,如果onColumn没有设置的话，默认按照主键匹配
     * @param model
     * @param onColumn
     * @param fixedParams
     * @return
     */
    public static String mergeSql(Object model, String[] onColumn, Map<String, String> fixedParams) {
        String mergeSql = null;

        List<Field> annoFieldList = getAnnoFieldList(model.getClass());
        Map<String, Field> pkAnnoFieldMap = getPKAnnoFieldMap(annoFieldList);
        if (annoFieldList != null && annoFieldList.size() > 0) {
            String tableName = getTableName(model.getClass());
            StringBuffer sqlStr = new StringBuffer("MERGE INTO " + tableName + " AS A  \n ");
            List<String> onList = new ArrayList<String>();

            // on ()
            StringBuffer on = new StringBuffer(" ON (");
            if (onColumn != null && onColumn.length > 0) {
                for (int i = 0; i < onColumn.length; i++) {
                    onList.add(onColumn[i].toUpperCase().trim());
                    on.append("A." + onColumn[i] + "  = B." + onColumn[i]);
                    if (i + 1 < onColumn.length) {
                        on.append(" AND ");
                    }
                }
                // 没有的话主键
            } else {
                List<Field> pkList = getPKAnnoFieldList(annoFieldList);
                for (int i = 0; i < pkList.size(); i++) {
                    String columnName = getColumnName(pkList.get(i));
                    onList.add(columnName);
                    on.append("A." + columnName + "  = B." + columnName);
                    if (i + 1 < pkList.size()) {
                        on.append(" AND ");
                    }
                }
            }
            on.append(")\n");

            StringBuffer using = new StringBuffer(" USING ( SELECT ");
            StringBuffer insert = new StringBuffer("WHEN NOT MATCHED THEN \n   INSERT ( ");
            StringBuffer insertValue = new StringBuffer("   VALUES (");
            StringBuffer update = new StringBuffer("WHEN  MATCHED THEN \n   UPDATE SET ");

            // 遍历所有带有注解的属性
            for (int i = 0, flag = 0; i < annoFieldList.size(); i++) {
                Field field = annoFieldList.get(i);
                String columnName = getColumnName(field);
                String columnValue = null;

                // 还需要考虑 主键部分
                if (pkAnnoFieldMap.containsKey(columnName)) {
                    String fieldValue = getFieldValue(model, field);
                    if (isBlank(fieldValue)) {
                        columnValue = getColumnValue(model, field);
                    } else {
                        columnValue = "'" + fieldValue + "'";
                    }
                    using.append(columnValue + " AS " + columnName + ",");
                    insert.append(columnName).append(",");
                    insertValue.append("B." + columnName).append(",");
                } else {
                    columnValue = getColumnValue(model, field);
                }

                // using 增加后，跳出
                if (onList.contains(columnName)) {
                    continue;
                }

                if (columnValue != null) {
                    if (flag > 0) {
                        using.append(",");
                        update.append(",");
                        insert.append(",");
                        insertValue.append(",");
                    }
                    flag++;
                    using.append(columnValue + " AS " + columnName);
                    update.append(columnName + " = B." + columnName);
                    insert.append(columnName);
                    insertValue.append("B." + columnName);
                }
            }

            // 遍历修正参数
            if (fixedParams != null && fixedParams.size() > 0) {
                Set<Entry<String, String>> fixedSet = fixedParams.entrySet();
                int t = 0;
                for (Entry<String, String> entry : fixedSet) {
                    if (t > 0 || annoFieldList.size() > 0) {
                        using.append(",");
                        update.append(",");
                        insert.append(",");
                        insertValue.append(",");
                    }
                    using.append("'" + entry.getValue() + "' AS " + entry.getKey());
                    update.append(entry.getKey() + " = B." + entry.getKey());
                    insertValue.append("B." + entry.getKey());
                    insert.append(entry.getKey());
                }
            }

            using.append(" FROM DUAL ) AS B \n");
            update.append(")\n");
            insertValue.append(")\n");
            insert.append(")\n").append(insertValue.toString());
            mergeSql = sqlStr.toString() + using.toString() + on.toString() + insert.toString() + update.toString();
        }
        return mergeSql;
    }

    /**
     * getInsertSql根据实体类对象字段的值生成INSERT SQL语句,可选固定参数
     * @param model
     * @return
     */
    public static String insertSql(Object model) {
        return insertSql(model, null);
    }

    /**
     * getInsertSql根据实体类对象字段的值生成INSERT SQL语句,可选固定参数
     *
     * @param model
     * @param fixedParams 固定参数(如该参数与实体类中有相同的字段,则忽略实体类中的对应字段,HashMap<String,String>,key=
     *                    指定字段名,value=对应字段的值)
     * @return
     */
    public static String insertSql(Object model, Map<String, String> fixedParams) {
        String insertSql = null;

        String tableName = getTableName(model.getClass());

        List<Field> annoFieldList = getAnnoFieldList(model.getClass());
        if (annoFieldList != null && annoFieldList.size() > 0) {
            StringBuffer sqlStr = new StringBuffer("INSERT INTO " + tableName + " (");
            StringBuffer valueStr = new StringBuffer(" VALUES (");

            // 增加固定参数部分
            if (fixedParams != null && fixedParams.size() > 0) {
                Iterator<String> keyNames = fixedParams.keySet().iterator();
                while (keyNames.hasNext()) {
                    String keyName = keyNames.next();
                    sqlStr.append(keyName + ",");
                    valueStr.append("'" + fixedParams.get(keyName) + "',");
                }
            }

            for (Field field : annoFieldList) {
                // 除去固定参数部分
                String columnName = getColumnName(field);
                if (fixedParams != null && fixedParams.containsKey(columnName)) {
                    break;
                }

                String columnValue = getColumnValue(model, field);
                if (!isBlank(columnValue)) {
                    sqlStr.append(columnName + ",");
                    valueStr.append(columnValue + ",");
                }
            }
            insertSql = sqlStr.toString().substring(0, sqlStr.length() - 1) + ")"
                    + valueStr.toString().substring(0, valueStr.length() - 1) + ")";
        }
        return insertSql;
    }

    /**
     * 如果注解没有填写表名，那么默认为类名
     *
     * @param clazz
     * @return
     */
    private static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        // 如果注解没有填写表名，那么默认为类名
        if (isBlank(table.value())) {
            return clazz.getSimpleName().toUpperCase();
        }
        return table.value();
    }

    /**
     * 根据属性获取字段名称
     *
     * @param field
     * @return
     */
    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (!isBlank(column.value())) {
            return column.value().toUpperCase();
        } else {
            return field.getName().toUpperCase();
        }
    }

    /**
     * 获取主键Field
     *
     * @param fieldList
     * @return
     */
    private static List<Field> getPKAnnoFieldList(List<Field> fieldList) {
        List<Field> list = new ArrayList<Field>();
        for (Field field : fieldList) {
            Column column = field.getAnnotation(Column.class);
            if (column.isPk()) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 获取素有主键的属性，这样分开来有利于后期扩展
     *
     * @param fieldList
     * @return Map<String,Field> key为列名称ColumnName，Field主键属性
     */
    private static Map<String, Field> getPKAnnoFieldMap(List<Field> fieldList) {
        Map<String, Field> pKColumnNameMap = new HashMap<String, Field>();
        for (Field field : fieldList) {
            Column column = field.getAnnotation(Column.class);
            if (column.isPk()) {
                pKColumnNameMap.put(getColumnName(field), field);
            }
        }
        return pKColumnNameMap;
    }

    /**
     * 获取所有有注释的字段,支持多重继承
     */
    @SuppressWarnings("unchecked")
    private static List<Field> getAnnoFieldList(Class<?> clazz) {
        return ReflectUtil.getAnnoFieldList(clazz, new Class[]{Column.class});
    }

    /**
     * 获取所有有注释的字段,支持多重继承
     */
    private static Map<String, Field> getAnnoFieldMap(List<Field> list) {
        Map<String, Field> map = new HashMap<String, Field>();
        for (Field field : list) {
            map.put(getColumnName(field), field);
        }
        return map;
    }

    /**
     * 主要用户查询，select  xx
     *
     * @param field
     * @return
     */
    private static String formatColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        ColumnType columnType = column.type();

        StringBuilder sb = new StringBuilder();
        String columnName = null;
        if (!isBlank(column.value())) {
            columnName = column.value().toUpperCase();
        } else {
            columnName = field.getName().toUpperCase();
        }

        if (ColumnType.time.equals(columnType)) {
            sb.append("TO_CHAR(");
            sb.append(columnName);
            sb.append(",'YYYY-MM-DD HH24:MI:SS') AS ");
            sb.append(columnName);
        } else if (ColumnType.date.equals(columnType)) {
            sb.append("TO_CHAR(");
            sb.append(columnName);
            sb.append(",'YYYY-MM-DD') AS ");
            sb.append(columnName);
        } else {
            sb.append(columnName);
        }
        return sb.toString();
    }

    /**
     * 根据对应的配置属性获取展缓后的值。如果where后面存在主键不能使用次方法，应为此方法获取的主键是根据配置生产，不是model的真实值
     *
     * @param obj
     * @param field
     * @return
     */
    private static String getColumnValue(Object obj, Field field) {
        String value = getFieldValue(obj, field);
        // 如果存在值，才考虑转换
        Column column = field.getAnnotation(Column.class);
        if (!isBlank(value) && !column.isPk()) {
            StringBuilder valueSb = new StringBuilder();
            // 转换值
            ColumnType columnType = column.type();
            // 字符串
            if (ColumnType.varchar.equals(columnType)) {
                valueSb.append("'").append(value).append("'");
                // 时间
            } else if (ColumnType.time.equals(columnType)) {
                valueSb.append("TO_DATE('");
                valueSb.append(DateUtil.formatDate(value, "yyyy-MM-dd HH:mm:ss"));
                valueSb.append("','YYYY-MM-DD HH24:MI:SS')");
                // 日期
            } else if (ColumnType.date.equals(columnType)) {
                valueSb.append("TO_DATE('");
                valueSb.append(DateUtil.formatDate(value, "yyyy-MM-dd"));
                valueSb.append("','YYYY-MM-DD')");
            }
            value = valueSb.toString();
        } else if (column.isPk()) {
            Generator generator = field.getAnnotation(Generator.class);
            // 程序指派
            if (generator == null || GeneratorType.assigned.equals(generator.type())) {
                // 获取的是getMethod的值，
                if (!isBlank(value)) {
                    value = "'" + value + "'";
                }
            }
            // oracle的sequence
            else if (GeneratorType.sequence.equals(generator.type())) {
                value = generator.value() + ".NEXTVALUE";
            }
            // mysql 自增主键
            else if (GeneratorType.increment.equals(generator.type())) {
                value = null;
            }
            // uuid
            else if (GeneratorType.uuid.equals(generator.type())) {
                value = "'" + uuid() + "'";
            }

        }
        return value;
    }

    /**
     * 返回field 的值,获取的是原始值，没有根据sql配置的类型转换
     *
     * @param obj
     * @param field
     * @return
     */
    private static String getFieldValue(Object obj, Field field) {
        return ReflectUtil.getFieldValue(obj, field);
    }

    /**
     * 判断字符是否为空 常见如果：<br>
     * "  " : true<br>
     * "" : true<br>
     * null : true<br>
     * "  null " : true<br>
     * "  NULL ": true<br>
     *
     * @param s
     * @return
     */
    private static boolean isBlank(String s) {
        return s == null || "".equals(s.trim());
    }

    /**
     * 获取UUID字符串 ，形如：c679340e10a14aa0bceb354d375795c4
     *
     * @return
     */
    private static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
