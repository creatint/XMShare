package com.merpyzf.transfermanager.entity;

import com.merpyzf.transfermanager.constant.Constant;

/**
 * Created by wangke on 2017/12/12.
 * 连接建立前的示意报文
 */

public class SignMessage {

    // 数据包的包名
    private String packetName;
    // 发送数据包的设备的主机地址
    private String hostAddress;
    //消息的内容
    private String msgContent;
    //发送设备的昵称
    private String nickName;
    // 这条消息对应的命令代号
    private int cmd;

    /**
     * 1. 上线/下线
     * 2. 配对请求(在界面上显示申请配对的那个用户)
     * 3. 回复配对请求
     */


    public SignMessage() {

        this.packetName = getTime();

    }


    public SignMessage(String hostAddress, String msgContent, String nickName) {
        this.packetName = getTime();
        this.hostAddress = hostAddress;
        this.msgContent = msgContent;
        this.nickName = nickName;
    }


    public String getPacketName() {
        return packetName;
    }

    public void setPacketName(String packetName) {
        this.packetName = packetName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    /**
     * 将对象转换成协议字符
     *
     * @return
     */
    public String convertProtocolStr() {

        StringBuilder protocolStr = new StringBuilder();
        protocolStr.append(packetName);
        protocolStr.append(Constant.S_SEPARATOR);
        protocolStr.append(hostAddress);
        protocolStr.append(Constant.S_SEPARATOR);
        protocolStr.append(nickName);
        protocolStr.append(Constant.S_SEPARATOR);
        protocolStr.append(msgContent);
        protocolStr.append(Constant.S_SEPARATOR);
        protocolStr.append(String.valueOf(cmd));
        protocolStr.append(Constant.S_END);
        return protocolStr.toString();

    }

    /**
     * 根据规则进行解码
     *
     * @param signMessage
     * @return
     */
    public static SignMessage decodeProtocol(String signMessage) {

        if (signMessage != null && signMessage.length() > 0) {

            int end = signMessage.indexOf(Constant.S_END);
            signMessage = signMessage.subSequence(0, end).toString();
            SignMessage message = new SignMessage();

            String[] MsgProperties = signMessage.split(Constant.S_SEPARATOR);

            message.setPacketName(MsgProperties[0]);
            message.setHostAddress(MsgProperties[1]);
            message.setNickName(MsgProperties[2]);
            message.setMsgContent(MsgProperties[3]);
            message.setCmd(Integer.valueOf(MsgProperties[4]));
            return message;

        }


        return null;

    }


    public String getTime() {
        return String.valueOf(System.currentTimeMillis());
    }


}
