package com.fantion.backend.auction.dto;

import com.fantion.backend.type.CategoryType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
  List<String> titleList;
  List<String> categoryList;
}
