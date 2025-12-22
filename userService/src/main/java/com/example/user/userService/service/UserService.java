package com.example.user.userService.service;

import com.example.user.userService.dto.UserDTO;
import com.example.user.userService.entity.User;
import com.example.user.userService.entity.UserRole;
import com.example.user.userService.exception.DuplicateResourceException;
import com.example.user.userService.exception.ResourceNotFoundException;
import com.example.user.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email: {}", userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("User with email " + userDTO.getEmail() + " already exists");
        }

        User user = mapToEntity(userDTO);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return mapToDTO(savedUser);
    }

    public UserDTO getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getActiveUsers() {
        log.info("Fetching active users");
        return userRepository.findByActive(true)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("User with email " + userDTO.getEmail() + " already exists");
        }

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole());
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : true);

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return mapToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }

    @Transactional
    public UserDTO deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(false);
        User updatedUser = userRepository.save(user);

        log.info("User deactivated successfully with id: {}", id);
        return mapToDTO(updatedUser);
    }

    // Mapper methods
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private User mapToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.USER);
        user.setActive(dto.getActive() != null ? dto.getActive() : true);
        return user;
    }
}