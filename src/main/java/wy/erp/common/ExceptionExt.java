package wy.erp.common;

/**
 * Created by diaomingkai on 2016-2-1.
 */
public class ExceptionExt extends RuntimeException {
    public ExceptionExt(String msg) {
        super(msg);
    }

    public static void Throw(String msg) {
        System.out.println("自定义运行时异常：" + msg);
    }

    public static void Throw(Exception e) {
        System.out.println(e.getMessage());
        ExceptionExt exp = new ExceptionExt(e.getMessage());
        exp.setStackTrace(e.getStackTrace());
        throw exp;
    }
}
