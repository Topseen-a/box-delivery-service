package com.polaris.boxdeliveryservice.service;

import com.polaris.boxdeliveryservice.dto.request.ItemLoadRequest;
import com.polaris.boxdeliveryservice.exception.BoxNotFoundException;
import com.polaris.boxdeliveryservice.exception.DuplicateTxrefException;
import com.polaris.boxdeliveryservice.exception.InsufficientBatteryException;
import com.polaris.boxdeliveryservice.exception.InvalidBoxStateException;
import com.polaris.boxdeliveryservice.exception.WeightLimitExceededException;
import com.polaris.boxdeliveryservice.model.Box;
import com.polaris.boxdeliveryservice.model.BoxState;
import com.polaris.boxdeliveryservice.model.Item;
import com.polaris.boxdeliveryservice.repository.BoxRepository;
import com.polaris.boxdeliveryservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BoxService boxService;

    private Box box;

    @BeforeEach
    public void setUp() {
        box = new Box();
        box.setId(1L);
        box.setTxref("BOX-0001");
        box.setWeightLimit(new BigDecimal("500.00"));
        box.setBatteryCapacity(80);
        box.setState(BoxState.IDLE);
    }

    @Test
    public void createBox_createsSuccessfully_whenTxrefIsUniqueTest() {
        when(boxRepository.existsByTxref("BOX-0001")).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        Box result = boxService.createBox(box);

        assertThat(result.getState()).isEqualTo(BoxState.IDLE);
        verify(boxRepository).save(box);
    }

    @Test
    public void createBox_throwsDuplicateTxrefException_whenTxrefAlreadyExistsTest() {
        when(boxRepository.existsByTxref("BOX-0001")).thenReturn(true);

        assertThatThrownBy(() -> boxService.createBox(box))
                .isInstanceOf(DuplicateTxrefException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    public void createBox_forcesStateToIdle_regardlessOfIncomingStateTest() {
        box.setState(BoxState.LOADED);
        when(boxRepository.existsByTxref("BOX-0001")).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        Box result = boxService.createBox(box);

        assertThat(result.getState()).isEqualTo(BoxState.IDLE);
    }

    @Test
    public void loadBox_loadsSuccessfully_whenIdleAndBatteryAndWeightAreFineTest() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Water_Bottle", new BigDecimal("100.00"), "WTR_001")
        );

        Box result = boxService.loadBox(1L, requests);

        assertThat(result.getState()).isEqualTo(BoxState.LOADED);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.totalLoadedWeight()).isEqualByComparingTo("100.00");
    }

    @Test
    public void loadBox_throwsBoxNotFoundException_whenBoxDoesNotExistTest() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.loadBox(99L, List.of()))
                .isInstanceOf(BoxNotFoundException.class);
    }

    @Test
    public void loadBox_throwsInvalidBoxStateException_whenBoxIsNotIdleTest() {
        box.setState(BoxState.LOADED);
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Item", new BigDecimal("10.00"), "ITM_001")
        );

        assertThatThrownBy(() -> boxService.loadBox(1L, requests))
                .isInstanceOf(InvalidBoxStateException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    public void loadBox_throwsInsufficientBatteryException_whenBatteryBelow25PercentTest() {
        box.setBatteryCapacity(24);
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Item", new BigDecimal("10.00"), "ITM_001")
        );

        assertThatThrownBy(() -> boxService.loadBox(1L, requests))
                .isInstanceOf(InsufficientBatteryException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    public void loadBox_succeeds_whenBatteryIsExactlyAt25PercentBoundaryTest() {
        box.setBatteryCapacity(25);
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Item", new BigDecimal("10.00"), "ITM_001")
        );

        Box result = boxService.loadBox(1L, requests);

        assertThat(result.getState()).isEqualTo(BoxState.LOADED);
    }

    @Test
    public void loadBox_throwsWeightLimitExceededException_whenTotalWeightExceedsLimitTest() {
        box.setWeightLimit(new BigDecimal("100.00"));
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Heavy_Item", new BigDecimal("100.01"), "HVY_001")
        );

        assertThatThrownBy(() -> boxService.loadBox(1L, requests))
                .isInstanceOf(WeightLimitExceededException.class);

        verify(boxRepository, never()).save(any());
    }

    @Test
    public void loadBox_succeeds_whenTotalWeightIsExactlyAtLimitBoundaryTest() {
        box.setWeightLimit(new BigDecimal("100.00"));
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Exact_Item", new BigDecimal("100.00"), "EXA_001")
        );

        Box result = boxService.loadBox(1L, requests);

        assertThat(result.getState()).isEqualTo(BoxState.LOADED);
        assertThat(result.totalLoadedWeight()).isEqualByComparingTo("100.00");
    }

    @Test
    public void loadBox_throwsWeightLimitExceededException_whenSumOfMultipleItemsExceedsLimitTest() {
        box.setWeightLimit(new BigDecimal("100.00"));
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("Item_A", new BigDecimal("60.00"), "ITA_001"),
                new ItemLoadRequest("Item_B", new BigDecimal("50.00"), "ITB_001")
        );

        assertThatThrownBy(() -> boxService.loadBox(1L, requests))
                .isInstanceOf(WeightLimitExceededException.class);
    }

    @Test
    public void loadBox_accountsForAlreadyLoadedItems_whenCheckingWeightLimitTest() {
        box.setWeightLimit(new BigDecimal("100.00"));
        Item existing = new Item();
        existing.setName("Existing_Item");
        existing.setWeight(new BigDecimal("70.00"));
        existing.setCode("EXI_001");
        existing.setBox(box);
        box.getItems().add(existing);

        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        List<ItemLoadRequest> requests = List.of(
                new ItemLoadRequest("New_Item", new BigDecimal("40.00"), "NEW_001")
        );

        assertThatThrownBy(() -> boxService.loadBox(1L, requests))
                .isInstanceOf(WeightLimitExceededException.class);
    }

    @Test
    public void loadBox_loadsEmptyItemList_withoutErrorTest() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        when(boxRepository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));

        Box result = boxService.loadBox(1L, List.of());

        assertThat(result.getState()).isEqualTo(BoxState.LOADED);
        assertThat(result.getItems()).isEmpty();
    }


    @Test
    public void getItemsForBox_returnsItems_whenBoxExistsTest() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));
        Item item = new Item();
        item.setName("Item");
        item.setWeight(new BigDecimal("10.00"));
        item.setCode("ITM_001");
        when(itemRepository.findByBoxId(1L)).thenReturn(List.of(item));

        List<Item> result = boxService.getItemsForBox(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    public void getItemsForBox_throwsBoxNotFoundException_whenBoxDoesNotExistTest() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.getItemsForBox(99L))
                .isInstanceOf(BoxNotFoundException.class);
    }

    @Test
    public void getAvailableBoxes_returnsOnlyIdleBoxesWithSufficientBatteryTest() {
        when(boxRepository.findByStateAndBatteryCapacityGreaterThanEqual(BoxState.IDLE, 25))
                .thenReturn(List.of(box));

        List<Box> result = boxService.getAvailableBoxes();

        assertThat(result).containsExactly(box);
    }

    @Test
    public void getAvailableBoxes_returnsEmptyList_whenNoBoxesQualifyTest() {
        when(boxRepository.findByStateAndBatteryCapacityGreaterThanEqual(BoxState.IDLE, 25))
                .thenReturn(List.of());

        List<Box> result = boxService.getAvailableBoxes();

        assertThat(result).isEmpty();
    }

    @Test
    public void getBatteryLevel_returnsBatteryLevel_whenBoxExistsTest() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(box));

        Integer battery = boxService.getBatteryLevel(1L);

        assertThat(battery).isEqualTo(80);
    }

    @Test
    public void getBatteryLevel_throwsBoxNotFoundException_whenBoxDoesNotExistTest() {
        when(boxRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.getBatteryLevel(99L))
                .isInstanceOf(BoxNotFoundException.class);
    }
}