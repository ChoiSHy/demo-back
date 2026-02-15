package com.example.system.menu.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.common.enums.YesNo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MenuTreeResponse {
    private Long menuId;
    private String menuName;
    private String menuUrl;
    private Long parentId;
    private Integer menuOrder;
    private Integer menuDepth;
    private String icon;
    private YesNo useYn;

    private List<MenuTreeResponse> children = new ArrayList<>();

    public void addChild(MenuTreeResponse child) {
        this.children.add(child);
    }
}
