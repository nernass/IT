package com.example.examplerest.endpoint;

import com.example.examplerest.dto.*;
import com.example.examplerest.mapper.UserMapper;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.CustomUserRepo;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.example.examplerest.service.UserService;
import com.example.examplerest.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserEndpoint {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil tokenUtil;

    private final CurrencyService currencyService;

    private final CustomUserRepo customUserRepo;

    @PostMapping("/user")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserDto createUserDto) {
        Optional<User> existingUser = userService.findByEmail(createUserDto.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        HashMap<String, String> currencies = currencyService.getCurrencies();
        if (currencies != null) {
            System.out.println(currencies.get("USD"));
        }
        User user = userMapper.map(createUserDto);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userMapper.map(userService.save(user)));
    }

    @PostMapping("/user/auth")
    public ResponseEntity<?> auth(@Valid @RequestBody UserAuthDto userAuthDto) {
        Optional<User> byEmail = userService.findByEmail(userAuthDto.getEmail());
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            if (passwordEncoder.matches(userAuthDto.getPassword(), user.getPassword())) {
//                log.info("User with username {} get auth token", user.getEmail());
                return ResponseEntity.ok(UserAuthResponseDto.builder()
                        .token(tokenUtil.generateToken(user.getEmail()))
                        .user(userMapper.map(user))
                        .build()
                );
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserResponseDto>> getUsers(@RequestBody UserFilterDto userFilterDto) {
        return ResponseEntity.ok(userMapper.map(customUserRepo.users(userFilterDto)));
    }

}
