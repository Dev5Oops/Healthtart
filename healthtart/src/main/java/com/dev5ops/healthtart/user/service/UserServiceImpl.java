package com.dev5ops.healthtart.user.service;

import com.dev5ops.healthtart.common.exception.CommonException;
import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.gym.domain.entity.Gym;
import com.dev5ops.healthtart.gym.repository.GymRepository;
import com.dev5ops.healthtart.security.JwtUtil;
import com.dev5ops.healthtart.user.domain.CustomUserDetails;
import com.dev5ops.healthtart.user.domain.dto.*;
import com.dev5ops.healthtart.user.domain.entity.UserEntity;
import com.dev5ops.healthtart.user.domain.vo.request.RegisterGymPerUserRequest;
import com.dev5ops.healthtart.user.domain.vo.request.RequestInsertUserVO;
import com.dev5ops.healthtart.user.domain.vo.request.RequestOauth2VO;
import com.dev5ops.healthtart.user.domain.vo.request.RequestResetPasswordVO;
import com.dev5ops.healthtart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private GymRepository gymRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;  // StringRedisTemplate 사용
    private final RestTemplate restTemplate;

    @Autowired
    public UserServiceImpl(GymRepository gymRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper, UserRepository userRepository, JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate, RestTemplate restTemplate) {
        this.gymRepository = gymRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
        this.restTemplate = restTemplate;
    }

    // 회원가입
    @Override
    public ResponseInsertUserDTO signUpUser(RequestInsertUserVO request) {

        // Redis에서 이메일 인증 여부 확인
        String emailVerificationStatus = stringRedisTemplate.opsForValue().get(request.getUserEmail());

        if (!"True".equals(emailVerificationStatus)) {
            log.error("이메일 인증이 완료되지 않았습니다: {}", request.getUserEmail());
            throw new CommonException(StatusEnum.EMAIL_VERIFICATION_REQUIRED); // 이메일 인증이 필요하다는 커스텀 예외 던지기
        }

        if (userRepository.findByUserEmail(request.getUserEmail()) != null) throw new CommonException(StatusEnum.EMAIL_DUPLICATE);

        String curDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString();

        String userCode = curDate + "-" + uuid;

        request.changePwd(bCryptPasswordEncoder.encode(request.getUserPassword()));

        UserDTO userDTO = UserDTO.builder()
                .userCode(userCode)
                .userType(request.getUserType())
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .userPassword(request.getUserPassword())
                .userPhone(request.getUserPhone())
                .userNickname(request.getUserNickname())
                .userAddress(request.getUserAddress())
                .userFlag(true)
                .userGender("M")
                .userHeight(request.getUserHeight())
                .userWeight(request.getUserWeight())
                .userAge(request.getUserAge())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserEntity insertUser = modelMapper.map(userDTO, UserEntity.class);

        userRepository.save(insertUser);

        ResponseInsertUserDTO insertUserDTO= modelMapper.map(insertUser, ResponseInsertUserDTO.class);

        return insertUserDTO;
    }

    // 회원 전체 조회
    @Override
    public List<UserDTO> findAllUsers() {
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();

        for(UserEntity user : allUsers) {
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    // 이메일로 회원 조회
    @Override
    public UserDTO findUserByEmail(String userEmail) {
        UserEntity findUser = userRepository.findByUserEmail(userEmail);

        return modelMapper.map(findUser, UserDTO.class);
    }

    @Override
    public UserDTO findById(String userCode) {
        UserEntity user = userRepository.findById(userCode).get();

        return modelMapper.map(user, UserDTO.class);
    }


    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUserEmail(userEmail);

        // 사용자가 없을 경우 예외 발생
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + userEmail);
        }

        // 사용자의 권한 설정
        // ADMIN인 경우 MEMBER 속성도 가짐.
        List<GrantedAuthority> roles = new ArrayList<>();
        if("MEMBER".equals(user.getUserType().name())){
            roles.add(new SimpleGrantedAuthority("MEMBER"));
        }else{
            roles.add(new SimpleGrantedAuthority("MEMBER"));
            roles.add(new SimpleGrantedAuthority("ADMIN"));
        }

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        if(userDTO.getUserFlag() == false) throw new UsernameNotFoundException("User not found with email: " + userEmail);
        return new CustomUserDetails(
                                    userDTO,
                                    roles,
                                    true,
                                    true,
                                    true,
                                    user.getUserFlag());
    }

    @Override
    public void deleteUser(String userCode) {
        UserEntity user = userRepository.findById(userCode).orElseThrow(() -> new CommonException(StatusEnum.USER_NOT_FOUND));

        user.removeRequest(user);
        userRepository.save(user);
    }

    @Override
    public Boolean checkDuplicateNickname(String userNickname) {

        Boolean response = true;

        // 특수기호 확인을 위한 정규표현식
        String specialCharacters = "[!@#$%^&*()_+=|<>?{}\\[\\]~-]";
        // 특수기호가 포함되어 있는지 확인
        if (userNickname.matches(".*" + specialCharacters + ".*")) return response;

        UserEntity user = userRepository.findByUserNickname(userNickname);
        if(user == null) response = false;

        return response;
    }

    @Override
    public void saveOauth2User(RequestOauth2VO request) {

        String userCode = getUserCode();
        UserEntity findUser = userRepository.findById(userCode).orElseThrow(() -> new CommonException(StatusEnum.USER_NOT_FOUND));

        findUser.setUserPhone(request.getUserPhone());
        findUser.setUserNickname(request.getUserNickname());
        findUser.setUserAddress(request.getUserAddress());
        findUser.setUserGender(request.getUserGender());
        findUser.setUserHeight(request.getUserHeight());
        findUser.setUserWeight(request.getUserWeight());
        findUser.setUserAge(request.getUserAge());
        findUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(findUser);
    }

    @Override
    public ResponseMypageDTO getMypageInfo() {

        String userCode = getUserCode();

        ResponseMypageDTO responseMypageDTO = userRepository.findMypageInfo(userCode);
        if(responseMypageDTO == null) throw new CommonException(StatusEnum.USER_NOT_FOUND);

        return responseMypageDTO;
    }

    @Override
    public void editPassword(EditPasswordDTO editPasswordDTO) {
        String userCode = getUserCode();
        UserEntity user = userRepository.findById(userCode)
                .orElseThrow(() -> new CommonException(StatusEnum.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(editPasswordDTO.getCurrentPassword(), user.getUserPassword())) {
            throw new CommonException(StatusEnum.INVALID_PASSWORD);
        }

        // 새 비밀번호 설정
        user.setUserPassword(bCryptPasswordEncoder.encode(editPasswordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void updateUserGym(RegisterGymPerUserRequest registerGymRequest) {
        String userCode = registerGymRequest.getUserCode();
        String businessNumber = registerGymRequest.getBusinessNumber().trim();

        log.info(businessNumber);

        UserEntity user = userRepository.findById(userCode).orElseThrow(() -> new CommonException(StatusEnum.USER_NOT_FOUND));
        Gym gym = gymRepository.findByBusinessNumber(businessNumber).orElseThrow(() -> new CommonException(StatusEnum.GYM_NOT_FOUND));

        log.info(businessNumber);

        user.setGym(gym);
        userRepository.save(user);
    }

    @Override
    public void deleteUserGym(RegisterGymPerUserRequest registerGymRequest) {
        String userCode = registerGymRequest.getUserCode();

        UserEntity user = userRepository.findById(userCode).orElseThrow(() -> new CommonException(StatusEnum.USER_NOT_FOUND));

        user.setGym(null);
        userRepository.save(user);
    }
    @Override
    public void resetPassword(RequestResetPasswordVO request) {

        UserEntity findUser = userRepository.findByUserEmail(request.getUserEmail());
        if(findUser == null) throw new CommonException(StatusEnum.USER_NOT_FOUND);

        // 비밀번호 bcrypt 해야함.
        findUser.setUserPassword(bCryptPasswordEncoder.encode(request.getUserPassword()));

        userRepository.save(findUser);
    }

    public String getUserCode() {
        // 현재 인증된 사용자 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 인증된 사용자가 문자열(String)인 경우 (로그인하지 않은 상태)
        if (principal instanceof String) {
            throw new CommonException(StatusEnum.USER_NOT_FOUND);
        }

        // 인증된 사용자가 CustomUserDetails인 경우
        CustomUserDetails userDetails = (CustomUserDetails) principal;

        log.info("Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        log.info("userDetails: {}", userDetails.toString());

        // 현재 로그인한 유저의 유저코드 반환
        return userDetails.getUserDTO().getUserCode();
    }
}