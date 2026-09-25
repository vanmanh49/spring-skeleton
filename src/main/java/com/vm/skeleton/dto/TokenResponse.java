package com.vm.skeleton.dto;

import java.time.Instant;
import java.util.Set;

public record TokenResponse(String userName, Set<String> roles, String jwt, Instant expiresAt) {
}
