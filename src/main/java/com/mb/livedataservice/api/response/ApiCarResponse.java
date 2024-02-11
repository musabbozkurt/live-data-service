package com.mb.livedataservice.api.response;

import java.util.List;

public record ApiCarResponse(String id, String model, Integer yearOfManufacture, String brand,
                             List<ApiOwnerResponse> owners) {
}
