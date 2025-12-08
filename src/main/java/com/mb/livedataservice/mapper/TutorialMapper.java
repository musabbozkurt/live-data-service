package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.filter.ApiTutorialFilter;
import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.data.filter.TutorialFilter;
import com.mb.livedataservice.data.model.Tutorial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TutorialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    Tutorial map(ApiTutorialRequest apiTutorialRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    Tutorial map(ApiTutorialUpdateRequest apiTutorialRequest);

    ApiTutorialResponse map(Tutorial tutorial);

    List<ApiTutorialResponse> map(List<Tutorial> tutorial);

    TutorialFilter map(ApiTutorialFilter apiTutorialFilter);
}
