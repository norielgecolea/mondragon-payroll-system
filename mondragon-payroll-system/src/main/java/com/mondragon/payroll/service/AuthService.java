package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.*;
import com.mondragon.payroll.exception.BusinessException;
import com.mondragon.payroll.exception.ResourceNotFoundException;
import com.mondragon.payroll.model.User;
import com.mondragon.payroll.repository.UserRepository;
import com.mondragon.payroll.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));
        if (!Boolean.TRUE.equals(user.getActive()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }
        return toLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String username) {
        User user = requireUser(username);
        return AccountDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public LoginResponse updateAccount(String currentUsername, UpdateAccountRequest request) {
        User user = requireUser(currentUsername);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        boolean changed = false;
        if (request.getUsername() != null) {
            String newUsername = request.getUsername().trim();
            if (newUsername.isEmpty()) {
                throw new BusinessException("Username cannot be empty");
            }
            if (!newUsername.equalsIgnoreCase(user.getUsername())
                    && userRepository.existsByUsername(newUsername)) {
                throw new BusinessException("Username is already taken");
            }
            if (!newUsername.equals(user.getUsername())) {
                user.setUsername(newUsername);
                changed = true;
            }
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                throw new BusinessException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            changed = true;
        }

        if (!changed) {
            throw new BusinessException("No account changes provided");
        }

        return toLoginResponse(userRepository.save(user));
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private LoginResponse toLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
