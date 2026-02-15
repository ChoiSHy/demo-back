package com.example.system.menu.service;

import com.example.common.dto.PageResponse;
import com.example.common.enums.YesNo;
import com.example.system.menu.converter.MenuConverter;
import com.example.system.menu.domain.entity.Menu;
import com.example.system.menu.dto.MenuInsertRequest;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.dto.MenuTreeResponse;
import com.example.system.menu.dto.MenuUpdateRequest;
import com.example.system.menu.repository.MenuMapper;
import com.example.system.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final MenuConverter menuConverter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuResponse> findAdmMenus(MenuSearchRequest searchRequest, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        Page<Menu> menuPage = menuRepository.findAll(pageable);
        return new PageResponse<>(menuConverter.toMenuResponseList(menuPage.getContent()), menuPage.getNumberOfElements(), menuPage);
    }

    @Override
    @Transactional
    public void insertMenu(MenuInsertRequest insertRequest) {
        Menu newMenu = menuConverter.toEntity(insertRequest);
        menuRepository.save(newMenu);
    }

    @Override
    @Transactional()
    public MenuResponse updateMenu(MenuUpdateRequest updateRequest) {
        Menu originMenu = menuRepository.findById(updateRequest.menuId()).orElseThrow(NoSuchElementException::new);
        originMenu.setMenuInfo(updateRequest);

        return menuConverter.toMenuResponse(originMenu);
    }

    @Override
    public MenuResponse updateUseYn(Long menuId, YesNo useYn) {
        Menu originMenu = menuRepository.findById(menuId).orElseThrow(NoSuchElementException::new);
        originMenu.setUseYn(useYn);

        return menuConverter.toMenuResponse(originMenu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuTreeResponse> getMenuTree() {
        List<MenuTreeResponse> menuTree = menuMapper.selectMenuTree();
        return buildMenuTree(menuTree);
    }

    private List<MenuTreeResponse> buildMenuTree(List<MenuTreeResponse> flatMenuList) {
        // 메뉴 ID를 키로 하는 맵 생성
        Map<Long, MenuTreeResponse> menuMap = flatMenuList.stream()
                .collect(Collectors.toMap(MenuTreeResponse::getMenuId, Function.identity()));

        List<MenuTreeResponse> rootMenus = new ArrayList<>();

        for (MenuTreeResponse menu : flatMenuList) {
            if (menu.getParentId() == null) {
                // 최상위 메뉴인 경우 루트 메뉴 리스트에 추가
                rootMenus.add(menu);
            } else {
                // 부모 메뉴가 존재하는 경우, 부모 메뉴의 자식 리스트에 추가
                MenuTreeResponse parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.addChild(menu);
                }
            }
        }

        return rootMenus;
    }
}
