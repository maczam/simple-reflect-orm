simple-reflect-orm
=====
使用java的注解和反射机制动态生成简单sql，以及将查询结果封装成对象。
CreateSqlUtil可以生成查询、update、delete、oracle的merge甚至可以根据主键生成策略去根据不同的数据生成主键，可以根据注解将日期格式化成对应的字符串。
同时支持spring JdbcTemplate将查询结果封装指定类型的对象，可以转化简单格式。

AutoRowMapper主要支持spring。

不支持多表操作。



Quick Examples
===

生成查询sql
---
```java
    Map<String, String> param = new HashMap<String, String>();
    param.put("id", id);
    String sql = CreateSqlUtil.findSql(clazz, param);
    logger.debug(sql);
```

查找并封装
---
```java
public <T> List<T> findDataList(Class<T> clazz, Map<String, String> map) {
    String sql = CreateSqlUtil.findSql(clazz, map);
    logger.debug(sql);
    return (List<T>) jdbcTemplate.query(sql, new AutoRowMapper<T>(clazz));
    }
```
update,merge...
----
需要自己探索，相见CreateSqlUtil