package com.example.bankingservice.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

@Setter
@Getter
public class PageRequestDto {
    private Integer pageNumber = 0;
    private Integer pageSize = 10;
    private Sort.Direction sort = Sort.Direction.ASC;
    private String sortByColumn = "id";

    public Pageable getPageable(PageRequestDto dto) {
        Integer page = Objects.nonNull(dto.getPageNumber()) ? dto.getPageNumber() : this.pageNumber;
        Integer size = Objects.nonNull(dto.getPageSize()) ? dto.getPageSize() : this.pageSize;
        Sort.Direction sort = Objects.nonNull(dto.getSort()) ? dto.getSort() : this.sort;
        String sortByColumn = Objects.nonNull(dto.getSortByColumn()) ? dto.getSortByColumn() : this.sortByColumn;

        return PageRequest.of(page, size, sort, sortByColumn);
    }
}
