package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.IdSequence;

/*
 * IdSequenceRepository.java
 * Keeps the running number behind each automatic ID (CUS-0001, PRD-0001, ...).
 */
public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {
}
