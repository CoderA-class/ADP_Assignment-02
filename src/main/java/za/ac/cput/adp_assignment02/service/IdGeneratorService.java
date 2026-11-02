package za.ac.cput.adp_assignment02.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.IdSequence;
import za.ac.cput.adp_assignment02.repository.IdSequenceRepository;

import java.util.function.Predicate;

/*
 * IdGeneratorService.java
 * Generates readable, sequential IDs such as CUS-0001, EMP-0002, PRD-0010, ORD-0123.
 * The next number for each prefix is stored in the id_sequence table. If an ID is
 * somehow already taken (e.g. older data that was typed in by hand) it is skipped.
 */
@Service
public class IdGeneratorService {

    public static final String CUSTOMER = "CUS";
    public static final String EMPLOYEE = "EMP";
    public static final String PRODUCT = "PRD";
    public static final String SUPPLIER = "SUP";
    public static final String ORDER = "ORD";
    public static final String ORDER_ITEM = "ITM";
    public static final String LEAVE = "LVE";

    private final IdSequenceRepository repository;

    public IdGeneratorService(IdSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public synchronized String nextId(String prefix, Predicate<String> alreadyExists) {
        IdSequence sequence = repository.findById(prefix).orElseGet(() -> new IdSequence(prefix, 1));
        String id;
        do {
            id = String.format("%s-%04d", prefix, sequence.getNextValue());
            sequence.setNextValue(sequence.getNextValue() + 1);
        } while (alreadyExists.test(id));
        repository.save(sequence);
        return id;
    }
}
