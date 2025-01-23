package com.kai.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai.RedisOptEnum;
import com.kai.context.UserContext;
import com.kai.exception.ServiceException;
import com.kai.model.ChatRoomMessage;
import com.kai.model.PrivateMessage;
import com.kai.service.ChatRoomService;
import com.kai.service.PrivateMessageService;
import com.kai.service.RedisService;
import com.kai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
@Scope("prototype") // 每次需要创建新的实例
public class WebSocketMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChatRoomService chatRoomService;
    private final RedisService redisService;
    private final ObjectMapper mapper;
    private final PrivateMessageService privateMessageService;

    @Autowired
    public WebSocketMessageHandler(ChatRoomService chatRoomService, RedisService redisService, ObjectMapper mapper, PrivateMessageService privateMessageService) {
        this.chatRoomService = chatRoomService;
        this.redisService = redisService;
        this.mapper = mapper;
        this.privateMessageService = privateMessageService;
    }

    // 聊天室集合
    private static final ConcurrentMap<String, ConcurrentHashMap<String, Channel>> chatRooms = new ConcurrentHashMap<>();

    // 用户与 Channel 的映射
    private static final ConcurrentMap<String, Channel> userChannels = new ConcurrentHashMap<>();


    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String  jsonMessage= frame.text();

        try {

            JsonNode jsonNode = mapper.readTree(jsonMessage);
            String action = jsonNode.get("action").asText();
            String id = jsonNode.get("id").asText();



            if ("authenticate".equals(action)){
                String token = jsonNode.get("token").asText();
                if (token != null){
                    this.auth(token);
                }else {
                    throw new ServletException("未认证");
                }


                userChannels.put(UserContext.getUsername(), ctx.channel());
                logger.info("用户 {} 已连接", UserContext.getUsername());
                return;

            }else {
                JsonNode auth = jsonNode.get("auth");
                if (auth != null){
                    this.auth(auth.asText());
                }else {
                    throw new ServletException("未认证");
                }
            }

            String roomId = jsonNode.get("roomId").asText();




            if ("subscribe".equals(action)) {
                // 处理订阅逻辑，比如将客户端加入特定房间
                this.subscribe(roomId, ctx.channel());
            } else if ("unsubscribe".equals(action)) {
                // 处理取消订阅逻辑
                this.unsubscribe(roomId, ctx.channel());
            } else if ("message".equals(action)) {

                String content = jsonNode.get("content").asText();


                ChatRoomMessage chatRoomMessage = new ChatRoomMessage();
                chatRoomMessage.setMessageText(content);
                chatRoomMessage.setMessageId(Long.valueOf(id));
                // 将消息保存到数据库
                ChatRoomMessage savedMessage = chatRoomService.sendMessage(Long.valueOf(roomId), chatRoomMessage);

                // 处理发送消息逻辑
                broadcastMessage(roomId, content,id);
            }else if ("private".equals(action)){
                String receiverId = jsonNode.get("receiverId").asText();
                String content = jsonNode.get("content").asText();

                PrivateMessage privateMessage = new PrivateMessage();
                privateMessage.setSender(com.kai.model.User.builder().id(UserContext.getUserId()).build());
                privateMessage.setChatRoomId(Long.valueOf(roomId));
                privateMessage.setReceiver(com.kai.model.User.builder().id(Long.valueOf(receiverId)).build());
                privateMessage.setMessageText(content);
                privateMessage.setId(Long.valueOf(id));
                String receiverUserName = privateMessageService.sendPrivateMessage(privateMessage);
                broadcastPrivateMessage(roomId, receiverUserName, content, ctx.channel(),id);
            }else{
                throw new ServletException("未知消息类型");

            }




        } catch (Exception e) {
            logger.error("处理消息时发生异常", e);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
//        logger.info("用户连接：{}", ctx.channel().id());
        //怎么获取用户信息

//        userChannels.put(, ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
//        logger.info("用户断开连接：{}", ctx.channel().id());

        // 从全局用户映射中移除
        userChannels.entrySet().removeIf(entry -> entry.getValue().equals(ctx.channel()));

        // 从房间映射中移除
        chatRooms.forEach((roomId, subscribers) -> subscribers.values().remove(ctx.channel()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("发生异常", cause);
        ctx.close();
    }

    /**
     * 订阅房间
     * @param roomId 房间 ID
     * @param channel 客户端连接通道
     */
    public void subscribe(String roomId, Channel channel) {
        chatRooms.putIfAbsent(roomId,  new ConcurrentHashMap<>());
        chatRooms.get(roomId).put(Objects.requireNonNull(UserContext.getUsername()), channel);
        logger.info("{}订阅了聊天室{} 当前还有 {} 人  当前聊天室人员列表：{}", UserContext.getUsername(), roomId,chatRooms.get(roomId).size(), chatRooms.get(roomId));
    }

    /**
     * 取消订阅房间
     * @param roomId 房间 ID
     * @param channel 客户端连接通道
     */
    public void unsubscribe(String roomId, Channel channel) {
        ConcurrentHashMap<String, Channel> subscribers = chatRooms.get(roomId);
        if (subscribers != null) {
            subscribers.values().remove(channel);
            logger.info("{}取消订阅了聊天室{}，当前在线人数：{}，成员列表：{}",
                    UserContext.getUsername(), roomId, subscribers.size(), subscribers.keySet());
        }
    }

    /**
     * 广播消息到房间
     * @param roomId 房间 ID
     * @param message 消息内容
     */
    public static void broadcastMessage(String roomId, String message,String id) {
        ConcurrentHashMap<String, Channel> subscribers = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());

        if (subscribers != null) {
            subscribers.forEach((username, channel) -> {
                try {
                    HashMap<String, String> res = new HashMap<>();
                    res.put("message", message);
                    res.put("messageType", "text");
                    res.put("type","group");
                    res.put("id",id);
                    //转换为json
                    String json = new ObjectMapper().writeValueAsString(res);
                    channel.writeAndFlush(new TextWebSocketFrame(json));
                } catch (Exception e) {
                    logger.error("广播文本消息到聊天室{}时发生异常", roomId, e);
                }
            });
            //日志记录
            logger.info("广播消息到聊天室{}: {}", roomId, message);
            logger.info("当前人员个数：{} 人员列表：{}", chatRooms.get(roomId).size(), chatRooms.get(roomId));
        }
    }


    /**
     * 广播消息到房间
     * @param roomId 房间 ID
     */
    public static void broadcastMessage(String roomId,String msgId) {
        broadcastMessage(roomId,"多媒体消息",msgId);
//        ConcurrentHashMap<String, Channel> subscribers = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
//
//        if (subscribers != null) {
//            subscribers.forEach((username, channel) -> {
//                try {
//                    HashMap<String, String> res = new HashMap<>();
//                    res.put("message", "多媒体消息");
//                    res.put("messageType", "media");
//                    res.put("type","group");
//                    res.put("id",msgId);
//                    String json = new ObjectMapper().writeValueAsString(res);
//                    channel.writeAndFlush(new TextWebSocketFrame(json));
//                } catch (Exception e) {
//                    logger.error("广播文本消息到聊天室{}时发生异常", roomId, e);
//                }
//            });
//            //日志记录
//            logger.info("广播消息到聊天室{}: {}", roomId,"多媒体消息");
//            logger.info("当前人员个数：{} 人员列表：{}", chatRooms.get(roomId).size(), chatRooms.get(roomId));
//        }
    }



    public static void broadcastPrivateMessage(String roomId, String target, String message, Channel sender,String msgId) {
        ConcurrentHashMap<String, Channel> subscribers = chatRooms.get(roomId);
        if (subscribers != null && subscribers.containsKey(target)) {
            Channel targetChannel = userChannels.get(target);

            if (targetChannel != null) {
                try {
                    HashMap<String, String> res = new HashMap<>();
                    res.put("message", message);
                    res.put("type","private");
                    res.put("id",msgId);
                    res.put("from",UserContext.getUsername());
                    res.put("to",target);
                    //转换为json
                    String json = new ObjectMapper().writeValueAsString(res);

                    targetChannel.writeAndFlush(new TextWebSocketFrame(json));
                    sender.writeAndFlush(new TextWebSocketFrame(json));
                } catch (Exception e) {
                    logger.error("发送私聊消息给 {} 时发生异常", target, e);
                }
            } else {
                sender.writeAndFlush(new TextWebSocketFrame("用户 " + target + " 不在线或不存在"));
            }
        } else {
            sender.writeAndFlush(new TextWebSocketFrame("用户 " + target + " 不在房间 " + roomId));
        }
    }


    public static void broadcastPrivateMessage(String roomId, String targetUsername,String msgId) {
            broadcastPrivateMessage(roomId,targetUsername,"多媒体消息",userChannels.get(UserContext.getUsername()),msgId);


//        ConcurrentHashMap<String, Channel> subscribers = chatRooms.get(roomId);
//        //获取发送者的channel
//        Channel sender = userChannels.get(UserContext.getUsername());
//
//        if (subscribers != null && subscribers.containsKey(targetUsername)) {
//            Channel targetChannel = userChannels.get(targetUsername);
//
//
//
//            if (targetChannel != null) {
//                try {
//                    HashMap<String, String> res = new HashMap<>();
//                    res.put("message", "多媒体消息");
//                    res.put("type","private");
//                    res.put("id",msgId);
//                    res.put("from",UserContext.getUsername());
//                    res.put("to",targetUsername);
//                    //转换为json
//                    String json = new ObjectMapper().writeValueAsString(res);
//
//
//                    targetChannel.writeAndFlush(new TextWebSocketFrame(json));
//                    sender.writeAndFlush(new TextWebSocketFrame(json));
//                } catch (Exception e) {
//                    logger.error("发送私聊消息给 {} 时发生异常", targetUsername, e);
//                }
//            } else {
//                sender.writeAndFlush(new TextWebSocketFrame("用户 " + targetUsername + " 不在线或不存在"));
//            }
//        } else {
//            sender.writeAndFlush(new TextWebSocketFrame("用户 " + targetUsername + " 不在房间 " + roomId));
//        }
    }


    private void auth(String authorizationHeader) throws ServletException {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {


                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();
                String userId = claims.getId(); // 假设 token 中存储了 userId

                UserContext.setUserInfo(Long.valueOf(userId), username);
                UserDetails userDetails = User.withUsername(username).password("").authorities("USER").build();


                boolean existed = redisService.existKey(RedisOptEnum.LOGIN_INFO.getValue() + "_" + UserContext.getUserId());
                if(!existed){
                    throw new ServiceException("当前未登录！！！");
                }else {
                    String redisToken = redisService.getRedisToken(String.valueOf(UserContext.getUserId()));
                    if (!token.equals(redisToken)){
                        throw new ServletException("当前会话已经过期，请重新登录");
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                throw new ServletException(e.getMessage());
            }
        }else {
            throw new ServletException("未认证");
        }
    }

}
