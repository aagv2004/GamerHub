package cl.duoc.gamerhub.controller;

import cl.duoc.gamerhub.model.Account;
import cl.duoc.gamerhub.model.Event;
import cl.duoc.gamerhub.repository.AccountRepository;
import cl.duoc.gamerhub.repository.EventRepository;
import cl.duoc.gamerhub.service.AccountService;
import cl.duoc.gamerhub.service.EventCatalog;
import cl.duoc.gamerhub.client.BackendClient;
import java.security.Principal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
@Controller
public class WebController {
    @ModelAttribute("signedIn") boolean signedIn(Principal principal){return principal!=null;}
    private final EventRepository events;
    private final EventCatalog catalog;
    private final BackendClient backend;
    private final AccountRepository accounts;
    private final AccountService service;
    public WebController(EventRepository events,AccountRepository accounts,AccountService service,EventCatalog catalog,BackendClient backend){this.backend=backend;this.catalog=catalog;this.events=events;this.accounts=accounts;this.service=service;}
    public record Registration(@NotBlank @Size(max=80) String name,@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(min=10,max=60) String password){}
    public record Profile(@NotBlank @Size(max=40) String nickname,@Size(max=300) String games,@NotBlank String avatar,boolean notifications){}
    @GetMapping({"/","/home","/recetas"}) String home(Model model){
        var upcoming=events.findAll().stream().filter(e -> !e.getDate().isBefore(LocalDate.now())).sorted(Comparator.comparing(Event::getDate)).toList();
        model.addAttribute("events",upcoming);return "home";
    }
    @GetMapping("/login") String login(){return "login";}
    @GetMapping("/registro") String register(Model model){model.addAttribute("registration",new Registration("","",""));return "register";}
    @PostMapping("/registro") String register(@Valid @ModelAttribute("registration") Registration data,BindingResult result){
        if(data.password()!=null && data.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            result.rejectValue("password","length","La contraseña supera 72 bytes; usa menos caracteres.");
        if(result.hasErrors())return "register";
        try{service.register(data.name(),data.email(),data.password());}
        catch(IllegalArgumentException|DataIntegrityViolationException ex){result.rejectValue("email","duplicate","Ese correo ya tiene una cuenta.");return "register";}
        return "redirect:/login?registered";
    }
    @GetMapping("/eventos") String events(@RequestParam(defaultValue="") String category,@RequestParam(defaultValue="") String location,@RequestParam(defaultValue="") String date,@RequestParam(defaultValue="") String name,Model model){
        model.addAttribute("events",catalog.search(category,location,date,name));
        model.addAttribute("category",category);model.addAttribute("location",location);model.addAttribute("date",date);model.addAttribute("name",name);return "events";
    }
    @GetMapping("/eventos/{id}")
    String detail(@PathVariable Long id, Authentication authentication, HttpServletRequest request, Model model) {
        if (authentication.getDetails() instanceof BackendClient.SessionToken token) {
            try {
                model.addAttribute("event", backend.detail(id, token.value()));
                return "detail";
            } catch (RestClientResponseException exception) {
                if (exception.getStatusCode().value() != 401) {
                    throw new ResponseStatusException(exception.getStatusCode());
                }
            } catch (RestClientException exception) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            }
        }

        new SecurityContextLogoutHandler().logout(request, null, authentication);
        return "redirect:/login";
    }
    private Account current(Principal principal){return accounts.findByEmail(principal.getName()).orElseThrow();}
    @GetMapping("/perfil") String profile(Principal principal,Model model){var a=current(principal);model.addAttribute("profile",new Profile(a.getNickname(),a.getGames(),a.getAvatar(),a.isNotifications()));return "profile";}
    @PostMapping("/perfil") String profile(Principal principal,@Valid @ModelAttribute("profile") Profile data,BindingResult result){
        if(!List.of("🎮","👾","🕹️","🚀").contains(data.avatar()))result.rejectValue("avatar","invalid","Selecciona un avatar de la lista.");
        if(result.hasErrors())return "profile";
        var a=current(principal);a.updateProfile(data.nickname().strip(),data.games(),data.avatar(),data.notifications());accounts.save(a);return "redirect:/perfil?saved";
    }
}
