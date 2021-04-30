package com.coachmybody.record.interfaces;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coachmybody.common.dto.HeaderDto;
import com.coachmybody.common.dto.ProblemResponse;
import com.coachmybody.record.application.RecordService;
import com.coachmybody.record.interfaces.dto.RecordCreateRequest;
import com.coachmybody.user.application.UserService;
import com.coachmybody.user.domain.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = {"Record"})
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class RecordController {
	private final RecordService recordService;
	private final UserService userService;

	@ApiOperation("운동 기록")
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "운동 기록 성공"),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/records")
	public void create(@RequestHeader HttpHeaders headers,
		@RequestBody @Valid RecordCreateRequest request) {
		HeaderDto header = HeaderDto.of(headers);

		User user = userService.findByToken(header.getToken());

		recordService.create(request, user);
	}
}