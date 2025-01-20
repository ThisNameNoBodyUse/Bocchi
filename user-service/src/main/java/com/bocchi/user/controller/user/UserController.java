package com.bocchi.user.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.bocchi.auth.JwtService;
import com.bocchi.constant.HeaderConstant;
import com.bocchi.constant.JwtClaimsConstant;
import com.bocchi.context.BaseContext;
import com.bocchi.entity.User;
import com.bocchi.properties.JwtProperties;
import com.bocchi.result.Result;
import com.bocchi.user.service.UserService;
import com.bocchi.utils.EmailUtils;
import com.bocchi.utils.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bocchi.constant.HeaderConstant.CODE;
import static com.bocchi.constant.HeaderConstant.EMAIL;
import static com.bocchi.constant.MessageConstant.*;

@RestController("userUserController")
@Api(tags = "用户接口")
@Slf4j
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    private static final long CACHE_DURATION = 24;  // 缓存时间24h

    private static final String userPrefix = "userInfo_";

    /**
     * 发送验证码并返回
     *
     * @param email
     * @param status
     * @return
     */
    @ApiOperation("发送验证码")
    @GetMapping("/sendCode/{email}/{status}")
    public Result<Object> sendCode(@PathVariable String email, @PathVariable Integer status) {
        if (!StringUtils.isEmpty(email)) {
            String code = EmailUtils.getRandomCode();
            boolean result = EmailUtils.sendVerificationEmail(email, code);
            if (result) {
                //验证码发送成功,将生成的验证码缓存到redis中,设置有效期为5min
                String temp;
                if (status == 0) {
                    //表示登录的验证码
                    log.info("正在登录...");
                    temp = email + "_login";
                } else {
                    //表示注册的验证码
                    log.info("正在注册...");
                    temp = email + "_register";
                }

                redisTemplate.opsForValue().set(temp, code, 5, TimeUnit.MINUTES);
                return Result.success(); //前端将这个码进行本地存储5min过期
                //在有效期内,可以执行注册,登录操作
            }
            //发送失败
        }

        return Result.error(SEND_CODE_FAILED);
    }

    /**
     * 用户注册
     *
     * @param map
     * @return
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Object> register(@RequestBody Map map) {
        String email = map.get(EMAIL).toString();
        String code = map.get(CODE).toString(); //前端传入的验证码

        //redis中获得的验证码
        String temp = email + "_register";
        String codeInRedis = (String) redisTemplate.opsForValue().get(temp);

        if (codeInRedis != null && codeInRedis.equals(code)) {
            //验证码配对成功

            //清除注册的验证码
            redisTemplate.delete(temp);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, email);
            User user1 = userService.getOne(wrapper);
            if (user1 != null) {
                return Result.error(EMAIL_HAD_EXIT);
            }
            User user = new User();
            user.setEmail(email);
            user.setSex("1");
            userService.save(user);
            return Result.success(REGISTER_SUCCESS); //前端清空本地存储的code
        }

        return Result.error(REGISTER_FAILED);


    }

    /**
     * 用户登录
     *
     * @param map1
     * @return
     */

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Object> login(@RequestBody Map<String, String> map1) {
        String email = map1.get(EMAIL);
        String code = map1.get(HeaderConstant.CODE); //验证码

        //从redis中获取缓存的验证码
        String temp = email + "_login";
        Object codeInRedis = redisTemplate.opsForValue().get(temp);

        //验证码比对(页面提交的验证码和redis中保存的验证码比对)
        if (codeInRedis != null && codeInRedis.equals(code)) {
            //能比对成功
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, email);
            User user1 = userService.getOne(wrapper);
            if (user1 != null) {
                //该用户存在
                Long userId = user1.getId();
                log.info("user id:{}", userId);

                //登陆成功,删除redis中的登录验证码
                redisTemplate.delete(temp);

                Map<String, Object> claims = new HashMap<>();
                claims.put(JwtClaimsConstant.USER_ID, userId);

                // 创建访问令牌和刷新令牌
                String accessToken = JwtUtil.createJWT(
                        jwtProperties.getUserSecretKey(),
                        jwtProperties.getUserTtl(),
                        claims);
                String refreshToken = JwtUtil.createJWT(
                        jwtProperties.getUserRefreshSecretKey(),
                        jwtProperties.getUserRefreshTtl(),
                        claims);

                Map<String, String> tokens = new HashMap<>();
                tokens.put(HeaderConstant.USER_ACCESS, accessToken);
                tokens.put(HeaderConstant.USER_REFRESH, refreshToken);

                // 提前缓存用户信息
                if (getCachedData(userId) == null) {
                    cacheData(userId, user1);
                }
                return Result.success(tokens);
            } else {
                return Result.error(LOGIN_FAILED);
            }

        }
        return Result.error(CODE_ERROR_OR_TIMEOUT);
    }


    /**
     * 查询用户信息
     *
     * @return
     */
    @ApiOperation("查询当前用户信息")
    @GetMapping("/info")
    public Result<Object> info() {
        Long userId = BaseContext.getCurrentUserId();
        User user = getCachedData(userId);
        if (user != null) {
            return Result.success(user);
        }
        user = userService.getById(userId);
        cacheData(userId, user); // 缓存用户信息
        return Result.success(user);
    }

    /**
     * 修改用户名
     *
     * @param name
     * @return
     */
    @ApiOperation("修改用户名")
    @PostMapping("/updateName/{name}")
    public Result<Object> updateName(@PathVariable String name) {
        Long userId = BaseContext.getCurrentUserId();
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(User::getName, name);
        userLambdaUpdateWrapper.eq(User::getId, userId);
        userService.update(userLambdaUpdateWrapper);
        deleteCachedData(userId); //删除缓存
        return Result.success(USERNAME_UPDATE_SUCCESS);
    }

    /**
     * 修改性别
     *
     * @param sex
     * @return
     */
    @ApiOperation("修改性别")
    @PostMapping("/updateSex/{sex}")
    public Result<Object> updateSex(@PathVariable String sex) {
        Long userId = BaseContext.getCurrentUserId();
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(User::getSex, sex);
        userLambdaUpdateWrapper.eq(User::getId, userId);
        userService.update(userLambdaUpdateWrapper);
        deleteCachedData(userId); //删除缓存
        return Result.success(SEX_UPDATE_SUCCESS);
    }

    /**
     * 修改头像
     *
     * @param user
     * @return
     */
    @ApiOperation("修改头像")
    @PostMapping("/updateAvatar")
    public Result<Object> updateAvatar(@RequestBody User user) {
        Long userId = BaseContext.getCurrentUserId();
        String avatar = user.getAvatar();
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(User::getAvatar, avatar);
        userLambdaUpdateWrapper.eq(User::getId, userId);
        userService.update(userLambdaUpdateWrapper);
        deleteCachedData(userId); //删除缓存
        return Result.success(AVATAR_UPDATE_SUCCESS);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */

    @ApiOperation("用户退出登录")
    @PostMapping("/logout")
    public Result<Object> logout(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HeaderConstant.USER_ACCESS);
            String refreshToken = request.getHeader(HeaderConstant.USER_REFRESH);

            if ((accessToken == null || accessToken.isEmpty()) && (refreshToken == null || refreshToken.isEmpty())) {
                return Result.error(JWT_EMPTY);
            }

            // 使访问令牌失效
            if (accessToken != null && !accessToken.isEmpty()) {
                String accessJti = JwtUtil.getJti(jwtProperties.getUserSecretKey(), accessToken);
                long accessExpireTime = JwtUtil.getExpireTime(jwtProperties.getUserSecretKey(), accessToken);
                jwtService.storeToken(accessJti, accessExpireTime);
            }

            // 使刷新令牌失效
            if (refreshToken != null && !refreshToken.isEmpty()) {
                String refreshJti = JwtUtil.getJti(jwtProperties.getUserRefreshSecretKey(), refreshToken);
                long refreshExpireTime = JwtUtil.getExpireTime(jwtProperties.getUserRefreshSecretKey(), refreshToken);
                jwtService.storeToken(refreshJti, refreshExpireTime);
            }

            return Result.success(LOGOUT_SUCCESS);
        } catch (Exception e) {
            log.error("Parse JWT ERROR: ", e);
            return Result.error(JWT_INVALID);
        }
    }

    /**
     * 获取缓存数据
     *
     * @param userId
     * @return
     */

    @SuppressWarnings("unchecked")
    private User getCachedData(Long userId) {
        String key = userPrefix + userId;
        ObjectMapper mapper = new ObjectMapper();
        // 将缓存中的对象转换为指定的User类型 2024.10.26
        return mapper.convertValue(redisTemplate.opsForValue().get(key), new TypeReference<User>() {
        });
    }

    /**
     * 缓存数据
     * CACHE_DURATION : 缓存时间的数值
     * TimeUnit. ： 缓存时间的单位
     *
     * @param userId
     * @param user
     */
    private void cacheData(Long userId, User user) {
        String key = userPrefix + userId;
        redisTemplate.opsForValue().set(key, user, CACHE_DURATION, TimeUnit.HOURS);
    }

    private void deleteCachedData(Long userId) {
        String key = userPrefix + userId;
        redisTemplate.delete(key);
    }


}


