package com.coachmybody.routine.interfaces;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coachmybody.common.dto.HeaderDto;
import com.coachmybody.common.dto.ProblemResponse;
import com.coachmybody.routine.application.RoutineService;
import com.coachmybody.routine.interfaces.dto.RoutineCreateRequest;
import com.coachmybody.routine.interfaces.dto.RoutineDeleteRequest;
import com.coachmybody.routine.interfaces.dto.RoutineDetailResponse;
import com.coachmybody.routine.interfaces.dto.RoutineExerciseAddRequest;
import com.coachmybody.routine.interfaces.dto.RoutineExerciseDeleteRequest;
import com.coachmybody.routine.interfaces.dto.RoutineExerciseOrderRequest;
import com.coachmybody.routine.interfaces.dto.RoutineExerciseUpdateRequest;
import com.coachmybody.routine.interfaces.dto.RoutineSimpleResponse;
import com.coachmybody.user.application.UserService;
import com.coachmybody.user.domain.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = {"Routine"})
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class RoutineController {
	private final RoutineService routineService;
	private final UserService userService;

	@ApiOperation("루틴 생성")
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "루틴 생성 성공"),
		@ApiResponse(code = 401, message = "유효하지 않은 토큰", response = ProblemResponse.class),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/routines")
	public void create(@RequestHeader HttpHeaders headers,
		@RequestBody @Valid RoutineCreateRequest request) {
		HeaderDto headerDto = HeaderDto.of(headers);
		User user = userService.findByToken(headerDto.getToken());

		routineService.create(request.getTitle(), user);
	}

	@ApiOperation("나의 루틴 리스트 조회")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "나의 루틴 리스트 조회 성공")
	})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/users/routines")
	public List<RoutineSimpleResponse> myRoutines(@RequestHeader HttpHeaders headers,
		@RequestParam(value = "hasExercise", required = false) boolean hasExercise) {
		HeaderDto headerDto = HeaderDto.of(headers);
		User user = userService.findByToken(headerDto.getToken());

		return routineService.findMyRoutines(user, hasExercise);
	}

	@ApiOperation("루틴 상세 조회")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 상세 조회 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/routines/{routineId}")
	public ResponseEntity<RoutineDetailResponse> findRoutineById(@RequestHeader HttpHeaders headers,
		@PathVariable("routineId") Long routineId) {
		HeaderDto header = HeaderDto.of(headers);
		User user = userService.findByToken(header.getToken());

		return ResponseEntity.ok(routineService.findRoutineById(routineId, user.getId()));
	}

	@ApiOperation("루틴 삭제")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 삭제 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class),
		@ApiResponse(code = 406, message = "접근할 수 없는 루틴", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("/routines")
	public void delete(@RequestHeader HttpHeaders headers,
		@RequestBody @Valid RoutineDeleteRequest request) {
		HeaderDto headerDto = HeaderDto.of(headers);
		User user = userService.findByToken(headerDto.getToken());

		routineService.deleteByIds(request.getRoutineIds());
	}

	@ApiOperation("루틴 운동 추가")
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "루틴 운동 추가 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/routines/{routineId}/exercises")
	public void addRoutineExercises(@RequestHeader HttpHeaders headers,
		@PathVariable("routineId") Long routineId,
		@RequestBody @Valid RoutineExerciseAddRequest request) {
		routineService.addExercises(routineId, request.getExerciseIds());
	}

	@ApiOperation("루틴 이름 변경")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴명 수정 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping("/routines/{routineId}/title")
	public void updateTitle(@RequestHeader HttpHeaders headers,
		@PathVariable("routineId") Long routineId,
		@RequestParam String newTitle) {
		routineService.updateTitle(routineId, newTitle);
	}

	@ApiOperation("루틴 운동 삭제")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 운동 삭제 성공"),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("/routines/exercises")
	public void deleteRoutineExercises(@RequestHeader HttpHeaders headers,
		@RequestBody @Valid RoutineExerciseDeleteRequest request) {
		routineService.deleteExercises(request.getRoutineExerciseIds());
	}

	@ApiOperation("루틴 운동 순서 편집")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 운동 순서 편집 성공"),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping("/routines/exercises/order")
	public void updateOrder(@RequestHeader HttpHeaders headers,
		@RequestBody @Valid RoutineExerciseOrderRequest request) {
		routineService.updateRoutineExerciseOrder(request.getRoutineExerciseIds());
	}

	@ApiOperation("루틴 운동 횟수 수정")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 운동 편집 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 운동", response = ProblemResponse.class),
		@ApiResponse(code = 400, message = "요청 프로퍼티 오류", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping("/routines/exercises/{routineExerciseId}")
	public void updateRoutineExercise(@RequestHeader HttpHeaders headers,
		@PathVariable("routineExerciseId") Long routineExerciseId,
		@RequestBody RoutineExerciseUpdateRequest request) {
		routineService.updateRoutineExercise(routineExerciseId, request);
	}

	@ApiOperation("루틴 북마크 등록 / 해제")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 북마크 등록 / 해제 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/routines/{routineId}/bookmark")
	public boolean bookmarkRoutine(@RequestHeader HttpHeaders headers,
		@PathVariable("routineId") Long routineId) {
		HeaderDto header = HeaderDto.of(headers);
		User user = userService.findByToken(header.getToken());
		return routineService.bookmark(routineId, user.getId());
	}

	@ApiOperation("루틴 북마크 해제")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "루틴 북마크 해제 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 루틴", response = ProblemResponse.class)
	})
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("/routines/bookmark")
	public void deleteBookmark(@RequestHeader HttpHeaders headers,
		@RequestBody RoutineDeleteRequest request) {
		HeaderDto header = HeaderDto.of(headers);
		User user = userService.findByToken(header.getToken());
		routineService.deleteBookmark(user, request.getRoutineIds());
	}
}