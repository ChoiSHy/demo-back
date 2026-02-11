package com.example.system.menu.dto;

import com.example.common.enums.YesNo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuSearchRequest {

    private String menuName;
    private YesNo useYn;
    private Integer menuDepth;
    private Long parentId;
}
