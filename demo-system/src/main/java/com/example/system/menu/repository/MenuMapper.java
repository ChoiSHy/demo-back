package com.example.system.menu.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.system.menu.domain.entity.Menu;
import com.example.system.menu.dto.MenuTreeResponse;

@Mapper
public interface MenuMapper {

    List<Menu> selectAllMenus();
    // SELECT
    List<MenuTreeResponse> selectMenuTree();

    List<Menu> selectChildMenus(Long parentId);
    // DELETE
    int deleteMenuWithChildren(Long menuId);
}
