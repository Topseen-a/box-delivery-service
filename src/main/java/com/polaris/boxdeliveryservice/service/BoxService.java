package com.polaris.boxdeliveryservice.service;

import com.polaris.boxdeliveryservice.dto.request.ItemLoadRequest;
import com.polaris.boxdeliveryservice.exception.*;
import com.polaris.boxdeliveryservice.model.Box;
import com.polaris.boxdeliveryservice.model.BoxState;
import com.polaris.boxdeliveryservice.model.Item;
import com.polaris.boxdeliveryservice.repository.BoxRepository;
import com.polaris.boxdeliveryservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxService {

    private final BoxRepository boxRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Box createBox(Box box) {
        if (boxRepository.existsByTxref(box.getTxref())) {
            throw new DuplicateTxrefException(box.getTxref());
        }
        box.setState(BoxState.IDLE);
        return boxRepository.save(box);
    }

    @Transactional
    public Box loadBox(Long boxId, List<ItemLoadRequest> itemRequests) {
        Box box = getBoxOrThrow(boxId);

        if (box.getState() != BoxState.IDLE) {
            throw new InvalidBoxStateException(boxId, box.getState().name(), BoxState.IDLE.name());
        }

        if (box.getBatteryCapacity() < Box.MIN_BATTERY_FOR_LOADING) {
            throw new InsufficientBatteryException(boxId, box.getBatteryCapacity());
        }

        box.setState(BoxState.LOADING);

        BigDecimal newItemsWeight = itemRequests.stream()
                .map(ItemLoadRequest::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWeight = box.totalLoadedWeight().add(newItemsWeight);

        if (totalWeight.compareTo(box.getWeightLimit()) > 0) {
            throw new WeightLimitExceededException(boxId, totalWeight, box.getWeightLimit());
        }

        for (ItemLoadRequest itemLoadRequest : itemRequests) {
            Item item = new Item();
            item.setName(itemLoadRequest.name());
            item.setWeight(itemLoadRequest.weight());
            item.setCode(itemLoadRequest.code());
            item.setBox(box);
            box.getItems().add(item);
        }

        box.setState(BoxState.LOADED);
        return boxRepository.save(box);
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsForBox(Long boxId) {
        getBoxOrThrow(boxId);
        return itemRepository.findBoxId(boxId);

    }

    @Transactional(readOnly = true)
    public List<Box> getAvailableBoxes() {
        return boxRepository.findByStateAndBatteryCapacityGreaterThanEqual(
                BoxState.IDLE, Box.MIN_BATTERY_FOR_LOADING
        );
    }

    @Transactional(readOnly = true)
    public Integer getBatteryLevel(Long  boxId) {
        Box box = getBoxOrThrow(boxId);
        return box.getBatteryCapacity();
    }

    private Box getBoxOrThrow(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxNotFoundException(boxId));
    }
}
