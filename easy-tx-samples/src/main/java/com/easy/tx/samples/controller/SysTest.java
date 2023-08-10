package com.easy.tx.samples.controller;



import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysTest {

    private Long id;

    @ApiModelProperty(value = "用户名")
    private String name;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "密码")
    private String password;
    @ApiModelProperty(value = "密码")
    private String aaa;

//    @EsField(type = EsFieldType.DATE)
//    private Date dateTimed;
    private LocalDateTime dateTime;

    private Long[] ids;
    private String key;
}
