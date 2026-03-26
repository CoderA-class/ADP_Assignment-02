package repository;

import java.util.Set;

public interface IEmployeeRepository<E, ID> {
    E create(E entity);

    E read(ID id);

    E update(E entity);

    boolean delete(ID id);

    Set<E> getAll();
}