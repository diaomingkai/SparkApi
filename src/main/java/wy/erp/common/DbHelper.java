package wy.erp.common;


import org.apache.commons.dbcp.BasicDataSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by diaomingkai on 2016-2-1.
 */
public class DbHelper {


    /**
     * 获取数据库连接对象
     *
     * @return
     */
    private static Connection getConnection(String databaseName) {
        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            ds.setUrl("jdbc:sqlserver://192.168.16.20:1433;databaseName=" + databaseName);
            ds.setUsername("Es");
            ds.setPassword("Es123456~");
            ds.setInitialSize(5);
            ds.setMaxWait(5000);
            ds.setMaxActive(5);
            ds.setMinIdle(10);
            System.out.println("数据库初始化已完成.....");
            Connection conn = ds.getConnection();
            return conn;
        } catch (Exception e) {
            ExceptionExt.Throw(e);
        }
        return null;
    }

    /**
     * 调用存储过程（返回 CallableStatement）
     *
     * @param pro  存储过程调用
     * @param args 参数
     * @return
     */
    public static Map callProcMap(String databaseName, String pro, Object... args) {
        CallableStatement proc = null;
        Connection conn = null;
        ResultSet rs = null;
        Map<String, Object> map = new HashMap<>();
        StringBuffer stringBuffer = new StringBuffer();

        try {
            conn = getConnection(databaseName);
            for (int i = 0; i < args.length; i++) {
                stringBuffer.append((args.length - 1) == i ? "?" : "?,");
            }
            proc = conn.prepareCall("{ call " + pro + "(" + stringBuffer.toString() + ")}");
            for (int i = 0; i < args.length; i++) {
                proc.setObject(i + 1, args[i]);
            }
            proc.execute();
            rs = proc.getResultSet();
            ResultSetMetaData m = rs.getMetaData();
            if (rs != null) {
                int i = 0;
                while (rs.next()) {
                    //显示列,表格的表头
                    for (int j = 1; j <= m.getColumnCount(); j++) {
                        map.put(m.getColumnName(j), rs.getObject(m.getColumnName(j)));
                    }
                    i++;
                    break;
                }
                rs.close();
            }
        } catch (SQLException e) {
            ExceptionExt.Throw(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                ExceptionExt.Throw(e);
            }
        }
        return map;
    }

    /**
     * 调用存储过程（返回 CallableStatement）
     *
     * @param pro  存储过程调用
     * @param args 参数
     * @return
     */
    public static <T> List<T> callProcHasResult(String databaseName, String pro, Class<T> type, Object... args) {
        PreparedStatement proc = null;
        Connection conn = null;
        ResultSet rs = null;
        StringBuffer stringBuffer = new StringBuffer();
        List<T> list = new ArrayList<T>();
        try {
            conn = getConnection(databaseName);
            for (int i = 0; i < args.length; i++) {
                stringBuffer.append((args.length - 1) == i ? "?" : "?,");
            }
            // proc = conn.prepareCall("{SET NOCOUNT ON; call " + pro + "(" + stringBuffer.toString() + ")}");
            proc = conn.prepareStatement("SET NOCOUNT ON; EXEC "+pro+" "+stringBuffer.toString());
            for (int i = 0; i < args.length; i++) {
                proc.setObject(i + 1, args[i]);
            }
            rs = proc.executeQuery();
            //  rs = proc.getResultSet();
            if (rs != null) {
                while (rs.next()) {
                    T t = null;
                    try {
                        //重新实例化，避免每次更改单个对象
                        t = type.newInstance();
                    } catch (Exception e) {
                        ExceptionExt.Throw(e);
                    }
                    t = setClassAttribute(t, rs);
                    list.add(t);
                }
                rs.close();
            }

        } catch (SQLException e) {
            ExceptionExt.Throw(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                ExceptionExt.Throw(e);
            }
        }
        return list;
    }

    /**
     * 根据ResultSet反射指定实体
     *
     * @param c  指定实体实例
     * @param rs ResultSet数据
     * @return
     */
    protected static <T> T setClassAttribute(T c, ResultSet rs) throws SQLException {

        Class<?> cls = c.getClass();
        //判断是否基本类型
        if (IsSimpleType(cls)) {
            return (T) rs.getObject(0);
        }


        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            //  method.invoke();
            if (method.getName().startsWith("set") == false) continue;

            if (method.getParameterTypes().length == 0) continue;

            String name = method.getName().substring(3);
            if (name.length() == 0) continue;

            //判断数据集中是否存在当前列
            if (isExistColumn(rs, name)) {
                try {
                    Class firstType = method.getParameterTypes()[0];
                    Object val = rs.getObject(name);
                    if (val == null) continue;
                    if (firstType.toString().equals("class java.util.Date")) {
                        method.invoke(c, AsDate(val));
                    } else {
                        method.invoke(c, val.toString());
                    }
                } catch (Exception e) {
                    ExceptionExt.Throw(e);
                }
            }
        }

     /*
        * 得到类中的所有属性集合
        */
        /*Field[] fs = c.getClass().getDeclaredFields();
        for (Field field : fs) {

            //判断数据集中是否存在当前列
            if (isExistColumn(rs, field.getName())) {
                field.setAccessible(true); //设置些属性是可以访问的
                //设置指定属性值
                try {
                    if (field.getType().toString().toLowerCase().equals("class java.util.date")) {
                        field.set(c, rs.getDate(field.getName()));
                    } else {
                        field.set(c, ConvertUtils.convert(rs.getObject(field.getName()), field.getType()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }*/


        return c;
    }

    /**
     * 转换时间类型
     *
     * @param Value
     * @return
     */
    private static Date AsDate(Object Value) {
        if (Value == null) return new Date(0);
        Class cls = Value.getClass();
        if (cls.isAssignableFrom(Date.class)) {
            return (Date) Value;
        } else if (cls.isAssignableFrom(Integer.class) || cls.isAssignableFrom(int.class) || cls.isAssignableFrom(Long.class) || cls.isAssignableFrom(long.class)) {
            return new Date((Long.parseLong(Value != null ? Value.toString() : "0")));
        }

        //字符串格式。转换两种格式 : 2015-12-28 , 2015/12/28
        String strValue = Value.toString();
        if (strValue.length() == 0) return new Date(0);

        strValue = strValue.replace('/', '-');
        boolean hasDate = strValue.contains("-");
        boolean hasTime = strValue.contains(":");

        DateFormat format = null;
        if (hasDate && hasTime) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else if (hasDate) {
            format = new SimpleDateFormat("yyyy-MM-dd");
        } else {
            format = new SimpleDateFormat("HH:mm:ss");
        }

        try {
            return format.parse(strValue);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(0);
    }

    /**
     * 判断是否基本类型
     *
     * @param type
     * @return
     */
    private static boolean IsSimpleType(Class type) {
        if (type == null) return false;

        if (type.isPrimitive()) return true;
        if (type.isEnum()) return true;

        if (type.isAssignableFrom(String.class)) return true;
        if (type.isAssignableFrom(Integer.class)) return true;
        if (type.isAssignableFrom(Boolean.class)) return true;
        if (type.isAssignableFrom(Long.class)) return true;
        if (type.isAssignableFrom(Float.class)) return true;
        if (type.isAssignableFrom(Character.class)) return true;
        if (type.isAssignableFrom(Byte.class)) return true;
        if (type.isAssignableFrom(Float.class)) return true;
        if (type.isAssignableFrom(Double.class)) return true;
        if (type.isAssignableFrom(Date.class)) return true;

        return false;
    }

    /**
     * 判断查询结果集中是否存在某列
     *
     * @param rs         查询结果集
     * @param columnName 列名
     * @return true 存在; false 不存在
     */
    private static boolean isExistColumn(ResultSet rs, String columnName) {
        try {
            if (rs.findColumn(columnName) > 0) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }

        return false;
    }
}
