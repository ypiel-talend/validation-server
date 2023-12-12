package org.talend.components.test.validationserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.talend.components.test.validationserver.exception.OAuthException;
import org.talend.components.test.validationserver.model.Token;
import org.talend.components.test.validationserver.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


class OAuth2ControllerTest {

    private OAuth2Controller controller;

    @BeforeEach
    public void beforeEach(){
        controller = new OAuth2Controller();
    }

    @ParameterizedTest
    @CsvSource({
            "1234567890,secret_1234567890_,client_credentials,scA,true",
            "1234567890X,secret_1234567890_,client_credentials,scA,false",
            "1234567890,secret_1234567890_X,client_credentials,scA,false",
            "1234567890,secret_1234567890_,client_credentialsX,scA,false",
            "1234567890,secret_1234567890_,client_credentials,scAX,false"
    })
    public void clientCredentialsTokenTest(String clientId, String clientSecret, String grantType, String scope, Boolean success){
        Map<String, String> urlencodedForm = new HashMap<>();
        urlencodedForm.put(OAuth2Controller.client_id, clientId);
        urlencodedForm.put(OAuth2Controller.client_secret, clientSecret);
        urlencodedForm.put(OAuth2Controller.grant_type, grantType);
        urlencodedForm.put(OAuth2Controller.scope, scope);

        Supplier<Token> getTokenSupplier = () -> controller.clientCredentialsToken(urlencodedForm);

        if(success){
            Token token = getTokenSupplier.get();
            Assertions.assertNotNull(token);
            Assertions.assertEquals(token.getAccess_token(), OAuth2Controller.successToken);
            Assertions.assertEquals(token.getToken_type(), OAuth2Controller.tokenType);
        }
        else {
            Assertions.assertThrows(OAuthException.class, () -> {
                getTokenSupplier.get();
            });
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Bearer _success_token_,,,,true",
            "Bearer _success_token_,5,,,true",
            "Bearer _success_token_,,Jean,,true",
            "Bearer _success_token_,,,false,true",
            "Bearer _success_token_,10,John,false,true",
            "Bearer _success_token_,7,Marc,true,true",
            "Bearer _success_token_X,,,,false",
            "BearerX _success_token_,,,,false"
    })
    public void getEntityTest(String secret, String id, String name, String active, Boolean success){
        Supplier<User> getUserSupplier = () ->controller.getEntity(secret, id, name, active);

        if(success) {
            User user = getUserSupplier.get();
            Assertions.assertNotNull(user);
            Assertions.assertEquals(id == null ? 1 : Integer.parseInt(id), user.getId());
            Assertions.assertEquals(name == null ? "Peter" : name, user.getName());
            Assertions.assertEquals(active == null ? true : Boolean.parseBoolean(active), user.getActive());
        }
        else {
            Assertions.assertThrows(OAuthException.class, () -> {
                getUserSupplier.get();
            });
        }
    }

}