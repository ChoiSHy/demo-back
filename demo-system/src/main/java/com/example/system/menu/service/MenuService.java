package com.example.system.menu.service;

import com.example.common.dto.PageResponse;
import com.example.common.enums.YesNo;
import com.example.system.menu.dto.MenuInsertRequest;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.dto.MenuTreeResponse;
import com.example.system.menu.dto.MenuUpdateRequest;

import java.util.List;

public interface MenuService {

    PageResponse<MenuResponse> findAdmMenus(MenuSearchRequest searchRequest, int page, int pageSize);

    void insertMenu(MenuInsertRequest insertRequest);

    MenuResponse updateMenu(MenuUpdateRequest updateRequest);

    MenuResponse updateUseYn(Long menuId, YesNo useYn);

    List<MenuTreeResponse> getMenuTree();
}
