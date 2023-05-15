import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ChangeActiveStatusDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.LoginDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.RefreshJwtDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ResetPasswordDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.response.AccountDto;
import pl.lodz.p.it.ssbd2023.ssbd05.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegrationTests {

    protected static RequestSpecification ownerSpec;
    protected static RequestSpecification managerSpec;
    protected static RequestSpecification adminSpec;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/eBok";

        generateOwnerSpec();
        generateManagerSpec();
        generateAdminSpec();
    }

    static void generateOwnerSpec() {
        LoginDto loginDto = new LoginDto("pzielinski", "P@ssw0rd");

        String ownerJWT = RestAssured.given().body(loginDto)
            .contentType(ContentType.JSON)
            .when()
            .post("/login")
            .jsonPath()
            .get("jwt");

        ownerSpec = new RequestSpecBuilder()
            .addHeader("Authorization", "Bearer " + ownerJWT)
            .build();
    }

    static void generateManagerSpec() {
        LoginDto loginDto = new LoginDto("pduda", "P@ssw0rd");

        String managerJWT = RestAssured.given().body(loginDto)
            .contentType(ContentType.JSON)
            .when()
            .post("/login")
            .jsonPath()
            .get("jwt");

        managerSpec = new RequestSpecBuilder()
            .addHeader("Authorization", "Bearer " + managerJWT)
            .build();
    }

    static void generateAdminSpec() {
        LoginDto loginDto = new LoginDto("bjaworski", "P@ssw0rd");

        String adminJWT = RestAssured.given().body(loginDto)
            .contentType(ContentType.JSON)
            .when()
            .post("/login")
            .jsonPath()
            .get("jwt");

        adminSpec = new RequestSpecBuilder()
            .addHeader("Authorization", "Bearer " + adminJWT)
            .build();
    }


    //Zaloguj się
    @Nested
    class MOK1 {
        @Test
        void shouldLoginWhenCredentialsAreValidAndAccountHasActiveAndVerifiedAccessLevels() {
            LoginDto loginDto = new LoginDto("pduda", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", refreshToken);

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class));
        }

        @Test
        void shouldReturnSC401WhenAccountDoesntExist() {
            LoginDto loginDto = new LoginDto("randomAccount", "randomPassword");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenPasswordIsInvalid() {
            LoginDto loginDto = new LoginDto("dchmielewski", "wrongPassword");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenAccountIsLocked() {
            LoginDto loginDto = new LoginDto("aczarnecki", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenAccountIsUnverified() {
            LoginDto loginDto = new LoginDto("ntracz", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenAccountHasNoActiveAccessLevels() {
            LoginDto loginDto = new LoginDto("bkowalewski", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenLoginIsEmpty() {
            LoginDto loginDto = new LoginDto("", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnSC401WhenPasswordIsEmpty() {
            LoginDto loginDto = new LoginDto("pduda", "");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldLockAccountAfterThreeUnsuccessfulLoginAttemptsInARow() {
            LoginDto loginDto = new LoginDto("jstanczyk", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class));

            LoginDto wrongLoginDto = new LoginDto("jstanczyk", "wrongPassword");

            for (int i = 0; i < 3; i++) {
                given()
                    .contentType(ContentType.JSON)
                    .body(wrongLoginDto)
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                    .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
            }

            given().spec(adminSpec)
                .when()
                .get("/accounts/-14")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(false));

            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenRefreshTokenWasUsed() {
            LoginDto loginDto = new LoginDto("pduda", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", refreshToken);

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.allOf(Matchers.any(String.class), Matchers.not(refreshToken)),
                    "language", Matchers.any(String.class));

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC401WhenGivenLoginDoesntMatchRefreshTokenLogin() {
            LoginDto loginDto = new LoginDto("pduda", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("bjaworski", refreshToken);

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldReturnSC400WhenRefreshTokenIsNotUUID() {
            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", "definitelyNotUUID");

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnSC400WhenRefreshTokenIsEmpty() {
            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", "");

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnSC400WhenLoginIsEmpty() {
            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("", UUID.randomUUID().toString());

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnSC401WhenRefreshTokenDoesntExist() {
            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", UUID.randomUUID().toString());

            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .assertThat()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldRefreshOnlyOneSessionForConcurrentRequests() throws BrokenBarrierException, InterruptedException {
            LoginDto loginDto = new LoginDto("ikaminski", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            int threadNumber = 5;
            CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNumber + 1);
            List<Thread> threads = new ArrayList<>(threadNumber);
            AtomicInteger numberFinished = new AtomicInteger();
            AtomicInteger numberOfSuccessfulAttempts = new AtomicInteger();

            for (int i = 0; i < threadNumber; i++) {
                threads.add(new Thread(() -> {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    RefreshJwtDto refreshJwtDto = new RefreshJwtDto("ikaminski", refreshToken);

                    int statusCode = given().body(refreshJwtDto)
                        .contentType(ContentType.JSON)
                        .when()
                        .post("/refresh").getStatusCode();

                    if (statusCode == 200) {
                        numberOfSuccessfulAttempts.getAndIncrement();
                    }
                    numberFinished.getAndIncrement();
                }));
            }
            threads.forEach(Thread::start);
            cyclicBarrier.await();
            while (numberFinished.get() != threadNumber) {
            }

            assertEquals(1, numberOfSuccessfulAttempts.get());
        }
    }

    @Nested
    class MOK3 {
        @Test
        void shouldCreateTokenAndSendPasswordResetMessageToUser() {
            given()
                .queryParam("email", "ikaminski@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(adminSpec)
                .queryParam("email", "ikaminski@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(ownerSpec)
                .queryParam("email", "ikaminski@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(managerSpec)
                .queryParam("email", "ikaminski@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        }

        @Test
        void shouldReturnSC404WhenAccountDoesntExist() {
            given()
                .queryParam("email", "non_existent@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.ACCOUNT_NOT_FOUND));
        }

        @Test
        void shouldReturnSC403WhenUnverifiedAccount() {

            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("accounts/-9")
                .then()
                .assertThat()
                .body("verified", Matchers.equalTo(false));

            given()
                .queryParam("email", "rnowak@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.UNVERIFIED_ACCOUNT));
        }

        @Test
        void shouldReturnSC403WhenInactiveAccount() {

            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("accounts/-10")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(false));

            given()
                .queryParam("email", "nkowalska@gmail.local")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.INACTIVE_ACCOUNT));
        }

        @Test
        void shouldReturnSC400WhenWrongRequestParameters() {

            //Empty email
            given()
                .queryParam("email", "")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Provided value is not an email
            given()
                .queryParam("email", "email")
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //No parameter provided
            given()
                .when()
                .post("accounts/reset-password-message")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);
        }

        @Test
        void shouldReturnSC400WhenTokenNotFoundDuringPasswordResetConfirm() {
            ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("accounts/reset-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.TOKEN_NOT_FOUND));
        }

        @Test
        void shouldReturnSC400WhenWrongParametersDuringPasswordResetConfirm() {
            //Invalid password, should have at least one number and one special character
            ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
            resetPasswordDto.setPassword("newPassword");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("accounts/reset-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Invalid password, should have at least one number and one special character
            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken("not_uuid");
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("accounts/reset-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Empty password
            resetPasswordDto.setPassword("");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("accounts/reset-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Empty token
            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken("");
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .when()
                .post("accounts/reset-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);
        }
    }

    @Nested
    class MOK8 {

        @Test
        void shouldLogoutWithValidRefreshTokenAndInvalidateIt() {
            LoginDto loginDto = new LoginDto("pzielinski", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            given().spec(ownerSpec)
                .when()
                .delete("/logout?token=" + refreshToken)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            RefreshJwtDto refreshJwtDto = new RefreshJwtDto("pduda", refreshToken);
            given().body(refreshJwtDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/refresh")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("message", Matchers.equalTo(I18n.AUTHENTICATION_EXCEPTION));
        }

        @Test
        void shouldLogoutWithNonExistentRefreshToken() {
            given().spec(ownerSpec)
                .when()
                .delete("/logout?token=" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        }

        @Test
        void shouldReturnSC400WhenRefreshTokenIsNotUUID() {
            given().spec(ownerSpec)
                .when()
                .delete("/logout?token=definitelyNotUUID")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenJwtIsNotAttached() {
            given()
                .when()
                .delete("/logout?token=" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC204ForAllConcurrentRequests() throws BrokenBarrierException, InterruptedException {
            LoginDto loginDto = new LoginDto("pzielinski", "P@ssw0rd");

            String refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("jwt", Matchers.any(String.class),
                    "refreshToken", Matchers.any(String.class),
                    "language", Matchers.any(String.class))
                .extract()
                .jsonPath()
                .getString("refreshToken");

            int threadNumber = 5;
            CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNumber + 1);
            List<Thread> threads = new ArrayList<>(threadNumber);
            AtomicInteger numberFinished = new AtomicInteger();
            AtomicInteger numberOfSuccessfulAttempts = new AtomicInteger();

            for (int i = 0; i < threadNumber; i++) {
                threads.add(new Thread(() -> {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }

                    int statusCode = given().spec(ownerSpec)
                        .when()
                        .delete("/logout?token=" + refreshToken).getStatusCode();

                    if (statusCode == 204) {
                        numberOfSuccessfulAttempts.getAndIncrement();
                    }
                    numberFinished.getAndIncrement();
                }));
            }
            threads.forEach(Thread::start);
            cyclicBarrier.await();
            while (numberFinished.get() != threadNumber) {
            }

            assertEquals(numberFinished.get(), numberOfSuccessfulAttempts.get());
        }
    }

    @Nested
    class MOK9 {
        @Test
        void shouldChangeLanguageWhenUserAuthenticatedAdmin() {
            given()
                .spec(adminSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));

            given()
                .spec(adminSpec)
                .put("accounts/me/change-language/EN")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(adminSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("EN"));

            given()
                .spec(adminSpec)
                .put("accounts/me/change-language/PL")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(adminSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));
        }

        @Test
        void shouldChangeLanguageWhenUserAuthenticatedOwner() {
            given()
                .spec(ownerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));

            given()
                .spec(ownerSpec)
                .put("accounts/me/change-language/En")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(ownerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("EN"));

            given()
                .spec(ownerSpec)
                .put("accounts/me/change-language/Pl")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(ownerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));
        }

        @Test
        void shouldChangeLanguageWhenUserAuthenticatedManager() {
            given()
                .spec(managerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));

            given()
                .spec(managerSpec)
                .put("accounts/me/change-language/en")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(managerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("EN"));

            given()
                .spec(managerSpec)
                .put("accounts/me/change-language/pl")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            given()
                .spec(managerSpec)
                .get("accounts/me")
                .then()
                .assertThat()
                .body("language", Matchers.equalTo("PL"));
        }

        @Test
        void shouldReturnSC403WhenUserNotAuthenticated() {
            given()
                .put("accounts/me/change-language/en")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC400WhenLanguageDoesntExist() {
            given()
                .spec(adminSpec)
                .put("accounts/me/change-language/ger")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.LANGUAGE_NOT_FOUND));

            given()
                .spec(adminSpec)
                .put("accounts/me/change-language/1")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.LANGUAGE_NOT_FOUND));
        }
    }

    @Nested
    class MOK15 {

        @Test
        void shouldGetAllAccountsAsAdmin() {
            List<AccountDto> accounts =
                given().spec(adminSpec)
                    .when()
                    .get("/accounts")
                    .getBody().as(ArrayList.class);

            assertNotNull(accounts);
            assertTrue(accounts.size() > 0);
        }

        @Test
        void shouldGetAllAccountsAsManager() {
            List<AccountDto> accounts =
                given().spec(managerSpec)
                    .when()
                    .get("/accounts")
                    .getBody().as(ArrayList.class);

            assertNotNull(accounts);
            assertTrue(accounts.size() > 0);
        }

        @Test
        void shouldGetAllAdminsAsAdmin() {
            List<AccountDto> admins =
                given().spec(adminSpec)
                    .when()
                    .get("/accounts/admins")
                    .getBody().as(ArrayList.class);

            assertNotNull(admins);
        }

        @Test
        void shouldGetAllManagersAsAdmin() {
            List<AccountDto> managers =
                given().spec(adminSpec)
                    .when()
                    .get("/accounts/managers")
                    .getBody().as(ArrayList.class);

            assertNotNull(managers);
        }

        @Test
        void shouldGetAllOwnersAsAdmin() {
            List<AccountDto> owners =
                given().spec(adminSpec)
                    .when()
                    .get("/accounts/owners")
                    .getBody().as(ArrayList.class);

            assertNotNull(owners);
        }

        @Test
        void shouldGetAllOwnersAsManager() {
            List<AccountDto> owners =
                given().spec(managerSpec)
                    .when()
                    .get("/accounts/owners")
                    .getBody().as(ArrayList.class);

            assertNotNull(owners);
        }

        @Test
        void shouldGetUnapprovedOwnersAsManager() {
            List<AccountDto> owners =
                given().spec(managerSpec)
                    .when()
                    .get("/accounts/owners/unapproved")
                    .getBody().as(ArrayList.class);

            assertNotNull(owners);
        }

        @Test
        void shouldGetUnapprovedManagersAsAdmin() {
            List<AccountDto> managers =
                given().spec(adminSpec)
                    .when()
                    .get("/accounts/managers/unapproved")
                    .getBody().as(ArrayList.class);

            assertNotNull(managers);
        }

        @Test
        void shouldReturnSC403WhenGettingAllAccountsAsOwner() {
            given().spec(ownerSpec)
                .when()
                .get("/accounts")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingManagersAsManager() {
            given().spec(managerSpec)
                .when()
                .get("/accounts/managers")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingAdminsAsManager() {
            given().spec(managerSpec)
                .when()
                .get("/accounts/admins")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingManagersAsOwner() {
            given().spec(ownerSpec)
                .when()
                .get("/accounts/owners")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingUnapprovedOwnersAsAdmin() {
            given().spec(adminSpec)
                .when()
                .get("/accounts/owners/unapproved")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingUnapprovedOwnersAsOwner() {
            given().spec(ownerSpec)
                .when()
                .get("/accounts/owners/unapproved")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingUnapprovedManagersAsOwner() {
            given().spec(ownerSpec)
                .when()
                .get("/accounts/managers/unapproved")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingAccountsAsGuest() {
            given()
                .when()
                .get("/accounts")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingOwnersAsGuest() {
            given()
                .when()
                .get("/accounts/owners")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingManagersAsGuest() {
            given()
                .when()
                .get("/accounts/managers")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingAdminsAsGuest() {
            given()
                .when()
                .get("/accounts/admins")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingUnapprovedOwnersAsGuest() {
            given()
                .when()
                .get("/accounts/owners/unapproved")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingUnapprovedManagersAsGuest() {
            given()
                .when()
                .get("/accounts/managers/unapproved")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }
    }

    @Nested
    class MOK16 {
        @Test
        void shouldForcefullyChangeOtherAccountsPasswordByAdmin() {

            //User can log in with P@ssw0rd

            JSONObject credentials = new JSONObject();
            credentials.put("login", "jkubiak");
            credentials.put("password", "P@ssw0rd");

            given()
                .contentType(ContentType.JSON)
                .body(credentials.toString())
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

            // User is active and verified
            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("/accounts/-26")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(true))
                .body("verified", Matchers.equalTo(true));

            //Change user password by admin
            given()
                .spec(adminSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/jkubiak")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            //User's account has become inactive
            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("/accounts/-26")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(false))
                .body("verified", Matchers.equalTo(true));

            //Activate user in order to change if password has changed
            ChangeActiveStatusDto dto = new ChangeActiveStatusDto();
            dto.setId(-26L);
            dto.setActive(true);

            given().contentType(ContentType.JSON)
                .spec(adminSpec)
                .body(dto)
                .when()
                .put("/accounts/admin/change-active-status")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            //Check if user is active
            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("/accounts/-26")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(true))
                .body("verified", Matchers.equalTo(true));

            //User cannot log in with the same credentials, password has changed
            given()
                .contentType(ContentType.JSON)
                .body(credentials.toString())
                .when()
                .post("/login")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenUserCurrentlyDoesntHaveActiveAdminAccessLevel() {

            //Logged in as manager
            given()
                .spec(managerSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/jkubiak")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

            //Logged in as owner
            given()
                .spec(ownerSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/jkubiak")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

            //Unauthorized user
            given()
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/jkubiak")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenUserTriesToForcefullyChangeOwnAccountPassword() {
            given()
                .spec(adminSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/bjaworski")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.ILLEGAL_SELF_ACTION));
        }

        @Test
        void shouldReturnSC403WhenChangingInactiveAccountsPassword() {

            //Check if account is inactive
            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("/accounts/-10")
                .then()
                .assertThat()
                .body("active", Matchers.equalTo(false));

            given()
                .spec(adminSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/nkowalska")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.INACTIVE_ACCOUNT));
        }

        @Test
        void shouldReturnSC403WhenChangingUnverifiedAccountsPassword() {

            //Check if account is unverified
            given()
                .contentType(ContentType.JSON)
                .spec(adminSpec)
                .when()
                .get("/accounts/-9")
                .then()
                .assertThat()
                .body("verified", Matchers.equalTo(false));

            given()
                .spec(adminSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/rnowak")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.UNVERIFIED_ACCOUNT));
        }

        @Test
        void shouldReturnSC404WhenAccountDoesntExist() {
            given()
                .spec(adminSpec)
                .contentType(ContentType.JSON)
                .when()
                .put("/accounts/force-password-change/non_existent")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .and().body("message", Matchers.equalTo(I18n.ACCOUNT_NOT_FOUND));
        }

        @Test
        void shouldReturnSC400WhenForcePasswordChangeAndTokenNotFound() {
            ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .put("/accounts/override-forced-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.TOKEN_NOT_FOUND));
        }

        @Test
        void shouldReturnSC400WhenForcePasswordChangeAndWrongParameters() {
            ResetPasswordDto resetPasswordDto = new ResetPasswordDto();

            //Empty token

            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken("");
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .put("/accounts/override-forced-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Empty password
            resetPasswordDto.setPassword("");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .put("/accounts/override-forced-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Password should contain at least one number and one special character
            resetPasswordDto.setPassword("newPassword");
            resetPasswordDto.setToken(UUID.randomUUID().toString());
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .put("/accounts/override-forced-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);

            //Invalid uuid
            resetPasswordDto.setPassword("newPassword@1");
            resetPasswordDto.setToken("not-uuid");
            given()
                .contentType(ContentType.JSON)
                .body(resetPasswordDto)
                .put("/accounts/override-forced-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .assertThat().contentType(ContentType.HTML);
        }

        @Test
        void shouldReturnSC204OnlyForOneConcurrentAdminOperation() throws BrokenBarrierException, InterruptedException {
            int threadNumber = 4;
            CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNumber + 1);
            List<Thread> threads = new ArrayList<>();
            AtomicInteger numberFinished = new AtomicInteger();
            AtomicInteger numberOfSuccessfulAttempts = new AtomicInteger();

            for (int i = 0; i < threadNumber; i++) {
                threads.add(new Thread(() -> {
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }

                    int statusCode = given()
                        .spec(adminSpec)
                        .contentType(ContentType.JSON)
                        .when()
                        .put("/accounts/force-password-change/jkubiak")
                        .getStatusCode();

                    if (statusCode == 204) {
                        numberOfSuccessfulAttempts.getAndIncrement();
                    }
                    numberFinished.getAndIncrement();
                }));
            }
            threads.forEach(Thread::start);
            cyclicBarrier.await();
            while (numberFinished.get() != threadNumber) {
            }
            assertEquals(1, numberOfSuccessfulAttempts.get());
        }
    }
}