package steps;

import dto.OrderRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static endpoints.EndPoints.INGREDIENTS;
import static endpoints.EndPoints.USER_ORDERS;
import static io.restassured.RestAssured.given;

public class OrderSteps {
    private final OrderRequest orderRequest = new OrderRequest();

    @Step("Получение списка ингредиентов '{list_ingredients}'")
    public List<String> getIngredients() {
        Response response = given()
                .get(INGREDIENTS)
                .then()
                .statusCode(200)
                .extract().response();

        return response.jsonPath().getList("data._id");
    }

    @Step("Получение случайного списка ингредиентов '{list_ingredients}'")
    public List<String> getRandomIngredients(List<String> ingredients) {
        Random random = new Random();
        int count = random.nextInt(ingredients.size()) + 1; //случайное количество от 1 до конечного размера списка
        return ingredients.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Step("Создание заказа с пустым списком ингредиентов")
    public Response createOrderWithoutIngredients(String accessToken) {
        Map<String, List<String>> order = orderRequest.createOrderWithoutIngredientsRequest();
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(order)
                .when()
                .post(USER_ORDERS);
    }

    @Step("Создание заказа '{order}'")
    public Response createOrder(List<String> ingredientIds, String accessToken) {
        Map<String, List<String>> order = orderRequest.createOrderRequest(ingredientIds);
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(order)
                .when()
                .post(USER_ORDERS);
    }

    @Step("Создание заказа без авторизации '{order}'")
    public Response createOrderWithoutAuth(List<String> ingredientIds) {
        Map<String, List<String>> order = orderRequest.createOrderWithoutAuthRequest(ingredientIds);
        return given()
                .body(order)
                .when()
                .post(USER_ORDERS);
    }

    @Step("Получение заказов пользователя '{orders}'")
    public Response getUserOrders(String accessToken) {
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .when()
                .get(USER_ORDERS);
    }
}