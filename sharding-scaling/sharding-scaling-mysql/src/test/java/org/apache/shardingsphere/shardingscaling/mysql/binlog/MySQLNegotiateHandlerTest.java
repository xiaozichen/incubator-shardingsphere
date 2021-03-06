/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingscaling.mysql.binlog;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Promise;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.apache.shardingsphere.shardingscaling.utils.ReflectionUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLNegotiateHandlerTest {
    
    private static final String USER_NAME = "username";
    
    private static final String PASSWORD = "password";
    
    @Mock
    private Promise<Object> authResultCallback;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    private MySQLNegotiateHandler mySQLNegotiateHandler;
    
    @Before
    public void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        mySQLNegotiateHandler = new MySQLNegotiateHandler(USER_NAME, PASSWORD, authResultCallback);
    }
    
    @Test
    public void assertChannelReadHandshakeInitPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLHandshakePacket handshakePacket = new MySQLHandshakePacket(0, new MySQLAuthPluginData(new byte[8], new byte[12]));
        handshakePacket.setAuthPluginName(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION);
        mySQLNegotiateHandler.channelRead(channelHandlerContext, handshakePacket);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLHandshakeResponse41Packet.class));
        ServerInfo serverInfo = ReflectionUtil.getFieldValueFromClass(mySQLNegotiateHandler, "serverInfo", ServerInfo.class);
        assertThat(serverInfo.getServerVersion().getMajor(), is(5));
        assertThat(serverInfo.getServerVersion().getMinor(), is(6));
        assertThat(serverInfo.getServerVersion().getSeries(), is(4));
    }
    
    @Test
    public void assertChannelReadOkPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLOKPacket okPacket = new MySQLOKPacket(0);
        ServerInfo serverInfo = new ServerInfo();
        ReflectionUtil.setFieldValueToClass(mySQLNegotiateHandler, "serverInfo", serverInfo);
        mySQLNegotiateHandler.channelRead(channelHandlerContext, okPacket);
        verify(pipeline).remove(mySQLNegotiateHandler);
        verify(authResultCallback).setSuccess(serverInfo);
    }
    
    @Test(expected = RuntimeException.class)
    public void assertChannelReadErrorPacket() {
        MySQLErrPacket errorPacket = new MySQLErrPacket(0, MySQLServerErrorCode.ER_NO_DB_ERROR);
        mySQLNegotiateHandler.channelRead(channelHandlerContext, errorPacket);
    }
}
