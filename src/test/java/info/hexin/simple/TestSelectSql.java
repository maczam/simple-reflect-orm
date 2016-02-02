package info.hexin.simple;

import info.hexin.simple.model.AA;
import info.hexin.simple.reflect.orm.persistence.util.CreateSqlUtil;
import info.hexin.simple.reflect.orm.persistence.util.S;
import org.junit.Test;

/**
 * Created by Administrator on 2016/2/2.
 */
public class TestSelectSql {
    @Test
    public void test1(){
        String id = CreateSqlUtil.findSql(AA.class, S.of("id", "1"));
        System.out.println(id);
    }
    
    @Test
    public void test2(){
        AA aa = new AA();
        String id = CreateSqlUtil.deleteSql(aa, S.of("id", "1"));
        System.out.println(id);

    }
}
