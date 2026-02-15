package com.example.system.menu.converter;

import com.example.system.menu.domain.entity.Menu;
import com.example.system.menu.dto.MenuInsertRequest;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.dto.MenuUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuConverter {

    @Mapping(target = "menuId", ignore = true)
    @Mapping(target = "menuDepth", ignore = true)
    @Mapping(target = "menuUrl", ignore = true)
    @Mapping(target = "icon", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "menuOrder", ignore = true)
    Menu toEntity(MenuSearchRequest searchRequest);

    Menu toEntity(MenuUpdateRequest updateRequest);

    MenuResponse toMenuResponse(Menu menu);

    List<MenuResponse> toMenuResponseList(List<Menu> menuList);

    @Mapping(target = "menuId", ignore = true) 
    @Mapping(target = "menuDepth", ignore = true)
    Menu toEntity(MenuInsertRequest insertRequest);



}
