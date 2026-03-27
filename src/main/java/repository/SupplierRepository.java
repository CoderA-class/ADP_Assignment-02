package repository;

/*
  SupplierRepository.java
  SupplierRepository model class
  Author: Luke John Zyster (220489114)
  Date: 25 March 2026
 */

import domain.Supplier;

public interface SupplierRepository {
    Supplier create(Supplier supplier);

    Supplier read(String id);

    Supplier update(Supplier supplier);

    Boolean delete(String id);

}
