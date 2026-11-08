package cl.duoc.gamerhub.service;

import cl.duoc.gamerhub.model.Account;
import cl.duoc.gamerhub.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;


@Service
public class AccountService {
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    public AccountService(AccountRepository accounts,PasswordEncoder encoder){this.accounts=accounts;this.encoder=encoder;}
    public void register(String name,String email,String password){
        String normalized=email.strip().toLowerCase(Locale.ROOT);
        if(accounts.existsByEmail(normalized)) throw new IllegalArgumentException("Ese correo ya tiene una cuenta.");
        accounts.save(new Account(name.strip(),normalized,encoder.encode(password)));
    }
}
