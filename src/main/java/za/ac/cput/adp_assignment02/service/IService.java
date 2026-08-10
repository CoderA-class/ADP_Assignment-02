package za.ac.cput.adp_assignment02.service;

/*
* IService.java
* Generic Service Interface
* Author: David Daniel Sepkitt (240046935)
* Date: 10 August 2026
* */
import java.util.List;

public interface IService<T, ID> {
    T create(T entity);

    T read(ID id);

    T update(T entity);

    boolean delete(ID id);

    List<T> getAll();
}