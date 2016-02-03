package wy.erp.modules;

import com.google.gson.Gson;
import wy.erp.idao.ISearch;

import static spark.Spark.get;

/**
 * Created by diaomingkai on 2016-2-3.
 */
public class StockDayModule {


    public static void main(String[] args) {
        /**
         * 采购日报表查询
         */
        /*get("/erpsearch", (req, res) -> {

            ISearch is = (ISearch) Class.forName("wy.erp.service.StockDayService").newInstance();
            Gson gson = new Gson();
            return gson.toJson(is.search());

        });*/

        get("/StockDayList/:name", (req, res) -> req.params(":name"), new Gson()::toJson);





    }
}
