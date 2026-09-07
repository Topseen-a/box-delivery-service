package com.polaris.boxdeliveryservice.repository;

import com.polaris.boxdeliveryservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByBoxId(Long boxId);
}
