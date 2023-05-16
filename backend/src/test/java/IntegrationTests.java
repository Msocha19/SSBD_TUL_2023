import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.AccessLevelDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.AddressDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.AdminDataDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.ManagerDataDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.OwnerDataDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mok.Language;
import pl.lodz.p.it.ssbd2023.ssbd05.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ChangeAccessLevelDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ChangePasswordDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.EditAnotherPersonalDataDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ChangeActiveStatusDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.LoginDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.RefreshJwtDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.response.AccountDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.response.OwnAccountDto;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request.ResetPasswordDto;
import pl.lodz.p.it.ssbd2023.ssbd05.utils.I18n;

import java.util.ArrayList;
import java.util.HashSet;
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
                    .contentType(ContentType.JSON)
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

    //Wyświetl dane konta użytkownika
    @Nested
    class MOK4 {

        private static final String ownProfileURL = "/accounts/me";
        private static final String profileURL = "/accounts/%d";
        private static RequestSpecification testSpec;

        @BeforeAll
        static void generateTestSpec() {
            LoginDto loginDto = new LoginDto("kgraczyk", "P@ssw0rd");

            String jwt = RestAssured.given().body(loginDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/login")
                .jsonPath()
                .get("jwt");

            testSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
        }

        @Test
        void shouldPassGettingOwnAccountDetails() {
            io.restassured.response.Response response = given().spec(testSpec)
                .when()
                .get(ownProfileURL)
                .thenReturn();

            assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());

            String etag = response.getHeader("ETag");
            assertNotNull(etag);
            assertTrue(etag.length() > 0);

            OwnAccountDto dto = response.getBody().as(OwnAccountDto.class);
            assertNotNull(dto);
            assertEquals("kgraczyk", dto.getLogin());
            assertEquals("kgraczyk@gmail.local", dto.getEmail());
            assertEquals("PL", dto.getLanguage());
            assertEquals("Kamil", dto.getFirstName());
            assertEquals("Graczyk", dto.getLastName());
            assertEquals(-27, dto.getId());
            assertEquals(2, dto.getAccessLevels().size());
            for (AccessLevelDto level : dto.getAccessLevels()) {
                if (level instanceof OwnerDataDto ownerData) {
                    assertTrue(ownerData.isActive());
                    assertTrue(ownerData.isVerified());

                    AddressDto addressDto = ownerData.getAddress();
                    assertEquals(14, addressDto.buildingNumber());
                    assertEquals("Łódź", addressDto.city());
                    assertEquals("99-150", addressDto.postalCode());
                    assertEquals("Smutna", addressDto.street());
                } else if (level instanceof ManagerDataDto managerData) {
                    assertEquals("9566541", managerData.getLicenseNumber());
                    assertTrue(managerData.isActive());
                    assertTrue(managerData.isVerified());

                    AddressDto addressDto = managerData.getAddress();
                    assertEquals(14, addressDto.buildingNumber());
                    assertEquals("Łódź", addressDto.city());
                    assertEquals("99-150", addressDto.postalCode());
                    assertEquals("Smutna", addressDto.street());
                }
            }
        }

        @Test
        void shouldReturnSC403WhenGettingOwnAccountDetailsAsGuest() {
            when()
                .get(ownProfileURL)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldPassGettingAccountDetails() {
            io.restassured.response.Response response = given().spec(adminSpec)
                .when()
                .get(profileURL.formatted(-27))
                .thenReturn();

            assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());

            String etag = response.getHeader("ETag");
            assertNotNull(etag);
            assertTrue(etag.length() > 0);

            AccountDto dto = response.getBody().as(AccountDto.class);
            assertNotNull(dto);
            assertEquals("kgraczyk", dto.getLogin());
            assertEquals("kgraczyk@gmail.local", dto.getEmail());
            assertEquals("PL", dto.getLanguage());
            assertEquals("Kamil", dto.getFirstName());
            assertEquals("Graczyk", dto.getLastName());
            assertEquals(-27, dto.getId());
            assertEquals(2, dto.getAccessLevels().size());

            assertTrue(dto.isActive());
            assertTrue(dto.isVerified());
            for (AccessLevelDto level : dto.getAccessLevels()) {
                if (level instanceof OwnerDataDto ownerData) {
                    assertTrue(ownerData.isActive());
                    assertTrue(ownerData.isVerified());

                    AddressDto addressDto = ownerData.getAddress();
                    assertEquals(14, addressDto.buildingNumber());
                    assertEquals("Łódź", addressDto.city());
                    assertEquals("99-150", addressDto.postalCode());
                    assertEquals("Smutna", addressDto.street());
                } else if (level instanceof ManagerDataDto managerData) {
                    assertEquals("9566541", managerData.getLicenseNumber());
                    assertTrue(managerData.isActive());
                    assertTrue(managerData.isVerified());

                    AddressDto addressDto = managerData.getAddress();
                    assertEquals(14, addressDto.buildingNumber());
                    assertEquals("Łódź", addressDto.city());
                    assertEquals("99-150", addressDto.postalCode());
                    assertEquals("Smutna", addressDto.street());
                }
            }
        }

        @Test
        void shouldReturnSC403WhenGettingAccountDetailsAsManager() {
            given().spec(managerSpec)
                .when()
                .get(profileURL.formatted(-1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC403WhenGettingAccountDetailsAsOwnerWithStatusCode403() {
            given().spec(ownerSpec)
                .when()
                .get(profileURL.formatted(-1))
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        void shouldReturnSC404WhenGettingNonExistentAccountDetails() {
            given().spec(adminSpec)
                .when()
                .get(profileURL.formatted(-2137))
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    //Zmień dane osobowe
    @Nested
    class MOK7 {
        private static RequestSpecification testSpec;
        private static final String ownProfileURL = "/accounts/me";

        @BeforeAll
        static void generateTestSpec() {
            LoginDto loginDto = new LoginDto("wodwazny", "P@ssw0rd");

            String jwt = RestAssured.given().body(loginDto)
                .contentType(ContentType.JSON)
                .when()
                .post("/login")
                .jsonPath()
                .get("jwt");

            testSpec = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
        }

        @Test
        void shouldPassWhenEditingOwnAccountDetails() {
            io.restassured.response.Response response = given().spec(testSpec)
                .when()
                .get(ownProfileURL)
                .thenReturn();
            OwnAccountDto dto = response.body().as(OwnAccountDto.class);
            String etag = response.getHeader("ETag");

            dto.setFirstName("Wojroll");
            dto.setLastName("Brave");

            AddressDto addressDto = new AddressDto(
                "99-151",
                "Łęczyca",
                "Belwederska",
                12);
            String licenseNumber = "123456";
            for (AccessLevelDto level : dto.getAccessLevels()) {
                if (level instanceof OwnerDataDto ownerData) {
                    ownerData.setAddress(addressDto);
                } else if (level instanceof ManagerDataDto managerData) {
                    managerData.setLicenseNumber(licenseNumber);
                    managerData.setAddress(addressDto);
                }
            }

            given()
                .header("If-Match", etag)
                .spec(testSpec)
                .when()
                .contentType(ContentType.JSON)
                .body(dto)
                .put(ownProfileURL)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());

            io.restassured.response.Response edited = given().spec(testSpec)
                .when()
                .get(ownProfileURL)
                .thenReturn();

            OwnAccountDto editedDto = edited.as(OwnAccountDto.class);
            assertEquals(dto.getFirstName(), editedDto.getFirstName());
            assertEquals(dto.getLastName(), editedDto.getLastName());

            ManagerDataDto editedManagerData = edited.body().jsonPath()
                .getObject("accessLevels.find{it.level=='MANAGER'}", ManagerDataDto.class);
            assertEquals(editedManagerData.getAddress(), addressDto);
            assertEquals(editedManagerData.getLicenseNumber(), licenseNumber);

            OwnerDataDto editedOwnerData = edited.body().jsonPath()
                .getObject("accessLevels.find{it.level=='OWNER'}", OwnerDataDto.class);

            assertEquals(editedOwnerData.getAddress(), addressDto);
        }

        @Test
        void shouldReturnSC403WhenEditingOwnAccountDetailsAsGuest() {
            io.restassured.response.Response response = given().spec(testSpec)
                .when()
                .get(ownProfileURL)
                .thenReturn();
            OwnAccountDto dto = response.body().as(OwnAccountDto.class);
            String etag = response.getHeader("ETag");

            given()
                .header("If-Match", etag)
                .when()
                .contentType(ContentType.JSON)
                .body(dto)
                .put(ownProfileURL)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Nested
        class InactiveEdit {

            private static RequestSpecification omitManagerSpec;
            private static RequestSpecification omitOwnerSpec;

            @BeforeAll
            static void generateTestSpec() {
                LoginDto loginDto = new LoginDto("ptomczyk", "P@ssw0rd");

                String jwt = RestAssured.given().body(loginDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/login")
                    .jsonPath()
                    .get("jwt");

                omitManagerSpec = new RequestSpecBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

                loginDto = new LoginDto("kkowalski", "P@ssw0rd");

                jwt = RestAssured.given().body(loginDto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/login")
                    .jsonPath()
                    .get("jwt");

                omitOwnerSpec = new RequestSpecBuilder()
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();
            }

            @Test
            void shouldOmitInactiveManagerAccessLevelWhenEditingOwnAccountDetails() {
                io.restassured.response.Response response = given().spec(omitManagerSpec)
                    .when()
                    .get(ownProfileURL)
                    .then().extract().response();

                OwnAccountDto dto = response.body().as(OwnAccountDto.class);

                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof ManagerDataDto managerData) {
                        managerData.setLicenseNumber("654321");
                        AddressDto addressDto = new AddressDto(
                            "99-151",
                            "Łęczyca",
                            "Belwederska",
                            12);
                        managerData.setAddress(addressDto);
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(omitManagerSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.OK.getStatusCode());

                io.restassured.response.Response edited = given().spec(omitManagerSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();

                OwnAccountDto verifyDto = edited.as(OwnAccountDto.class);

                String firstName = response.body().jsonPath().getString("firstName");
                assertEquals(firstName, verifyDto.getFirstName());

                String lastName = response.body().jsonPath().getString("lastName");
                assertEquals(lastName, verifyDto.getLastName());

                ManagerDataDto managerData = response.body().jsonPath()
                    .getObject("accessLevels.find{it.level=='MANAGER'}", ManagerDataDto.class);
                ManagerDataDto editedManagerData = edited.body().jsonPath()
                    .getObject("accessLevels.find{it.level=='MANAGER'}", ManagerDataDto.class);
                assertEquals(editedManagerData.getAddress(), managerData.getAddress());
                assertEquals(editedManagerData.getLicenseNumber(), managerData.getLicenseNumber());
            }

            @Test
            void shouldOmitInactiveOwnerAccessLevelWhenEditingOwnAccountDetails() {
                io.restassured.response.Response response = given().spec(omitOwnerSpec)
                    .when()
                    .get(ownProfileURL)
                    .then().extract().response();

                OwnAccountDto dto = response.body().as(OwnAccountDto.class);

                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof OwnerDataDto ownerData) {
                        AddressDto addressDto = new AddressDto(
                            "99-151",
                            "Łęczyca",
                            "Belwederska",
                            12);
                        ownerData.setAddress(addressDto);
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(omitOwnerSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.OK.getStatusCode());

                io.restassured.response.Response edited = given().spec(omitOwnerSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();

                OwnAccountDto verifyDto = edited.as(OwnAccountDto.class);

                String firstName = response.body().jsonPath().getString("firstName");
                assertEquals(firstName, verifyDto.getFirstName());

                String lastName = response.body().jsonPath().getString("lastName");
                assertEquals(lastName, verifyDto.getLastName());

                OwnerDataDto ownerData = response.body().jsonPath()
                    .getObject("accessLevels.find{it.level=='OWNER'}", OwnerDataDto.class);
                OwnerDataDto editedOwnerData = edited.body().jsonPath()
                    .getObject("accessLevels.find{it.level=='OWNER'}", OwnerDataDto.class);
                assertEquals(editedOwnerData.getAddress(), ownerData.getAddress());
            }
        }

        @Nested
        class SignatureMismatch {
            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidETag() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = "eTag";

                given()
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithChangedVersion() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                dto.setVersion(-1L);

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithChangedAccessLevelVersion() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    level.setVersion(-1L);
                }

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithAddedAccessLevel() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                dto.getAccessLevels().add(new AdminDataDto());

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithRemovedAccessLevel() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                OwnerDataDto ownerDataDto = null;
                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof OwnerDataDto dataDto) {
                        ownerDataDto = dataDto;
                    }
                }
                dto.getAccessLevels().remove(ownerDataDto);

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }
        }

        @Nested
        class Constraints {
            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithoutIfMatch() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                given()
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Test
            void shouldReturnSC409WhenSettingExistingLicenseNumber() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof ManagerDataDto managerData) {
                        managerData.setLicenseNumber("9566541");
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.CONFLICT.getStatusCode());
            }

            @Test
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidPersonalData() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                //firstName
                dto.setFirstName("");

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

                //lastName
                dto.setLastName("");

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

                //accountVersion
                dto.setVersion(null);

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidLicenseNumber(String value) {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof ManagerDataDto managerData) {
                        managerData.setLicenseNumber(value);
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
            }

            @Nested
            class Address {

                @ParameterizedTest
                @NullSource
                @ValueSource(strings = {"99-1511", "99-51"})
                void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidPostalCode(String postalCode) {
                    io.restassured.response.Response response = given().spec(testSpec)
                        .when()
                        .get(ownProfileURL)
                        .thenReturn();
                    OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                    String etag = response.getHeader("ETag");

                    for (AccessLevelDto level : dto.getAccessLevels()) {
                        if (level instanceof ManagerDataDto managerData) {
                            AddressDto addressDto = new AddressDto(
                                "99-1511",
                                "Łęczyca",
                                "Belwederska",
                                12);
                            managerData.setAddress(addressDto);
                        }
                    }

                    given()
                        .header("If-Match", etag)
                        .spec(testSpec)
                        .when()
                        .contentType(ContentType.JSON)
                        .body(dto)
                        .put(ownProfileURL)
                        .then()
                        .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
                }

                @ParameterizedTest
                @ValueSource(ints = {-1, 0})
                @NullSource
                void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidBuildingNumber(Integer buildingNumber) {
                    io.restassured.response.Response response = given().spec(testSpec)
                        .when()
                        .get(ownProfileURL)
                        .thenReturn();
                    OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                    String etag = response.getHeader("ETag");

                    //buildingNumber negative
                    for (AccessLevelDto level : dto.getAccessLevels()) {
                        if (level instanceof ManagerDataDto managerData) {
                            AddressDto addressDto = new AddressDto(
                                "99-151",
                                "Łęczyca",
                                "Belwederska",
                                buildingNumber);
                            managerData.setAddress(addressDto);
                        }
                    }

                    given()
                        .header("If-Match", etag)
                        .spec(testSpec)
                        .when()
                        .contentType(ContentType.JSON)
                        .body(dto)
                        .put(ownProfileURL)
                        .then()
                        .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
                }

                @ParameterizedTest
                @ValueSource(strings = {"Ł",
                    "ŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczycaŁęczyca"})
                @NullSource
                void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidCity(String city) {
                    io.restassured.response.Response response = given().spec(testSpec)
                        .when()
                        .get(ownProfileURL)
                        .thenReturn();
                    OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                    String etag = response.getHeader("ETag");

                    for (AccessLevelDto level : dto.getAccessLevels()) {
                        if (level instanceof ManagerDataDto managerData) {
                            AddressDto addressDto = new AddressDto(
                                "99-151",
                                city,
                                "Belwederska",
                                12);
                            managerData.setAddress(addressDto);
                        }
                    }

                    given()
                        .header("If-Match", etag)
                        .spec(testSpec)
                        .when()
                        .contentType(ContentType.JSON)
                        .body(dto)
                        .put(ownProfileURL)
                        .then()
                        .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
                }

                @Test
                void shouldReturnSC400WhenEditingOwnAccountDetailsWithInvalidAddress() {
                    io.restassured.response.Response response = given().spec(testSpec)
                        .when()
                        .get(ownProfileURL)
                        .thenReturn();
                    OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                    String etag = response.getHeader("ETag");

                    //address null
                    for (AccessLevelDto level : dto.getAccessLevels()) {
                        if (level instanceof ManagerDataDto managerData) {
                            managerData.setAddress(null);
                        }
                    }

                    given()
                        .header("If-Match", etag)
                        .spec(testSpec)
                        .when()
                        .contentType(ContentType.JSON)
                        .body(dto)
                        .put(ownProfileURL)
                        .then()
                        .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
                }
            }


        }

        @Nested
        class OptimistickLock {
            @Test
            void shouldReturnSC409WhenEditingOwnAccountDetailsThatChangedInTheMeantime() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                dto.setFirstName("Changed");

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.OK.getStatusCode());

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.CONFLICT.getStatusCode());
            }

            @Test
            void shouldReturnSC409WhenEditingOwnAccountDetailsWithOwnerDataChangedInTheMeantime() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof OwnerDataDto ownerData) {
                        AddressDto addressDto = new AddressDto(
                            "91-151",
                            "Łęczyca3",
                            "Belwederska3",
                            12);
                        ownerData.setAddress(addressDto);
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.OK.getStatusCode());

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.CONFLICT.getStatusCode());
            }

            @Test
            void shouldReturnSC409WhenEditingOwnAccountDetailsWithManagerDataChangedInTheMeantime() {
                io.restassured.response.Response response = given().spec(testSpec)
                    .when()
                    .get(ownProfileURL)
                    .thenReturn();
                OwnAccountDto dto = response.body().as(OwnAccountDto.class);
                String etag = response.getHeader("ETag");

                for (AccessLevelDto level : dto.getAccessLevels()) {
                    if (level instanceof ManagerDataDto managerData) {
                        managerData.setLicenseNumber("654321");
                        AddressDto addressDto = new AddressDto(
                            "91-151",
                            "Łęczyca4",
                            "Belwederska4",
                            12);
                        managerData.setAddress(addressDto);
                    }
                }

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.OK.getStatusCode());

                given()
                    .header("If-Match", etag)
                    .spec(testSpec)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .put(ownProfileURL)
                    .then()
                    .contentType(ContentType.JSON)
                    .statusCode(Response.Status.CONFLICT.getStatusCode());
            }
        }

        @Nested
        class ConcurrentTests {
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
                        .put("/accounts/force-password-change/wlokietek")
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

    @Nested // zmiana hasła
    class MOK5 {

        @Test
        void shouldChangePasswordWhenProvidedValidNewPasswordAndValidOldPassword() {

            String oldPass = "P@ssw0rd";
            String newPass = "Haslo123@rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals((Integer)account.get("version"), (Integer)oldAccount.get("version") + 1);

            given().spec(adminSpec).when()
                .body(new ChangePasswordDto(newPass, oldPass))
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        }

        @Test
        public void shouldReturnSC401WhenProvidedWrongOldPassword() {
            String oldPass = "P@ssw0rd7";
            String newPass = "Haslo123@rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedInvalidOldPassword() {
            String oldPass = "";
            String newPass = "Haslo123@rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedWeakNewPasswordLength() {
            String oldPass = "P@ssw0rd";
            String newPass = "h@Sl0";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedWeakNewPasswordNumber() {
            String oldPass = "P@ssw0rd";
            String newPass = "h@Slopasswd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedWeakNewPasswordSpecialCharacter() {
            String oldPass = "P@ssw0rd";
            String newPass = "h4Slopa55wo";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedWeakNewPasswordLowerCase() {
            String oldPass = "P@ssw0rd";
            String newPass = "H@S7OPASSWD";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedWeakNewPasswordUpperCase() {
            String oldPass = "P@ssw0rd";
            String newPass = "h@slo1passwd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedIdenticalPasswords() {
            String oldPass = "P@ssw0rd";
            String newPass = "P@ssw0rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC400WhenProvidedEmptyOldPasswords() {
            String oldPass = "";
            String newPass = "P@ssw0rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().spec(adminSpec).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }

        @Test
        public void shouldReturnSC403WhenNoAccountIsLoggedIn() {
            String oldPass = "P@ssw0rd";
            String newPass = "h@P4ssw0rd";
            ChangePasswordDto dto = new ChangePasswordDto(oldPass, newPass);

            JsonPath oldAccount = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();

            given().when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-password")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

            JsonPath account = given().spec(adminSpec)
                .when()
                .get("/accounts/me").jsonPath();
            Assertions.assertEquals(account.get("version"), (Integer)oldAccount.get("version"));
        }
    }

    @Nested // zmiana poziomu dostępu
    class MOK10 {

        private final RequestSpecification ownerAndManagerSpec = new RequestSpecBuilder()
            .addHeader("Authorization", "Bearer "
                +  RestAssured.given().body( new LoginDto("pduda", "P@ssw0rd"))
                .contentType(ContentType.JSON)
                .when()
                .post("/login")
                .jsonPath()
                .get("jwt"))
            .build();

        private RequestSpecification makeSpec(String login) {
            return new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer "
                    +  RestAssured.given().body( new LoginDto(login, "P@ssw0rd"))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/login")
                    .jsonPath()
                    .get("jwt"))
                .build();
        }

        @Test
        public void shouldChangeAccessLevelWhenValidAccessTypeProvidedAndAccessLevelActiveAndVerified() {

            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("OWNER");

            given().spec(makeSpec("pduda")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("accessType", Matchers.equalTo("OWNER"));
        }

        @Test
        public void shouldReturnSC400WhenInvalidAccessLevelProvided1() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("adad");

            given().spec(makeSpec("pduda")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        public void shouldReturnSC400WhenInvalidAccessLevelProvided2() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("ADMINISTRATOR");

            given().spec(makeSpec("pduda")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        }

        @Test
        public void shouldReturnSC400WhenBlankAccessLevelProvided() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("");

            given().spec(makeSpec("pduda")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());}

        @Test
        public void shouldReturnSC403WhenAccountNotHaveProvidedAccessLevel() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("ADMIN");

            given().spec(makeSpec("pduda")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        public void shouldReturnSC403WhenAccountHasInactiveProvidedAccessLevel() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("ADMIN");

            given().spec(makeSpec("bkowalewski")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        public void shouldReturnSC403WhenAccountHasUnverifiedProvidedAccessLevel() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("OWNER");

            given().spec(makeSpec("nkowalska")).when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }

        @Test
        public void shouldReturnSC403WhenNoAccountIsLoggedIn() {
            ChangeAccessLevelDto dto = new ChangeAccessLevelDto("OWNER");

            given().when()
                .body(dto)
                .contentType(ContentType.JSON)
                .put("/accounts/me/change-access-level")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
        }


    }

    @Nested // edycje dowolnego niewłasnego konta przez administratora
    class MOK17 {

        private EditAnotherPersonalDataDto makeEditPersonalDataDto(AccountDto acc) {
            EditAnotherPersonalDataDto dto = new EditAnotherPersonalDataDto();
            dto.setAccessLevels(acc.getAccessLevels());
            dto.setLastName(acc.getLastName());
            dto.setFirstName(acc.getFirstName());
            dto.setLogin(acc.getLogin());
            dto.setVersion(acc.getVersion());
            dto.setEmail(acc.getEmail());
            dto.setLanguage(Language.valueOf(acc.getLanguage()));
            return dto;
        }

        @Test
        public void shouldChangeUserFirstNameAndLastName() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setFirstName("newFirstName");
            acc.setLastName("newLastName");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);


            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("login", Matchers.equalTo(acc.getLogin()))
                .body("firstName", Matchers.equalTo("newFirstName"))
                .body("lastName", Matchers.equalTo("newLastName"));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertEquals(acc2, acc);
        }

        @Test
        public void shouldChangeUserEmailAndLanguage() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setEmail("newFirstName@gmail.com");
            acc.setLanguage("EN");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);


            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("login", Matchers.equalTo(acc.getLogin()))
                .body("email", Matchers.equalTo("newFirstName@gmail.com"))
                .body("language", Matchers.equalTo("EN"));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertEquals(acc2, acc);
        }

        @Test
        public void shouldChangeUserOwnerData() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            OwnerDataDto accessLevelDto = (OwnerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto("99-000", "Wrocław", "Warszawska", 99));
            acc.setAccessLevels(new HashSet<>(List.of((new OwnerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("login", Matchers.equalTo(acc.getLogin()))
                .body("accessLevels[0].address.buildingNumber", Matchers.equalTo(accessLevelDto.getAddress().buildingNumber()))
                .body("accessLevels[0].address.city", Matchers.equalTo(accessLevelDto.getAddress().city()))
                .body("accessLevels[0].address.street", Matchers.equalTo(accessLevelDto.getAddress().street()))
                .body("accessLevels[0].address.postalCode", Matchers.equalTo(accessLevelDto.getAddress().postalCode()));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertEquals(acc2, acc);
        }

        @Test
        public void shouldChangeUserManagerData() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto("99-000", "Wrocław", "Warszawska", 99));
            accessLevelDto.setLicenseNumber("98765432");
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("login", Matchers.equalTo(acc.getLogin()))
                .body("accessLevels[0].address.buildingNumber", Matchers.equalTo(accessLevelDto.getAddress().buildingNumber()))
                .body("accessLevels[0].licenseNumber", Matchers.equalTo(accessLevelDto.getLicenseNumber()))
                .body("accessLevels[0].address.city", Matchers.equalTo(accessLevelDto.getAddress().city()))
                .body("accessLevels[0].address.street", Matchers.equalTo(accessLevelDto.getAddress().street()))
                .body("accessLevels[0].address.postalCode", Matchers.equalTo(accessLevelDto.getAddress().postalCode()));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2, acc);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "imie"})
        public void shouldReturnSC400WhenInvalidEmailAddress(String email) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setEmail(email);
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"})
        public void shouldReturnSC400WhenInvalidFirstName(String name) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setLastName(name);
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"})
        public void shouldReturnSC400WhenInvalidLastName(String name) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setLastName(name);
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "ES", "ESD"})
        public void shouldReturnSC400WhenLanguageNotFound(String lang) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);
            JSONObject obj = new JSONObject(dto);
            obj.put("language", lang);


            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(obj)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @Test
        public void shouldReturnSC400WhenInvalidSignatureLogin() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setLogin("newLogin1");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.SIGNATURE_MISMATCH));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2.getLogin(), acc.getLogin());
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @Test
        public void shouldReturnSC400WhenInvalidSignatureVersion() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setVersion(acc.getVersion() + 1);
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.SIGNATURE_MISMATCH));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion() - 1);

        }

        @ParameterizedTest
        @ValueSource(strings = {"", "999", "1234567", "12"})
        public void shouldReturnSC400WhenInvalidAccessLevelPostalCode(String postalCode) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto(postalCode, "Wrocław", "Warszawska", 99));
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "1", "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"})
        public void shouldReturnSC400WhenInvalidAccessLevelCity(String str) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto("90-987", str, "Warszawska", 99));
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(strings = {"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"})
        public void shouldReturnSC400WhenInvalidAccessLevelStreet(String str) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto("90-987", "Wrocław", str, 99));
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @ParameterizedTest
        @ValueSource(ints = {-99, 0})
        public void shouldReturnSC400WhenInvalidAccessLevelBuildingNumber(int l) {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setAddress(new AddressDto("90-987", "Wrocław", "Zielona", l));
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @Test
        public void shouldReturnSC400WhenInvalidSignatureAccessLevelId() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setId(accessLevelDto.getId() + 1);
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.SIGNATURE_MISMATCH));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @Test
        public void shouldReturnSC400WhenInvalidSignatureAccessLevelVersion() {
            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-16");
            AccountDto acc = resp.as(AccountDto.class);
            ManagerDataDto accessLevelDto = (ManagerDataDto) acc.getAccessLevels().stream().findFirst().get();
            accessLevelDto.setVersion(accessLevelDto.getVersion() + 1);
            acc.setAccessLevels(new HashSet<>(List.of((new ManagerDataDto[] {accessLevelDto}))));
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("message", Matchers.equalTo(I18n.SIGNATURE_MISMATCH));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-16").as(AccountDto.class);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
            Assertions.assertNotEquals(acc2.getAccessLevels().stream().findFirst().get().getVersion(),
                acc.getAccessLevels().stream().findFirst().get().getVersion());
        }

        @Test
        public void shouldReturnSC403WhenSelfEdit() {
            AccountDto me = given().spec(adminSpec)
                .when()
                .get("/accounts/me").as(AccountDto.class);

            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/" + me.getId());
            AccountDto acc = resp.as(AccountDto.class);
            acc.setLastName("Nowakowski1");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(adminSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .body("message", Matchers.equalTo(I18n.ILLEGAL_SELF_ACTION));

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/" + me.getId()).as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
        }

        @Test
        public void shouldReturnSC403WhenManagerAccount() {

            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setEmail("nowy@gmail.local");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(managerSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }

        @Test
        public void shouldReturnSC403WhenOwnerAccount() {

            io.restassured.response.Response resp = given().spec(adminSpec)
                .when()
                .get("/accounts/-15");
            AccountDto acc = resp.as(AccountDto.class);
            acc.setEmail("nowy@gmail.local");
            EditAnotherPersonalDataDto dto = makeEditPersonalDataDto(acc);

            given().spec(ownerSpec).when()
                .header(new Header("If-Match", resp.getHeader("ETag")))
                .contentType(ContentType.JSON)
                .body(dto)
                .put("/accounts/admin/edit-other")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

            AccountDto acc2 = given().spec(adminSpec)
                .when()
                .get("/accounts/-15").as(AccountDto.class);
            Assertions.assertNotEquals(acc2, acc);
            Assertions.assertEquals(acc2.getVersion(), acc.getVersion());
        }
    }

}
