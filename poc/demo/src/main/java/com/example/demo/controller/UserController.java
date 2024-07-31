package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public User createUser(@RequestBody User user) {
        MDC.put("tenantId", user.getTenantId());
        User savedUser = userRepository.save(user);
        MDC.clear();
        return savedUser;
    }

    @GetMapping
    public List<User> getAllUsers(@RequestParam String tenantId) {
        MDC.put("tenantId", tenantId);
        List<User> users = userRepository.findAll();
        MDC.clear();
        return users;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id, @RequestParam String tenantId) {
        MDC.put("tenantId", tenantId);
        User user = userRepository.findById(id).orElse(null);
        MDC.clear();
        return user;
    }
}
