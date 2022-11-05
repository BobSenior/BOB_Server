package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.user.User;
import com.bob_senior.bob_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public boolean checkUserExist(Integer userIdx){
        return userRepository.existsUserByUserIdx(userIdx);
    }

    public String getNickNameByIdx(Integer userIdx) {
        User user = userRepository.findUserByUserIdx(userIdx);
        return user.getNickName();
    }
}
