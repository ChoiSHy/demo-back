package com.example.system.menu.converter;

import com.example.system.menu.domain.entity.Menu;
import com.example.system.menu.dto.MenuResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuConverter {

    Menu toEntity(MenuSearchRequest searchRequest);

    MenuResponse toMenuResponse(Menu menu);

    List<MenuResponse> toMenuResponseList(List<Menu> menuList);
}
