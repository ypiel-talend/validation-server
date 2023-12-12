package org.talend.components.test.validationserver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.talend.components.test.validationserver.exception.OAuthException;
import org.talend.components.test.validationserver.model.ControllerError;
import org.talend.components.test.validationserver.model.Token;
import org.talend.components.test.validationserver.model.User;

import java.util.Map;
import java.util.Optional;

@RestController
public class OAuth2Controller {

    public final static String authorization_header = "Authorization";

    public final static String client_id = "client_id";
    public final static String client_secret = "client_secret";
    public final static String grant_type = "grant_type";
    public final static String scope = "scope";

    public final static String expectedClientId = "1234567890";
    public final static String expectedClientSecret = "secret_1234567890_";
    public final static String expectedGrantType = "client_credentials";
    public final static String expectedScope = "scA scB scC";

    public final static String successToken = "_success_token_";
    public final static String tokenType = "Bearer";

    @PostMapping("/oauth2/client-credentials/token")
    public Token clientCredentialsToken(
            @RequestParam(required = true) Map<String, String> urlencodedForm,
            @RequestHeader(name = "expected_client_id") Optional<String> expectedClientIdParam,
            @RequestHeader(name = "expected_client_secret") Optional<String> expectedClientSecretParam,
            @RequestHeader(name = "expected_scope") Optional<String> expectedScopeParam,
            @RequestHeader(name = "expected_additional") Optional<String> expectedAdditionalParam
    ) {
        String clientIdValue = urlencodedForm.get(client_id);
        String clientSecretValue = urlencodedForm.get(client_secret);
        String grantTypeValue = urlencodedForm.get(grant_type);
        String scopeValue = urlencodedForm.get(scope);
        String additionalValue = urlencodedForm.getOrDefault("additional", "");



        if(!expectedClientIdParam.orElse(expectedClientId).equals(clientIdValue) ||
        !expectedClientSecretParam.orElse(expectedClientSecret).equals(clientSecretValue) ||
        !expectedGrantType.equals(grantTypeValue) ||
        !expectedScopeParam.orElse(expectedScope).equals(scopeValue) ||
        !expectedAdditionalParam.orElse("").equals(additionalValue)){
            throw new OAuthException("Wrong credentials, can't provide token.");
        }
        Token token = new Token(successToken, tokenType, System.currentTimeMillis() + (1000 * 60 * 60));

        return token;
    }

    @GetMapping("/oauth2/get/user")
    public User getEntity(
            @RequestHeader(name=authorization_header, required = true) String authorization,
            @RequestParam(name="id", required = false) String id,
            @RequestParam(name="name", required = false) String name,
            @RequestParam(name="active", required = false) String active
    ) {
        checkToken(authorization);

        User user = new User(id == null ? 1 :Integer.parseInt(id),
                name == null ? "Peter" : name,
                active == null ? true : Boolean.parseBoolean(active));

        return user;
    }

    private void checkToken(String token){
        if(!String.format("%s %s", tokenType, successToken).equals(token)){
            throw new OAuthException("Unrecognized token.");
        }
    }

    @ExceptionHandler(OAuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ControllerError handleException(OAuthException e){
        return new ControllerError("OAuth2 security issue.", e.getMessage());
    }
}
