package com.mb.livedataservice.api.request;

import java.util.List;

public record ApiCarRequest(String model, Integer yearOfManufacture, String brand, List<ApiOwnerRequest> owners) {

}
