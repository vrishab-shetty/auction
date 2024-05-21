package me.vrishab.auction.user;

import jakarta.validation.Valid;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.converter.UserCreationToUserConverter;
import me.vrishab.auction.user.converter.UserToUserDTOConverter;
import me.vrishab.auction.user.dto.UserDTO;
import me.vrishab.auction.user.dto.UserEditableDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final UserToUserDTOConverter userToUserDTOConverter;
    private final UserCreationToUserConverter userCreationToUserConverter;

    public UserController(UserService userService, UserToUserDTOConverter userToUserDTOConverter, UserCreationToUserConverter userCreationToUserConverter) {
        this.userService = userService;
        this.userToUserDTOConverter = userToUserDTOConverter;
        this.userCreationToUserConverter = userCreationToUserConverter;
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
    public Result addUser(@Valid @RequestBody UserEditableDTO userEditableDTO) {
        User newUser = this.userCreationToUserConverter.convert(userEditableDTO);
        User savedUser = this.userService.save(newUser);
        UserDTO savedUserDto = this.userToUserDTOConverter.convert(savedUser);
        return new Result(true, "Add a user", savedUserDto);
    }
}
