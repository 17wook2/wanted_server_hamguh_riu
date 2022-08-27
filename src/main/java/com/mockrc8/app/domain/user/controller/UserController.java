package com.mockrc8.app.domain.user.controller;

import com.mockrc8.app.domain.employment.vo.ReducedEmploymentVo;
import com.mockrc8.app.domain.user.dto.*;
import com.mockrc8.app.domain.user.service.UserService;
import com.mockrc8.app.domain.user.vo.UserInterestTagVo;
import com.mockrc8.app.domain.user.vo.UserProfileVo;
import com.mockrc8.app.global.config.BaseResponse;
import com.mockrc8.app.global.error.ErrorCode;
import com.mockrc8.app.global.error.exception.User.UserNotFoundException;
import com.mockrc8.app.global.oAuth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mockrc8.app.global.util.InfinityScroll.getScrollCount;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserRegisterResponseDto> register(@RequestBody @Valid final UserRegisterRequestDto userRegisterRequestDto){
        return userService.registerUser(userRegisterRequestDto);
    }

    @GetMapping("/email-validation")
    public ResponseEntity<EmailValidationResponseDto> validateEmail(@RequestParam String email){
        return userService.validateEmail(email);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto userLoginRequestDto){
        return userService.loginUser(userLoginRequestDto);
    }

    @GetMapping("/login/{provider}")
    public ResponseEntity<Object> loginByThirdParty(@RequestParam String code, @PathVariable String provider){
        return userService.loginByThirdParty(code,provider);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getUserProfile(@CurrentUser String userEmail, @PathVariable Long userId){

        if(userEmail == null){
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        // 유저가 존재하는지, 유저가 일치하는지 검증
        userService.checkUserMatch(userEmail, userId);

        Map<String, Object> result = new HashMap<>();

        //유저 정보 가져오기
        UserProfileVo userProfileVo = userService.getUserProfile(userId);
        result.put("userProfile", userProfileVo);

        //유저의 관심 태그 가져오기, 최대 2개까지만 보여준다.
        List<UserInterestTagVo> userInterestTagVoList = userService.getUserInterestTagVoByUserId(userId, 2);
        result.put("userInterestTagList", userInterestTagVoList);

        // 유저가 북마크한 채용 목록 가져오기 (최대 4개)
        List<ReducedEmploymentVo> userEmploymentBookmarkVoList = userService.getUserEmploymentBookmarkVoList(userId, 4, null);
        result.put("userEmploymentBookmarkList", userEmploymentBookmarkVoList);

        // 유저가 좋아요한 채용 목록 가져오기 (최대 4개)
        List<ReducedEmploymentVo> userEmploymentLikeVoList = userService.getUserEmploymentLikeVoList(userId, 4, null);
        result.put("userEmploymentLikeList", userEmploymentLikeVoList);

        BaseResponse<Map<String, Object>> response = new BaseResponse<>(result);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}/bookmark")
    public ResponseEntity<Object> getUserEmploymentBookmarkVoList(HttpServletRequest request,
                                                                  @CurrentUser String userEmail,
                                                                  @PathVariable Long userId){

        if(userEmail == null){
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        userService.checkUserMatch(userEmail, userId);

        Integer scrollCount = getScrollCount(request);

        List<ReducedEmploymentVo> reducedEmploymentVoList = userService.getUserEmploymentBookmarkVoList(userId, null, scrollCount);

        BaseResponse<List<ReducedEmploymentVo>> response = new BaseResponse<>(reducedEmploymentVoList);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}/like")
    public ResponseEntity<Object> getUserEmploymentLikeVoList(HttpServletRequest request,
                                                              @CurrentUser String userEmail,
                                                              @PathVariable Long userId){

        if(userEmail == null){
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        userService.checkUserMatch(userEmail, userId);

        Integer scrollCount = getScrollCount(request);

        List<ReducedEmploymentVo> reducedEmploymentVoList = userService.getUserEmploymentLikeVoList(userId, null, scrollCount);

        BaseResponse<List<ReducedEmploymentVo>> response = new BaseResponse<>(reducedEmploymentVoList);
        return ResponseEntity.ok(response);
    }






}
