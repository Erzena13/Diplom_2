import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import steps.OrderSteps;
import steps.UserSteps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class GetOrdersForUserTest extends AbstractTest {
    OrderSteps orderSteps = new OrderSteps();
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
    public void successGetUserOrdersIfLogin() {
        //регистрация и логин пользователя
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

        accessToken = userSteps
                .login(email, password)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");

        //получаем заказы пользователя
        orderSteps
                .getUserOrders(accessToken)
                .then()
                .statusCode(200)
                .body("orders", hasSize(Matchers.lessThanOrEqualTo(50))) //проверка, что выводится не более 50 заказов
                .body("success", equalTo(true));
    }

    @Test
    public void errorGetUserOrdersWithoutLogin() {
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

        //попытка получить заказы без авторизации
        orderSteps.getUserOrders(null)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
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