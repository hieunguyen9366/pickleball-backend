package com.pickleball.app.service;

import com.pickleball.app.dto.auth.RegisterRequest;
import com.pickleball.app.dto.common.UserDTO;
import com.pickleball.app.entity.User;

import java.util.List;

public interface UserService {
    UserDTO getProfile(User user);

    UserDTO updateProfile(User user, UserDTO dto);

    // Admin ops
    List<UserDTO> getAllUsers();

    UserDTO createUser(RegisterRequest request, String role); // Admin create

    void blockUser(Long id);

    void unblockUser(Long id);
    
    UserDTO updateUser(Long id, RegisterRequest request);
    
    void deleteUser(Long id);
    
    String resetPassword(Long id);
}
