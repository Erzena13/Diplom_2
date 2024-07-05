package steps;

import dto.UserCreateRequest;
import dto.UserLoginRequest;
import dto.UserUpdateRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static endpoints.EndPoints.*;
import static io.restassured.RestAssured.given;

public class UserSteps {
    private String refreshToken;
    @Step("Создание пользователя с email '{email}', паролем '{password}' и именем '{name}'")
    public Response register(String email, String password, String name) {
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setEmail(email);
        userCreateRequest.setPassword(password);
        userCreateRequest.setName(name);

        return given()
                .body(userCreateRequest)
                .when()
                .post(REGISTRATION);
    }

    @Step("Авторизация пользователя с email '{email}', паролем '{password}'")
    public Response login(String email, String password) {
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setEmail(email);
        userLoginRequest.setPassword(password);

        Response response = given()
                .body(userLoginRequest)
                .when()
                .post(LOGIN);
        if (response.getStatusCode() == 200) {
            String accessToken = response.jsonPath().getString("accessToken");
            this.refreshToken = response.jsonPath().getString("refreshToken");
        }

        return response;
    }

    @Step("Получение данных пользователя")
    public Response getUserDetails(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .get(USERS);
    }

    @Step("Удаление пользователя с использованием accessToken")
    public Response deleteUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .delete(DELETE);
    }

    @Step("Обновление данных о пользователе")
    public Response updateUserDetails(String name, String email, String password, String accessToken) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setName(name);
        userUpdateRequest.setEmail(email);
        userUpdateRequest.setPassword(password);

        return given()
                .header("Authorization", accessToken)
                .body(userUpdateRequest)
                .when()
                .patch(UPDATE);
    }

    @Step("Обновление данных о пользователе, без токена")
    public Response updateUserDetailsWithoutToken(String name, String email, String password) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setName(name);
        userUpdateRequest.setEmail(email);
        userUpdateRequest.setPassword(password);

        return given()
                .body(userUpdateRequest)
                .when()
                .patch(UPDATE);
    }
}