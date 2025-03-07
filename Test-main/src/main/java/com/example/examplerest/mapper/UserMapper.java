package com.example.examplerest.mapper;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserDto;
import com.example.examplerest.dto.UserResponseDto;
import com.example.examplerest.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

//    @Mapping(target = "role", defaultValue = "USER")
    User map(CreateUserDto createUserDto);

    UserDto map(User user);

    List<UserResponseDto> map(List<User> user);

}
