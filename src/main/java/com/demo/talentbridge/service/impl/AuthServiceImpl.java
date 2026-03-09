package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.GoogleAuthRequest;
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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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

    @Value("{spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
//        if (request.getRole() == UserRole.ADMIN) {
//            throw new BadRequestException("Cannot register as ADMIN");
//        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .provider("local")
                .build();
        user = userRepository.save(user);

        if (request.getRole() == UserRole.CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .user(user)
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
    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());

            if(idToken == null){
                throw new BadRequestException("Invalid idToken");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;

            if(userOptional.isPresent()){
                user = userOptional.get();
                String currentProvider = user.getProvider() != null ? user.getProvider() : "";
                if(!currentProvider.contains("google")){
                    user.setProvider(currentProvider.equals("local") ? "local,google" : currentProvider + ",google");
                if(user.getAvatarUrl() == null && pictureUrl != null){
                    user.setAvatarUrl(pictureUrl);
                    }
                    user = userRepository.save(user);
                }
            } else {
                if(request.getRole() == null || request.getRole() == UserRole.ADMIN){
                    throw new BadRequestException("Invalid role");
                }

                user = User.builder()
                        .username(email)
                        .email(email)
                        .fullName(name)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(request.getRole())
                        .active(true)
                        .provider("google")
                        .avatarUrl(pictureUrl)
                        .build();
                user = userRepository.save(user);

                if (request.getRole() == UserRole.CANDIDATE) {
                    Candidate candidate = Candidate.builder().user(user).build();
                    candidateRepository.save(candidate);
                } else if (request.getRole() == UserRole.EMPLOYER) {
                    if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                        throw new BadRequestException("Tên công ty là bắt buộc đối với Employer");
                    }
                    Employer employer = Employer.builder()
                            .user(user)
                            .companyName(request.getCompanyName())
                            .build();
                    employerRepository.save(employer);
                }
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
        } catch (Exception e) {
            throw new BadRequestException("Invalid Google token");
        }
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
