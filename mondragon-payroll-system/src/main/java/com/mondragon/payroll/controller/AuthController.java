package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.*;
import com.mondragon.payroll.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AccountDto me(Authentication authentication) {
        return authService.getAccount(authentication.getName());
    }

    @PutMapping("/account")
    public LoginResponse updateAccount(Authentication authentication,
                                       @Valid @RequestBody UpdateAccountRequest request) {
        return authService.updateAccount(authentication.getName(), request);
    }
}
