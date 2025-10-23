package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.TableTypeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableTypeRepository extends JpaRepository <TableType, Long> {
    Optional<TableType> findByCategory(TableTypeCategory tableTypeCategory);
}
