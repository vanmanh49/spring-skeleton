package com.vm.skeleton.service;

import com.vm.skeleton.dto.LoginRequest;
import com.vm.skeleton.dto.TokenResponse;

public interface AuthenticationService {

    TokenResponse authenticate(LoginRequest loginRequest);
}
