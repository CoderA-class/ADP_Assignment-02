package repository;

import java.util.Set;

public interface IRepository<T, ID> {
    T create(T entity);
    T read(ID id);
    T update(T entity);
    boolean delete(ID id);
    Set<T> getAll();
}