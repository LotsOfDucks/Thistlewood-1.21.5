package lod.thistlewood.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.function.Function;

public class CreepingThistleBlock extends MultifaceGrowthBlock {
    public static final MapCodec<CreepingThistleBlock> CODEC = createCodec(CreepingThistleBlock::new);
    private final MultifaceGrower grower = new MultifaceGrower(this);
    public static final BooleanProperty GROWING;
    public static final BooleanProperty GROWINGVIS;
    public static final BooleanProperty WATERLOGGED;
    public static final BooleanProperty CAN_REACTIVATE;
    private final Function<BlockState, VoxelShape> shapeFunction;

    public CreepingThistleBlock(Settings settings) {
        super(settings);
        this.shapeFunction = this.createShapeFunction();
        this.setDefaultState(this.getDefaultState().with(GROWING, true).with(GROWINGVIS, true).with(CAN_REACTIVATE, true));
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        Map<Direction, VoxelShape> map = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(16.0, 0.0, 3.0));
        return this.createShapeFunction((state) -> {
            VoxelShape voxelShape = VoxelShapes.empty();
            Direction[] var3 = DIRECTIONS;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Direction direction = var3[var5];
                if (hasDirection(state, direction)) {
                    voxelShape = VoxelShapes.union(voxelShape, map.get(direction));
                }
            }

            return voxelShape.isEmpty() ? VoxelShapes.fullCube() : voxelShape;
        });
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public MapCodec<? extends MultifaceGrowthBlock> getCodec() {
        return CODEC;
    }

    @Override
    public MultifaceGrower getGrower() {
        return this.grower;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.BEE) {
            if (world instanceof ServerWorld serverWorld) {
                Vec3d vec3d = entity.isControlledByPlayer() ? entity.getMovement() : entity.getLastRenderPos().subtract(entity.getPos());
                if (vec3d.horizontalLengthSquared() > 0.0) {
                    double d = Math.abs(vec3d.getX());
                    double e = Math.abs(vec3d.getZ());
                    if (d >= 0.003000000026077032 || e >= 0.003000000026077032) {
                        entity.damage(serverWorld, world.getDamageSources().sweetBerryBush(), 1.0F);
                        if (world.getTime() % 10L == 0L && !state.get(GROWING) && state.get(CAN_REACTIVATE)) {
                            if (world.random.nextInt(50) == 0) {
                                world.setBlockState(pos, state.with(GROWING, true));
                            }
                        }
                    }
                }
            }
        }
    }

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.SHEARS) && (state.get(GROWING) || state.get(CAN_REACTIVATE))) {
            world.setBlockState(pos, state.with(GROWING, false).with(CAN_REACTIVATE, false));
            if (player != null) {
                stack.damage(1, player);
            }
            if (player instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, stack);
            }
            world.playSound(player, pos, SoundEvents.BLOCK_MANGROVE_ROOTS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state.with(GROWING, false).with(CAN_REACTIVATE, false)));
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
    }

    protected boolean hasRandomTicks(BlockState state) {
        return state.get(GROWING);
    }

    public boolean isGrowing(WorldView world, BlockPos pos, BlockState state) {
        return Direction.stream().anyMatch((direction) -> this.grower.canGrow(state, world, pos, direction.getOpposite()));
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getGameRules().getBoolean(GameRules.DO_VINES_SPREAD)) {
            if (random.nextInt(10) == 0) {
                if (!this.isGrowing(world, pos, state)) {
                    if (world.getBiome(pos).isIn(BiomeTags.IS_FOREST)) {
                        world.setBlockState(pos, state.with(GROWING, false).with(GROWINGVIS, false), 2);
                    } else {
                        world.setBlockState(pos, state.with(GROWING, false), 2);
                    }
                } else {
                    this.grow(world, random, pos, state);
                }
            }
        }
    }

    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.grower.grow(state, world, pos, random);
    }

    protected boolean isTransparent(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        Direction[] var2 = DIRECTIONS;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Direction direction = var2[var4];
            if (this.canHaveDirection(direction)) {
                builder.add(getProperty(direction));
            }
        }

        builder.add(WATERLOGGED, GROWING, GROWINGVIS, CAN_REACTIVATE);
    }

    static {
        WATERLOGGED = Properties.WATERLOGGED;
        GROWING = BooleanProperty.of("growing");
        GROWINGVIS = BooleanProperty.of("growingvis");
        CAN_REACTIVATE = BooleanProperty.of("can_reactivate");
    }
}
