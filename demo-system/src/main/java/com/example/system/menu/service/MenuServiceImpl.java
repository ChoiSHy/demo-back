package com.example.system.menu.service;

import com.example.common.dto.PageResponse;
import com.example.system.menu.converter.MenuConverter;
import com.example.system.menu.domain.entity.Menu;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService{

    private final MenuRepository menuRepository;
    private final MenuConverter menuConverter;

    @Override
    public PageResponse<MenuResponse> findAdmMenus(MenuSearchRequest searchRequest, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        Page<Menu> menuPage = menuRepository.findAll(pageable);
        return new PageResponse<>(menuConverter.toMenuResponseList(menuPage.getContent()), menuPage.getNumberOfElements(), menuPage);
    }
}
