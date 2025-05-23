package com.ProjectGraduation.offers.autoOffer.dto;

import com.ProjectGraduation.offers.autoOffer.utils.AutoOfferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoOfferRequest {

    private AutoOfferType type;
    private String name;
    private Double discount;
    private Double maxDiscount;
    private Double fixedPrice;
    private Double minOrderAmount;
    private Integer buyQuantity;
    private Integer getQuantity;
    private Integer requiredPoints;
    private Date startDate;
    private Date endDate;
    private Boolean active;
}