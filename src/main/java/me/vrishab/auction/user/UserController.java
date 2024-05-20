package me.vrishab.auction.user;

import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.converter.UserToUserDTOConverter;
import me.vrishab.auction.user.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final UserToUserDTOConverter userToUserDTOConverter;

    public UserController(UserService userService, UserToUserDTOConverter userToUserDTOConverter) {
        this.userService = userService;
        this.userToUserDTOConverter = userToUserDTOConverter;
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
}
