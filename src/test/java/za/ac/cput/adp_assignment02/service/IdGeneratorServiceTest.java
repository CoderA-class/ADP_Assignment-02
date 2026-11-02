package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.IdSequence;
import za.ac.cput.adp_assignment02.repository.IdSequenceRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * IdGeneratorServiceTest.java
 * Auto-generated IDs: CUS-0001, CUS-0002, … and taken IDs are skipped.
 */
@ExtendWith(MockitoExtension.class)
class IdGeneratorServiceTest {

    @Mock
    private IdSequenceRepository repository;

    @Test
    void firstIdForAPrefixIs0001() {
        when(repository.findById("CUS")).thenReturn(Optional.empty());
        IdGeneratorService generator = new IdGeneratorService(repository);
        assertEquals("CUS-0001", generator.nextId("CUS", id -> false));
        verify(repository).save(any(IdSequence.class));
    }

    @Test
    void continuesFromTheStoredNumber() {
        when(repository.findById("PRD")).thenReturn(Optional.of(new IdSequence("PRD", 42)));
        IdGeneratorService generator = new IdGeneratorService(repository);
        assertEquals("PRD-0042", generator.nextId("PRD", id -> false));
    }

    @Test
    void skipsIdsThatAlreadyExist() {
        when(repository.findById("EMP")).thenReturn(Optional.of(new IdSequence("EMP", 1)));
        Set<String> taken = Set.of("EMP-0001", "EMP-0002");
        IdGeneratorService generator = new IdGeneratorService(repository);
        assertEquals("EMP-0003", generator.nextId("EMP", taken::contains));
    }
}
