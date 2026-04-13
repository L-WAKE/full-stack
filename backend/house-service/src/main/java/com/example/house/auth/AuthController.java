package com.example.house.auth;

import com.example.house.common.api.ApiResponse;
import com.example.house.modules.system.MenuItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(new AuthService.LoginCommand(request.username(), request.password())));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(resolveToken(request));
        return ApiResponse.success();
    }

    @GetMapping("/profile")
    public ApiResponse<AuthUser> profile(HttpServletRequest request) {
        return ApiResponse.success(authService.getUserByToken(resolveToken(request)));
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuItem>> menus(HttpServletRequest request) {
        AuthUser user = authService.getUserByToken(resolveToken(request));
        return ApiResponse.success(authService.getMenus(user));
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring(7);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }
}
