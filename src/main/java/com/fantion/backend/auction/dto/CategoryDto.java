package com.fantion.backend.auction.dto;

import com.fantion.backend.type.CategoryType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
  String title;
  String category;
}
