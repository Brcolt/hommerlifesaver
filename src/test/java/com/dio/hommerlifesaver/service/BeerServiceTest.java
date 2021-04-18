package com.dio.hommerlifesaver.service;


import com.dio.hommerlifesaver.builder.BeerDTOBuilder;
import com.dio.hommerlifesaver.dto.BeerDTO;
import com.dio.hommerlifesaver.entity.Beer;
import com.dio.hommerlifesaver.exception.BeerAlreadyRegisteredException;
import com.dio.hommerlifesaver.exception.BeerNotFoundException;
import com.dio.hommerlifesaver.exception.BeerStockExceededException;
import com.dio.hommerlifesaver.mapper.BeerMapper;
import com.dio.hommerlifesaver.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {

        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        // then
        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

    }

    @Test
    void whenAlreadyRegistredBeerInformedThanAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {

        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        // then
        assertThrows(BeerAlreadyRegisteredException.class, ()-> beerService.createBeer(expectedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {

        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBear = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.of(expectedFoundBear));

        //then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));

    }

    @Test
    void whenNotRegistredBeerNameIsGivenThanThrowAnException() throws BeerNotFoundException {

        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));

    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {

        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBear = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBear));

        //then
        List<BeerDTO> foundBeerDTO = beerService.listAll();

        assertThat(foundBeerDTO, is(not(empty())));
        assertThat(foundBeerDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAEmptyListOfBeers() {

        //Given

        //when
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
        List<BeerDTO> foundBeerDTO = beerService.listAll();

        assertThat(foundBeerDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithCalledIdThenABeerMustBeExcluded() throws BeerNotFoundException {
        // Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBear = beerMapper.toModel(expectedFoundBeerDTO);

        // when

        when(beerRepository.findById(expectedFoundBeerDTO.getId())).thenReturn(Optional.of(expectedFoundBear));
        doNothing().when(beerRepository).deleteById(expectedFoundBeerDTO.getId());

        // then
        beerService.deleteById(expectedFoundBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedFoundBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedFoundBeerDTO.getId());
    }

    @Test
    void whenIncrementIsCalledthenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {

        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);


        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        //then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;

        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;

        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenncrementIsCalledWithInvalidIdThenThrowException() {

        int quantityToIncrement = 10;

        when(beerRepository.findById( INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }


}
