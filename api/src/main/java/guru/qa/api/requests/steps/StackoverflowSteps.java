package guru.qa.api.requests.steps;

import guru.qa.api.models.dto.response.StackoverflowResponseDto;
import guru.qa.api.requests.defaultRequests.StackoverflowRequest;
import guru.qa.api.util.JSONParser;
import guru.qa.api.util.JSOUPParser;
import guru.qa.api.util.Props;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static guru.qa.api.specs.StackoverflowSpec.requestSpec;
import static guru.qa.api.specs.StackoverflowSpec.responseSpec;
import static io.restassured.RestAssured.given;

public class StackoverflowSteps {

    private Props props = new Props();
    private Map<String, String> cookies;
    private String fkey;
    
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    @Step("Log in")
    public Response logIn() {
        return given()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("email", props.getEmail())
                .formParam("password", props.getPassword())
                .when()
                .post("https://stackoverflow.com/users/login")
                .then()
                .spec(responseSpec)
                .extract().response();
    }

    @Step("Get Stackoverflow post as HTML")
    private Response getStackoverflowPostAsHtml(String postUrl) {
        return given()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookies(cookies)
                .post(postUrl)
                .then()
                .spec(responseSpec)
                .extract().response();
    }

    @Step("Get key from javascript element on page")
    public void getKeyFromJavascriptElementOnPage(String postUrl) {
        Response stackoverflowPostAsHtmlResponse = getStackoverflowPostAsHtml(postUrl);
        String scriptContent = JSOUPParser.parsingJavascriptElement(stackoverflowPostAsHtmlResponse.prettyPrint());

        fkey = JSONParser.getKeyFromScriptContentFormatJson(scriptContent);
    }

    @Step("Bookmark post")
    public StackoverflowResponseDto saveBookmarkPost(String postUrl) {
        StackoverflowResponseDto dto = given()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookies(cookies)
                .body("fkey=" + fkey)
                .post(postUrl + "/save")
                .then()
                .spec(responseSpec)
                .extract().as(StackoverflowResponseDto.class);

        StackoverflowRequest.confirmPreviousAction(cookies);

        return dto;
    }

    @Step("Remove post from bookmark")
    public void removePostFromBookmark(String postUrl) {
        given()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookies(cookies)
                .body("fkey=" + fkey)
                .post(postUrl + "/save?isUndo=true")
                .then()
                .spec(responseSpec);

        StackoverflowRequest.confirmPreviousAction(cookies);
    }
}
