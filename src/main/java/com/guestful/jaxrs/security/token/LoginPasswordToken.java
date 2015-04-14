/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jaxrs.security.token;

import com.guestful.jaxrs.security.annotation.AuthScheme;

/**
 * date 2014-05-23
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class LoginPasswordToken extends AbstractAuthenticationToken {

    private final String login;
    private final AuthScheme scheme;
    private Object password;
    private String system;

    public LoginPasswordToken(String login, String password) {
        this("", AuthScheme.FORM, login, password);
    }

    public LoginPasswordToken(String system, String login, String password) {
        this(system, AuthScheme.FORM, login, password);
    }

    LoginPasswordToken(AuthScheme scheme, String login, String password) {
        this("", scheme, login, password);
    }

    LoginPasswordToken(String system, AuthScheme scheme, String login, String password) {
        this.scheme = scheme;
        this.login = login;
        this.password = password;
        this.system = system;
    }

    @Override
    public Object getToken() {
        return login;
    }

    @Override
    public Object readCredentials() {
        if (password == READ_MARKER) {
            throw new IllegalStateException("Credentials already read");
        }
        Object read = password;
        password = READ_MARKER;
        return read;
    }

    @Override
    public String getScheme() {
        return scheme.name();
    }

    @Override
    public boolean isSessionAllowed() {
        return true;
    }

    @Override
    public boolean isAuthenticationRequired() {
        return true;
    }

    @Override
    public String getSystem() {
        return system;
    }
}
