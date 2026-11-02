package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.IdSequence;

public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {
}
