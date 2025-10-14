package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyPetRepository extends JpaRepository<Pet, Long> {

    @Query("SELECT p FROM Pet p JOIN FETCH p.user u WHERE u.username = :username")
    List<Pet> findMyPetProfilesByPetOwner(@Param("username") String username);
}
