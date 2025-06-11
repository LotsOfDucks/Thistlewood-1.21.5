package lod.thistlewood.item;

import net.minecraft.component.type.FoodComponent;

public class ModFoodComponents {

    //public static final FoodComponent KIBBLESTONE = (new FoodComponent.Builder()).nutrition(1).saturationModifier(0.1F).build();

    private static FoodComponent.Builder createStew(int nutrition) {
        return (new FoodComponent.Builder()).nutrition(nutrition).saturationModifier(0.6F);
    }

    public static void initialize() {
    }
}
