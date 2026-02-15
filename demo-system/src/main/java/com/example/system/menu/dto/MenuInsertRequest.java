package com.example.system.menu.dto;

import com.example.common.enums.YesNo;

public record MenuInsertRequest(
        String menuName,
        String menuUrl,
        Long parentId,
        Integer menuOrder,
        String icon,
        YesNo useYn,
        String description
) {
    
}
