package mostwanted.repository;

import mostwanted.domain.entities.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RaceRepository extends JpaRepository<Race,Long> {
    Optional<Race> findByDistrict_Name(String name);

}
