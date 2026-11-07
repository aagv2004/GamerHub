package cl.duoc.gamerhub.model;
import jakarta.persistence.*;
@Entity
@Table(name="accounts")
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String email;
    @Column(nullable=false) private String password;
    private String name;
    private String nickname;
    private String games="";
    private String avatar="🎮";
    private boolean notifications;
    protected Account() {}
    public Account(String name,String email,String password) { this.name=name; this.nickname=name; this.email=email; this.password=password; }
    public Long getId(){return id;}
    public String getEmail(){return email;}
    public String getPassword(){return password;}
    public String getName(){return name;}
    public String getNickname(){return nickname;}
    public String getGames(){return games;}
    public String getAvatar(){return avatar;}
    public boolean isNotifications(){return notifications;}
    public void updateProfile(String nickname,String games,String avatar,boolean notifications){this.nickname=nickname;this.games=games;this.avatar=avatar;this.notifications=notifications;}
}
