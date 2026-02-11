package com.example.system.menu.dto;

import com.example.common.enums.YesNo;
import com.example.system.menu.domain.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private Long menuId;
    private String menuName;
    private String menuUrl;
    private Long parentId;
    private Integer menuOrder;
    private Integer menuDepth;
    private String icon;
    private YesNo useYn;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuUrl(menu.getMenuUrl())
                .parentId(menu.getParentId())
                .menuOrder(menu.getMenuOrder())
                .menuDepth(menu.getMenuDepth())
                .icon(menu.getIcon())
                .useYn(menu.getUseYn())
                .description(menu.getDescription())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
