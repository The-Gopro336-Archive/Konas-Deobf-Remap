package com.viaversion.viabackwards.protocol.protocol1_13_2to1_14;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappings;
import com.viaversion.viabackwards.api.rewriters.TranslatableRewriter;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.data.CommandRewriter1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.BlockItemPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.EntityPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.PlayerPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.SoundPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.ChunkLightStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_14Types;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import java.util.Objects;

public class Protocol1_13_2To1_14 extends BackwardsProtocol {
   public static final BackwardsMappings MAPPINGS = new BackwardsMappings("1.14", "1.13.2", Protocol1_14To1_13_2.class, true);
   private final EntityRewriter entityRewriter = new EntityPackets1_14(this);
   private BlockItemPackets1_14 blockItemPackets;

   public Protocol1_13_2To1_14() {
      super(ClientboundPackets1_14.class, ClientboundPackets1_13.class, ServerboundPackets1_14.class, ServerboundPackets1_13.class);
   }

   protected void registerPackets() {
      BackwardsMappings var10002 = MAPPINGS;
      Objects.requireNonNull(var10002);
      this.executeAsyncAfterLoaded(Protocol1_14To1_13_2.class, var10002::load);
      TranslatableRewriter translatableRewriter = new TranslatableRewriter(this);
      translatableRewriter.registerBossBar(ClientboundPackets1_14.BOSSBAR);
      translatableRewriter.registerChatMessage(ClientboundPackets1_14.CHAT_MESSAGE);
      translatableRewriter.registerCombatEvent(ClientboundPackets1_14.COMBAT_EVENT);
      translatableRewriter.registerDisconnect(ClientboundPackets1_14.DISCONNECT);
      translatableRewriter.registerTabList(ClientboundPackets1_14.TAB_LIST);
      translatableRewriter.registerTitle(ClientboundPackets1_14.TITLE);
      translatableRewriter.registerPing();
      (new CommandRewriter1_14(this)).registerDeclareCommands(ClientboundPackets1_14.DECLARE_COMMANDS);
      this.blockItemPackets = new BlockItemPackets1_14(this, translatableRewriter);
      this.blockItemPackets.register();
      this.entityRewriter.register();
      (new PlayerPackets1_14(this)).register();
      (new SoundPackets1_14(this)).register();
      (new StatisticsRewriter(this)).register(ClientboundPackets1_14.STATISTICS);
      this.cancelClientbound(ClientboundPackets1_14.UPDATE_VIEW_POSITION);
      this.cancelClientbound(ClientboundPackets1_14.UPDATE_VIEW_DISTANCE);
      this.cancelClientbound(ClientboundPackets1_14.ACKNOWLEDGE_PLAYER_DIGGING);
      this.registerClientbound(ClientboundPackets1_14.TAGS, new PacketRemapper() {
         public void registerMap() {
            this.handler(new PacketHandler() {
               public void handle(PacketWrapper wrapper) throws Exception {
                  int blockTagsSize = (Integer)wrapper.passthrough(Type.VAR_INT);

                  int itemTagsSize;
                  int entityTagsSize;
                  int i;
                  int itemId;
                  for(itemTagsSize = 0; itemTagsSize < blockTagsSize; ++itemTagsSize) {
                     wrapper.passthrough(Type.STRING);
                     int[] blockIds = (int[])wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);

                     for(entityTagsSize = 0; entityTagsSize < blockIds.length; ++entityTagsSize) {
                        i = blockIds[entityTagsSize];
                        itemId = Protocol1_13_2To1_14.this.getMappingData().getNewBlockId(i);
                        blockIds[entityTagsSize] = itemId;
                     }
                  }

                  itemTagsSize = (Integer)wrapper.passthrough(Type.VAR_INT);

                  int fluidTagsSize;
                  for(fluidTagsSize = 0; fluidTagsSize < itemTagsSize; ++fluidTagsSize) {
                     wrapper.passthrough(Type.STRING);
                     int[] itemIds = (int[])wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);

                     for(i = 0; i < itemIds.length; ++i) {
                        itemId = itemIds[i];
                        int oldId = Protocol1_13_2To1_14.this.getMappingData().getItemMappings().get(itemId);
                        itemIds[i] = oldId;
                     }
                  }

                  fluidTagsSize = (Integer)wrapper.passthrough(Type.VAR_INT);

                  for(entityTagsSize = 0; entityTagsSize < fluidTagsSize; ++entityTagsSize) {
                     wrapper.passthrough(Type.STRING);
                     wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                  }

                  entityTagsSize = (Integer)wrapper.read(Type.VAR_INT);

                  for(i = 0; i < entityTagsSize; ++i) {
                     wrapper.read(Type.STRING);
                     wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
                  }

               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_14.UPDATE_LIGHT, null, new PacketRemapper() {
         public void registerMap() {
            this.handler(new PacketHandler() {
               public void handle(PacketWrapper wrapper) throws Exception {
                  int x = (Integer)wrapper.read(Type.VAR_INT);
                  int z = (Integer)wrapper.read(Type.VAR_INT);
                  int skyLightMask = (Integer)wrapper.read(Type.VAR_INT);
                  int blockLightMask = (Integer)wrapper.read(Type.VAR_INT);
                  int emptySkyLightMask = (Integer)wrapper.read(Type.VAR_INT);
                  int emptyBlockLightMask = (Integer)wrapper.read(Type.VAR_INT);
                  byte[][] skyLight = new byte[16][];
                  if (this.isSet(skyLightMask, 0)) {
                     wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                  }

                  for(int i = 0; i < 16; ++i) {
                     if (this.isSet(skyLightMask, i + 1)) {
                        skyLight[i] = (byte[])wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                     } else if (this.isSet(emptySkyLightMask, i + 1)) {
                        skyLight[i] = ChunkLightStorage.EMPTY_LIGHT;
                     }
                  }

                  if (this.isSet(skyLightMask, 17)) {
                     wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                  }

                  byte[][] blockLight = new byte[16][];
                  if (this.isSet(blockLightMask, 0)) {
                     wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                  }

                  for(int ix = 0; ix < 16; ++ix) {
                     if (this.isSet(blockLightMask, ix + 1)) {
                        blockLight[ix] = (byte[])wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                     } else if (this.isSet(emptyBlockLightMask, ix + 1)) {
                        blockLight[ix] = ChunkLightStorage.EMPTY_LIGHT;
                     }
                  }

                  if (this.isSet(blockLightMask, 17)) {
                     wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                  }

                  ((ChunkLightStorage)wrapper.user().get(ChunkLightStorage.class)).setStoredLight(skyLight, blockLight, x, z);
                  wrapper.cancel();
               }

               private boolean isSet(int mask, int i) {
                  return (mask & 1 << i) != 0;
               }
            });
         }
      });
   }

   public void init(UserConnection user) {
      if (!user.has(ClientWorld.class)) {
         user.put(new ClientWorld(user));
      }

      user.addEntityTracker(this.getClass(), new EntityTrackerBase(user, Entity1_14Types.PLAYER, true));
      if (!user.has(ChunkLightStorage.class)) {
         user.put(new ChunkLightStorage(user));
      }

   }

   public BackwardsMappings getMappingData() {
      return MAPPINGS;
   }

   public EntityRewriter getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPackets1_14 getItemRewriter() {
      return this.blockItemPackets;
   }
}
