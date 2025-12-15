package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.registry.ModCreativeTabs;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 无限供应器基类
 * 提供基本功能，子类可以扩展特定类型的供应器
 */
public abstract class BlockInfiniteProviderBase extends Block {

    protected final String typeName;

    public BlockInfiniteProviderBase(String typeName, Material material) {
        super(material);
        this.typeName = typeName;
        setTranslationKey(Tags.MOD_ID + "." + typeName);
        setRegistryName(Tags.MOD_ID + ":" + typeName);
        setCreativeTab(ModCreativeTabs.INFINITE_PROVIDER_TAB);
        setHardness(3.0F);
        setResistance(10.0F);
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createProviderTileEntity();
    }
    
    /**
     * 创建对应类型的TileEntity，子类需要实现
     */
    protected abstract TileEntity createProviderTileEntity();
    
    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityInfiniteProviderBase) {
                TileEntityInfiniteProviderBase provider = (TileEntityInfiniteProviderBase) te;
                boolean isShiftClick = player.isSneaking();
                provider.onLeftClick(player, isShiftClick);
            }
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, 
                                   EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityInfiniteProviderBase) {
                if (!world.isRemote) {
                    TileEntityInfiniteProviderBase provider = (TileEntityInfiniteProviderBase) te;
                    provider.onRightClick(player);
                }
                // 在客户端和服务端都返回true，防止触发物品的右键使用事件
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    /**
     * 获取供应器类型名称
     */
    public String getTypeName() {
        return typeName;
    }
}