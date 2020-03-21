package mostwanted.repository;

import mostwanted.domain.entities.Racer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RacerRepository extends JpaRepository<Racer,Long> {
    Optional<Racer> findByName(String name);
    @Query("select r from mostwanted.domain.entities.Racer as r join r.cars as c " +
            "group by r order by size(r.cars) desc, r.name")
    List<Racer>exportRacingCars();
}
