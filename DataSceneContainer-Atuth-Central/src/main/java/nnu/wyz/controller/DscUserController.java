package nnu.wyz.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wyz
 * @since 2023-08-16
 */
@RestController
@RequestMapping("/dsc-user")
public class DscUserController {

    @GetMapping("/test1")
    public String test1() {
        return "ok!";
    }
}
