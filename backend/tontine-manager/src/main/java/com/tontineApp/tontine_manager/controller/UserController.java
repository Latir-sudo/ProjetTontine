package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.UserRequest;
import com.tontineApp.tontine_manager.dto.UserResponse;
import com.tontineApp.tontine_manager.service.UserService;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable("id") int id) {
        return userService.getById(id);
    }

    @GetMapping("/search")
    public UserResponse getUserByTelephone(@RequestParam String telephone){
        return userService.getUserByTelephone(telephone);
    }
    @PostMapping
    public UserResponse saveUser(@RequestBody UserRequest userRequest) {
         return userService.saveUser(userRequest);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Integer id){
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(@PathVariable("id") Integer id, @RequestBody UserRequest userRequest) {
        return userService.updateUser(userRequest,id);
    }
}
