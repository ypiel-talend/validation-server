package org.talend.components.test.validationserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;
import org.talend.components.test.validationserver.model.Token;
import org.talend.components.test.validationserver.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


class OAuth2ControllerTest {

    private OAuth2Controller controller;

    @BeforeEach
    public void beforeEach() {
        controller = new OAuth2Controller();
    }

    @ParameterizedTest
    @CsvSource({
            "1234567890,secret_1234567890_,client_credentials,scA scB scC,true,,,,,false",
            "1234567890,secret_1234567890_,client_credentials,scA scB scC,true,,,,,true",
            "1234567890X,secret_1234567890_,client_credentials,scA scB scC,false,,,,,false",
            "1234567890,secret_1234567890_X,client_credentials,scA scB scC,false,,,,,false",
            "1234567890,secret_1234567890_,client_credentialsX,scA scB scC,false,,,,,false",
            "1234567890,secret_1234567890_,client_credentials,scA scB scCX,false,,,,,false",
            "azerty,secret_1234567890_,client_credentials,scA scB scC,true,azerty,,,,false",
            "1234567890,azerty,client_credentials,scA scB scC,true,,azerty,,,false",
            "1234567890,secret_1234567890_,client_credentials,azerty,true,,,azerty,,false",
            "1234567890,secret_1234567890_,client_credentials,scA scB scC,true,,,,azerty,false"
    })
    public void clientCredentialsTokenTest(String clientId, String clientSecret, String grantType, String scope, Boolean success,
                                           String expectedClientId, String expectedClientSecret, String expectedScope,
                                           String expectedAdditional, String expiresInAsString) {
        Map<String, String> urlencodedForm = new HashMap<>();
        urlencodedForm.put(OAuth2Controller.client_id, clientId);
        urlencodedForm.put(OAuth2Controller.client_secret, clientSecret);
        urlencodedForm.put(OAuth2Controller.grant_type, grantType);
        urlencodedForm.put(OAuth2Controller.scope, scope);

        if (expectedAdditional != null) {
            urlencodedForm.put("additional", expectedAdditional);
        }

        Supplier<ResponseEntity<?>> getTokenSupplier = () -> controller.clientCredentialsToken(urlencodedForm,
                Optional.ofNullable(expectedClientId),
                Optional.ofNullable(expectedClientSecret),
                Optional.ofNullable(expectedScope),
                Optional.ofNullable(expectedAdditional),
                Optional.ofNullable(expiresInAsString));

        if (success) {
            ResponseEntity<?> token = getTokenSupplier.get();
            Assertions.assertNotNull(token);
            Token<?> body = (Token<?>) token.getBody();
            Assertions.assertEquals(body.getAccess_token(), OAuth2Controller.successToken);
            Assertions.assertEquals(body.getToken_type(), OAuth2Controller.defaultTokenType);

            Object expiresIn = body.getExpires_in();
            if ("true".equals(expiresInAsString)) {
                Assertions.assertTrue(expiresIn instanceof String);
            } else {
                Assertions.assertTrue(expiresIn instanceof Long);
            }

        } else {
            ResponseEntity<?> responseEntity = getTokenSupplier.get();
            Assertions.assertEquals(401, responseEntity.getStatusCode().value());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Bearer _success_token_,,,,true,false",
            "Bearer _success_token_,5,,,true,false",
            "Bearer _success_token_,,Jean,,true,false",
            "Bearer _success_token_,,,false,true,false",
            "Bearer _success_token_,10,John,false,true,false",
            "Bearer _success_token_,7,Marc,true,true,false",
            "Bearer _success_token_X,,,,false,false",
            "BearerX _success_token_,,,,false,false",
            "AlternativeTokenPrefix _success_token_,,,,true,true",
            "AlternativeTokenPrefix _success_token_,5,,,true,true",
            "AlternativeTokenPrefix _success_token_,,Jean,,true,true",
            "AlternativeTokenPrefix _success_token_,,,false,true,true",
    })
    public void getEntityTest(String secret, String id, String name, String active, Boolean success, final boolean alternative) {
        Supplier<ResponseEntity<?>> getUserSupplier = () -> {
            if (!alternative) {
                return controller.getEntity(secret, id, name, active, Optional.empty());
            } else {
                return controller.getAlternativeEntity(secret, id, name, active, Optional.empty());
            }
        };

        if (success) {
            ResponseEntity<?> o = getUserSupplier.get();
            User user = (User) o.getBody();
            Assertions.assertNotNull(user);
            Assertions.assertEquals(id == null ? 1 : Integer.parseInt(id), user.getId());
            Assertions.assertEquals(name == null ? "Peter" : name, user.getName());
            Assertions.assertEquals(active == null ? true : Boolean.parseBoolean(active), user.getActive());
        } else {
            ResponseEntity<?> responseEntity = getUserSupplier.get();
            Assertions.assertEquals(401, responseEntity.getStatusCode().value());
        }
    }

}