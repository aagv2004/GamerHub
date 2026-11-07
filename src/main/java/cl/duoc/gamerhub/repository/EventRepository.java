package cl.duoc.gamerhub.repository;

import cl.duoc.gamerhub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventRepository extends JpaRepository<Event,Long> {}
