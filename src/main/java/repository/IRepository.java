package repository;

/*
  IRepository.java
  IRepository model class
  Author: David Daniel Sepkitt(240046935)
  Date: 19 March 2026
 */

import java.util.Set;

public interface IRepository<T, ID> {
    T create(T entity);

    T read(ID id);

    T update(T entity);

    boolean delete(ID id);

    Set<T> getAll();
}