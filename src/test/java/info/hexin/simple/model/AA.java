package info.hexin.simple.model;

import info.hexin.simple.reflect.orm.persistence.annotation.Column;
import info.hexin.simple.reflect.orm.persistence.annotation.Table;

/**
 * Created by Administrator on 2016/2/2.
 */
@Table("T_aa")
public class AA  extends AB{
    @Column(isPk = true)
    public String id;

    @Column()
    public String a1;

}
