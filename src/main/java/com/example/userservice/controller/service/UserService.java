package com.example.userservice.controller.service;

import com.example.userservice.controller.dto.UserDto;
import org.springframework.stereotype.Service;

public interface UserService {
    UserDto createUser(UserDto userDto);
}
