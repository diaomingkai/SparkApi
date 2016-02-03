package wy.erp.service;

import com.google.gson.Gson;
import wy.erp.common.DbHelper;
import wy.erp.entity.StockDay;
import wy.erp.idao.ISearch;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;

/**
 * Created by diaomingkai on 2016-2-2.
 */
public class StockDayService implements ISearch {
    /**
     * 查询指定企业采购日报
     *
     * @return
     */
    @Override
    public List<StockDay> search() {
        List<StockDay> list = DbHelper.callProcHasResult("SD65512_Sample", "rpt_MPDailyRpt", StockDay.class, "2016-01-17", "2016-01-23", "(m.moneyid=0) and  1=1", 0);
        return list;
    }


}
