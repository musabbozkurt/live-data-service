package com.sportradar.livedataservice.mapper;

import com.sportradar.livedataservice.api.request.ApiScoreBoardRequest;
import com.sportradar.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.sportradar.livedataservice.api.response.ApiScoreBoardResponse;
import com.sportradar.livedataservice.data.model.ScoreBoard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScoreBoardMapper {

    ScoreBoard map(ApiScoreBoardRequest apiScoreBoardRequest);

    ApiScoreBoardResponse map(ScoreBoard scoreBoard);

    ScoreBoard map(ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest);
}
