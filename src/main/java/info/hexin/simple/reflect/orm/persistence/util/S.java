package info.hexin.simple.reflect.orm.persistence.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成sql语句辅助类
 * @author maczam@163.com
 *
 */
public class S {
	/**
	 * 封装类
	 * @param str
	 * @return
	 */
	public static Map<String, String> of(String... str) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < str.length; i = i + 2) {
			map.put(str[i], str[i + 1]);
		}
		return map;
	}
}
