package org.example.recipebookserver.service;

import org.example.recipebookserver.model.User;
import org.example.recipebookserver.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
       user.setPassword(SimplePasswordEncoder.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }
    public User login(String email, String rewPassword){
        User user = userRepository.findByEmail(email);
        if(user != null && SimplePasswordEncoder.matches(rewPassword, user.getPassword())){
         return user;
        }
        throw new RuntimeException("Невірний email або пароль");
    }
}

