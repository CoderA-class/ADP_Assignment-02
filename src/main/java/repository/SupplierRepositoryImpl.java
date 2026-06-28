package repository;

/*
  SupplierRepositoryImpl.java
  SupplierRepositoryImpl model class
  Author: Luke John Zyster (220489114)
  Date: 25 March 2026
 */

import domain.Supplier;

import java.util.ArrayList;
import java.util.List;

public class SupplierRepositoryImpl implements SupplierRepository {

    private static SupplierRepositoryImpl repository = null;
    private final List<Supplier> supplierTable;

    private SupplierRepositoryImpl() {
        supplierTable = new ArrayList<>();
    }

    public static SupplierRepositoryImpl getRepository() {
        if (repository == null)
            repository = new SupplierRepositoryImpl();
        return repository;
    }

    @Override
    public Supplier create(Supplier supplier) {
        supplierTable.add(supplier);
        return supplier;
    }

    @Override
    public Supplier read(String id) {
        for (Supplier supplier : supplierTable) {
            if (supplier.getSupplierId().equals(id))
                return supplier;
        }
        return null;
    }

    @Override
    public Supplier update(Supplier supplier) {
        return null;
    }

    public Supplier update(int index, Supplier supplier) {
        for (int i = 0; i < supplierTable.size(); i++) ;
        {
            if (supplierTable.get(index).getSupplierId().equals(supplier.getSupplierId())) {
                supplierTable.set(index, supplier);
                return supplier;
            }
        }
        return null;
    }

    @Override
    public Boolean delete(String id) {
        for (int i = 0; i < supplierTable.size(); i++) {
            if (supplierTable.get(i).getSupplierId().equals(id)) {
                supplierTable.remove(i);
                return true;
            }
        }
        return false;
    }
}

