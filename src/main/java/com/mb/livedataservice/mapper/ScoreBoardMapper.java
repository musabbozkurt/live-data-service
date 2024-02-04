package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.ApiScoreBoardRequest;
import com.mb.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.mb.livedataservice.api.response.ApiScoreBoardResponse;
import com.mb.livedataservice.data.model.ScoreBoard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ScoreBoardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    ScoreBoard map(ApiScoreBoardRequest apiScoreBoardRequest);

    ApiScoreBoardResponse map(ScoreBoard scoreBoard);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    ScoreBoard map(ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest);

    default Page<ApiScoreBoardResponse> map(Page<ScoreBoard> scoreBoards) {
        return scoreBoards.map(this::map);
    }
}
