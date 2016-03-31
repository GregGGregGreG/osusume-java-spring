package com.tokyo.beach.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tokyo.beach.logon.LogonCredentials;
import com.tokyo.beach.session.TokenGenerator;
import com.tokyo.beach.token.TokenWrapper;

@RestController
public class UserController {

    private UserRepository userRepository;
    private TokenGenerator tokenGenerator;

    @Autowired
    public UserController(
            UserRepository userRepository,
            TokenGenerator tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @RequestMapping(value = "/auth/session", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public TokenWrapper login(@RequestBody LogonCredentials credentials) {

        System.out.println("credentials = " + credentials);

//        userRepository.
        // User Repo - Login

        return new TokenWrapper(tokenGenerator);
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public TokenWrapper registerUser(@RequestBody LogonCredentials credentials) {

        System.out.println("credentials = " + credentials);

        // User Repo - Create

        return new TokenWrapper(tokenGenerator);
    }

}