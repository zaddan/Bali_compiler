package Bali_package;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
 
public class function_table{
		List<String> messages = Arrays.asList("while", "return!", "int", "if");
    Hashtable<String, String> function_code_table = new Hashtable<String, String>();    

    
    public void add_function(String function_name, String code){
        function_code_table.put(function_name, code);
    }
   

    public String get_function_code(String function_name){
        if(function_code_table.containsKey(function_name) == false) {
            System.out.println("function: " + function_name + " has not been added to the function_code_table");
            System.exit(0); 
        }
        return function_code_table.get(function_name);
    }

    public boolean has_func(String function_name){
        return (function_code_table.containsKey(function_name)); 
    }
    public void print_symbol_table_content(){
       System.out.println("****here is the list of functions\n");
       for (String key : function_code_table.keySet()) {
           System.out.println(key);
       }
    }

}
