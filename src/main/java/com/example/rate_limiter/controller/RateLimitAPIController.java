package com.example.rate_limiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateLimitAPIController {

    @GetMapping("/rate-limit")
    public ResponseEntity<String> rateLimit() {
        return ResponseEntity.ok("You have successfully called the API within the rate limit.");
    }
}
