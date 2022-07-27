package net.zestyblaze.dragonmounts.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.zestyblaze.dragonmounts.DragonMountsLegacy;
import net.zestyblaze.dragonmounts.data.BreedManager;
import net.zestyblaze.dragonmounts.dragon.DragonBreed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executor;

public class UpdateBreedsPacket
{
    public static final UpdateBreedsPacket INSTANCE = new UpdateBreedsPacket(); // breeds collection is known on the server, no need to make new instances

    public static ResourceLocation PACKET_ID = DragonMountsLegacy.id("update_breeds");

    private final Collection<DragonBreed> breeds;

    private UpdateBreedsPacket()
    {
        this.breeds = Collections.emptyList();
    }

    public UpdateBreedsPacket(FriendlyByteBuf buf)
    {
        this.breeds = buf.readList(UpdateBreedsPacket::fromBytes);
    }

    public FriendlyByteBuf encode()
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeCollection(BreedManager.getBreeds(), UpdateBreedsPacket::toBytes);
        return buf;
    }

    public void handle(Executor executor)
    {
        executor.execute(() -> BreedManager.populate(breeds::forEach));
    }

    private static void toBytes(FriendlyByteBuf buf, DragonBreed breed)
    {
        buf.writeResourceLocation(breed.id());
        buf.writeInt(breed.primaryColor());
        buf.writeInt(breed.secondaryColor());
        int id = breed.hatchParticles().map(i -> Registry.PARTICLE_TYPE.getId(i.getType())).orElse(-1);
        buf.writeVarInt(id);
        if (id != -1) breed.hatchParticles().get().writeToNetwork(buf);
        var props = breed.modelProperties();
        buf.writeBoolean(props.middleTailScales());
        buf.writeBoolean(props.tailHorns());
        buf.writeBoolean(props.thinLegs());
        buf.writeInt(breed.growthTime());
    }

    /**
     * Create only the bare minimum information clients need for breeds to be functional.
     * Some of this stuff can easily be done on the server with no client work needed.
     */
    private static DragonBreed fromBytes(FriendlyByteBuf buf)
    {
        return new DragonBreed(buf.readResourceLocation(),
                buf.readInt(),
                buf.readInt(),
                readPossibleParticle(buf),
                new DragonBreed.ModelProperties(buf.readBoolean(), buf.readBoolean(), buf.readBoolean()),
                ImmutableMap.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableSet.of(),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                buf.readInt(),
                0);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private static <T extends ParticleOptions> Optional<ParticleOptions> readPossibleParticle(FriendlyByteBuf buf)
    {
        var id = buf.readVarInt();
        if (id != -1)
        {
            var type = (ParticleType<T>) Registry.PARTICLE_TYPE.byId(id);
            return Optional.of(type.getDeserializer().fromNetwork(type, buf));
        }
        return Optional.empty();
    }

    private static MinecraftServer currentServer;

    public static void send(@Nullable ServerPlayer player)
    {
        if (player == null)
            PlayerLookup.all(currentServer).forEach(serverPlayer -> ServerPlayNetworking.send(serverPlayer, PACKET_ID, INSTANCE.encode()));
        else
            ServerPlayNetworking.send(player, PACKET_ID, INSTANCE.encode());
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
    }
}