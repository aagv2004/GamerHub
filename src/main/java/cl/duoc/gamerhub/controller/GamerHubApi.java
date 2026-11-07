package cl.duoc.gamerhub.controller;

import cl.duoc.gamerhub.model.Event;
import cl.duoc.gamerhub.service.EventCatalog;
import cl.duoc.gamerhub.security.JwtTokens;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GamerHubApi {
    private final AuthenticationManager authentication;
    private final JwtTokens tokens;
    private final EventCatalog events;

    GamerHubApi(AuthenticationConfiguration configuration, JwtTokens tokens, EventCatalog events) throws Exception {
        this.authentication = configuration.getAuthenticationManager();
        this.tokens = tokens;
        this.events = events;
    }

    public record Credentials(@NotBlank @Email @Size(max = 150) String email,
                       @NotBlank @Size(max = 72) String password) {}
    public record LoginToken(String token, String type) {}
    record EventSummary(Long id, String title, String category, String location, LocalDate date, boolean featured) {}

    public record EventDetail(Long id, String title, String category, String location, String address, LocalDate date,
                       String time, String description, String organizer, String services, String exhibitors,
                       boolean featured) {}

    @PostMapping("/login")
    ResponseEntity<LoginToken> login(@Valid @RequestBody Credentials credentials) {
        if (credentials.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            var identity = authentication.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new LoginToken(tokens.issue(identity.getName()), "Bearer"));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/eventos")
    List<EventSummary> search(@RequestParam(defaultValue = "") String category,
                              @RequestParam(defaultValue = "") String location,
                              @RequestParam(defaultValue = "") String date,
                              @RequestParam(defaultValue = "") String name) {
        return events.search(category, location, date, name).stream()
            .map(event -> new EventSummary(event.getId(), event.getTitle(), event.getCategory(),
                event.getLocation(), event.getDate(), event.isFeatured()))
            .toList();
    }

    @GetMapping("/eventos/{id}")
    EventDetail detail(@PathVariable Long id) {
        Event event = events.detail(id);
        return new EventDetail(event.getId(), event.getTitle(), event.getCategory(), event.getLocation(),
            event.getAddress(), event.getDate(), event.getTime(), event.getDescription(), event.getOrganizer(),
            event.getServices(), event.getExhibitors(), event.isFeatured());
    }
}
