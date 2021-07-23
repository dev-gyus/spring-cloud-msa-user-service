package com.example.userservice.controller;

import com.example.userservice.controller.dto.UserDto;
import com.example.userservice.controller.service.UserService;
import com.example.userservice.controller.vo.Greeting;
import com.example.userservice.controller.vo.RequestUser;
import com.example.userservice.controller.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Greeting greeting;
    private final Environment env;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status(){
        return "it's working in user service";
    }

    @GetMapping("/welcome")
    public String welcome(){
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestUser requestUser){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto dto = userService.createUser(modelMapper.map(requestUser, UserDto.class));

        return new ResponseEntity<>(modelMapper.map(dto, ResponseUser.class), HttpStatus.CREATED);
    }
}
