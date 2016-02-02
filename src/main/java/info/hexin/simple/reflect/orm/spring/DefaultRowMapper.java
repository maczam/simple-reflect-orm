package info.hexin.simple.reflect.orm.spring;

import org.springframework.jdbc.core.RowMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/2.
 */
public abstract class DefaultRowMapper {
    static Map<Class<?>, AutoRowMapper<?>> INS = new HashMap<>();

    /**
     * 获取RowMapper对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> RowMapper autoRowMapper(Class<T> clazz) {
        AutoRowMapper<?> autoRowMapper = INS.get(clazz);
        if (autoRowMapper == null) {
            autoRowMapper = new AutoRowMapper(clazz);
            INS.put(clazz, autoRowMapper);
        }
        return autoRowMapper;
    }
}
