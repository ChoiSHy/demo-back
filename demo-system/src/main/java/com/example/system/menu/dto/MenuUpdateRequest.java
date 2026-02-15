package com.example.system.menu.dto;

import com.example.common.enums.YesNo;
import lombok.Builder;

@Builder(toBuilder = true)
public record MenuUpdateRequest(
        Long menuId,

        String menuName,

        String menuUrl,

        Long parentId,

        Integer menuOrder,

        Integer menuDepth,

        String icon,

        YesNo useYn,

        String description
) {
}
