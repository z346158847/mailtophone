package cn.woconnect.mailtophone.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 语音通知返回json格式
 * @author wjzhang
 * @date 2019/11/1  18:26
 */
@Data
public class VoiceJson {

    @NotNull
    private Integer resultCode;
    @NotNull
    private String taskId;
    @NotNull
    private String resultMsg;

}
