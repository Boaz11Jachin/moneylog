package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.codenova.moneylog.entity.Category;

import java.util.List;

@Mapper
public interface CategoryRepository {

    public List<Category> findAll();
    public Category findById(int id);

}
