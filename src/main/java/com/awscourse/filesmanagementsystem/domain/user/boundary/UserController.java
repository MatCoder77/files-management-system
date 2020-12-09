package com.awscourse.filesmanagementsystem.domain.user.boundary;

import com.awscourse.filesmanagementsystem.api.common.ResourceDTO;
import com.awscourse.filesmanagementsystem.api.common.ResponseDTO;
import com.awscourse.filesmanagementsystem.api.user.CredentialsDTO;
import com.awscourse.filesmanagementsystem.api.user.RegistrationDTO;
import com.awscourse.filesmanagementsystem.api.user.TokenDTO;
import com.awscourse.filesmanagementsystem.api.user.UserDTO;
import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.domain.user.control.UserService;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.HasAnyRole;
import com.awscourse.filesmanagementsystem.infrastructure.security.annotation.LoggedUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;


import javax.validation.Valid;

import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID;
import static com.awscourse.filesmanagementsystem.infrastructure.rest.ResourcePaths.ID_PATH;

@Api(tags = "Users")
@RestController
@RequestMapping(UserController.MAIN_RESOURCE)
@RequiredArgsConstructor
public class UserController {

    public static final String MAIN_RESOURCE = "/api/user";
    private static final String USERNAME_PATH_PARAM = "username";
    private static final String EMAIL_PATH_PARAM = "email";
    private static final String CHECK_USERNAME_RESOURCE = "/check/username/{" + USERNAME_PATH_PARAM + "}";
    private static final String CHECK_EMAIL_RESOURCE = "/check/email/{" + EMAIL_PATH_PARAM + "}";

    private final UserService userService;
    private final UserMapper userMapper;

    @ApiOperation(value = "${api.users.authenticateUser.value}", notes = "${api.users.authenticateUser.notes}")
    @PostMapping("/signin")
    public TokenDTO authenticateUser(@Valid @RequestBody CredentialsDTO credentials) {
        String token = userService.signInUser(credentials.getUsername(), credentials.getPassword());
        return new TokenDTO(token);
    }

    @ApiOperation(value = "${api.users.registerUser.value}", notes = "${api.users.registerUser.notes}")
    @PostMapping("/signup")
    public ResourceDTO registerUser(@Valid @RequestBody RegistrationDTO registrationFormDTO) {
        User userToCreate = userMapper.mapToUser(registrationFormDTO);
        User createdUser = userService.createUser(userToCreate);
        return userMapper.mapToResourceDTO(createdUser);
    }

    @ApiOperation(value = "${api.users.checkIfUsernameIsAvailable.value}", notes = "${api.users.checkIfUsernameIsAvailable.notes}")
    @GetMapping(CHECK_USERNAME_RESOURCE)
    public ResponseDTO<Boolean> checkIfUsernameIsAvailable(@PathVariable(USERNAME_PATH_PARAM) String username) {
        boolean isAvailable = !userService.userExistsByUsername(username);
        return new ResponseDTO<>(isAvailable, isAvailable ? "Username is available" : "Username is already taken");
    }

    @ApiOperation(value = "${api.users.checkIfEmailIsAvailable.value}", notes = "${api.users.checkIfEmailIsAvailable.notes}")
    @GetMapping(CHECK_EMAIL_RESOURCE)
    public ResponseDTO<Boolean> checkIfEmailIsAvailable(@PathVariable(EMAIL_PATH_PARAM) String email) {
        boolean isAvailable = !userService.userExistsByEmail(email);
        return new ResponseDTO<>(isAvailable, isAvailable ? "Email is available" : "Email is already taken");
    }

    @ApiOperation(value = "${api.users.getCurrentUser.value}", notes = "${api.users.getCurrentUser.notes}")
    @GetMapping("/current")
    @HasAnyRole
    public UserDTO getCurrentUser(@ApiIgnore  @LoggedUser UserInfo currentUserInfo) {
        User user = userService.getExistingUser(currentUserInfo.getId());
        return userMapper.mapToUserDTO(user);
    }

    @ApiOperation(value = "${api.users.getUser.value}", notes = "${api.users.getUser.notes}")
    @GetMapping(ID_PATH)
    @HasAnyRole
    public UserDTO getUser(@PathVariable(ID) Long id) {
        User user = userService.getExistingUser(id);
        return userMapper.mapToUserDTO(user);
    }

}
