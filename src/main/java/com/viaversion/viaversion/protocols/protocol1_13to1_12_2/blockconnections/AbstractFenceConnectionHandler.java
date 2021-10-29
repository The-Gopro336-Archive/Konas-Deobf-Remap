package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFenceConnectionHandler extends ConnectionHandler {
   private static final StairConnectionHandler STAIR_CONNECTION_HANDLER = new StairConnectionHandler();
   private final String blockConnections;
   private final Set blockStates = new HashSet();
   private final Map connectedBlockStates = new HashMap();

   protected AbstractFenceConnectionHandler(String blockConnections) {
      this.blockConnections = blockConnections;
   }

   public ConnectionData.ConnectorInitAction getInitAction(String key) {
      return (blockData) -> {
         if (key.equals(blockData.getMinecraftKey())) {
            if (blockData.hasData("waterlogged") && blockData.getValue("waterlogged").equals("true")) {
               return;
            }

            thisx.blockStates.add(blockData.getSavedBlockStateId());
            ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), this);
            thisx.connectedBlockStates.put(thisx.getStates(blockData), blockData.getSavedBlockStateId());
         }

      };
   }

   protected byte getStates(WrappedBlockData blockData) {
      byte states = 0;
      if (blockData.getValue("east").equals("true")) {
         states = (byte)(states | 1);
      }

      if (blockData.getValue("north").equals("true")) {
         states = (byte)(states | 2);
      }

      if (blockData.getValue("south").equals("true")) {
         states = (byte)(states | 4);
      }

      if (blockData.getValue("west").equals("true")) {
         states = (byte)(states | 8);
      }

      return states;
   }

   protected byte getStates(UserConnection user, Position position, int blockState) {
      byte states = 0;
      boolean pre1_12 = user.getProtocolInfo().getServerProtocolVersion() < ProtocolVersion.v1_12.getVersion();
      if (this.connects(BlockFace.EAST, this.getBlockData(user, position.getRelative(BlockFace.EAST)), pre1_12)) {
         states = (byte)(states | 1);
      }

      if (this.connects(BlockFace.NORTH, this.getBlockData(user, position.getRelative(BlockFace.NORTH)), pre1_12)) {
         states = (byte)(states | 2);
      }

      if (this.connects(BlockFace.SOUTH, this.getBlockData(user, position.getRelative(BlockFace.SOUTH)), pre1_12)) {
         states = (byte)(states | 4);
      }

      if (this.connects(BlockFace.WEST, this.getBlockData(user, position.getRelative(BlockFace.WEST)), pre1_12)) {
         states = (byte)(states | 8);
      }

      return states;
   }

   public int getBlockData(UserConnection user, Position position) {
      return STAIR_CONNECTION_HANDLER.connect(user, position, super.getBlockData(user, position));
   }

   public int connect(UserConnection user, Position position, int blockState) {
      Integer newBlockState = (Integer)this.connectedBlockStates.get(this.getStates(user, position, blockState));
      return newBlockState == null ? blockState : newBlockState;
   }

   protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
      if (this.blockStates.contains(blockState)) {
         return true;
      } else if (this.blockConnections == null) {
         return false;
      } else {
         BlockData blockData = (BlockData)ConnectionData.blockConnectionData.get(blockState);
         return blockData != null && blockData.connectsTo(this.blockConnections, side.opposite(), pre1_12);
      }
   }

   public Set getBlockStates() {
      return this.blockStates;
   }
}