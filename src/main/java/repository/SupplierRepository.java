package repository;

import domain.Supplier;

public interface SupplierRepository {
    Supplier create(Supplier supplier);

    Supplier read(String id);

    Supplier update(Supplier supplier);

    Boolean delete(String id);

}
