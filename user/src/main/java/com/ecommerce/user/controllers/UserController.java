package com.ecommerce.user.controllers;

import com.ecommerce.user.dto.AuthDtos.UserView;
import com.ecommerce.user.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserView> me(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }
}
