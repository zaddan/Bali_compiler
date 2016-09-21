import Bali_package.BaliCompiler;
public class hello {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//BaliCompiler.compiler("good.while.bali");
    BaliCompiler simple_compiler = new BaliCompiler();
    //simple_compiler.compiler("main.bali");
    //simple_compiler.compiler("simple_bali_test.bali");
    //simple_compiler.compiler("one_func.bali");
    //String prg = simple_compiler.compiler("good.while.bali");
    String prg = simple_compiler.compiler("abs.bali");
    System.out.println(prg);
    }

}
