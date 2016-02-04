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
        get("/erpsearch/:type", (request, response) -> {

            ISearch iSearch = (ISearch) Class.forName("wy.erp.service." + request.params("type")).newInstance();

            return JSONObject.toJSON(iSearch.search());

        });
        /**
         * 请求前
         */
        before((request, response) -> {
            boolean authenticated = false;
            // ... check if authenticated
            if (!authenticated) {
                halt(401, request.session().id()+"不欢迎当前会话！");
            }
        });

        /**
         * 请求后
         */
        after((request, response) -> {
            response.header("foo", "set by after filter");
        });

    }

}
