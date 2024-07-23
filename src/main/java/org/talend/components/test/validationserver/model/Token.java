package org.talend.components.test.validationserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public abstract class Token<T> {

    protected String access_token;
    protected String token_type;
    protected T expires_in;

    @Data
    public static class TokenWithLongExpiresIn extends Token<Long>{

        public TokenWithLongExpiresIn(final String access_token, final String token_type, final Long expires_in){
            this.access_token = access_token;
            this.token_type = token_type;
            this.expires_in = expires_in;
        }
    }

    @Data
    public static class TokenWithStringExpiresIn extends Token<String>{

        public TokenWithStringExpiresIn(final String access_token, final String token_type, final String expires_in){
            this.access_token = access_token;
            this.token_type = token_type;
            this.expires_in = expires_in;
        }
    }

}
