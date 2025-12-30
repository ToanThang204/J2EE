package com.hutech.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // Respond with 204 No Content to prevent browser console errors when no favicon is present
        return ResponseEntity.noContent().build();
    }
}
