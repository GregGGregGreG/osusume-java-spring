package com.tokyo.beach.session;

import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restutils.RestControllerExceptionHandler;
import com.tokyo.beach.restaurants.session.SessionController;
import com.tokyo.beach.restaurants.session.SessionDataMapper;
import com.tokyo.beach.restaurants.session.TokenGenerator;
import com.tokyo.beach.restaurants.session.UserSession;
import com.tokyo.beach.restaurants.session.LogonCredentials;
import com.tokyo.beach.restaurants.user.UserDataMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static com.tokyo.beach.restutils.ControllerTestingUtils.createControllerAdvice;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SessionControllerTest {
    private MockMvc mvc;
    private SessionDataMapper sessionDataMapper;
    private UserDataMapper userDataMapper;
    private TokenGenerator tokenGenerator;

    private LogonCredentials credentials;
    private Optional<User> maybeUser;

    @Before
    public void setUp() throws Exception {
        sessionDataMapper = mock(SessionDataMapper.class);
        userDataMapper = mock(UserDataMapper.class);
        tokenGenerator = mock(TokenGenerator.class);
        mvc = MockMvcBuilders.standaloneSetup(new SessionController(
                sessionDataMapper,
                userDataMapper,
                tokenGenerator)
        )
                .setControllerAdvice(createControllerAdvice(new RestControllerExceptionHandler()))
                .build();

        credentials = new LogonCredentials("jmiller@gmail.com", "mypassword");
        maybeUser = Optional.of(new User(999, "jmiller@gmail.com", "Joe Miller"));
        when(userDataMapper.get(credentials))
                .thenReturn(maybeUser);
    }

    @Test
    public void test_postToSession_returnsAcceptedHttpStatus() throws Exception {
        UserSession userSession = new UserSession(tokenGenerator, "jmiller@gmail.com", "Jim Miller", maybeUser.get().getId());
        when(sessionDataMapper.create(tokenGenerator, maybeUser.get()))
                .thenReturn(userSession);


        mvc.perform(post("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"email\":\"jmiller@gmail.com\",\"password\":\"mypassword\"}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isAccepted());
    }

    @Test
    public void test_postToSession_invokesSessionRepoCreate() throws Exception {
        mvc.perform(post("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"email\":\"jmiller@gmail.com\",\"password\":\"mypassword\"}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        );

        verify(sessionDataMapper, times(1)).create(tokenGenerator, maybeUser.get());
    }

    @Test
    public void test_postToSession_returnsValidSession() throws Exception {
        when(tokenGenerator.nextToken())
                .thenReturn("abcde12345");
        UserSession userSession = new UserSession(tokenGenerator, "jmiller@gmail.com", "Jim Miller", 1L);
        when(sessionDataMapper.create(tokenGenerator, maybeUser.get()))
                .thenReturn(userSession);


        mvc.perform(post("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"email\":\"jmiller@gmail.com\",\"password\":\"mypassword\",\"id\":1}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.email", is("jmiller@gmail.com")))
                .andExpect(jsonPath("$.name", is("Jim Miller")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.token", is("abcde12345")));
    }

    @Test
    public void test_postToSessionWithInvalidUserCredentials_throwsException() throws Exception {
        when(userDataMapper.get(anyObject()))
                .thenReturn(Optional.empty());


        mvc.perform(post("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"email\":\"invalid@email\",\"password\":\"invalid password\"}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"Invalid email or password.\"}"));
    }

    @Test
    public void test_deleteSession_returnsAcceptedHttpStatus() throws Exception {


        mvc.perform(delete("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"token\":\"ABCDE12345\"}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isAccepted());
    }

    @Test
    public void test_deleteSession_invokesSessionRepoLogout() throws Exception {


        mvc.perform(delete("/session")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content("{\"token\":\"ABCDE12345\"}")
                .accept(APPLICATION_JSON_UTF8_VALUE)
        );


        verify(sessionDataMapper, times(1)).delete("ABCDE12345");
    }

    @Test
    public void test_unauthenticated_returnsBadRequestHttpStatus() throws Exception {
        mvc.perform(get("/unauthenticated"))
                .andExpect(status().isBadRequest());
    }
}
