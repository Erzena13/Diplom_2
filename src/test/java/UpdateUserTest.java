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

public class UpdateUserTest extends AbstractTest {
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
    public void successUpdateNameIfLogin() {
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

        //логинимся и сохраняем accessToken
        accessToken = userSteps
                .login(email, password)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //получаем данные пользователя
        userSteps.getUserDetails(accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true));

        //готовим новое имя для обновления
        String updatedName = RandomStringUtils.randomAlphabetic(8);

        //обновляем данные
        userSteps.updateUserDetails(updatedName, email, password, accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("user.name", equalTo(updatedName));

        // Проверяем, что обновленные данные действительно сохранились
        userSteps.getUserDetails(accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("user.name", equalTo(updatedName));
    }

    @Test
    public void successUpdateEmailIfLogin() {
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

        //логинимся и сохраняем accessToken
        accessToken = userSteps
                .login(email, password)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //получаем данные пользователя
        userSteps.getUserDetails(accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true));

        //готовим новый email для обновления
        String updatedEmail = RandomStringUtils.randomAlphabetic(5) + "@example.com";

        //обновляем данные
        userSteps.updateUserDetails(name, updatedEmail, password, accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true));

        //проверяем, что обновленные данные действительно сохранились
        userSteps.getUserDetails(accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true));
    }

    @Test
    public void errorUpdateIfNotLogin() {
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

        String updatedEmail = RandomStringUtils.randomAlphabetic(5) + "@example.com";
        String updatedName = RandomStringUtils.randomAlphabetic(8);
        userSteps
                .updateUserDetailsWithoutToken(updatedName, updatedEmail, null)
                .then()
                .statusCode(401)
                .body("success", Matchers.is(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    public void errorUpdateWithExistingEmailIfLogin() {
        //создаем первого пользователя
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

        //логинимся первым пользователем и сохраняем accessToken
        accessToken = userSteps.login(email, password)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //создаем второго пользователя
        String email2 = RandomStringUtils.randomAlphabetic(5) + "@example.com";;
        String password2 = RandomStringUtils.randomAlphabetic(8);
        String name2 = RandomStringUtils.randomAlphabetic(5);
        userSteps
                .register(email2, password2, name2)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("accessToken", Matchers.notNullValue())
                .body("refreshToken", Matchers.notNullValue());

        //логинимся вторым пользователем и сохраняем accessToken
        String accessToken2 = userSteps.login(email2, password2)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //пытаемся обновить email второго пользователя на email первого
        userSteps.updateUserDetails(name2, email, password2, accessToken2)
                .then()
                .statusCode(403)
                .body("success", Matchers.is(false))
                .body("message", equalTo("User with such email already exists"));
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