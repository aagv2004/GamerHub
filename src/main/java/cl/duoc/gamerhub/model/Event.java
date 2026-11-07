package cl.duoc.gamerhub.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="gamer_events")
public class Event {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String title,category,location,address,organizer,services,exhibitors;
    @Column(length=2000) private String description;
    private LocalDate date;
    private String time;
    private boolean featured;
    protected Event(){}
    public Event(String title,String category,String location,String address,LocalDate date,String time,String description,String organizer,String services,String exhibitors,boolean featured){this.title=title;this.category=category;this.location=location;this.address=address;this.date=date;this.time=time;this.description=description;this.organizer=organizer;this.services=services;this.exhibitors=exhibitors;this.featured=featured;}
    public Long getId(){return id;} public String getTitle(){return title;} public String getCategory(){return category;} public String getLocation(){return location;} public String getAddress(){return address;} public LocalDate getDate(){return date;} public String getTime(){return time;} public String getDescription(){return description;} public String getOrganizer(){return organizer;} public String getServices(){return services;} public String getExhibitors(){return exhibitors;} public boolean isFeatured(){return featured;}
}
