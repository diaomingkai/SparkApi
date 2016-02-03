/**
 * Created by diaomingkai on 2016-2-3.
 */
public class Impl {
    public static synchronized void test(final String path, final Route route) {
        // addRoute(HttpMethod.get.name(), wrap(path, route));
    }


    public static void main(String[] args) {
        test("", (a, b) -> {
            return "111";
        });
    }
}
