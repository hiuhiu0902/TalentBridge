package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.LoginRequest;
import com.demo.talentbridge.dto.request.RegisterRequest;
import com.demo.talentbridge.dto.response.AuthResponse;
import com.demo.talentbridge.dto.response.UserResponse;
import com.demo.talentbridge.entity.Candidate;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.CandidateRepository;
import com.demo.talentbridge.repository.EmployerRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.security.JwtTokenProvider;
import com.demo.talentbridge.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .provider("local")
                .build();
        user = userRepository.save(user);

        if (request.getRole() == UserRole.CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .build();
            candidateRepository.save(candidate);
        } else if (request.getRole() == UserRole.EMPLOYER) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new BadRequestException("Company name is required for employer registration");
            }
            Employer employer = Employer.builder()
                    .user(user)
                    .companyName(request.getCompanyName())
                    .build();
            employerRepository.save(employer);
        }

        String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
