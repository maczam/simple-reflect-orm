package info.hexin.simple.reflect.orm.persistence.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * @author ydhexin@163.com
 *
 */
public class DateUtil {
	/**
	 * 获取当前时间
	 * @return
	 */
	public static Date now(){
		return new Date();
	}
	
	/**
	 * 格式化 时间字符串   
	 * 
	 * 输入：Wed Nov 09 18:59:00 CST 2011
	 * 输出：2011-11-09 18:59
	 * @param timeStr
	 * @return
	 */
	public static String formatTimeFromFront(String timeStr){
		SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy",Locale.US);
		String result = "";
		try {
			Date date = format.parse(timeStr);
			format.applyPattern("yyyy-MM-dd HH:mm:ss");
			result = format.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			format.applyPattern("yyyy-MM-dd HH:mm:ss");
			result = format.format(new Date());
		}
		return result;
	}
	
	/**
	 * 格式化 时间字符串   
	 * 
	 * @param timeStr
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	//Tue May 22 13:38:16 CST 2012
	
	/**
	 * 格式化 时间字符串   
	 * 
	 * @param timeStr
	 * @return
	 */
	public static String formatDate(String timeStr, String format) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		return sdf.format(date);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy",Locale.US);
		String result = "";
		try {
			Date date = sdf.parse(timeStr);
			sdf.applyPattern(format);
			result = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
