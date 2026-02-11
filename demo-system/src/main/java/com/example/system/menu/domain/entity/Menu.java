package com.example.system.menu.domain.entity;

import com.example.common.entity.BaseEntity;
import com.example.common.enums.YesNo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_menu")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_url", length = 255)
    private String menuUrl;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "menu_order")
    private Integer menuOrder;

    @Column(name = "menu_depth")
    private Integer menuDepth;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "use_yn", length = 1)
    @Builder.Default
    private YesNo useYn = YesNo.YES;

    @Column(name = "description", length = 500)
    private String description;
}
