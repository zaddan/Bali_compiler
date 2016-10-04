import Bali_package.BaliCompiler;
public class hello {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    // TODO Auto-generated method stub
    String hll_prg_file = "abs.bali";
//    String hll_prg_file = "good_while.bali";
//    String hll_prg_file = "simple_bali_tset.bali";
    
    
    //--- generated SAM code  
    BaliCompiler simple_compiler = new BaliCompiler();
    String code = simple_compiler.code_gen(hll_prg_file);
    System.out.println("***********************************************\n");
    System.out.println("generated code:\n");
    System.out.println("***********************************************\n");
    System.out.println(code);
    
    /*   
    //--- generate x86 code using SAM code
    BaliCompiler simple_x86_compiler = new BaliCompiler();
    String x86_code = simple_x86_compiler.x86_code_gen(hll_prg_file);
    System.out.println("***********************************************\n");
    System.out.println("x86 generated code:\n");
    System.out.println("***********************************************\n");
    System.out.println(x86_code);
    */ 
    }

}
