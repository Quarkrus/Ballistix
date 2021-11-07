package ballistix.compatability.jei.recipecategories.specificmachines.ballistix;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;

import ballistix.References;
import ballistix.compatability.jei.recipecategories.psuedorecipes.BallistixPsuedoRecipes;
import electrodynamics.common.recipe.categories.do2o.DO2ORecipe;
import electrodynamics.compatability.jei.recipecategories.ElectrodynamicsRecipeCategory;
import electrodynamics.compatability.jei.recipecategories.psuedorecipes.PsuedoDO2ORecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;

public class WarheadRecipeCategory extends ElectrodynamicsRecipeCategory<PsuedoDO2ORecipe> {

    // JEI Window Parameters
    private static final int INPUT_1_SLOT = 0;
    private static final int INPUT_2_SLOT = 1;

    private static int[] GUI_BACKGROUND = { 0, 0, 132, 58 };
    private static int[] PROCESSING_ARROW_COORDS = { 0, 0, 10, 10 };

    private static int[] INPUT_1_OFFSET = { 89, 7 };
    private static int[] INPUT_2_OFFSET = { 89, 33 };

    private static int[] PROCESSING_ARROW_OFFSET = { 0, 0 };

    private static int SMELT_TIME = 50;
    private static int TEXT_Y_HEIGHT = 48;

    private static String MOD_ID = References.ID;
    private static String RECIPE_GROUP = "warhead_template";
    private static String GUI_TEXTURE = "textures/gui/jei/warhead_template_gui.png";

    public static ItemStack INPUT_MACHINE = new ItemStack(ballistix.DeferredRegisters.blockMissileSilo);

    private static IDrawableAnimated.StartDirection START_DIRECTION = IDrawableAnimated.StartDirection.LEFT;
    private LoadingCache<Integer, IDrawableAnimated> CACHED_ARROWS;

    public static ResourceLocation UID = new ResourceLocation(MOD_ID, RECIPE_GROUP);

    public WarheadRecipeCategory(IGuiHelper guiHelper) {

	super(guiHelper, MOD_ID, RECIPE_GROUP, GUI_TEXTURE, INPUT_MACHINE, GUI_BACKGROUND, PsuedoDO2ORecipe.class, TEXT_Y_HEIGHT, SMELT_TIME);
	CACHED_ARROWS = CacheBuilder.newBuilder().maximumSize(25).build(new CacheLoader<Integer, IDrawableAnimated>() {
	    @Override
	    public IDrawableAnimated load(Integer cookTime) {
		return guiHelper.drawableBuilder(getGuiTexture(), PROCESSING_ARROW_COORDS[0], PROCESSING_ARROW_COORDS[1], PROCESSING_ARROW_COORDS[2],
			PROCESSING_ARROW_COORDS[3]).buildAnimated(cookTime, START_DIRECTION, false);
	    }
	});

    }

    @Override
    public ResourceLocation getUid() {
	return UID;
    }

    @Override
    public void setIngredients(PsuedoDO2ORecipe recipe, IIngredients ingredients) {

	ingredients.setInputLists(VanillaTypes.ITEM, recipeInput(recipe));

    }

    @Override

    public void setRecipe(IRecipeLayout recipeLayout, PsuedoDO2ORecipe recipe, IIngredients ingredients) {

	IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

	guiItemStacks.init(INPUT_2_SLOT, true, INPUT_1_OFFSET[0], INPUT_1_OFFSET[1]);
	guiItemStacks.init(INPUT_1_SLOT, true, INPUT_2_OFFSET[0], INPUT_2_OFFSET[1]);

	guiItemStacks.set(ingredients);

    }

    @Override

    public void draw(PsuedoDO2ORecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
	IDrawableAnimated arrow = getArrow(recipe);
	arrow.draw(matrixStack, PROCESSING_ARROW_OFFSET[0], PROCESSING_ARROW_OFFSET[1]);

	drawSmeltTime(recipe, matrixStack, getYHeight());
    }

    protected void drawSmeltTime(PsuedoDO2ORecipe recipe, PoseStack matrixStack, int y) {
	int smeltTimeSeconds = getArrowSmeltTime() / 20;

	TranslatableComponent missileString = new TranslatableComponent("gui.jei.category." + getRecipeGroup() + ".info.missile",
		smeltTimeSeconds);
	TranslatableComponent explosiveString = new TranslatableComponent("gui.jei.category." + getRecipeGroup() + ".info.explosive",
		smeltTimeSeconds);

	Minecraft minecraft = Minecraft.getInstance();
	Font fontRenderer = minecraft.font;

	int missileWidth = fontRenderer.width(missileString);
	int explosiveWidth = fontRenderer.width(explosiveString);

	fontRenderer.draw(matrixStack, missileString, GUI_BACKGROUND[2] - missileWidth - 46, y - 37, 0xFF333333);
	fontRenderer.draw(matrixStack, explosiveString, GUI_BACKGROUND[2] - explosiveWidth - 46, y - 10, 0xFF333333);
    }

    protected IDrawableAnimated getArrow(DO2ORecipe recipe) {
	return CACHED_ARROWS.getUnchecked(getArrowSmeltTime());
    }

    private static List<List<ItemStack>> recipeInput(PsuedoDO2ORecipe recipe) {

	List<List<ItemStack>> inputSlots = new ArrayList<>();
	inputSlots.add(BallistixPsuedoRecipes.BALLISTIX_ITEMS.get(0));
	inputSlots.add(BallistixPsuedoRecipes.BALLISTIX_ITEMS.get(1));

	return inputSlots;

    }

}
