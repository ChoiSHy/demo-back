package com.example.system.menu.repository;

import com.example.common.enums.YesNo;
import com.example.system.menu.domain.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    Page<Menu> findAll(Pageable pageable);

    List<Menu> findByUseYnOrderByMenuOrderAsc(YesNo useYn);

    List<Menu> findByParentIdAndUseYnOrderByMenuOrderAsc(Long parentId, YesNo useYn);

    List<Menu> findByParentIdIsNullAndUseYnOrderByMenuOrderAsc(YesNo useYn);

    List<Menu> findByMenuDepthAndUseYnOrderByMenuOrderAsc(Integer menuDepth, YesNo useYn);
}
