package com.coachmybody.user.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coachmybody.common.exception.DuplicatedEntityException;
import com.coachmybody.common.exception.InvalidAccessTokenException;
import com.coachmybody.common.exception.InvalidRefreshTokenException;
import com.coachmybody.common.exception.NotFoundEntityException;
import com.coachmybody.routine.domain.Routine;
import com.coachmybody.routine.domain.repository.RoutineBookmarkQueryRepository;
import com.coachmybody.user.domain.User;
import com.coachmybody.user.domain.UserAuth;
import com.coachmybody.user.domain.repository.UserAuthRepository;
import com.coachmybody.user.domain.repository.UserRepository;
import com.coachmybody.user.interfaces.dto.AuthResponse;
import com.coachmybody.user.interfaces.dto.RegisterRequest;
import com.coachmybody.user.interfaces.dto.UserResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserAuthRepository userAuthRepository;
	private final RoutineBookmarkQueryRepository routineBookmarkQueryRepository;

	public void register(RegisterRequest request) {
		if (userRepository.findBySocialId(request.getSocialId()).isPresent()) {
			throw new DuplicatedEntityException();
		}

		User user = User.of(request);

		userRepository.save(user);
	}

	public AuthResponse login(@NonNull final String socialId) {
		User user = userRepository.findBySocialId(socialId)
			.orElseThrow(NotFoundEntityException::new);

		UUID userId = user.getId();

		UserAuth newAuth = userAuthRepository.findByUserId(userId)
			.orElseGet(() -> UserAuth.newAuth(userId));
		newAuth.refresh();

		userAuthRepository.save(newAuth);

		return AuthResponse.of(newAuth);
	}

	public boolean isValidToken(@NonNull final String accessToken) {
		Optional<UserAuth> optionalUserAuth = userAuthRepository.findByAccessToken(accessToken);

		if (optionalUserAuth.isEmpty()) {
			return false;
		}

		UserAuth userAuth = optionalUserAuth.get();

		Instant now = Instant.now();

		if (userAuth.getExpiredAt().isBefore(now)) {
			return false;
		}

		return true;
	}

	public AuthResponse refresh(@NonNull final String refreshToken) {
		UserAuth userAuth = userAuthRepository.findByRefreshToken(refreshToken)
			.orElseThrow(InvalidRefreshTokenException::new);

		userAuth.refresh();

		userAuthRepository.save(userAuth);

		return AuthResponse.of(userAuth);
	}

	public User findByToken(@NonNull final String token) {
		UserAuth userAuth = userAuthRepository.findByAccessToken(token)
			.orElseThrow(InvalidAccessTokenException::new);

		return userRepository.findById(userAuth.getUserId())
			.orElseThrow(NotFoundEntityException::new);
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getUsers() {
		return userRepository.findAll()
			.stream()
			.map(UserResponse::of)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<Routine> findBookmarkRoutines(User user, Pageable pageable) {
		return routineBookmarkQueryRepository.findBookmarkRoutines(user, pageable);
	}
}
