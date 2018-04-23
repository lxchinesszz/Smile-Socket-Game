package smile.service.handler;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import org.smileframework.tool.string.StringTools;
import smile.config.ErrorEnum;
import smile.database.domain.HomeInfoEntity;
import smile.database.domain.UserEntity;
import smile.database.dto.*;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.protocol.impl.ResultDatagram;
import smile.service.home.*;
import smile.service.poker.CardPoker;
import smile.tool.ChannelAttributeTools;
import smile.tool.GameHelper;
import smile.tool.IOC;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Package: smile.service.handler
 * @Description: 1. 创建房间
 * 2. 加入房间
 * 3. 离开房间
 * 4. 解散房间
 * @date: 2018/4/16 下午8:02
 * @author: liuxin
 */
@SmileComponent
@Action
public class HomeActionHandler extends AbstractActionHandler {

    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    /**
     * 创建房间
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 3,model = CreateRoomC2S_DTO.class)
    public SocketPackage createHome(SocketPackage socketPackage, Channel channel) {
        CreateRoomC2S_DTO homeDTO = (CreateRoomC2S_DTO) socketPackage.getDatagram();
        String uid = homeDTO.getUid();
        String hid0 = "";
        /**
         * 获取创建房间牌局信息
         */
        String personNum = homeDTO.getPersonNum();
        String blind = homeDTO.getBlind();
        String multiple = homeDTO.getMultiple();
        String roomNum = homeDTO.getRoomNum();
        String method = homeDTO.getMethod();
        String AA = homeDTO.getAA();
        String sharedIP = homeDTO.getSharedIP();
        String query = String.format("{\"uid\":\"%s\"}", uid);
        /**
         * 查询到当前登录用户信息
         * 1. 校验剩余的房卡数量
         */
        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
        int carNum = Integer.parseInt(userInfo.getCardNum());
        Integer.parseInt(roomNum);
        MongoDao mongoDao = IOC.get().getBean(MongoDao.class);
        /**
         * 根据p牌局数roomNum/5= 需要的房卡
         * 1. 当用户剩余房卡,不足以支付当前创建房间的房卡时,提醒用户
         * 2. 当房卡扣除成功,更新用户房卡信息
         */
        //1.
        int i = BigDecimal.valueOf(Integer.parseInt(roomNum)).divide(BigDecimal.valueOf(5L)).intValue();
        if (i > carNum) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.CARD_BUGOU);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        //2.
        userInfo.setCardNum(String.valueOf(carNum - i));
        String del_query = String.format("{\"uid\":\"%s\"}", uid);
        boolean ddz_user = mongoDao.del(del_query, "ddz_user");
        if (ddz_user) {
            mongoDao.insert(userInfo);
        }
        /**
         * 1.当前创建房间的人作为房主玩家
         * 2.为当前房间创建一副扑克牌
         * 3.构建房间管理器
         * 4.通过用户创建房间信息,构建房间信息HomeInfo
         * 5.当没有房间号hid时候回根据房间信息HomeInfo生成Home房间对象
         *   当已经有房间号hid,则根据房间号去创建房间
         * 6.设置房主座位号
         * 7.构建返回信息
         * 8.通过Channel异步发送给client
         * 9.当创建房间信息发送成功,将房主玩家信息返回给前端
         */
        //1
        Player ownPlayer = new Player(uid, userInfo.getName(), channel);
        //2.
        Poker poker = new CardPoker(personNum);
        //3.
        HomeManager homeManager = GameHelper.homeManager();
        //4.
        HomeInfo homeInfo = new HomeInfo(ownPlayer, personNum, poker, multiple, blind, sharedIP, AA, method, roomNum);
        //5.
        Home home = null;
        if (StringTools.isEmpty(hid0)) {
            home = homeManager.createHome(homeInfo);
        } else {
            home = homeManager.createHome(hid0, homeInfo);
        }
        //6.
        ownPlayer.setChairId(home.getPlayers().size() - 1 + "");
//        home.updatePlayer(ownPlayer);
        //返回六位房间号
        String hid = home.getHid();
        //7.
        CreateRoomS2C_DTO createRoomS2CDto = CreateRoomS2C_DTO.builder().blind(blind).hid(hid)
                .ownerId(uid)
                .roomNum(roomNum).multiple(multiple)
                .AA(AA).method(method).sharedIP(sharedIP).currentRoomNum(roomNum).build();
        socketPackage.setDatagram(createRoomS2CDto);
        //8.
        ChannelFuture sync = channel.writeAndFlush(socketPackage);
        sync.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    //9.
                    if (channelFuture.isSuccess()) {
                        //返回用户信息
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(ownPlayer.getChairId()).ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status(ownPlayer.getStatus()).uid(uid).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                        channelFuture.channel().writeAndFlush(playerInfoSocket);
                    }
                    System.err.println("玩家:" + ownPlayer + ",已创建好房间");
                }
            }
        });
        /**
         * 在创建房间时候,保存用户uid和hid当channel中
         */
        ChannelAttributeTools.attr(channel, "hid", hid);
        ChannelAttributeTools.attr(channel, "uid", uid);
        return socketPackage;
    }

    /**
     * 加入房间操作
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 4,model = JoinRoomC2S_DTO.class)
    public SocketPackage joinRoom(SocketPackage socketPackage, Channel channel) {
        JoinRoomC2S_DTO joinRoomC2S_dto = (JoinRoomC2S_DTO) socketPackage.getDatagram();
        String hid = joinRoomC2S_dto.getHid();
        String uid = joinRoomC2S_dto.getUid();
        /**
         * 1. 将用户id和房间id添加到用户连接信息中
         * 2. 获取加入玩家信息
         * 3. 通过用户id构建出玩家信息
         * 4. 获取数据库中，是否有当前房间信息
         *    如果有，则根据数据库中房间信息，重新生成房间
         * 5. 获取房间信息
         */
        //1.
        ChannelAttributeTools.attr(channel, "hid", hid);
        ChannelAttributeTools.attr(channel, "uid", uid);
        //2.
        String query = String.format("{\"uid\":\"%s\"}", uid);
        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
        //3.
        Player player = new Player(uid, userInfo.getName(), channel);
        //4.
        String query0 = String.format("{\"ownerId\":\"%s\"}", uid);
        HomeInfoEntity all = mongoDao.findOne(query0, HomeInfoEntity.class);
        //创建一个房管,对房间进行管理
        HomeManager homeManager = GameHelper.homeManager();
        Home home = homeManager.getHome(hid);
        if (all == null && home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.HOME_UNFOUNJD);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        if (all != null) {
            String personNum = all.getPersonNum() + "";
            String blind = all.getBlind();
            String multiple = all.getMultiple();
            String roomNum = all.getRoomNum();
            String method = all.getMethod();
            String AA = all.getAA();
            String sharedIP = all.getSharedIP();
            String hid0 = all.getHid();
            //创建一副扑克牌
            Poker poker = new CardPoker(personNum);
            HomeInfo homeInfo0 = new HomeInfo(player, personNum, poker, multiple, blind, sharedIP, AA, method, roomNum);
            //如果房间号不等于空，剩余牌局数大于0则从数据库回复房间信息
            if (!StringTools.isEmpty(hid0) && homeInfo0.getShengyuRoomNum() > 0) {
                home = homeManager.getHome(hid0);
                //避免重复添加
                if (home == null) {
                    homeManager.createHome(hid0, homeInfo0);
                }
            }
        }
        //5.
       Home currentHome = GameHelper.homeManager().getHome(hid);
        if (currentHome == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.HOME_UNFOUNJD);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        /**
         * 如果当前房间人数大于4则,不允许加入到房间
         * 1. 判断当前加入房间的玩家是否已经存在房间中
         *    如果存在只添加链接channel到玩家信息中
         *    否则，则添加当前玩家到房间中，并分配给玩家作为号
         */
        if (currentHome.getPlayers().size() >=4) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.MANYUAN);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        //1.
        Player isExit = currentHome.getPlayer(uid);
        if (isExit == null) {
            List<Player> players = currentHome.getPlayers();
            int size = players.size();
            ArrayList<String> objects = Lists.newArrayList();
            for (int i = 0; i < size; i++) {
                objects.add(players.get(i).getChairId());
            }
            List<String> strings = new ArrayList();
            strings.add("0");
            strings.add("1");
            strings.add("2");
            strings.add("3");
            strings.removeAll(objects);
            System.err.println("当前玩家: " + uid + ",分配作为号为: " + strings.get(0));
            player.setChairId(String.valueOf(strings.get(0)));
            currentHome.addPlayers(player);
        } else {
            player = isExit;
            player.setChannel(channel);
        }
        if (currentHome.getPlayers().size() > currentHome.getHomeInfo().getPersonNum()) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.MANYUAN);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            return socketPackage;
        }
        GameHelper.homeManager().updateHome(currentHome);
        HomeInfo homeInfo = currentHome.getHomeInfo();
        /**
         * 1. 玩家加入成功,将房主创建房间信息，同步给当前加入的玩家
         * 2. 将当前加入的玩家信息，同步给当前房间中其他的玩家
         * 3. 将其他玩家当前的状态，同步给当前加入的玩家
         */
        String blind = homeInfo.getBlind();
        String roomNum = homeInfo.getRoomNum();
        Protocol protocol = socketPackage.getProtocol();
        //1.
        CreateRoomS2C_DTO createRoomS2CDto = CreateRoomS2C_DTO.builder().blind(blind).hid(hid + "")
                .ownerId(homeInfo.getHomeOwner().getUid())
                .roomNum(roomNum).multiple(homeInfo.getMultiple())
                .AA(homeInfo.getAA()).method(homeInfo.getMethod()).sharedIP(homeInfo.getSharedIP()).currentRoomNum(roomNum).build();
        socketPackage.setProtocol(protocol);
        socketPackage.setDatagram(createRoomS2CDto);
        ChannelFuture channelFuture = channel.writeAndFlush(socketPackage);
        final Player joinPlayer = currentHome.getPlayer(uid);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //2.
                if (channelFuture.isSuccess()) {
                    System.err.println("玩家:" + joinPlayer + ", 加入到房间");
                    List<Player> players = currentHome.getPlayers();
                    for (int i = 0; i < players.size(); i++) {
                        Player otherPlayer = players.get(i);
                        Channel otherChannel = otherPlayer.getChannel();
                        String uid = joinPlayer.getUid();
                        String query = String.format("{\"uid\":\"%s\"}", uid);
                        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
                        //返回用户信息
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(joinPlayer.getChairId()).ip(userInfo.getIp())
                                .gender(userInfo.getGender()).name(userInfo.getName())
                                .iconurl(userInfo.getIconurl()).status(joinPlayer.getStatus()).uid(uid).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9),
                                playerInfoS2CDto);
                        otherChannel.writeAndFlush(playerInfoSocket);
                    }
                    //3.
                    Channel joinPlayerChannel = joinPlayer.getChannel();
                    for (int i = 0; i < players.size(); i++) {
                        Player otherPlayer = players.get(i);
                        if (otherPlayer.getUid().equalsIgnoreCase(joinPlayer.getUid())) {
                            continue;
                        }
                        String uid = otherPlayer.getUid();
                        String query = String.format("{\"uid\":\"%s\"}", uid);
                        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(otherPlayer.getChairId()).ip(userInfo.getIp())
                                .gender(userInfo.getGender()).name(userInfo.getName())
                                .iconurl(userInfo.getIconurl())
                                .status(otherPlayer.getStatus()).uid(otherPlayer
                                        .getUid()).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                        joinPlayerChannel.writeAndFlush(playerInfoSocket);
                    }
                }
            }
        });

        return socketPackage;
    }


    /**
     * 离开房间操作
     * 1. 获取将要离开房间的用户信息
     * 2. 获取房间信息，非空判断，空： 错误提示
     * 3. 获取当前离开房间的玩家，如果状态不为开始状态就可以退出，并从home房间信息中，移除当前玩家
     * 4. 当玩家从房间里面移除，如果玩家处于准备状态,就将房间准备的人数减1
     * 5. 判断当前房间中没有玩家,就移除当前房间
     * 6. 当房间中没有玩家，房主并没有解散房间，而是离开房间，则保存房间信息到数据库,下次继续登录
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 5,model = LeaveRoomC2S_DTO.class)
    public SocketPackage leaveHome(SocketPackage socketPackage, Channel channel) {
        LeaveRoomC2S_DTO leaveRoomC2SDto = (LeaveRoomC2S_DTO) socketPackage.getDatagram();
        //1.
        String leaveUid = leaveRoomC2SDto.getUid();
        String leaveHid = leaveRoomC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(leaveHid);
        //2.
        if (home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.HOME_UNFOUNJD);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        //3.

        Player player = home.getPlayer(leaveUid);
        if (player.getStatus().equalsIgnoreCase("3")){
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.YOUXIZHONG);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }else {
            home.removePlayer(leaveUid);
        }
        //4.
        if (player.getStatus().equalsIgnoreCase("2")) {
            home.subReady();
        }
        //5.
        if (home.isExit()) {
            //6.
            Home willLeavedHome = GameHelper.homeManager().clearHome(leaveHid);
            HomeInfoEntity homeInfoEntity = new HomeInfoEntity(willLeavedHome.getHomeInfo());
            homeInfoEntity.setHid(home.getHid() + "");
            String query = String.format("{\"hid\":\"%s\"}", willLeavedHome.getHid());
            mongoDao.del(query, "ddz_home_info");
            mongoDao.insert(homeInfoEntity);
            if (willLeavedHome != null) {
                System.err.println("当前房间号已备份:" + leaveHid + " , 并已经移除....");
            }
        }
        /**
         * 1. 将退出房间的请求结果返回给client
         * 2. 查询出当前退出房间的玩家信息
         * 3. 将当前退出房间的玩家信息，通知给剩余其他玩家
         */
        //1.
        socketPackage.setDatagram(new ResultDatagram());
        ChannelFuture channelFuture = channel.writeAndFlush(socketPackage);
        //2.
        String findUserQuery = mongoDao.getFindJson("uid", player.getUid());
        UserEntity userInfo = mongoDao.findOne(findUserQuery, UserEntity.class);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    System.err.println("玩家:" + player + ", 离开房间");
                    //判断当前离开的玩家是否是已经准备状态,如果不等于-1，则是准备状态，则有离开通知
                    //否则观战玩家离开，不通知其他玩家
                    if (!player.getChairId().equalsIgnoreCase("-1")) {
                        List<Player> players = home.getPlayers();
                        for (int i = 0; i < players.size(); i++) {
                            Player otherPlayer = players.get(i);
                            Channel otherChannel = otherPlayer.getChannel();
                            //返
                            PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                    .chairId("-1").ip(userInfo.getIp()).gender(userInfo.getGender())
                                    .name(userInfo.getName()).iconurl(userInfo.getIconurl())
                                    .status("5").uid(player.getUid()).build();
                            SocketPackage playerInfoSocket =
                                    new SocketPackage(new Protocol(2, 9)
                                            , playerInfoS2CDto);
                            otherChannel.writeAndFlush(playerInfoSocket);
                        }
                    }
                }
            }
        });
        /**
         * 房卡数量推送
         */
        playerInfoNotify.notifyCardNum(Arrays.asList(new Player(leaveUid, "", channel)));
        return socketPackage;
    }


    /**
     * 解散房间
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 16,model = RemoveRoomC2S_DTO.class)
    private SocketPackage remove(SocketPackage socketPackage, Channel channel) {
        RemoveRoomC2S_DTO datagram = (RemoveRoomC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        String uid = datagram.getUid();
        Home home = GameHelper.homeManager().getHome(hid);
        if (home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.HOME_UNFOUNJD);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        HomeInfo homeInfo = home.getHomeInfo();
        String roomNum = homeInfo.getRoomNum();
        int shengyuRoomNum = homeInfo.getShengyuRoomNum();
        /**
         * 获取当前房主信息
         * 1. 判断当前操作解散房间的用户是否是房主，如果不等于则提示无权限解散房间
         * 2. 判断房间剩余人数是否只有一个玩家
         *    当房间只有一个玩家，则判断剩余玩家是否是房主。
         *    如果满足房间只有房主一个人，则可以解散，否则提示错误
         * 3. 判断当房卡还未使用一张时候解散房间，将房卡重新退换为房主
         */
        String ownerId = homeInfo.getHomeOwner().getUid();
        //1.
        if (!ownerId.equalsIgnoreCase(uid)) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.WUQVAN);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
        }
        //2.
        if (home.getPlayers().size() != 1) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.JIESAN_FAIL);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        //3.
        if (shengyuRoomNum == Integer.parseInt(roomNum)) {
            //将房卡信息(房间牌局数/5=房卡),加入到用户总房卡中
            int i = BigDecimal.valueOf(Integer.parseInt(roomNum)).divide(BigDecimal.valueOf(5L)).intValue();
            UserEntity byUid = mongoDao.findByUid(uid, UserEntity.class);
            String cardNum = byUid.getCardNum();
            byUid.setCardNum((Integer.parseInt(cardNum) + i) + "");
            String query = String.format("{\"uid\":\"%s\"}", uid);
            mongoDao.del(query, "ddz_user");
            mongoDao.insert(byUid);
        }
        /**
         * 1. 清理当前解散的房间
         * 2. 构建解散房间信息并返回给client
         */
        //1.
        Home rm_home = GameHelper.homeManager().clearHome(hid);
        //2.
        RemoveRoomS2C_DTO.RemoveRoomS2C_DTOBuilder builder = RemoveRoomS2C_DTO.builder();
        if (rm_home != null) {
            String query = String.format("{\"hid\":\"%s\"}", hid);
            boolean ddz_home_info = mongoDao.del(query, "ddz_home_info");
            if (ddz_home_info) {
                System.err.println("房间号已经解散：" + hid);
            }
            socketPackage.setDatagram(builder.code("0").build());
        } else {
            socketPackage.setDatagram(builder.code("1").build());
        }
        channel.writeAndFlush(socketPackage);
        /**
         * 房卡数量推送
         */
        playerInfoNotify.notifyCardNum(home);
        return socketPackage;
    }
}
