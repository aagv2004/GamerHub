package cl.duoc.gamerhub.client;

import cl.duoc.gamerhub.controller.GamerHubApi;

import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

@Component
public class BackendClient {
    private final RestClient client;
    private final ServletWebServerApplicationContext server;

    BackendClient(RestClient.Builder builder, ServletWebServerApplicationContext server) {
        this.client = builder.build();
        this.server = server;
    }

    public record SessionToken(String value) implements java.io.Serializable {
        @Override
        public String toString() {
            return "[JWT protegido]";
        }
    }

    public Authentication authenticate(Authentication request) {
        String email = request.getName().strip().toLowerCase(Locale.ROOT);
        String password = String.valueOf(request.getCredentials());
        try {
            var login = client.post().uri(endpoint("/login")).contentType(MediaType.APPLICATION_JSON)
                .body(new GamerHubApi.Credentials(email, password))
                .retrieve().body(GamerHubApi.LoginToken.class);
            if (login == null || login.token() == null) {
                throw new AuthenticationServiceException("Respuesta de autenticación incompleta.");
            }
            var session = UsernamePasswordAuthenticationToken.authenticated(
                email, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
            session.setDetails(new SessionToken(login.token()));
            return session;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 400 || exception.getStatusCode().value() == 401) {
                throw new BadCredentialsException("Credenciales inválidas.");
            }
            throw new AuthenticationServiceException("Servicio de autenticación no disponible.");
        } catch (RestClientException exception) {
            throw new AuthenticationServiceException("Servicio de autenticación no disponible.");
        }
    }

    public GamerHubApi.EventDetail detail(Long id, String token) {
        return client.get().uri(endpoint("/eventos/{id}"), id)
            .headers(headers -> headers.setBearerAuth(token))
            .retrieve().body(GamerHubApi.EventDetail.class);
    }

    private String endpoint(String path) {
        return "http://127.0.0.1:" + server.getWebServer().getPort() + "/api" + path;
    }
}
