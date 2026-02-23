package com.example.system.menu.controller;

import com.example.common.dto.ApiResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.dto.MenuUpdateRequest;
import com.example.system.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${path.base-url}/adm-menus")
public class AdmMenuController {
    private final MenuService menuService;

    @GetMapping("list")
    public ResponseEntity<?> findMenus(MenuSearchRequest searchRequest, int page, int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(menuService.findAdmMenus(searchRequest, page, pageSize)));
    }

    @PostMapping("update")
    public ResponseEntity<?> updateMenu(@RequestBody MenuUpdateRequest updateRequest) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateMenu(updateRequest)));
    }

    @PostMapping("update/use-yn")
    public ResponseEntity<?> updateUseYN(@RequestBody MenuUpdateRequest updateRequest) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateUseYn(updateRequest.menuId(), updateRequest.useYn())));
    }
}
