package repository;

/*
        IEmployeeRepository.java
        IEmployeeRepository model class
        Author: Robyn Dominique Stevens (222201789)
        Date: 19 March 2026
        */

import java.util.Set;

public interface IEmployeeRepository<E, ID> {
    E create(E entity);

    E read(ID id);

    E update(E entity);

    boolean delete(ID id);

    Set<E> getAll();
}