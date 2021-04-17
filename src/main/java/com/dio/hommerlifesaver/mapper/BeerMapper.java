package com.dio.hommerlifesaver.mapper;


import com.dio.hommerlifesaver.dto.BeerDTO;
import com.dio.hommerlifesaver.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {
    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}
