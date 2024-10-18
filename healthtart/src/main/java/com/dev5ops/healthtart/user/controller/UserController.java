package com.dev5ops.healthtart.user.controller;

import com.dev5ops.healthtart.common.exception.CommonException;
import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.user.domain.dto.*;
import com.dev5ops.healthtart.security.JwtUtil;
import com.dev5ops.healthtart.user.domain.vo.EmailVerificationVO;
import com.dev5ops.healthtart.user.domain.vo.ResponseEmailMessageVO;
import com.dev5ops.healthtart.user.domain.vo.request.RegisterGymPerUserRequest;
import com.dev5ops.healthtart.user.domain.vo.request.RequestEditPasswordVO;
import com.dev5ops.healthtart.user.domain.vo.request.RequestInsertUserVO;
import com.dev5ops.healthtart.user.domain.vo.request.RequestOauth2VO;
import com.dev5ops.healthtart.user.domain.vo.response.ResponseEditPasswordVO;
import com.dev5ops.healthtart.user.domain.vo.request.RequestResetPasswordVO;
import com.dev5ops.healthtart.user.domain.vo.response.ResponseFindUserVO;
import com.dev5ops.healthtart.user.domain.vo.response.ResponseInsertUserVO;
import com.dev5ops.healthtart.user.domain.vo.response.ResponseMypageVO;
import com.dev5ops.healthtart.user.service.EmailVerificationService;
import com.dev5ops.healthtart.user.service.UserService;
//import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {

    private JwtUtil jwtUtil;
    private Environment env;
    private ModelMapper modelMapper;
    private UserService userService;
    private EmailVerificationService emailVerificationService;

    @Autowired
    public UserController(JwtUtil jwtUtil, Environment env, ModelMapper modelMapper, UserService userService, EmailVerificationService emailVerificationService) {
        this.jwtUtil = jwtUtil;
        this.env = env;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }

    //설명. 이메일 전송 API (회원가입전 실행)
    @PostMapping("/verification-email")
    public ResponseEmailDTO<?> sendVerificationEmail(@RequestBody @Validated EmailVerificationVO request) {

        // 이메일 중복체크
        UserDTO userByEmail = userService.findUserByEmail(request.getEmail());
        log.info("userByEmail: {}", userByEmail);
        if(userByEmail != null){
            return ResponseEmailDTO.fail(new CommonException(StatusEnum.EMAIL_DUPLICATE));
        }

        // 이메일로 인증번호 전송
        try {
            emailVerificationService.sendVerificationEmail(request.getEmail());

            ResponseEmailMessageVO responseEmailMessageVO =new ResponseEmailMessageVO();
            responseEmailMessageVO.setMessage("인증 코드가 이메일로 전송되었습니다.");
            return ResponseEmailDTO.ok(responseEmailMessageVO);
        } catch (Exception e) {
            return ResponseEmailDTO.fail(new CommonException(StatusEnum.INTERNAL_SERVER_ERROR));
        }
    }

    //설명. 이메일 전송 API -> 비밀번호 재설정시 사용
    @PostMapping("/verification-email/password")
    public ResponseEmailDTO<?> sendVerificationEmailPassword(@RequestBody @Validated EmailVerificationVO request) {

        // 이메일 중복체크
        UserDTO userByEmail = userService.findUserByEmail(request.getEmail());
        if(userByEmail == null){
            return ResponseEmailDTO.fail(new CommonException(StatusEnum.EMAIL_DUPLICATE));
        }

        // 이메일로 인증번호 전송
        try {
            emailVerificationService.sendVerificationEmail(request.getEmail());

            ResponseEmailMessageVO responseEmailMessageVO =new ResponseEmailMessageVO();
            responseEmailMessageVO.setMessage("인증 코드가 이메일로 전송되었습니다.");
            return ResponseEmailDTO.ok(responseEmailMessageVO);
        } catch (Exception e) {
            return ResponseEmailDTO.fail(new CommonException(StatusEnum.INTERNAL_SERVER_ERROR));
        }
    }

    //설명. 이메일 인증번호 검증 API (회원가입전 실행)
    @PostMapping("/verification-email/confirmation")
    public ResponseEmailDTO<?> verifyEmail(@RequestBody @Validated EmailVerificationVO request) {
        boolean isVerified = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        ResponseEmailMessageVO responseEmailMessageVO =new ResponseEmailMessageVO();
        responseEmailMessageVO.setMessage("이메일 인증이 완료되었습니다.");
        if (isVerified) {
            return ResponseEmailDTO.ok(responseEmailMessageVO);
        } else {
            return ResponseEmailDTO.fail(new CommonException(StatusEnum.INVALID_VERIFICATION_CODE));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseInsertUserVO> insertUser(@RequestBody RequestInsertUserVO request) {
        if (request.getUserType() == null) {
            request.setUserType("MEMBER");
        }

        ResponseInsertUserVO responseUser =
                modelMapper.map(userService.signUpUser(request), ResponseInsertUserVO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/mypage")
    public ResponseEntity<ResponseMypageVO> getMypageInfo(){

        ResponseMypageDTO mypageInfo = userService.getMypageInfo();

        ResponseMypageVO response = modelMapper.map(mypageInfo, ResponseMypageVO.class);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/mypage/edit/password")
    public ResponseEntity<ResponseEditPasswordVO> editPassword(@RequestBody RequestEditPasswordVO request) {
        EditPasswordDTO editPasswordDTO = modelMapper.map(request, EditPasswordDTO.class);

        userService.editPassword(editPasswordDTO);

        ResponseEditPasswordVO response = new ResponseEditPasswordVO("비밀번호가 성공적으로 변경되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 회원 전체 조회
    @GetMapping
    public ResponseEntity<List<ResponseFindUserVO>> getAllUsers() {
        // service에서 DTO 형태로 찾은 애를 VO로 바꿔야한다
        List<UserDTO> userDTOList = userService.findAllUsers();
        List<ResponseFindUserVO> userVOList = userDTOList.stream()
                .map(userDTO -> modelMapper.map(userDTO, ResponseFindUserVO.class))
                .collect(Collectors.toList());

                return new ResponseEntity<>(userVOList, HttpStatus.OK);
    }

    // 이메일로 회원 정보 조회
    @GetMapping("/email/{email}")
    public ResponseEntity<ResponseFindUserVO> findUserByEmail(@PathVariable String email) {
        UserDTO userDTO = userService.findUserByEmail(email);
        ResponseFindUserVO responseFindUserVO = modelMapper.map(userDTO, ResponseFindUserVO.class);

        return new ResponseEntity<>(responseFindUserVO, HttpStatus.OK);
    }

    // 회원 코드로 회원 정보 조회
    @GetMapping("/usercode/{userCode}")
    public ResponseEntity<ResponseFindUserVO> findUserById(@PathVariable String userCode) {
        UserDTO userDTO = userService.findById(userCode);
        ResponseFindUserVO responseFindUserVO = modelMapper.map(userDTO, ResponseFindUserVO.class);

        return new ResponseEntity<>(responseFindUserVO, HttpStatus.OK);
    }

    // 회원 탈퇴
    @PatchMapping("/delete/{userCode}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userCode) {
        userService.deleteUser(userCode);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/nickname/check") // users/nickname/check
    public ResponseEntity<Map<String, Boolean>> checkDuplicateNickname(@RequestParam String userNickname){

        Boolean isDuplicate = userService.checkDuplicateNickname(userNickname);

        // JSON 형태로 반환할 Map 생성
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);

        return ResponseEntity.status(HttpStatus.OK).body(response); // true이면 사용 불가
    }

    // 여기서 회원가입 시킬 예정
    @PostMapping("/oauth2")
    public ResponseEntity<String> saveOauth2User(@RequestBody RequestOauth2VO requestOauth2VO){

        // userCode는 여기서 생성해서 저장하자.
        // member type도 여기서
        // flag도 여기서
        // 이것들 모두 oauth 로그인 과정에서 저장되어서 저걸 안해도 됨.
        userService.saveOauth2User(requestOauth2VO);

        return ResponseEntity.status(HttpStatus.OK).body("잘 저장했습니다");
    }

    @PostMapping("/register-gym")
    public ResponseEntity<String> registerGym(@RequestBody RegisterGymPerUserRequest registerGymRequest) {
        try {
            userService.updateUserGym(registerGymRequest);
            return ResponseEntity.ok("헬스장 등록이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("헬스장 등록에 실패했습니다.");
        }
    }

    @PostMapping("/remove-gym")
    public ResponseEntity<String> removeGym(@RequestBody RegisterGymPerUserRequest registerGymRequest) {
        try {
            userService.deleteUserGym(registerGymRequest);
            return ResponseEntity.ok("헬스장이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("헬스장 삭제에 실패했습니다.");
        }
    }
    @PostMapping("/password")
    public ResponseEntity<String> resetPassword(@RequestBody RequestResetPasswordVO request) {

        userService.resetPassword(request);

        return ResponseEntity.status(HttpStatus.OK).body("잘 수정 됐습니다.");
    }


}
