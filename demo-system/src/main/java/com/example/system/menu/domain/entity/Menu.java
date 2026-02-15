package com.example.system.menu.domain.entity;

import com.example.common.entity.BaseEntity;
import com.example.common.enums.YesNo;
import com.example.system.menu.dto.MenuUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "tbl_menu")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
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
    private Integer menuDepth = 0;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "use_yn", length = 1)
    @Builder.Default
    private YesNo useYn = YesNo.YES;

    @Column(name = "description", length = 500)
    private String description;

    public void setMenuInfo(MenuUpdateRequest updateRequest) {
        this.menuName = updateRequest.menuName();
        this.menuUrl = updateRequest.menuUrl();
        this.menuOrder = updateRequest.menuOrder();
        this.icon = updateRequest.icon();
        this.useYn = updateRequest.useYn();
        this.description = updateRequest.description();
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public void setUseYn(YesNo useYn) {
        this.useYn = useYn;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
