package cl.duoc.gamerhub.service;

import cl.duoc.gamerhub.model.Event;
import cl.duoc.gamerhub.repository.EventRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class EventCatalog {
    private final EventRepository events;

    EventCatalog(EventRepository events) {
        this.events = events;
    }

    public List<Event> search(String category, String location, String date, String name) {
        LocalDate selectedDate = parseDate(date);
        String city = location.strip().toLowerCase(Locale.ROOT);
        String title = name.strip().toLowerCase(Locale.ROOT);
        return events.findAll().stream()
            .filter(event -> category.isBlank() || event.getCategory().equalsIgnoreCase(category))
            .filter(event -> event.getLocation().toLowerCase(Locale.ROOT).contains(city))
            .filter(event -> event.getTitle().toLowerCase(Locale.ROOT).contains(title))
            .filter(event -> selectedDate == null || event.getDate().equals(selectedDate))
            .sorted(Comparator.comparing(Event::getDate))
            .toList();
    }

    public Event detail(Long id) {
        return events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private LocalDate parseDate(String date) {
        if (date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha inválida");
        }
    }
}
