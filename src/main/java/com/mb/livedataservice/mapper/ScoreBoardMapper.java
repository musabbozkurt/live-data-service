package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.ApiScoreBoardRequest;
import com.mb.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.mb.livedataservice.api.response.ApiScoreBoardResponse;
import com.mb.livedataservice.data.model.ScoreBoard;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ScoreBoardMapper {

    ScoreBoard map(ApiScoreBoardRequest apiScoreBoardRequest);

    ApiScoreBoardResponse map(ScoreBoard scoreBoard);

    ScoreBoard map(ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest);

    default Page<ApiScoreBoardResponse> map(Page<ScoreBoard> scoreBoards) {
        return scoreBoards.map(this::map);
    }

}
