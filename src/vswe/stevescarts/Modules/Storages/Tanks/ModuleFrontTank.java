package vswe.stevescarts.Modules.Storages.Tanks;
import vswe.stevescarts.Carts.MinecartModular;
import vswe.stevescarts.Models.Cart.ModelCartbase;
import vswe.stevescarts.Models.Cart.ModelFrontTank;
import java.util.HashMap;
public class ModuleFrontTank extends ModuleTank{
	public ModuleFrontTank(MinecartModular cart) {
		super(cart);
	}

	
	@Override
	protected int getTankSize() {
		return 8000;
	}

	
}