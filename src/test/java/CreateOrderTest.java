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

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

public class  CreateOrderTest extends AbstractTest {
    UserSteps userSteps = new UserSteps();
    OrderSteps orderSteps = new OrderSteps();
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
    public void createOrderWithAuthTest() {
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

        //получаем список ингредиентов
        List<String> ingredients = orderSteps.getIngredients();

        //создаем заказ с выбранными ингредиентами, когда клиент авторизован
        orderSteps
                .createOrder(ingredients.subList(0, 2), accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("order.ingredients", Matchers.hasSize(2));
    }

    @Test
    public void createOrderWithRandomIngredientsTest() {
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

        //получаем список ингредиентов
        List<String> ingredients = orderSteps.getIngredients();

        //получаем случайный список ингредиентов
        List<String> randomIngredients = orderSteps.getRandomIngredients(ingredients);

        //создаем заказ с случайными ингредиентами
        orderSteps
                .createOrder(randomIngredients, accessToken)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true))
                .body("order.ingredients", Matchers.hasSize(randomIngredients.size()));
    }

    @Test
    //при создании заказа, неавторизованным пользователем, нас перекидывает на экран авторизации, в ответе 200ОК
    // и success: true, долго думала, но вроде проверка валидная
    public void createOrderWithoutAuthTest() {
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

        List<String> ingredients = orderSteps.getIngredients();

        List<String> randomIngredients = orderSteps.getRandomIngredients(ingredients);

        //создаем заказ без авторизации
        orderSteps
                .createOrderWithoutAuth(randomIngredients)
                .then()
                .statusCode(200)
                .body("success", Matchers.is(true));
    }

    @Test
    public void createOrderWithoutIngredientsTest() {
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

        //попытка создать заказ без ингредиентов
        orderSteps
                .createOrderWithoutIngredients(accessToken)
                .then()
                .statusCode(400)
                .body("success", Matchers.is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidIngredientTest() {
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

        //попытка создать заказ с невалидным хешем ингредиента
        List<String> invalidIngredients = List.of("61c0c5a71d1f82001bdaaa78", "61c0c5a71d1f82001ijaaa79");

        orderSteps
                .createOrder(invalidIngredients, accessToken)
                .then()
                .statusCode(500);
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