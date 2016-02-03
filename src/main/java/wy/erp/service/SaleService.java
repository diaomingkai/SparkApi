package wy.erp.service;

import wy.erp.entity.User;
import wy.erp.idao.ISearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diaomingkai on 2016-2-3.
 */
public class SaleService implements ISearch {
    @Override
    public List<User> search() {
        List<User> list = new ArrayList<>();
        User user = new User();
        user.setCode("123");
        user.setName("刁刁");
        list.add(user);
        return list;
    }
}
