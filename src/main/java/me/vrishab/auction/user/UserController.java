package me.vrishab.auction.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.converter.UserCreationToUserConverter;
import me.vrishab.auction.user.converter.UserToUserDTOConverter;
import me.vrishab.auction.user.dto.UserDTO;
import me.vrishab.auction.user.dto.UserEditableDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserToUserDTOConverter userToUserDTOConverter;
    private final UserCreationToUserConverter userCreationToUserConverter;
    private final AuthService authService;

    public UserController(UserService userService, UserToUserDTOConverter userToUserDTOConverter, UserCreationToUserConverter userCreationToUserConverter, AuthService authService) {
        this.userService = userService;
        this.userToUserDTOConverter = userToUserDTOConverter;
        this.userCreationToUserConverter = userCreationToUserConverter;
        this.authService = authService;
    }

    @GetMapping("/users/{username}")
    public Result findUserByUsername(@PathVariable String username) {
        User user = this.userService.findByUsername(username);
        UserDTO userDTO = this.userToUserDTOConverter.convert(user);
        return new Result(true, "Find a user", userDTO);
    }

    @GetMapping("/users")
    public Result findAllUser() {
        List<UserDTO> userDTOS = this.userService.findAll().
                stream().map(userToUserDTOConverter::convert).toList();
        return new Result(true, "Find all users", userDTOS);
    }

    @PostMapping("/users")
    public Result addUser(@Valid @RequestBody UserEditableDTO newUserDTO) {
        User newUser = this.userCreationToUserConverter.convert(newUserDTO);
        User savedUser = this.userService.save(newUser);
        UserDTO savedUserDto = this.userToUserDTOConverter.convert(savedUser);
        return new Result(true, "Add a user", savedUserDto);
    }

    @PutMapping("/user/self")
    public Result updateUser(Authentication auth, @Valid @RequestBody UserEditableDTO updateDTO) {
        String userId = authService.getUserInfo(auth);

        User update = this.userCreationToUserConverter.convert(updateDTO);
        User updatedUser = this.userService.update(userId, update);
        UserDTO updatedUserDto = this.userToUserDTOConverter.convert(updatedUser);

        return new Result(true, "Update a user", updatedUserDto);
    }

    @DeleteMapping("/user/self")
    public Result deleteUser(Authentication auth) {
        String userId = authService.getUserInfo(auth);
        this.userService.delete(userId);

        return new Result(true, "Delete a user");
    }


}
