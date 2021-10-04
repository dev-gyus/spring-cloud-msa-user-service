package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final RestTemplate restTemplate;
    private final OrderServiceClient orderServiceClient;
    private final Environment env;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByEmail(email);
        if(entity == null){
            throw new UsernameNotFoundException(email);
        }

        return new User(entity.getEmail(), entity.getEncryptedPwd(), Arrays.asList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity entity = modelMapper.map(userDto, UserEntity.class);
        entity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        return modelMapper.map(userRepository.save(entity), UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto dto = new ModelMapper().map(userEntity, UserDto.class);

        // Using as RestTemplate
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId); // user-service.yml 파일에 주소 별도로 저장해서 관리
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                new ParameterizedTypeReference<List<ResponseOrder>>() {});
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        // Using as FeignClient
        // Manual Feign exception handling
//        try {
//            List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
//            dto.setOrders(orderList);
//        }catch(FeignException e){
//            log.error(e.getMessage());
//        }

        // Errordecoder 사용
//        List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
        log.info("Before call orders microservice");
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitBreaker");
        List<ResponseOrder> orders = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());
        log.info("After call orders microservice");
        dto.setOrders(orders);
        return dto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getDetailsByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email);
        if(entity == null){
            throw new UsernameNotFoundException("email");
        }
        return new ModelMapper().map(entity, UserDto.class);
    }

}
