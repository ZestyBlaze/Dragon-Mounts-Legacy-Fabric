package com.kay9.dragonmounts.dragon;

import com.kay9.dragonmounts.DMLClient;
import com.kay9.dragonmounts.DMLConfig;
import com.kay9.dragonmounts.DMLRegistry;
import com.kay9.dragonmounts.DragonMountsLegacy;
import com.kay9.dragonmounts.client.DragonAnimator;
import com.kay9.dragonmounts.dragon.ai.DragonBodyController;
import com.kay9.dragonmounts.dragon.ai.DragonBreedGoal;
import com.kay9.dragonmounts.dragon.ai.DragonMoveController;
import com.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.kay9.dragonmounts.dragon.breed.DragonBreed;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;

public class TameableDragon extends TamableAnimal implements Saddleable, FlyingAnimal, PlayerRideable {
    // base attributes
    public static final double BASE_SPEED_GROUND = 0.3;
    public static final double BASE_SPEED_FLYING = 0.525;
    public static final double BASE_DAMAGE = 8;
    public static final double BASE_HEALTH = 60;
    public static final double BASE_FOLLOW_RANGE = 16;
    public static final double BASE_FOLLOW_RANGE_FLYING = BASE_FOLLOW_RANGE * 2;
    public static final int BASE_KB_RESISTANCE = 1;
    public static final float BASE_WIDTH = 2.75f; // adult sizes
    public static final float BASE_HEIGHT = 2.75f;

    // data value IDs
    private static final EntityDataAccessor<String> DATA_BREED = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.INT);

    // data NBT IDs
    public static final String NBT_BREED = "Breed";
    private static final String NBT_SADDLED = "Saddle";
    private static final String NBT_REPRO_COUNT = "ReproCount";

    // other constants
    public static final int AGE_UPDATE_INTERVAL = 100;
    public static final UUID SCALE_MODIFIER_UUID = UUID.fromString("856d4ba4-9ffe-4a52-8606-890bb9be538b"); // just a random uuid I took online
    public static final int ALTITUDE_FLYING_THRESHOLD = 3;
    public static final int DEFAULT_REPRO_LIMIT = 2;
    public static final int DEFAULT_GROWTH_TIME = 72000;

    // server/client delegates
    private final DragonAnimator animator;
    private DragonBreed breed;
    private int reproCount;
    private float ageProgress;

    public TameableDragon(EntityType<? extends TameableDragon> type, Level level)
    {
        super(type, level);

        // enables walking over blocks
        maxUpStep = 1;
        noCulling = true;

        moveControl = new DragonMoveController(this);
        animator = level.isClientSide? new DragonAnimator(this) : null;
        breed = BreedRegistry.getFallback();
    }

    @Override
    @NotNull
    public BodyRotationControl createBodyControl()
    {
        return new DragonBodyController(this);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(MOVEMENT_SPEED, BASE_SPEED_GROUND)
                .add(MAX_HEALTH, BASE_HEALTH)
                .add(ATTACK_DAMAGE, BASE_FOLLOW_RANGE)
                .add(KNOCKBACK_RESISTANCE, BASE_KB_RESISTANCE)
                .add(ATTACK_DAMAGE, BASE_DAMAGE)
                .add(FLYING_SPEED, BASE_SPEED_FLYING);
    }

    @Override
    protected void registerGoals() // TODO: Much Smarter AI and features
    {
//        goalSelector.addGoal(1, new DragonLandGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
//        goalSelector.addGoal(4, new DragonBabuFollowParent(this, 10));
        goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.1, 10f, 3.5f, true));
        goalSelector.addGoal(5, new DragonBreedGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, LivingEntity.class, 16f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NonTameRandomTargetGoal<>(this, Animal.class, false, e -> !(e instanceof TameableDragon)));
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();

        entityData.define(DATA_BREED,"");
        entityData.define(DATA_FLYING, false);
        entityData.define(DATA_SADDLED, false);
        entityData.define(DATA_AGE, 0); // default to adult stage
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data)
    {
        if (DATA_BREED.equals(data)) updateBreed(BreedRegistry.get(entityData.get(DATA_BREED)));
        else if (DATA_FLAGS_ID.equals(data)) refreshDimensions();
        else if (DATA_AGE.equals(data)) updateAgeProperties();
        else super.onSyncedDataUpdated(data);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putString(NBT_BREED, breed.getRegistryName().toString());
        compound.putBoolean(NBT_SADDLED, isSaddled());
        compound.putInt(NBT_REPRO_COUNT, reproCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        setBreed(BreedRegistry.get(compound.getString(NBT_BREED)));
        setSaddled(compound.getBoolean(NBT_SADDLED));
        this.reproCount = compound.getInt(NBT_REPRO_COUNT);

        entityData.set(DATA_AGE, getAge());
    }

    public void setBreed(DragonBreed dragonBreed)
    {
        entityData.set(DATA_BREED, dragonBreed.getRegistryName().toString());
    }

    private void updateBreed(DragonBreed breed)
    {
        getBreed().close(this);
        this.breed = breed;
        getBreed().initialize(this);
    }

    public DragonBreed getBreed()
    {
        return breed;
    }

    /**
     * Returns true if the dragon is saddled.
     */
    public boolean isSaddled()
    {
        return entityData.get(DATA_SADDLED);
    }

    @Override
    public boolean isSaddleable()
    {
        return isAlive() && !isHatchling() && isTame();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource source)
    {
        setSaddled(true);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, getSoundSource(), 1, 1);
    }

    /**
     * Set or remove the saddle of the dragon.
     */
    public void setSaddled(boolean saddled)
    {
        entityData.set(DATA_SADDLED, saddled);
    }

    public void addReproCount()
    {
        reproCount++;
    }

    public boolean canFly()
    {
        // hatchling's can't fly
        return !isHatchling();
    }

    public boolean shouldFly()
    {
        return canFly() && !isInWater() && isHighEnough(ALTITUDE_FLYING_THRESHOLD);
    }

    /**
     * Returns true if the entity is flying.
     */
    public boolean isFlying()
    {
        return entityData.get(DATA_FLYING);
    }

    /**
     * Set the flying flag of the entity.
     */
    public void setFlying(boolean flying)
    {
        entityData.set(DATA_FLYING, flying);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (isServer())
        {
            if (age < 0 && tickCount % AGE_UPDATE_INTERVAL == 0) entityData.set(DATA_AGE, age);

            // update flying state based on the distance to the ground
            boolean flying = shouldFly();
            if (flying != isFlying())
            {
                // notify client
                setFlying(flying);

                // update AI follow range (needs to be updated before creating
                // new PathNavigate!)
                getAttribute(FOLLOW_RANGE).setBaseValue(flying? BASE_FOLLOW_RANGE_FLYING : BASE_FOLLOW_RANGE);

                // update pathfinding method
                if (flying) navigation = new FlyingPathNavigation(this, level);
                else navigation = new GroundPathNavigation(this, level);
            }
        }
        else
        {
            // update animations on the client
            animator.tick();

            // because age isn't incremented on client, do it ourselves...
            int age = getAge();
            if (age < 0) setAge(++age);
            else if (age > 0) setAge(--age);
        }

        updateAgeProgress();
        for (var ability : getBreed().abilities()) ability.tick(this);
    }

    @Override
    public void travel(Vec3 vec3)
    {
        boolean isFlying = isFlying();
        float speed = (float) getAttributeValue(isFlying? FLYING_SPEED : MOVEMENT_SPEED) * 0.225f;

        if (canBeControlledByRider()) // Were being controlled; override ai movement
        {
            LivingEntity driver = (LivingEntity) getControllingPassenger();
            double moveSideways = vec3.x;
            double moveY = vec3.y;
            double moveForward = Math.min(Math.abs(driver.zza) + Math.abs(driver.xxa), 1);

            // rotate head to match driver.
            float yaw = driver.yHeadRot;
            if (moveForward > 0) // rotate in the direction of the drivers controls
                yaw += (float) Mth.atan2(driver.zza, driver.xxa) * (180f / (float) Math.PI) - 90;
            yHeadRot = yaw;
            setXRot(driver.getXRot() * 0.68f);

            // rotate body towards the head
            setYRot(Mth.rotateIfNecessary(yHeadRot, getYRot(), 4));

            if (isControlledByLocalInstance()) // Client applies motion
            {
                if (isFlying)
                {
                    moveForward = moveForward > 0? moveForward : 0;
                    if (moveForward > 0 && DMLConfig.CAMERA_FLIGHT) moveY = -driver.getXRot() * (Math.PI / 180);
                    else moveY = driver.jumping? 1 : DMLClient.FLIGHT_DESCENT_KEY.getAsBoolean()? -1 : 0;
                }
                else if (driver.jumping && canFly()) liftOff();

                vec3 = new Vec3(moveSideways, moveY, moveForward);
                setSpeed(speed);
            }
            else if (driver instanceof Player) // other clients recieve animations
            {
                calculateEntityAnimation(this, true);
                setDeltaMovement(Vec3.ZERO);
                return;
            }
        }

        if (isFlying)
        {
            // Move relative to yaw - handled in the move controller or by driver
            moveRelative(speed, vec3);
            move(MoverType.SELF, getDeltaMovement());
            if (getDeltaMovement().lengthSqr() < 0.1) // we're not actually going anywhere, bob up and down.
                setDeltaMovement(getDeltaMovement().add(0, Math.sin(tickCount / 4f) * 0.03, 0));
            setDeltaMovement(getDeltaMovement().scale(0.9f)); // smoothly slow down

            calculateEntityAnimation(this, true);
        }
        else super.travel(vec3);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        InteractionResult stackResult = stack.interactLivingEntity(player, this, hand);
        if (stackResult.consumesAction()) return stackResult;

        final InteractionResult SUCCESS = InteractionResult.sidedSuccess(level.isClientSide);

        // tame
        if (!isTame())
        {
            if (isServer() && stack.is(getBreed().tamingItems()))
            {
                stack.shrink(1);
                tamedFor(player, getRandom().nextInt(5) == 0);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }

        // heal
        if (getHealthRelative() < 1 && isFoodItem(stack))
        {
            heal(stack.getItem().getFoodProperties().getNutrition());
            playSound(getEatingSound(stack), 0.7f, 1);
            stack.shrink(1);
            return SUCCESS;
        }

        // saddle up!
        if (isTamedFor(player) && isSaddleable() && !isSaddled() && stack.getItem() instanceof SaddleItem)
        {
            stack.shrink(1);
            equipSaddle(getSoundSource());
            return SUCCESS;
        }

        // sit!
        if (isTamedFor(player) && (player.isShiftKeyDown() || stack.is(Items.BONE))) // "bone sitting" for legacy reasons
        {
            if (isServer())
            {
                navigation.stop();
                setOrderedToSit(!isOrderedToSit());
                if (isOrderedToSit()) setTarget(null);
            }
            return SUCCESS;
        }

        // ride on
        if (isTamedFor(player) && isSaddled() && !isHatchling() && !isFood(stack))
        {
            if (isServer())
            {
                setRidingPlayer(player);
                navigation.stop();
                setTarget(null);
            }
            setOrderedToSit(false);
            setInSittingPose(false);
            return SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Returns the int-precision distance to solid ground.
     * Describe an inclusive limit to reduce iterations.
     */
    public double getAltitude(int limit)
    {
        var pointer = blockPosition().mutable().move(0, -1, 0);
        var min = level.dimensionType().minY();
        var i = 0;

        while(i <= limit && pointer.getY() > min && !level.getBlockState(pointer).getMaterial().isSolid())
            pointer.setY(getBlockY() - ++i);

        return i;
    }

    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public double getAltitude()
    {
        return getAltitude(level.getMaxBuildHeight());
    }

    public boolean isHighEnough(int height)
    {
        return getAltitude(height) >= height;
    }

    public void liftOff()
    {
        if (canFly()) jumpFromGround();
    }

    @Override
    protected float getJumpPower()
    {
        // stronger jumps for easier lift-offs
        return super.getJumpPower() * (canFly()? 3 : 1);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource)
    {
        return !canFly() && super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    @Override
    protected void tickDeath()
    {
        // unmount any riding entities
        ejectPassengers();

        // freeze at place
        setDeltaMovement(Vec3.ZERO);
        setYRot(yRotO);
        setYHeadRot(yHeadRotO);

        if (deathTime >= getMaxDeathTime()) remove(RemovalReason.KILLED); // actually delete entity after the time is up

        deathTime++;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        double random = getRandom().nextDouble();

        if (random < 0.2) return SoundEvents.ENDER_DRAGON_GROWL;
        if (getBreed().specialSound().isPresent() && random < 0.5) return getBreed().getAmbientSound();
        return DMLRegistry.DRAGON_BREATHE_SOUND;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    public SoundEvent getStepSound()
    {
        return DMLRegistry.DRAGON_STEP_SOUND;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected SoundEvent getDeathSound()
    {
        return DMLRegistry.DRAGON_DEATH_SOUND;
    }

    @Override
    public SoundEvent getEatingSound(ItemStack itemStackIn)
    {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getAttackSound()
    {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getWingsSound()
    {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void playStepSound(BlockPos entityPos, BlockState state)
    {
        if (isInWater()) return;

        // override sound type if the top block is snowy
        SoundType soundType = state.getSoundType();
        if (level.getBlockState(entityPos.above()).getBlock() == Blocks.SNOW)
            soundType = Blocks.SNOW.getSoundType(state);

        // play stomping for bigger dragons
        SoundEvent stepSound = getStepSound();
        if (isHatchling()) stepSound = soundType.getStepSound();

        playSound(stepSound, soundType.getVolume(), getVoicePitch());
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    @Override
    public int getAmbientSoundInterval()
    {
        return 240;
    }

    @Override
    protected float getSoundVolume()
    {
        return getScale();
    }

    @Override
    public float getVoicePitch()
    {
        return 2 - getScale();
    }

    @Override
    public void playSound(SoundEvent pSound, float pVolume, float pPitch)
    {
        if (pSound == breed.getAmbientSound()) pPitch /= 2;
        super.playSound(pSound, pVolume, pPitch);
    }

    /*
    @Override
    public ItemStack getPickedResult(HitResult target)
    {
        return DragonSpawnEgg.create(getBreed());
    }

     */

    @Override
    protected Component getTypeName()
    {
        return Component.translatable(getBreed().getTranslationKey());
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean isFoodItem(ItemStack stack)
    {
        return stack.getItem().isEdible() && stack.getItem().getFoodProperties().isMeat();
    }

    // the "food" that enables breeding mode
    @Override
    public boolean isFood(ItemStack stack)
    {
        return stack.is(getBreed().breedingItems());
    }

    public void tamedFor(Player player, boolean successful)
    {
        if (successful)
        {
            setTame(true);
            navigation.stop();
            setTarget(null);
            setOwnerUUID(player.getUUID());
            level.broadcastEntityEvent(this, (byte) 7);
        }
        else
        {
            level.broadcastEntityEvent(this, (byte) 6);
        }
    }

    public boolean isTamedFor(Player player)
    {
        return isTame() && isOwnedBy(player);
    }

    /**
     * Returns the height of the eyes. Used for looking at other entities.
     */
    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn)
    {
        return sizeIn.height * 1.2f;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
    public double getPassengersRidingOffset()
    {
        return getBbHeight() - 0.175;
    }

    /**
     * Returns render size modifier
     */
    @Override
    public float getScale()
    {
        return 0.33f + (0.67f * getAgeProgress());
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean onClimbable()
    {
        // this better doesn't happen...
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn)
    {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);

        if (isSaddled()) spawnAtLocation(Items.SADDLE);
    }

    @Override
    protected ResourceLocation getDefaultLootTable()
    {
        return breed.deathLoot();
    }

    @Override
    public boolean doHurtTarget(Entity entityIn)
    {
        boolean attacked = entityIn.hurt(DamageSource.mobAttack(this), (float) getAttribute(ATTACK_DAMAGE).getValue());

        if (attacked) doEnchantDamageEffects(this, entityIn);

        return attacked;
    }

    public void onWingsDown(float speed)
    {
        if (!isInWater())
        {
            // play wing sounds
            float pitch = (1 - speed);
            float volume = 0.3f + (1 - speed) * 0.2f;
            pitch *= getVoicePitch();
            volume *= getSoundVolume();
            level.playLocalSound(getX(), getY(), getZ(), getWingsSound(), SoundSource.VOICE, volume, pitch, true);
        }
    }

    @Override
    public void swing(InteractionHand hand)
    {
        // play eating sound
        playSound(getAttackSound(), 1, 0.7f);
        super.swing(hand);
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource src, float par2)
    {
        if (isInvulnerableTo(src)) return false;

        // don't just sit there!
        setOrderedToSit(false);

        return super.hurt(src, par2);
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMate(Animal mate)
    {
        if (mate == this) return false; // No. Just... no.
        else if (!(mate instanceof TameableDragon)) return false;
        else if (!canReproduce()) return false;

        TameableDragon dragonMate = (TameableDragon) mate;

        if (!dragonMate.isTame()) return false;
        else if (!dragonMate.canReproduce()) return false;
        else return isInLove() && dragonMate.isInLove();
    }

    public boolean canReproduce()
    {
        return isTame() && reproCount < DMLConfig.REPRO_LIMIT;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal animal)
    {
        if (!(animal instanceof TameableDragon mate))
        {
            DragonMountsLegacy.LOG.warn("Tried to mate with non-dragon? Hello? {}", animal);
            return;
        }

        DragonEgg egg = DMLRegistry.DRAGON_EGG.create(level);

        // pick a breed to inherit from
        egg.setEggBreed(getRandom().nextBoolean()? breed : mate.breed);

        // mix the custom names in case both parents have one
        if (hasCustomName() && animal.hasCustomName())
        {
            String p1Name = getCustomName().getString();
            String p2Name = animal.getCustomName().getString();
            String babyName;

            if (p1Name.contains(" ") || p2Name.contains(" "))
            {
                // combine two words with space
                // "Tempor Invidunt Dolore" + "Magna"
                // = "Tempor Magna" or "Magna Tempor"
                String[] p1Names = p1Name.split(" ");
                String[] p2Names = p2Name.split(" ");

                p1Name = StringUtils.capitalize(p1Names[getRandom().nextInt(p1Names.length)]);
                p2Name = StringUtils.capitalize(p2Names[getRandom().nextInt(p2Names.length)]);

                babyName = getRandom().nextBoolean()? p1Name + " " + p2Name : p2Name + " " + p1Name;
            }
            else
            {
                // scramble two words
                // "Eirmod" + "Voluptua"
                // = "Eirvolu" or "Volueir" or "Modptua" or "Ptuamod" or ...
                if (getRandom().nextBoolean()) p1Name = p1Name.substring(0, (p1Name.length() - 1) / 2);
                else p1Name = p1Name.substring((p1Name.length() - 1) / 2);

                if (getRandom().nextBoolean()) p2Name = p2Name.substring(0, (p2Name.length() - 1) / 2);
                else p2Name = p2Name.substring((p2Name.length() - 1) / 2);

                p2Name = StringUtils.capitalize(p2Name);

                babyName = getRandom().nextBoolean()? p1Name + p2Name : p2Name + p1Name;
            }

            egg.setCustomName(Component.literal(babyName));
        }

        // increase reproduction counter
        addReproCount();
        mate.addReproCount();
        egg.setPos(getX(), getY(), getZ());
        level.addFreshEntity(egg);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob)
    {
        var offspring = DMLRegistry.DRAGON.create(level);
        offspring.setBreed(getBreed());
        return offspring;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner)
    {
        return !(target instanceof TamableAnimal tameable) || !Objects.equals(tameable.getOwner(), owner);
    }

    @Override
    public boolean canAttackType(EntityType<?> typeIn)
    {
        return !isHatchling() && super.canAttackType(typeIn);
    }

    @Override
    public boolean canAttack(LivingEntity target)
    {
        return !isHatchling() && super.canAttack(target);
    }

    public boolean canBeControlledByRider() {
        return getControllingPassenger() instanceof LivingEntity driver && isOwnedBy(driver);
    }

    /**
     * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
     * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
     */
    @Override
    public Entity getControllingPassenger()
    {
        List<Entity> list = getPassengers();
        return list.isEmpty()? null : list.get(0);
    }

    public void setRidingPlayer(Player player)
    {
        player.setYRot(getYRot());
        player.setXRot(getXRot());
        player.startRiding(this);
    }

    @Override
    public void positionRider(Entity passenger)
    {
        Entity riddenByEntity = getControllingPassenger();
        if (riddenByEntity != null)
        {
            Vec3 pos = new Vec3(0, getPassengersRidingOffset() + riddenByEntity.getMyRidingOffset(), getScale())
                    .yRot((float) Math.toRadians(-yBodyRot))
                    .add(position());
            passenger.setPos(pos.x, pos.y, pos.z);

            // fix rider rotation
            if (getFirstPassenger() instanceof LivingEntity)
            {
                LivingEntity rider = ((LivingEntity) riddenByEntity);
                rider.xRotO = rider.getXRot();
                rider.yRotO = rider.getYRot();
                rider.yBodyRot = yBodyRot;
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src)
    {
        Entity srcEnt = src.getEntity();
        if (srcEnt != null && (srcEnt == this || hasPassenger(srcEnt))) return true;

        if (src == DamageSource.DRAGON_BREATH // inherited from it anyway
                || src == DamageSource.CACTUS) // assume cactus needles don't hurt thick scaled lizards
            return true;

        return breed.immunities().contains(src.getMsgId()) || super.isInvulnerableTo(src);
    }

    /**
     * Returns the entity's health relative to the maximum health.
     *
     * @return health normalized between 0 and 1
     */
    public double getHealthRelative()
    {
        return getHealth() / (double) getMaxHealth();
    }

    public int getMaxDeathTime()
    {
        return 120;
    }

    /**
     * Public wrapper for protected final setScale(), used by DragonLifeStageHelper.
     */
    @Override
    public void refreshDimensions()
    {
        double posXTmp = getX();
        double posYTmp = getY();
        double posZTmp = getZ();
        boolean onGroundTmp = onGround;

        super.refreshDimensions();

        // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client positioning
        setPos(posXTmp, posYTmp, posZTmp);

        // otherwise, setScale stops the dragon from landing while it is growing
        onGround = onGroundTmp;
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn)
    {
        var height = isInSittingPose()? 2.15f : BASE_HEIGHT;
        var scale = getScale();
        return new EntityDimensions(BASE_WIDTH * scale, height * scale, false);
    }

    @Override
    public int getAge()
    {
        return age;
    }

    public void updateAgeProgress()
    {
        // no reason to recalculate this value several times per tick/frame...
        float growth = -breed.growthTime();
        float min = Math.min(getAge(), 0);
        ageProgress = 1 - (min / growth);
    }

    public float getAgeProgress()
    {
        return ageProgress;
    }

    private void updateAgeProperties()
    {
        setAge(entityData.get(DATA_AGE));
        updateAgeProgress();
        refreshDimensions();

        var mod = new AttributeModifier(SCALE_MODIFIER_UUID, "Dragon size modifier", getScale(), AttributeModifier.Operation.ADDITION);
        for (var attribute : new Attribute[]{MAX_HEALTH, ATTACK_DAMAGE}) // avoid duped code
        {
            AttributeInstance instance = getAttribute(attribute);
            instance.removeModifier(mod);
            instance.addTransientModifier(mod);
        }
    }

    public boolean isHatchling()
    {
        return getAgeProgress() < 0.5f;
    }

    public boolean isJuvenile()
    {
        return getAgeProgress() >= 0.5f && getAgeProgress() < 1f;
    }

    public boolean isAdult()
    {
        return getAgeProgress() >= 1f;
    }

    @Override
    public boolean isBaby()
    {
        return !isAdult();
    }

    @Override
    public void setBaby(boolean baby)
    {
        setAge(baby? -breed.growthTime() : 0);
        entityData.set(DATA_AGE, age);
    }

    @Override
    public void ageUp(int p_146741_, boolean p_146742_)
    {
        super.ageUp(p_146741_, p_146742_);
        entityData.set(DATA_AGE, getAge());
    }

    // simple helper method to determine if we're on the server thread.
    public boolean isServer()
    {
        return !level.isClientSide;
    }

    public DragonAnimator getAnimator()
    {
        return animator;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return breed.immunities().contains("drown");
    }

    @Override
    public boolean fireImmune()
    {
        return super.fireImmune() || breed.immunities().contains("onFire");
    }

    @Override
    protected void onChangedBlock(BlockPos pos)
    {
        super.onChangedBlock(pos);
        for (var ability : getBreed().abilities()) ability.onMove(this);
    }

    @Override
    public boolean isInWall()
    {
        if (noPhysics) return false;
        else
        {
            var collider = getBoundingBox().deflate(getBbWidth() * 0.2f);
            return BlockPos.betweenClosedStream(collider).anyMatch((pos) ->
            {
                BlockState state = level.getBlockState(pos);
                return !state.isAir() && state.isSuffocating(level, pos) && Shapes.joinIsNotEmpty(state.getCollisionShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(collider), BooleanOp.AND);
            });
        }
    }

    @Override
    public Vec3 getLightProbePosition(float p_20309_)
    {
        return new Vec3(getX(), getY() + getBbHeight(), getZ());
    }
}
