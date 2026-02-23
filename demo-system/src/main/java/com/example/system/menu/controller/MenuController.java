package com.example.system.menu.controller;

import com.example.system.menu.service.MenuService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${path.public-url}/menus")
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/tree")
    public ResponseEntity<?> getMenuTree() {
        return ResponseEntity.ok(menuService.getMenuTree());
    }
}
