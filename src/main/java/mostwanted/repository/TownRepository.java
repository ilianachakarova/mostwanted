package mostwanted.repository;

import mostwanted.domain.entities.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TownRepository extends JpaRepository<Town,Long> {
   Optional <Town> getTownByName(String name);
//   @Query(value = "select t.name, count(r.id) as cnt from towns as t\n" +
//           "join racers r on t.id = r.town_id\n" +
//           "group by t.id order by cnt desc, t.name;", nativeQuery = true)
//   List<Object[]>findAllByCountOfRacers();

   @Query("select t from mostwanted.domain.entities.Town as t " +
           "join t.racers as r group by t.name order by size(t.racers) desc , t.name asc ")
   List<Town>exportTownsByRacerCount();
}
