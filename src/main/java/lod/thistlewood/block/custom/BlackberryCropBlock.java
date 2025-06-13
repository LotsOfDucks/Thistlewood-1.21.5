package lod.thistlewood.block.custom;

import com.mojang.serialization.MapCodec;
import lod.thistlewood.block.ModBlocks;
import lod.thistlewood.item.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;

public class BlackberryCropBlock extends PlantBlock implements Fertilizable {
    public static final MapCodec<BlackberryCropBlock> CODEC = createCodec(BlackberryCropBlock::new);
    public static final IntProperty AGE;
    public static final IntProperty STAGE;
    public static final BooleanProperty HAS_FRUIT;
    public static final BooleanProperty IS_SPREADING;
    private static final VoxelShape[] AGE_TO_SHAPE;

    protected MapCodec<? extends PlantBlock> getCodec() { return CODEC; }

    public BlackberryCropBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(AGE, 0).with(STAGE, 0).with(HAS_FRUIT, false).with(IS_SPREADING, true));
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[this.getAge(state)];
    }

    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ModItems.BLACKBERRY);
    }

    protected boolean hasRandomTicks(BlockState state) {
        return !this.hasFruit(state) || this.isSpreading(state);
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(IS_SPREADING, canSpread(world, pos)));
        if (state.get(IS_SPREADING)) {
            this.applySpread(world, pos, state, random);
        }
        if (world.getBaseLightLevel(pos, 0) >= 9) {
            int age = this.getAge(state);
            int maxAge = this.getMaxAge(state);
            int stage = this.getStage(state);
            if (age < this.getMaxAge(state)) {
                if (random.nextInt(10) == 0) {
                    world.setBlockState(pos, this.withAge(age + 1, state), 2);
                }
            }
            if (age == maxAge) {
                if (stage < 2 && world.getBlockState(pos.up()).isOf(Blocks.AIR)) {
                    if (random.nextInt(5) == 0) {
                        world.setBlockState(pos.up(), this.getStateWithProperties(state).with(STAGE, this.getStage(state)+1).with(AGE, this.getAge(state)+1), 2);
                    }
                }
                if (!state.get(HAS_FRUIT)) {
                    if (random.nextInt(20) == 0) {
                        world.setBlockState(pos, this.withFruit(true, state), 2);
                    }
                }
            }
        }
    }

    public int getAge(BlockState state) { return state.get(AGE); }

    public int getMaxAge(BlockState state) {
        return switch (this.getStage(state)) {
            case 1 -> 4;
            case 2 -> 5;
            default -> 2;
        };
    }

    public BlockState withAge(int age, BlockState state) {
        return this.getStateWithProperties(state).with(AGE, age);
    }

    public int getStage(BlockState state) {
        return state.get(STAGE);
    }

    public boolean hasFruit(BlockState state) { return state.get(HAS_FRUIT); }

    public BlockState withFruit(boolean hasfruit, BlockState state) { return this.getStateWithProperties(state).with(HAS_FRUIT, hasfruit); }

    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return (floor.isIn(BlockTags.DIRT));
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return !this.isStable(state, world, pos) ? Blocks.AIR.getDefaultState() : state;
    }

    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return this.canPlantOnTop(world.getBlockState(blockPos), world, blockPos);
    }

    protected boolean isStable(BlockState state, WorldView world, BlockPos pos) {
        if (world.getBlockState(pos.down()).isOf(ModBlocks.BLACKBERRY_CROP)) {
            return world.getBlockState(pos.down()).get(STAGE) < 2;
        } else return this.canPlaceAt(world.getBlockState(pos), world, pos);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        if (state.get(STAGE) == 0) {
            if (state.get(AGE) <= 2|| world.getBlockState(pos.up()).isOf(Blocks.AIR)) {
                return true;
            }
        } else if (state.get(STAGE) == 1) {
            if (state.get(AGE) <= 4|| world.getBlockState(pos.up()).isOf(Blocks.AIR)) {
                return true;
            }
        }
        return !state.get(HAS_FRUIT);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) { return true; }

    public boolean canSpread(ServerWorld world, BlockPos pos) {
        if (!world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD)){
            return false;
        }
        return getValidLocations(world, pos) > 0;
    }

    public static float getValidLocations(ServerWorld world, BlockPos pos) {
        int validLocations = 0;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z){
                    BlockState plantCheckState = world.getBlockState(pos.add(x, y, z));
                    BlockState dirtCheckState = world.getBlockState(pos.add(x, y-1, z));
                    if (plantCheckState.isOf(Blocks.AIR) && dirtCheckState.isIn(BlockTags.DIRT)) {
                        validLocations += 1;
                    }
                }
            }
        }
        return validLocations;
    }

    public boolean isSpreading(BlockState state) {
        return state.get(IS_SPREADING);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, pos, state);
    }

    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int i = Math.min(this.getMaxAge(state), this.getAge(state) + this.getGrowthAmount(world));
        world.setBlockState(pos, this.withAge(i, state), 2);
        if (this.getAge(state) == this.getMaxAge(state)) {
            world.setBlockState(pos, this.withFruit(true, state), 2);
            if (world.getBlockState(pos.up()).isOf(Blocks.AIR)) {
                world.setBlockState(pos.up(), this.getStateWithProperties(state).with(STAGE, this.getStage(state)+1).with(AGE, this.getAge(state)+1), 2);
            }
        }
    }

    protected int getGrowthAmount(World world) {
        return MathHelper.nextInt(world.random, 0, 1);
    }

    public void applySpread(World world, BlockPos pos, BlockState state, Random random) {
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z){
                    BlockState plantCheckState = world.getBlockState(pos.add(x, y, z));
                    BlockState dirtCheckState = world.getBlockState(pos.add(x, y-1, z));
                    if (plantCheckState.isOf(Blocks.AIR) && dirtCheckState.isIn(BlockTags.DIRT)) {
                        if (random.nextInt(100) <= 0) {
                            world.setBlockState(pos.add(x, y, z), ModBlocks.BLACKBERRY_CROP.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (entity instanceof LivingEntity) {
            entity.slowMovement(state, new Vec3d(0.800000011920929, 0.75, 0.800000011920929));
            if (world instanceof ServerWorld serverWorld) {
                Vec3d vec3d = entity.isControlledByPlayer() ? entity.getMovement() : entity.getLastRenderPos().subtract(entity.getPos());
                if (vec3d.horizontalLengthSquared() > 0.0) {
                    double d = Math.abs(vec3d.getX());
                    double e = Math.abs(vec3d.getZ());
                    if (d >= 0.003000000026077032 || e >= 0.003000000026077032) {
                        entity.damage(serverWorld, world.getDamageSources().sweetBerryBush(), 1.0F);
                    }
                }
            }
        }
    }

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockState(pos).get(HAS_FRUIT)) {
            int j = 1 + world.random.nextInt(3);
            if (world.getBlockState(pos).get(STAGE) == 0) {
                dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
                BlockState targetState = state.with(HAS_FRUIT, false);
                world.setBlockState(pos, targetState, 2);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, targetState));
                if (world.getBlockState(pos.up()).isOf(ModBlocks.BLACKBERRY_CROP)) {
                    BlockState targetState2 = world.getBlockState(pos.up()).with(HAS_FRUIT, false);
                    if (world.getBlockState(pos.up()).get(HAS_FRUIT)) {
                        dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                        world.setBlockState(pos.up(), targetState2, 2);
                    }
                }
                if (world.getBlockState(pos.up(2)).isOf(ModBlocks.BLACKBERRY_CROP)) {
                    BlockState targetState3 = world.getBlockState(pos.up(2)).with(HAS_FRUIT, false);
                    if (world.getBlockState(pos.up(2)).get(HAS_FRUIT)) {
                        dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                        world.setBlockState(pos.up(2), targetState3, 2);
                    }
                }
            }
            if (world.getBlockState(pos).get(STAGE) == 1) {
                world.playSound(null, pos.down(), SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
                dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                BlockState targetState = state.with(HAS_FRUIT, false);
                BlockState targetState2 = world.getBlockState(pos.down()).with(HAS_FRUIT, false);
                world.setBlockState(pos, targetState, 2);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.down(), GameEvent.Emitter.of(player, targetState));
                if (world.getBlockState(pos.down()).get(HAS_FRUIT)) {
                    dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                    world.setBlockState(pos.down(), targetState2, 2);
                }
                if (world.getBlockState(pos.up()).isOf(ModBlocks.BLACKBERRY_CROP)) {
                    BlockState targetState3 = world.getBlockState(pos.up()).with(HAS_FRUIT, false);
                    if (world.getBlockState(pos.up()).get(HAS_FRUIT)) {
                        dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                        world.setBlockState(pos.up(), targetState3, 2);
                    }
                }
            }
            if (world.getBlockState(pos).get(STAGE) == 2) {
                world.playSound(null, pos.down(), SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
                dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                BlockState targetState = state.with(HAS_FRUIT, false);
                BlockState targetState2 = world.getBlockState(pos.down()).with(HAS_FRUIT, false);
                BlockState targetState3 = world.getBlockState(pos.down(2)).with(HAS_FRUIT, false);
                world.setBlockState(pos, targetState, 2);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.down(), GameEvent.Emitter.of(player, targetState));
                if (world.getBlockState(pos.down()).get(HAS_FRUIT)) {
                    dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                    world.setBlockState(pos.down(), targetState2, 2);
                }
                if (world.getBlockState(pos.down(2)).get(HAS_FRUIT)) {
                    dropStack(world, pos, new ItemStack(ModItems.BLACKBERRY, j));
                    world.setBlockState(pos.down(2), targetState3, 2);
                }
            }
            return ActionResult.SUCCESS;
        } else {
            return super.onUse(state, world, pos, player, hit);
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, STAGE, HAS_FRUIT, IS_SPREADING);
    }

    static {
        AGE = IntProperty.of("age", 0, 5);
        STAGE = IntProperty.of("stage", 0, 2);
        HAS_FRUIT = BooleanProperty.of("has_fruit");
        IS_SPREADING = BooleanProperty.of("is_spreading");
        AGE_TO_SHAPE = new VoxelShape[]{
                Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 6.0, 12.0),
                Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 12.0, 13.0),
                Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0),
                Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 8.0, 15.0),
                Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0),
                Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 8.0, 15.0)
        };
    }
}
