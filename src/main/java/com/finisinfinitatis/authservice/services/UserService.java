package com.finisinfinitatis.authservice.services;

import com.finisinfinitatis.authservice.model.User;
import com.finisinfinitatis.authservice.model.repositories.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public User registerUser(String emailAddress, String password){
        if(checkUserExists(emailAddress)){
            throw new IllegalArgumentException("User "+emailAddress+" exists already!");
        }
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(emailAddress, hashedPassword);
        return userRepository.save(user);
    }

    public User getUser(String emailAddress){
        Optional<User> user = userRepository.findByEmailAddress(emailAddress);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User "+emailAddress+" does not exist!");
        }
        return user.get();
    }

    public boolean checkUserExists(String emailAddress){
        return userRepository.findByEmailAddress(emailAddress).isPresent();
    }

}
