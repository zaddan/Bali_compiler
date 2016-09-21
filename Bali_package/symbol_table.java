package Bali_package;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
 
public class symbol_table{
		List<String> messages = Arrays.asList("while", "return!", "int", "if");
    ArrayList variable_list = new ArrayList();


    public void add_variable(String variable){
        variable_list.add(variable);
    }
    
    public boolean has_var(String variable){
        return variable_list.contains(variable); 
    }
        
    
    public int get_variable_offset(String variable){
        if (variable_list.contains(variable)) {
            return variable_list.indexOf(variable);
        }else{
            System.out.println("ERROR. this variable is not defined in the symbol table\n");
            print_symbol_table_content();
            System.exit(0); 
            return 0;
        }
    }
    
        
    public void print_symbol_table_content(){
       
        System.out.println("******here is the list of the variables and their offset\n");
        for (int i =0; i < variable_list.size(); i++){
           System.out.println("index " + i + ": " + variable_list.get(i) + "\n");
       }       
       
       System.out.println("****here is the list of functions\n");
    }

}
