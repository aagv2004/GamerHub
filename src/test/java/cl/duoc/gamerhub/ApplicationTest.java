package cl.duoc.gamerhub;

import cl.duoc.gamerhub.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={"spring.datasource.url=jdbc:h2:mem:testdb","spring.jpa.hibernate.ddl-auto=create-drop","DEMO_PASSWORD=Only-test-password-123"})
@AutoConfigureMockMvc
class ApplicationTest {
    @Autowired MockMvc mvc;
    @Autowired AccountRepository accounts;
    @Test void threeDemoUsersCanLogin() throws Exception {
        for(String name:new String[]{"player1","player2","player3"})
            mvc.perform(post("/login").with(csrf()).param("username",name+"@gamerhub.test").param("password","Only-test-password-123")).andExpect(redirectedUrl("/eventos"));
    }
    @Test void publicAndPrivateRoutes() throws Exception {
        for(String path: new String[]{"/","/home","/recetas","/login","/registro","/css/style.css","/eventos"})mvc.perform(get(path)).andExpect(status().isOk());
        for(String path:new String[]{"/perfil","/eventos/1"})mvc.perform(get(path)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }
    @Test void registrationLoginProfileAndLogout() throws Exception {
        mvc.perform(post("/registro").param("name","Jugador").param("email","flow@example.test").param("password","Test-password-123")).andExpect(status().isForbidden());
        mvc.perform(post("/registro").with(csrf()).param("name","Jugador").param("email","FLOW@example.test").param("password","Test-password-123")).andExpect(redirectedUrl("/login?registered"));
        assertNotEquals("Test-password-123",accounts.findByEmail("flow@example.test").orElseThrow().getPassword());
        mvc.perform(post("/registro").with(csrf()).param("name","Otro").param("email","flow@example.test").param("password","Test-password-123")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registration","email"));
        mvc.perform(post("/login").with(csrf()).param("username","flow@example.test").param("password","incorrecta")).andExpect(redirectedUrl("/login?error"));
        var login=mvc.perform(post("/login").with(csrf()).param("username","flow@example.test").param("password","Test-password-123")).andExpect(redirectedUrl("/eventos")).andReturn();
        var session=(org.springframework.mock.web.MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/perfil").session(session)).andExpect(status().isOk());
        mvc.perform(post("/perfil").session(session).with(csrf()).param("nickname","Perzyro").param("games","League of Legends").param("avatar","🎮").param("notifications","true")).andExpect(redirectedUrl("/perfil?saved"));
        assertEquals("Perzyro",accounts.findByEmail("flow@example.test").orElseThrow().getNickname());
        mvc.perform(get("/eventos").session(session).param("category","Esports").param("location","Santiago")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Nexo competitivo")));
        mvc.perform(get("/eventos/1").session(session)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertTrue(session.isInvalid());
    }
    @Test void validationAndEmptySearch() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("name","").param("email","invalid").param("password","short")).andExpect(status().isOk()).andExpect(model().attributeHasErrors("registration"));
        mvc.perform(get("/eventos").with(user("test")).param("location","No existe")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("No hay eventos")));
        mvc.perform(get("/eventos").with(user("test")).param("date","bad")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/eventos/99999").with(user("test"))).andExpect(status().isNotFound());
    }
    @Test void apiRequiresAValidBearerTokenForDetails() throws Exception {
        mvc.perform(get("/api/eventos").param("name","nexo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Nexo competitivo"))
            .andExpect(jsonPath("$[0].address").doesNotExist());
        mvc.perform(get("/api/eventos/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/1").header("Authorization","Bearer invalido"))
            .andExpect(status().isUnauthorized());

        for (String name : new String[]{"player1","player2","player3"}) {
            String request = """
                {"email":"%s@gamerhub.test","password":"Only-test-password-123"}
                """.formatted(name);
            String body = mvc.perform(post("/api/login").contentType("application/json").content(request))
                .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
            String token = com.jayway.jsonpath.JsonPath.read(body, "$.token");
            mvc.perform(get("/api/eventos/1").header("Authorization","Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.address").isNotEmpty());
        }

        mvc.perform(post("/api/login").contentType("application/json")
            .content("""
                {"email":"player1@gamerhub.test","password":"incorrecta"}
                """)).andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/login").with(csrf())
            .param("username","player1@gamerhub.test").param("password","Only-test-password-123"))
            .andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/eventos/1").session(session)).andExpect(status().isUnauthorized());
    }
}
