package de.chrisicrafter.loadit.block;

import de.chrisicrafter.loadit.LoadIt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LoadIt.MOD_ID);

    public static final RegistryObject<Block> CHUNK_LOADER = BLOCKS.register("chunk_loader", () -> new Block(BlockBehaviour.Properties.copy(Blocks.BEACON)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
