import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import wy.erp.common.config.Global;
import wy.erp.idao.ISearch;
import wy.erp.modules.StockDayModule;

import static spark.Spark.*;

/**
 * Created by diaomingkai on 2016-1-28.
 */
public class Main {
    public static void main(String[] args) {
        //设置端口
        port(Global.PORT);
        /**
         * 采购日报路由
         */
        StockDayModule.main(args);

        /**
         * 调用查询接口
         */
        get("/erpsearch/:type", (req, res) -> {

            ISearch iSearch = (ISearch) Class.forName("wy.erp.service." + req.params("type")).newInstance();
            //123
            return JSONObject.toJSON(iSearch.search());

        });

    }

}
