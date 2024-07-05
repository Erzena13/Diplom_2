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

public class CreateUserTest extends AbstractTest {
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
    public void createSuccessUserTest() {
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
    }

    @Test
    public void createErrorDuplicateUser() {
        email = "login_duplicate@example.com";
        password = "password_duplicate";
        name = RandomStringUtils.randomAlphabetic(5);

        //создаем пользователя
        userSteps
                .register(email, password, name)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("accessToken", Matchers.notNullValue())
                .body("refreshToken", Matchers.notNullValue());

        //логинимся и сохраняем accessToken
        accessToken = userSteps.login(email, password)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //повторно создаем пользователя с теми же данными
        userSteps
                .register(email, password, name)
                .then()
                .statusCode(403)
                .body("success", Matchers.is(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    public void createErrorWithoutEmail() {
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(5);

        userSteps
                .register(null, password, name)
                .then()
                .statusCode(403)
                .body("success", Matchers.is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void createErrorWithoutPassword() {
        email = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        name = RandomStringUtils.randomAlphabetic(5);

        userSteps
                .register(email, null, name)
                .then()
                .statusCode(403)
                .body("success", Matchers.is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    public void createErrorWithoutName() {
        email = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        password = RandomStringUtils.randomAlphabetic(8);

        userSteps
                .register(email, password, null)
                .then()
                .statusCode(403)
                .body("success", Matchers.is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (email != null && password != null && accessToken != null) {
            // Удаление пользователя с использованием accessToken
            userSteps.deleteUser(accessToken)
                    .then()
                    .statusCode(202)
                    .body("message", equalTo("User successfully removed"));
        }
    }
}