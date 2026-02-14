package com.example.system.menu.controller;

import com.example.common.dto.ApiResponse;
import com.example.system.menu.dto.MenuSearchRequest;
import com.example.system.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${base.url}/adm-menus")
public class AdmMenuController {
    private final MenuService menuService;

    @GetMapping("list")
    public ResponseEntity<?> findMenus(MenuSearchRequest searchRequest, int page, int pageSize){
        return ResponseEntity.ok(ApiResponse.success(menuService.findAdmMenus(searchRequest, page, pageSize)));
    }
 }
