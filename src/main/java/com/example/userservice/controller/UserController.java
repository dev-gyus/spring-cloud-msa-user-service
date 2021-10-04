package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Greeting greeting;
    private final Environment env;
    private final UserService userService;

    @GetMapping("/health_check")
    @Timed(value = "users.status", longTask = true)
    public String status(){
        return String.format("it's working in user service " +
                ", Port(local.server.port)= " + env.getProperty("local.server.port") +
                ", Port(server.port)= " + env.getProperty("server.port") +
                ", token secret=" + env.getProperty("token.secret") +
                ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    @Timed(value = "users.welcome", longTask = true)
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

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> findAllUsers(){
        Iterable<UserEntity> users = userService.getUserByAll();
        List<ResponseUser> res = new ArrayList<>();
        users.forEach(entity ->
                res.add(new ModelMapper().map(entity, ResponseUser.class))
        );
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> findUserDetail(@PathVariable String userId){
        UserDto dto = userService.getUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(dto, ResponseUser.class));
    }
}
