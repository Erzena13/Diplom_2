import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.UserSteps;

import static org.hamcrest.CoreMatchers.equalTo;

public class LoginTest extends AbstractTest {
    UserSteps userSteps = new UserSteps();
    private String email;
    private String password;
    private String name;
    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void loginSuccessTest() {
        email = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(5);

        userSteps
                .register(email, password, name)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("accessToken", Matchers.notNullValue())
                .body("refreshToken", Matchers.notNullValue());

        userSteps
                .login(email, password)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .extract().jsonPath().getString("accessToken");
    }

    @Test
    public void loginErrorWithWrongEmailTest() {
        email = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(5);

        userSteps
                .register(email, password, name)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("accessToken", Matchers.notNullValue())
                .body("refreshToken", Matchers.notNullValue());

        userSteps
                .login("rtegert@example.com", password)
                .then()
                .statusCode(401)
                .body("success", Matchers.is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void loginErrorWithWrongPasswordTest() {
        email = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(5);

        userSteps
                .register(email, password, name)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("accessToken", Matchers.notNullValue())
                .body("refreshToken", Matchers.notNullValue());

        userSteps
                .login(email, "wrong_password")
                .then()
                .statusCode(401)
                .body("success", Matchers.is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        if (email != null && password != null && accessToken != null) {
            //удаление пользователя с использованием accessToken
            userSteps.deleteUser(accessToken)
                    .then()
                    .statusCode(202)
                    .body("message", equalTo("User successfully removed"));
        }
    }
}