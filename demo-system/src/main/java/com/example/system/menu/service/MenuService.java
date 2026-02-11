package com.example.system.menu.service;

import com.example.common.dto.PageResponse;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;

import java.util.List;

public interface MenuService {

    PageResponse<MenuResponse> findAdmMenus(MenuSearchRequest searchRequest, int page, int pageSize);
}
